package com.zhangnianshuai.servicemind.api.knowledge.dto;

import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;

public record KnowledgeSearchHitResponse(

    String vectorStoreId,
    String documentNo,
    String title,
    KnowledgeDocumentCategory category,
    int versionNo,
    int chunkNo,
    Double score,
    String content
) {
}