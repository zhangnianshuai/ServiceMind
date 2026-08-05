package com.zhangnianshuai.servicemind.api.knowledge.dto;

import java.util.List;

import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;

public record KnowledgeSearchResponse(

    String query,
    KnowledgeDocumentCategory category,
    int topK,
    double similarityThreshold,
    int resultCount,
    List<KnowledgeSearchHitResponse> hits
) {
    public KnowledgeSearchResponse {
        hits = List.copyOf(hits);
    }
}