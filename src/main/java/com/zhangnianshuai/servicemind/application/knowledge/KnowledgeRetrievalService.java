package com.zhangnianshuai.servicemind.application.knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeSearchHitResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeSearchResponse;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocument;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentRepository;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentStatus;

@Service
@Transactional(readOnly = true)
public class KnowledgeRetrievalService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOP_K = 10;
    private static final int OVERSAMPLE_FACTOR = 3;
    private static final int MAX_VECTOR_RESULTS = 30;

    private static final double DEFAULT_SIMILARITY_THRESHOLD =
        0.35d;

    private static final int MAX_QUERY_LENGTH = 500;

    private final VectorStore vectorStore;
    private final KnowledgeDocumentRepository documentRepository;

    public KnowledgeRetrievalService(
            VectorStore vectorStore,
            KnowledgeDocumentRepository documentRepository) {
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
    }

    public KnowledgeSearchResponse search(
            String query,
            KnowledgeDocumentCategory category,
            Integer requestedTopK,
            Double requestedSimilarityThreshold) {
        String normalizedQuery = normalizeQuery(query);
        int topK = normalizeTopK(requestedTopK);

        double similarityThreshold =
            normalizeSimilarityThreshold(
                requestedSimilarityThreshold
            );

        int vectorTopK = Math.min(
            MAX_VECTOR_RESULTS,
            topK * OVERSAMPLE_FACTOR
        );

        SearchRequest request = SearchRequest.builder()
            .query(normalizedQuery)
            .topK(vectorTopK)
            .similarityThreshold(similarityThreshold)
            .filterExpression(buildFilterExpression(category))
            .build();

        List<Document> vectorResults =
            vectorStore.similaritySearch(request);

        if (vectorResults == null
                || vectorResults.isEmpty()) {
            return emptyResponse(
                normalizedQuery,
                category,
                topK,
                similarityThreshold
            );
        }

        Map<String, KnowledgeDocument> documentCache =
            new HashMap<>();

        List<KnowledgeSearchHitResponse> hits =
            new ArrayList<>();

        for (Document vectorDocument : vectorResults) {
            KnowledgeSearchHitResponse hit = toActiveHit(
                vectorDocument,
                category,
                documentCache
            );

            if (hit == null) {
                continue;
            }

            hits.add(hit);

            if (hits.size() >= topK) {
                break;
            }
        }

        return new KnowledgeSearchResponse(
            normalizedQuery,
            category,
            topK,
            similarityThreshold,
            hits.size(),
            hits
        );
    }

    private KnowledgeSearchHitResponse toActiveHit(
            Document vectorDocument,
            KnowledgeDocumentCategory requestedCategory,
            Map<String, KnowledgeDocument> documentCache) {
        Map<String, Object> metadata =
            vectorDocument.getMetadata();

        String documentNo = metadataString(
            metadata,
            "document_no"
        );

        Integer versionNo = metadataInteger(
            metadata,
            "version_no"
        );

        Integer chunkNo = metadataInteger(
            metadata,
            "chunk_no"
        );

        String content = vectorDocument.getText();

        if (!StringUtils.hasText(documentNo)
                || versionNo == null
                || versionNo <= 0
                || chunkNo == null
                || chunkNo <= 0
                || !StringUtils.hasText(content)) {
            return null;
        }

        KnowledgeDocument knowledgeDocument =
            resolveDocument(
                documentNo,
                documentCache
            );

        if (knowledgeDocument == null
                || knowledgeDocument.getStatus()
                    != KnowledgeDocumentStatus.READY
                || knowledgeDocument.getActiveVersionNo()
                    == null
                || knowledgeDocument.getActiveVersionNo()
                    .intValue() != versionNo) {
            return null;
        }

        if (requestedCategory != null
                && knowledgeDocument.getCategory()
                    != requestedCategory) {
            return null;
        }

        return new KnowledgeSearchHitResponse(
            vectorDocument.getId(),
            knowledgeDocument.getDocumentNo(),
            knowledgeDocument.getTitle(),
            knowledgeDocument.getCategory(),
            versionNo,
            chunkNo,
            vectorDocument.getScore(),
            content.strip()
        );
    }

    private KnowledgeDocument resolveDocument(
            String documentNo,
            Map<String, KnowledgeDocument> documentCache) {
        if (documentCache.containsKey(documentNo)) {
            return documentCache.get(documentNo);
        }

        KnowledgeDocument document = documentRepository
            .findByDocumentNoIgnoreCase(documentNo)
            .orElse(null);

        documentCache.put(documentNo, document);

        return document;
    }

    private String buildFilterExpression(
            KnowledgeDocumentCategory category) {
        String expression =
            "knowledge_scope == 'servicemind'";

        if (category != null) {
            expression +=
                " && category == '"
                    + category.name()
                    + "'";
        }

        return expression;
    }

    private KnowledgeSearchResponse emptyResponse(
            String query,
            KnowledgeDocumentCategory category,
            int topK,
            double similarityThreshold) {
        return new KnowledgeSearchResponse(
            query,
            category,
            topK,
            similarityThreshold,
            0,
            List.of()
        );
    }

    private String normalizeQuery(String query) {
        if (!StringUtils.hasText(query)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Knowledge search query is required."
            );
        }

        String normalized = query.strip();

        if (normalized.length() > MAX_QUERY_LENGTH) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Knowledge search query exceeds "
                    + MAX_QUERY_LENGTH
                    + " characters."
            );
        }

        return normalized;
    }

    private int normalizeTopK(Integer requestedTopK) {
        int topK = requestedTopK == null
            ? DEFAULT_TOP_K
            : requestedTopK;

        if (topK <= 0 || topK > MAX_TOP_K) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "topK must be between 1 and "
                    + MAX_TOP_K
                    + "."
            );
        }

        return topK;
    }

    private double normalizeSimilarityThreshold(
            Double requestedThreshold) {
        double threshold = requestedThreshold == null
            ? DEFAULT_SIMILARITY_THRESHOLD
            : requestedThreshold;

        if (!Double.isFinite(threshold)
                || threshold < 0.0d
                || threshold > 1.0d) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "similarityThreshold must be between "
                    + "0.0 and 1.0."
            );
        }

        return threshold;
    }

    private String metadataString(
            Map<String, Object> metadata,
            String key) {
        Object value = metadata.get(key);

        if (value == null) {
            return null;
        }

        String text = String.valueOf(value).trim();

        return text.isEmpty() ? null : text;
    }

    private Integer metadataInteger(
            Map<String, Object> metadata,
            String key) {
        Object value = metadata.get(key);

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(
                String.valueOf(value).trim()
            );
        }
        catch (NumberFormatException exception) {
            return null;
        }
    }
}