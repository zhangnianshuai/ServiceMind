package com.zhangnianshuai.servicemind.domain.knowledge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeChunkRepository
        extends JpaRepository<KnowledgeChunk, Long> {

    List<KnowledgeChunk>
        findByDocumentVersion_IdOrderByChunkNoAsc(
            Long documentVersionId
        );

    Optional<KnowledgeChunk>
        findByVectorStoreId(UUID vectorStoreId);

    long countByDocumentVersion_Id(Long documentVersionId);

    void deleteByDocumentVersion_Id(Long documentVersionId);
}