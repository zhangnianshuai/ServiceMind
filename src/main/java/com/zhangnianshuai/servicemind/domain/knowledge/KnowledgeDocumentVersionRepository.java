package com.zhangnianshuai.servicemind.domain.knowledge;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentVersionRepository
        extends JpaRepository<KnowledgeDocumentVersion, Long> {

    Optional<KnowledgeDocumentVersion>
        findByDocument_IdAndVersionNo(
            Long documentId,
            int versionNo
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