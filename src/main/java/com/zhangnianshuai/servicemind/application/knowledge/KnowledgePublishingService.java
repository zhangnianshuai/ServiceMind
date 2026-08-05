package com.zhangnianshuai.servicemind.application.knowledge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgePublishResponse;
import com.zhangnianshuai.servicemind.application.knowledge.KnowledgeProcessingStateService.PreparedChunk;
import com.zhangnianshuai.servicemind.application.knowledge.KnowledgeProcessingStateService.ProcessingContext;

@Service
public class KnowledgePublishingService {

    private static final Logger log =
        LoggerFactory.getLogger(
            KnowledgePublishingService.class
        );

    private static final int CHUNK_SIZE = 300;
    private static final int MIN_CHUNK_SIZE_CHARS = 80;
    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 20;
    private static final int MAX_NUM_CHUNKS = 1000;

    private final KnowledgeProcessingStateService stateService;
    private final VectorStore vectorStore;
    private final String embeddingModel;
    private final int embeddingDimensions;
    private final TokenTextSplitter textSplitter;

    public KnowledgePublishingService(
            KnowledgeProcessingStateService stateService,
            VectorStore vectorStore,
            @Value(
                "${spring.ai.openai.embedding.options.model}"
            )
            String embeddingModel,
            @Value(
                "${spring.ai.openai.embedding.options.dimensions}"
            )
            int embeddingDimensions) {
        this.stateService = stateService;
        this.vectorStore = vectorStore;
        this.embeddingModel = embeddingModel;
        this.embeddingDimensions = embeddingDimensions;

        this.textSplitter = TokenTextSplitter.builder()
            .withChunkSize(CHUNK_SIZE)
            .withMinChunkSizeChars(
                MIN_CHUNK_SIZE_CHARS
            )
            .withMinChunkLengthToEmbed(
                MIN_CHUNK_LENGTH_TO_EMBED
            )
            .withMaxNumChunks(MAX_NUM_CHUNKS)
            .withKeepSeparator(true)
            .build();
    }

    public KnowledgePublishResponse publish(
            String documentNo,
            int versionNo) {
        ProcessingContext context =
            stateService.beginProcessing(
                documentNo,
                versionNo
            );

        List<PreparedChunk> preparedChunks =
            new ArrayList<>();

        List<String> newVectorIds = new ArrayList<>();

        try {
            preparedChunks = prepareChunks(context);

            if (preparedChunks.isEmpty()) {
                throw new IllegalStateException(
                    "Knowledge processing produced "
                        + "no valid chunks."
                );
            }

            List<Document> vectorDocuments =
                preparedChunks.stream()
                    .map(chunk -> {
                        newVectorIds.add(
                            chunk.vectorStoreId().toString()
                        );

                        return new Document(
                            chunk.vectorStoreId().toString(),
                            chunk.text(),
                            chunk.metadata()
                        );
                    })
                    .toList();

            log.info(
                "Publishing knowledge document {}, "
                    + "version {}, chunks {}",
                context.documentNo(),
                context.versionNo(),
                vectorDocuments.size()
            );

            vectorStore.add(vectorDocuments);

            KnowledgePublishResponse response =
                stateService.completeProcessing(
                    context,
                    preparedChunks,
                    embeddingModel,
                    embeddingDimensions
                );

            removePreviousActiveVectors(context);

            log.info(
                "Published knowledge document {}, "
                    + "version {}, chunks {}",
                context.documentNo(),
                context.versionNo(),
                response.chunkCount()
            );

            return response;
        }
        catch (ResponseStatusException exception) {
            cleanupNewVectors(newVectorIds);

            stateService.markFailed(
                context.documentNo(),
                context.versionNo(),
                safeErrorMessage(exception)
            );

            throw exception;
        }
        catch (Exception exception) {
            cleanupNewVectors(newVectorIds);

            stateService.markFailed(
                context.documentNo(),
                context.versionNo(),
                safeErrorMessage(exception)
            );

            log.error(
                "Knowledge publishing failed for "
                    + "document {}, version {}",
                context.documentNo(),
                context.versionNo(),
                exception
            );

            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Knowledge publishing failed while "
                    + "generating or storing embeddings.",
                exception
            );
        }
    }

    private List<PreparedChunk> prepareChunks(
            ProcessingContext context) {
        Map<String, Object> sourceMetadata =
            new LinkedHashMap<>();

        sourceMetadata.put(
            "knowledge_scope",
            "servicemind"
        );
        sourceMetadata.put(
            "document_no",
            context.documentNo()
        );
        sourceMetadata.put(
            "document_id",
            context.documentId()
        );
        sourceMetadata.put(
            "version_no",
            context.versionNo()
        );
        sourceMetadata.put(
            "version_id",
            context.versionId()
        );
        sourceMetadata.put(
            "title",
            context.title()
        );
        sourceMetadata.put(
            "category",
            context.category().name()
        );
        sourceMetadata.put(
            "source_type",
            context.sourceType().name()
        );
        sourceMetadata.put(
            "content_hash",
            context.contentHash()
        );

        Document sourceDocument = new Document(
            context.rawContent(),
            sourceMetadata
        );

        List<Document> splitDocuments =
            textSplitter.apply(List.of(sourceDocument));

        List<PreparedChunk> prepared =
            new ArrayList<>();

        int chunkNo = 1;

        for (Document splitDocument : splitDocuments) {
            String text = splitDocument.getText();

            if (!StringUtils.hasText(text)) {
                continue;
            }

            UUID vectorStoreId = UUID.randomUUID();

            Map<String, Object> metadata =
                new LinkedHashMap<>(sourceMetadata);

            metadata.put("chunk_no", chunkNo);
            metadata.put(
                "vector_store_id",
                vectorStoreId.toString()
            );

            prepared.add(
                new PreparedChunk(
                    vectorStoreId,
                    chunkNo,
                    text.strip(),
                    metadata
                )
            );

            chunkNo++;
        }

        return prepared;
    }

    private void removePreviousActiveVectors(
            ProcessingContext context) {
        if (context.previousVectorIds() == null
                || context.previousVectorIds().isEmpty()) {
            return;
        }

        try {
            vectorStore.delete(
                context.previousVectorIds()
            );

            stateService.unlinkPreviousVectors(
                context.previousActiveVersionId(),
                context.previousVectorIds()
            );

            log.info(
                "Removed {} vectors from previous "
                    + "active version {}",
                context.previousVectorIds().size(),
                context.previousActiveVersionNo()
            );
        }
        catch (Exception exception) {
            log.warn(
                "The new knowledge version was published, "
                    + "but cleanup of previous vectors failed. "
                    + "Previous version: {}",
                context.previousActiveVersionNo(),
                exception
            );
        }
    }

    private void cleanupNewVectors(
            List<String> vectorIds) {
        if (vectorIds == null || vectorIds.isEmpty()) {
            return;
        }

        try {
            vectorStore.delete(vectorIds);
        }
        catch (Exception cleanupException) {
            log.error(
                "Failed to clean up {} newly created "
                    + "knowledge vectors.",
                vectorIds.size(),
                cleanupException
            );
        }
    }

    private String safeErrorMessage(
            Exception exception) {
        String message = exception.getMessage();

        if (!StringUtils.hasText(message)) {
            message = exception
                .getClass()
                .getSimpleName();
        }

        message = message.replaceAll(
            "[\\r\\n\\t]+",
            " "
        );

        if (message.length() > 900) {
            return message.substring(0, 900);
        }

        return message;
    }
}