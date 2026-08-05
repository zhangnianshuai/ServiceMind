package com.zhangnianshuai.servicemind.domain.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class KnowledgeDocumentTest {

    @Test
    void shouldManageDraftAndActiveVersions() {
        KnowledgeDocument document = new KnowledgeDocument(
            " KB-001 ",
            " Repair policy ",
            KnowledgeDocumentCategory.REPAIR,
            " admin "
        );

        assertThat(document.getDocumentNo()).isEqualTo("KB-001");
        assertThat(document.getTitle()).isEqualTo("Repair policy");
        assertThat(document.getCreatedBy()).isEqualTo("admin");
        assertThat(document.getStatus())
            .isEqualTo(KnowledgeDocumentStatus.DRAFT);
        assertThat(document.getLatestVersionNo()).isZero();
        assertThat(document.getActiveVersionNo()).isNull();

        int versionOne = document.beginNewVersion();

        assertThat(versionOne).isEqualTo(1);
        assertThat(document.getLatestVersionNo()).isEqualTo(1);

        document.markProcessing(versionOne);

        assertThat(document.getStatus())
            .isEqualTo(KnowledgeDocumentStatus.PROCESSING);

        document.markReady(versionOne);

        assertThat(document.getStatus())
            .isEqualTo(KnowledgeDocumentStatus.READY);
        assertThat(document.getActiveVersionNo()).isEqualTo(1);

        int versionTwo = document.beginNewVersion();

        assertThat(versionTwo).isEqualTo(2);
        assertThat(document.getStatus())
            .isEqualTo(KnowledgeDocumentStatus.READY);
        assertThat(document.getActiveVersionNo()).isEqualTo(1);

        document.markProcessing(versionTwo);
        document.markFailed(versionTwo);

        assertThat(document.getStatus())
            .isEqualTo(KnowledgeDocumentStatus.READY);
        assertThat(document.getActiveVersionNo()).isEqualTo(1);

        document.markReady(versionTwo);

        assertThat(document.getActiveVersionNo()).isEqualTo(2);
    }

    @Test
    void shouldMarkDocumentFailedWhenNoActiveVersionExists() {
        KnowledgeDocument document = new KnowledgeDocument(
            "KB-002",
            "Warranty policy",
            KnowledgeDocumentCategory.WARRANTY,
            null
        );

        int versionNo = document.beginNewVersion();

        document.markProcessing(versionNo);
        document.markFailed(versionNo);

        assertThat(document.getStatus())
            .isEqualTo(KnowledgeDocumentStatus.FAILED);
        assertThat(document.getActiveVersionNo()).isNull();
    }

    @Test
    void shouldRejectInvalidVersionNumber() {
        KnowledgeDocument document = new KnowledgeDocument(
            "KB-003",
            "General policy",
            KnowledgeDocumentCategory.GENERAL,
            null
        );

        assertThatThrownBy(() -> document.markReady(1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(
                "Invalid knowledge document version number"
            );
    }

    @Test
    void shouldPreventModificationAfterArchive() {
        KnowledgeDocument document = new KnowledgeDocument(
            "KB-004",
            "Return policy",
            KnowledgeDocumentCategory.RETURN_EXCHANGE,
            null
        );

        document.archive();

        assertThat(document.getStatus())
            .isEqualTo(KnowledgeDocumentStatus.ARCHIVED);

        assertThatThrownBy(document::beginNewVersion)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("cannot be modified");

        assertThatThrownBy(() -> document.changeMetadata(
            "Updated title",
            KnowledgeDocumentCategory.GENERAL
        ))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("cannot be modified");
    }
}