package com.zhangnianshuai.servicemind.domain.knowledge;

import java.time.OffsetDateTime;

import org.springframework.util.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "knowledge_document_versions",
    schema = "servicemind"
)
public class KnowledgeDocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "document_id",
        nullable = false
    )
    private KnowledgeDocument document;

    @Column(
        name = "version_no",
        nullable = false
    )
    private int versionNo;

    @Enumerated(EnumType.STRING)
    @Column(
        name = "source_type",
        nullable = false,
        length = 24
    )
    private KnowledgeSourceType sourceType;

    @Column(
        name = "original_filename",
        length = 255
    )
    private String originalFilename;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(
        name = "content_hash",
        nullable = false,
        length = 64
    )
    private String contentHash;

    @Column(
        name = "raw_content",
        nullable = false,
        columnDefinition = "TEXT"
    )
    private String rawContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private KnowledgeVersionStatus status;

    @Column(
        name = "chunk_count",
        nullable = false
    )
    private int chunkCount;

    @Column(
        name = "embedding_model",
        length = 100
    )
    private String embeddingModel;

    @Column(name = "embedding_dimensions")
    private Integer embeddingDimensions;

    @Column(
        name = "error_message",
        length = 1000
    )
    private String errorMessage;

    @Column(
        name = "created_at",
        nullable = false
    )
    private OffsetDateTime createdAt;

    @Column(
        name = "updated_at",
        nullable = false
    )
    private OffsetDateTime updatedAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    protected KnowledgeDocumentVersion() {
    }

    public KnowledgeDocumentVersion(
            KnowledgeDocument document,
            int versionNo,
            KnowledgeSourceType sourceType,
            String originalFilename,
            String mimeType,
            String contentHash,
            String rawContent) {
        if (document == null) {
            throw new IllegalArgumentException(
                "Knowledge document is required."
            );
        }

        if (versionNo <= 0) {
            throw new IllegalArgumentException(
                "Version number must be greater than zero."
            );
        }

        if (sourceType == null) {
            throw new IllegalArgumentException(
                "Knowledge source type is required."
            );
        }

        this.document = document;
        this.versionNo = versionNo;
        this.sourceType = sourceType;
        this.originalFilename =
            normalizeOptional(originalFilename);
        this.mimeType = normalizeOptional(mimeType);
        this.contentHash = requireText(
            contentHash,
            "Content hash is required."
        );
        this.rawContent = requireText(
            rawContent,
            "Knowledge content is required."
        );
        this.status = KnowledgeVersionStatus.DRAFT;
        this.chunkCount = 0;
    }

    public void markProcessing() {
        if (status == KnowledgeVersionStatus.ARCHIVED
                || status == KnowledgeVersionStatus.READY) {
            throw new IllegalStateException(
                "This knowledge version cannot be processed."
            );
        }

        status = KnowledgeVersionStatus.PROCESSING;
        errorMessage = null;
    }

    public void markReady(
            int chunkCount,
            String embeddingModel,
            int embeddingDimensions) {
        if (chunkCount < 0) {
            throw new IllegalArgumentException(
                "Chunk count must not be negative."
            );
        }

        if (embeddingDimensions <= 0) {
            throw new IllegalArgumentException(
                "Embedding dimensions must be greater than zero."
            );
        }

        this.chunkCount = chunkCount;
        this.embeddingModel = requireText(
            embeddingModel,
            "Embedding model is required."
        );
        this.embeddingDimensions = embeddingDimensions;
        this.errorMessage = null;
        this.status = KnowledgeVersionStatus.READY;

        OffsetDateTime now = OffsetDateTime.now();
        this.processedAt = now;
        this.publishedAt = now;
    }

    public void markFailed(String errorMessage) {
        this.status = KnowledgeVersionStatus.FAILED;
        this.errorMessage = normalizeOptional(errorMessage);
        this.processedAt = OffsetDateTime.now();
    }

    public void archive() {
        this.status = KnowledgeVersionStatus.ARCHIVED;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    private static String requireText(
            String value,
            String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }

        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public KnowledgeDocument getDocument() {
        return document;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public KnowledgeSourceType getSourceType() {
        return sourceType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getRawContent() {
        return rawContent;
    }

    public KnowledgeVersionStatus getStatus() {
        return status;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public Integer getEmbeddingDimensions() {
        return embeddingDimensions;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }
}