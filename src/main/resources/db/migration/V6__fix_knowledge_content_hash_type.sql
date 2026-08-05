ALTER TABLE servicemind.knowledge_document_versions
    ALTER COLUMN content_hash TYPE VARCHAR(64)
    USING BTRIM(content_hash);