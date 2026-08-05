package com.zhangnianshuai.servicemind.api.knowledge.dto;

import java.time.OffsetDateTime;

import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentStatus;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeVersionStatus;

public record KnowledgePublishResponse(

    String documentNo,
    int versionNo,
    KnowledgeDocumentStatus documentStatus,
    KnowledgeVersionStatus versionStatus,
    int chunkCount,
    String embeddingModel,
    int embeddingDimensions,
    OffsetDateTime processedAt,
    OffsetDateTime publishedAt
) {
}