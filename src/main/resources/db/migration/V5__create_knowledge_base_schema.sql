CREATE TABLE servicemind.knowledge_documents (
    id BIGSERIAL PRIMARY KEY,
    document_no VARCHAR(32) NOT NULL UNIQUE,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(32) NOT NULL,
    status VARCHAR(24) NOT NULL,
    latest_version_no INTEGER NOT NULL DEFAULT 0,
    active_version_no INTEGER,
    created_by VARCHAR(100),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_knowledge_document_category
        CHECK (
            category IN (
                'PRODUCT',
                'WARRANTY',
                'RETURN_EXCHANGE',
                'REPAIR',
                'ORDER_PAYMENT',
                'ACCOUNT',
                'GENERAL'
            )
        ),

    CONSTRAINT chk_knowledge_document_status
        CHECK (
            status IN (
                'DRAFT',
                'PROCESSING',
                'READY',
                'FAILED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT chk_knowledge_document_latest_version
        CHECK (latest_version_no >= 0),

    CONSTRAINT chk_knowledge_document_active_version
        CHECK (
            active_version_no IS NULL
            OR (
                active_version_no > 0
                AND active_version_no <= latest_version_no
            )
        )
);

CREATE TABLE servicemind.knowledge_document_versions (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL,
    version_no INTEGER NOT NULL,
    source_type VARCHAR(24) NOT NULL,
    original_filename VARCHAR(255),
    mime_type VARCHAR(100),
    content_hash CHAR(64) NOT NULL,
    raw_content TEXT NOT NULL,
    status VARCHAR(24) NOT NULL,
    chunk_count INTEGER NOT NULL DEFAULT 0,
    embedding_model VARCHAR(100),
    embedding_dimensions INTEGER,
    error_message VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMPTZ,
    published_at TIMESTAMPTZ,

    CONSTRAINT fk_knowledge_version_document
        FOREIGN KEY (document_id)
        REFERENCES servicemind.knowledge_documents(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_knowledge_document_version
        UNIQUE (document_id, version_no),

    CONSTRAINT chk_knowledge_version_number
        CHECK (version_no > 0),

    CONSTRAINT chk_knowledge_source_type
        CHECK (
            source_type IN (
                'TEXT',
                'MARKDOWN',
                'FILE',
                'URL'
            )
        ),

    CONSTRAINT chk_knowledge_version_status
        CHECK (
            status IN (
                'DRAFT',
                'PROCESSING',
                'READY',
                'FAILED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT chk_knowledge_chunk_count
        CHECK (chunk_count >= 0),

    CONSTRAINT chk_knowledge_embedding_dimensions
        CHECK (
            embedding_dimensions IS NULL
            OR embedding_dimensions > 0
        )
);

CREATE TABLE servicemind.knowledge_chunks (
    id BIGSERIAL PRIMARY KEY,
    document_version_id BIGINT NOT NULL,
    chunk_no INTEGER NOT NULL,
    chunk_text TEXT NOT NULL,
    character_count INTEGER NOT NULL,
    token_count INTEGER,
    vector_store_id UUID UNIQUE,
    metadata JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_knowledge_chunk_version
        FOREIGN KEY (document_version_id)
        REFERENCES servicemind.knowledge_document_versions(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_knowledge_version_chunk
        UNIQUE (document_version_id, chunk_no),

    CONSTRAINT chk_knowledge_chunk_number
        CHECK (chunk_no > 0),

    CONSTRAINT chk_knowledge_character_count
        CHECK (character_count >= 0),

    CONSTRAINT chk_knowledge_token_count
        CHECK (
            token_count IS NULL
            OR token_count >= 0
        )
);

CREATE INDEX idx_knowledge_documents_category
    ON servicemind.knowledge_documents(category);

CREATE INDEX idx_knowledge_documents_status
    ON servicemind.knowledge_documents(status);

CREATE INDEX idx_knowledge_documents_updated_at
    ON servicemind.knowledge_documents(updated_at DESC);

CREATE INDEX idx_knowledge_versions_document_id
    ON servicemind.knowledge_document_versions(document_id);

CREATE INDEX idx_knowledge_versions_status
    ON servicemind.knowledge_document_versions(status);

CREATE INDEX idx_knowledge_versions_content_hash
    ON servicemind.knowledge_document_versions(content_hash);

CREATE INDEX idx_knowledge_chunks_version_id
    ON servicemind.knowledge_chunks(document_version_id);

CREATE INDEX idx_knowledge_chunks_vector_store_id
    ON servicemind.knowledge_chunks(vector_store_id);