package com.zhangnianshuai.servicemind.domain.knowledge;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface KnowledgeDocumentVersionRepository
        extends JpaRepository<KnowledgeDocumentVersion, Long> {

    Optional<KnowledgeDocumentVersion>
        findByDocument_IdAndVersionNo(
            Long documentId,
            int versionNo
        );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select version
        from KnowledgeDocumentVersion version
        join fetch version.document document
        where document.id = :documentId
          and version.versionNo = :versionNo
        """)
    Optional<KnowledgeDocumentVersion> findForUpdate(
        @Param("documentId") Long documentId,
        @Param("versionNo") int versionNo
    );

    List<KnowledgeDocumentVersion>
        findByDocument_DocumentNoIgnoreCaseOrderByVersionNoDesc(
            String documentNo
        );

    boolean existsByDocument_IdAndContentHash(
        Long documentId,
        String contentHash
    );
}