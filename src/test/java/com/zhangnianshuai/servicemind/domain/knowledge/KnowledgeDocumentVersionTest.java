package com.zhangnianshuai.servicemind.domain.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class KnowledgeDocumentVersionTest {

    @Test
    void shouldStartAsDraftAndNormalizeValues() {
        KnowledgeDocumentVersion version = createVersion();

        assertThat(version.getVersionNo()).isEqualTo(1);
        assertThat(version.getStatus())
            .isEqualTo(KnowledgeVersionStatus.DRAFT);
        assertThat(version.getOriginalFilename())
            .isEqualTo("policy.md");
        assertThat(version.getMimeType())
            .isEqualTo("text/markdown");
        assertThat(version.getRawContent())
            .isEqualTo("# Repair policy");
        assertThat(version.getChunkCount()).isZero();
    }

    @Test
    void shouldRequireProcessingBeforeReady() {
        KnowledgeDocumentVersion version = createVersion();

        assertThatThrownBy(() -> version.markReady(
            2,
            "text-embedding-v4",
            768
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PROCESSING");
    }

    @Test
    void shouldStoreEmbeddingDetailsWhenReady() {
        KnowledgeDocumentVersion version = createVersion();

        version.markProcessing();
        version.markReady(
            3,
            "text-embedding-v4",
            768
        );

        assertThat(version.getStatus())
            .isEqualTo(KnowledgeVersionStatus.READY);
        assertThat(version.getChunkCount()).isEqualTo(3);
        assertThat(version.getEmbeddingModel())
            .isEqualTo("text-embedding-v4");
        assertThat(version.getEmbeddingDimensions())
            .isEqualTo(768);
        assertThat(version.getErrorMessage()).isNull();
        assertThat(version.getProcessedAt()).isNotNull();
        assertThat(version.getPublishedAt()).isNotNull();
    }

    @Test
    void shouldAllowFailedVersionToRetry() {
        KnowledgeDocumentVersion version = createVersion();

        version.markProcessing();
        version.markFailed(" temporary network failure ");

        assertThat(version.getStatus())
            .isEqualTo(KnowledgeVersionStatus.FAILED);
        assertThat(version.getErrorMessage())
            .isEqualTo("temporary network failure");
        assertThat(version.getProcessedAt()).isNotNull();
        assertThat(version.getPublishedAt()).isNull();

        version.markProcessing();

        assertThat(version.getStatus())
            .isEqualTo(KnowledgeVersionStatus.PROCESSING);
        assertThat(version.getErrorMessage()).isNull();
        assertThat(version.getProcessedAt()).isNull();
        assertThat(version.getPublishedAt()).isNull();
    }

    @Test
    void shouldRejectInvalidReadyParameters() {
        KnowledgeDocumentVersion version = createVersion();

        version.markProcessing();

        assertThatThrownBy(() -> version.markReady(
            0,
            "text-embedding-v4",
            768
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Chunk count");

        assertThatThrownBy(() -> version.markReady(
            1,
            "text-embedding-v4",
            0
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Embedding dimensions");
    }

    @Test
    void shouldNotFailReadyOrArchivedVersion() {
        KnowledgeDocumentVersion readyVersion = createVersion();

        readyVersion.markProcessing();
        readyVersion.markReady(
            1,
            "text-embedding-v4",
            768
        );

        assertThatThrownBy(() ->
            readyVersion.markFailed("failure")
        )
            .isInstanceOf(IllegalStateException.class);

        KnowledgeDocumentVersion archivedVersion =
            createVersion();

        archivedVersion.archive();

        assertThatThrownBy(() ->
            archivedVersion.markFailed("failure")
        )
            .isInstanceOf(IllegalStateException.class);
    }

    private KnowledgeDocumentVersion createVersion() {
        KnowledgeDocument document = new KnowledgeDocument(
            "KB-TEST",
            "Repair policy",
            KnowledgeDocumentCategory.REPAIR,
            "tester"
        );

        int versionNo = document.beginNewVersion();

        return new KnowledgeDocumentVersion(
            document,
            versionNo,
            KnowledgeSourceType.MARKDOWN,
            " policy.md ",
            " text/markdown ",
            "a".repeat(64),
            " # Repair policy "
        );
    }
}