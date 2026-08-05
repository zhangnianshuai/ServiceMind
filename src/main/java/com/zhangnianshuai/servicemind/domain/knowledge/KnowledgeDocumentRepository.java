package com.zhangnianshuai.servicemind.domain.knowledge;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface KnowledgeDocumentRepository
        extends JpaRepository<KnowledgeDocument, Long> {

    Optional<KnowledgeDocument>
        findByDocumentNoIgnoreCase(String documentNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select document
        from KnowledgeDocument document
        where lower(document.documentNo)
            = lower(:documentNo)
        """)
    Optional<KnowledgeDocument> findByDocumentNoForUpdate(
        @Param("documentNo") String documentNo
    );

    boolean existsByDocumentNoIgnoreCase(String documentNo);

    List<KnowledgeDocument>
        findAllByOrderByUpdatedAtDesc();

    List<KnowledgeDocument>
        findByStatusOrderByUpdatedAtDesc(
            KnowledgeDocumentStatus status
        );

    List<KnowledgeDocument>
        findByCategoryOrderByUpdatedAtDesc(
            KnowledgeDocumentCategory category
        );

    List<KnowledgeDocument>
        findByCategoryAndStatusOrderByUpdatedAtDesc(
            KnowledgeDocumentCategory category,
            KnowledgeDocumentStatus status
        );
}