package com.zhangnianshuai.servicemind.domain.knowledge;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.util.StringUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "knowledge_chunks",
    schema = "servicemind"
)
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "document_version_id",
        nullable = false
    )
    private KnowledgeDocumentVersion documentVersion;

    @Column(name = "chunk_no", nullable = false)
    private int chunkNo;

    @Column(
        name = "chunk_text",
        nullable = false,
        columnDefinition = "TEXT"
    )
    private String chunkText;

    @Column(
        name = "character_count",
        nullable = false
    )
    private int characterCount;

    @Column(name = "token_count")
    private Integer tokenCount;

    @Column(
        name = "vector_store_id",
        unique = true
    )
    private UUID vectorStoreId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        nullable = false,
        columnDefinition = "jsonb"
    )
    private Map<String, Object> metadata =
        new LinkedHashMap<>();

    @Column(
        name = "created_at",
        nullable = false
    )
    private OffsetDateTime createdAt;

    protected KnowledgeChunk() {
    }

    public KnowledgeChunk(
            KnowledgeDocumentVersion documentVersion,
            int chunkNo,
            String chunkText,
            Integer tokenCount,
            Map<String, Object> metadata) {
        if (documentVersion == null) {
            throw new IllegalArgumentException(
                "Knowledge document version is required."
            );
        }

        if (chunkNo <= 0) {
            throw new IllegalArgumentException(
                "Chunk number must be greater than zero."
            );
        }

        if (!StringUtils.hasText(chunkText)) {
            throw new IllegalArgumentException(
                "Chunk text is required."
            );
        }

        if (tokenCount != null && tokenCount < 0) {
            throw new IllegalArgumentException(
                "Token count must not be negative."
            );
        }

        this.documentVersion = documentVersion;
        this.chunkNo = chunkNo;
        this.chunkText = chunkText.trim();
        this.characterCount = this.chunkText.length();
        this.tokenCount = tokenCount;

        if (metadata != null) {
            this.metadata = new LinkedHashMap<>(metadata);
        }
    }

    public void linkVectorStore(UUID vectorStoreId) {
        if (vectorStoreId == null) {
            throw new IllegalArgumentException(
                "Vector store identifier is required."
            );
        }

        this.vectorStoreId = vectorStoreId;
    }

    public void unlinkVectorStore() {
        this.vectorStoreId = null;
    }

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public KnowledgeDocumentVersion getDocumentVersion() {
        return documentVersion;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public String getChunkText() {
        return chunkText;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    public Integer getTokenCount() {
        return tokenCount;
    }

    public UUID getVectorStoreId() {
        return vectorStoreId;
    }

    public Map<String, Object> getMetadata() {
        return Map.copyOf(metadata);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}