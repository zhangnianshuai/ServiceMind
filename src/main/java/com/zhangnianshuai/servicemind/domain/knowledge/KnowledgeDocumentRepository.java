package com.zhangnianshuai.servicemind.domain.knowledge;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository
        extends JpaRepository<KnowledgeDocument, Long> {

    Optional<KnowledgeDocument>
        findByDocumentNoIgnoreCase(String documentNo);

    boolean existsByDocumentNoIgnoreCase(String documentNo);

    List<KnowledgeDocument>
        findByStatusOrderByUpdatedAtDesc(
            KnowledgeDocumentStatus status
        );

    List<KnowledgeDocument>
        findByCategoryOrderByUpdatedAtDesc(
            KnowledgeDocumentCategory category
        );
}