package com.zhangnianshuai.servicemind.domain.knowledge;

import java.time.OffsetDateTime;

import org.springframework.util.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(
    name = "knowledge_documents",
    schema = "servicemind"
)
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
        name = "document_no",
        nullable = false,
        unique = true,
        length = 32
    )
    private String documentNo;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private KnowledgeDocumentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private KnowledgeDocumentStatus status;

    @Column(
        name = "latest_version_no",
        nullable = false
    )
    private int latestVersionNo;

    @Column(name = "active_version_no")
    private Integer activeVersionNo;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Version
    @Column(nullable = false)
    private long version;

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

    protected KnowledgeDocument() {
    }

    public KnowledgeDocument(
            String documentNo,
            String title,
            KnowledgeDocumentCategory category,
            String createdBy) {
        this.documentNo = requireText(
            documentNo,
            "Document number is required."
        );
        this.title = requireText(
            title,
            "Document title is required."
        );

        if (category == null) {
            throw new IllegalArgumentException(
                "Document category is required."
            );
        }

        this.category = category;
        this.createdBy = normalizeOptional(createdBy);
        this.status = KnowledgeDocumentStatus.DRAFT;
        this.latestVersionNo = 0;
        this.activeVersionNo = null;
    }

    public int beginNewVersion() {
        ensureNotArchived();

        latestVersionNo++;

        if (activeVersionNo == null) {
            status = KnowledgeDocumentStatus.DRAFT;
        }

        return latestVersionNo;
    }

    public void markProcessing(int versionNo) {
        validateVersionNumber(versionNo);

        if (activeVersionNo == null) {
            status = KnowledgeDocumentStatus.PROCESSING;
        }
    }

    public void markReady(int versionNo) {
        validateVersionNumber(versionNo);

        activeVersionNo = versionNo;
        status = KnowledgeDocumentStatus.READY;
    }

    public void markFailed(int versionNo) {
        validateVersionNumber(versionNo);

        if (versionNo == latestVersionNo
                && activeVersionNo == null) {
            status = KnowledgeDocumentStatus.FAILED;
        }
    }

    public void changeMetadata(
            String title,
            KnowledgeDocumentCategory category) {
        ensureNotArchived();

        this.title = requireText(
            title,
            "Document title is required."
        );

        if (category == null) {
            throw new IllegalArgumentException(
                "Document category is required."
            );
        }

        this.category = category;
    }

    public void archive() {
        status = KnowledgeDocumentStatus.ARCHIVED;
    }

    private void validateVersionNumber(int versionNo) {
        if (versionNo <= 0 || versionNo > latestVersionNo) {
            throw new IllegalArgumentException(
                "Invalid knowledge document version number."
            );
        }
    }

    private void ensureNotArchived() {
        if (status == KnowledgeDocumentStatus.ARCHIVED) {
            throw new IllegalStateException(
                "Archived knowledge documents cannot be modified."
            );
        }
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

    public String getDocumentNo() {
        return documentNo;
    }

    public String getTitle() {
        return title;
    }

    public KnowledgeDocumentCategory getCategory() {
        return category;
    }

    public KnowledgeDocumentStatus getStatus() {
        return status;
    }

    public int getLatestVersionNo() {
        return latestVersionNo;
    }

    public Integer getActiveVersionNo() {
        return activeVersionNo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getVersion() {
        return version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}