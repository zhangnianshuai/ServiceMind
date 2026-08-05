package com.zhangnianshuai.servicemind.application.knowledge;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgePublishResponse;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeChunk;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeChunkRepository;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocument;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentRepository;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentStatus;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentVersion;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentVersionRepository;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeSourceType;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeVersionStatus;

@Service
public class KnowledgeProcessingStateService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeDocumentVersionRepository versionRepository;
    private final KnowledgeChunkRepository chunkRepository;

    public KnowledgeProcessingStateService(
            KnowledgeDocumentRepository documentRepository,
            KnowledgeDocumentVersionRepository versionRepository,
            KnowledgeChunkRepository chunkRepository) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
        this.chunkRepository = chunkRepository;
    }

    @Transactional
    public ProcessingContext beginProcessing(
            String documentNo,
            int versionNo) {
        if (versionNo <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Version number must be greater than zero."
            );
        }

        KnowledgeDocument document = documentRepository
            .findByDocumentNoForUpdate(documentNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Knowledge document was not found: "
                    + documentNo
            ));

        if (document.getStatus()
                == KnowledgeDocumentStatus.ARCHIVED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Archived knowledge documents cannot "
                    + "be published."
            );
        }

        if (versionNo != document.getLatestVersionNo()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Only the latest knowledge version can "
                    + "be published."
            );
        }

        KnowledgeDocumentVersion version = versionRepository
            .findForUpdate(document.getId(), versionNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Knowledge version was not found: "
                    + versionNo
            ));

        Integer previousActiveVersionNo =
            document.getActiveVersionNo();

        Long previousActiveVersionId = null;
        List<String> previousVectorIds = List.of();

        if (previousActiveVersionNo != null
                && previousActiveVersionNo != versionNo) {
            KnowledgeDocumentVersion previousVersion =
                versionRepository
                    .findByDocument_IdAndVersionNo(
                        document.getId(),
                        previousActiveVersionNo
                    )
                    .orElseThrow(() ->
                        new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "The active knowledge version "
                                + "could not be resolved."
                        )
                    );

            previousActiveVersionId =
                previousVersion.getId();

            previousVectorIds = chunkRepository
                .findByDocumentVersion_IdOrderByChunkNoAsc(
                    previousVersion.getId()
                )
                .stream()
                .map(KnowledgeChunk::getVectorStoreId)
                .filter(id -> id != null)
                .map(UUID::toString)
                .toList();
        }

        try {
            version.markProcessing();
            document.markProcessing(versionNo);

            versionRepository.saveAndFlush(version);
            documentRepository.saveAndFlush(document);
        }
        catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                exception
            );
        }

        return new ProcessingContext(
            document.getId(),
            document.getDocumentNo(),
            document.getTitle(),
            document.getCategory(),
            version.getId(),
            version.getVersionNo(),
            version.getSourceType(),
            version.getContentHash(),
            version.getRawContent(),
            previousActiveVersionNo,
            previousActiveVersionId,
            previousVectorIds
        );
    }

    @Transactional
    public KnowledgePublishResponse completeProcessing(
            ProcessingContext context,
            List<PreparedChunk> preparedChunks,
            String embeddingModel,
            int embeddingDimensions) {
        KnowledgeDocument document = documentRepository
            .findByDocumentNoForUpdate(context.documentNo())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Knowledge document was not found: "
                    + context.documentNo()
            ));

        KnowledgeDocumentVersion version = versionRepository
            .findForUpdate(
                document.getId(),
                context.versionNo()
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Knowledge version was not found: "
                    + context.versionNo()
            ));

        if (version.getStatus()
                != KnowledgeVersionStatus.PROCESSING) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The knowledge version is no longer "
                    + "in PROCESSING state."
            );
        }

        if (preparedChunks == null
                || preparedChunks.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Knowledge processing produced no chunks."
            );
        }

        chunkRepository.deleteByDocumentVersion_Id(
            version.getId()
        );
        chunkRepository.flush();

        List<KnowledgeChunk> chunks = preparedChunks
            .stream()
            .map(prepared -> {
                KnowledgeChunk chunk = new KnowledgeChunk(
                    version,
                    prepared.chunkNo(),
                    prepared.text(),
                    null,
                    prepared.metadata()
                );

                chunk.linkVectorStore(
                    prepared.vectorStoreId()
                );

                return chunk;
            })
            .toList();

        chunkRepository.saveAll(chunks);
        chunkRepository.flush();

        if (context.previousActiveVersionNo() != null
                && context.previousActiveVersionNo()
                    != context.versionNo()) {
            KnowledgeDocumentVersion previousVersion =
                versionRepository
                    .findByDocument_IdAndVersionNo(
                        document.getId(),
                        context.previousActiveVersionNo()
                    )
                    .orElseThrow(() ->
                        new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "The previous active version "
                                + "could not be resolved."
                        )
                    );

            previousVersion.archive();
            versionRepository.save(previousVersion);
        }

        version.markReady(
            chunks.size(),
            embeddingModel,
            embeddingDimensions
        );

        document.markReady(context.versionNo());

        versionRepository.saveAndFlush(version);
        documentRepository.saveAndFlush(document);

        return new KnowledgePublishResponse(
            document.getDocumentNo(),
            version.getVersionNo(),
            document.getStatus(),
            version.getStatus(),
            version.getChunkCount(),
            version.getEmbeddingModel(),
            version.getEmbeddingDimensions(),
            version.getProcessedAt(),
            version.getPublishedAt()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(
            String documentNo,
            int versionNo,
            String errorMessage) {
        KnowledgeDocument document = documentRepository
            .findByDocumentNoForUpdate(documentNo)
            .orElse(null);

        if (document == null) {
            return;
        }

        KnowledgeDocumentVersion version =
            versionRepository
                .findForUpdate(document.getId(), versionNo)
                .orElse(null);

        if (version == null
                || version.getStatus()
                    != KnowledgeVersionStatus.PROCESSING) {
            return;
        }

        version.markFailed(errorMessage);
        document.markFailed(versionNo);

        versionRepository.saveAndFlush(version);
        documentRepository.saveAndFlush(document);
    }

    @Transactional
    public void unlinkPreviousVectors(
            Long previousVersionId,
            List<String> deletedVectorIds) {
        if (previousVersionId == null
                || deletedVectorIds == null
                || deletedVectorIds.isEmpty()) {
            return;
        }

        Set<UUID> deletedIds = deletedVectorIds
            .stream()
            .map(UUID::fromString)
            .collect(Collectors.toSet());

        List<KnowledgeChunk> chunks = chunkRepository
            .findByDocumentVersion_IdOrderByChunkNoAsc(
                previousVersionId
            );

        chunks.stream()
            .filter(chunk ->
                chunk.getVectorStoreId() != null
                    && deletedIds.contains(
                        chunk.getVectorStoreId()
                    )
            )
            .forEach(KnowledgeChunk::unlinkVectorStore);

        chunkRepository.saveAll(chunks);
        chunkRepository.flush();
    }

    public record ProcessingContext(
        Long documentId,
        String documentNo,
        String title,
        KnowledgeDocumentCategory category,
        Long versionId,
        int versionNo,
        KnowledgeSourceType sourceType,
        String contentHash,
        String rawContent,
        Integer previousActiveVersionNo,
        Long previousActiveVersionId,
        List<String> previousVectorIds
    ) {
    }

    public record PreparedChunk(
        UUID vectorStoreId,
        int chunkNo,
        String text,
        Map<String, Object> metadata
    ) {
        public PreparedChunk {
            metadata = Map.copyOf(metadata);
        }
    }
}