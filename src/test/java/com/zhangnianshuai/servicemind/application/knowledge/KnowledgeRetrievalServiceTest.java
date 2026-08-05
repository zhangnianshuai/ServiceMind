package com.zhangnianshuai.servicemind.application.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeSearchResponse;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocument;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentRepository;

@ExtendWith(MockitoExtension.class)
class KnowledgeRetrievalServiceTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private KnowledgeDocumentRepository documentRepository;

    private KnowledgeRetrievalService service;

    @BeforeEach
    void setUp() {
        service = new KnowledgeRetrievalService(
            vectorStore,
            documentRepository
        );
    }

    @Test
    void shouldReturnOnlyActiveVersion() {
        KnowledgeDocument document = readyDocument(
            "KB-001",
            KnowledgeDocumentCategory.REPAIR,
            2
        );

        Document staleVector = vectorDocument(
            "vector-1",
            "KB-001",
            1,
            1,
            "Old repair policy",
            0.91d
        );

        Document activeVector = vectorDocument(
            "vector-2",
            "KB-001",
            2,
            3,
            "Raise the ticket priority.",
            0.82d
        );

        when(vectorStore.similaritySearch(
            any(SearchRequest.class)
        )).thenReturn(List.of(staleVector, activeVector));

        when(documentRepository
            .findByDocumentNoIgnoreCase("KB-001"))
            .thenReturn(Optional.of(document));

        KnowledgeSearchResponse response = service.search(
            " repair delay ",
            KnowledgeDocumentCategory.REPAIR,
            5,
            0.35d
        );

        assertThat(response.query()).isEqualTo("repair delay");
        assertThat(response.resultCount()).isEqualTo(1);
        assertThat(response.hits()).hasSize(1);
        assertThat(response.hits().getFirst().versionNo())
            .isEqualTo(2);
        assertThat(response.hits().getFirst().chunkNo())
            .isEqualTo(3);
        assertThat(response.hits().getFirst().content())
            .isEqualTo("Raise the ticket priority.");

        verify(documentRepository)
            .findByDocumentNoIgnoreCase("KB-001");
    }

    @Test
    void shouldExcludeArchivedDocument() {
        KnowledgeDocument document = readyDocument(
            "KB-ARCHIVED",
            KnowledgeDocumentCategory.REPAIR,
            1
        );

        document.archive();

        Document archivedVector = vectorDocument(
            "vector-3",
            "KB-ARCHIVED",
            1,
            1,
            "Archived content",
            0.90d
        );

        when(vectorStore.similaritySearch(
            any(SearchRequest.class)
        )).thenReturn(List.of(archivedVector));

        when(documentRepository
            .findByDocumentNoIgnoreCase("KB-ARCHIVED"))
            .thenReturn(Optional.of(document));

        KnowledgeSearchResponse response = service.search(
            "repair",
            KnowledgeDocumentCategory.REPAIR,
            5,
            0.35d
        );

        assertThat(response.resultCount()).isZero();
        assertThat(response.hits()).isEmpty();
    }

    @Test
    void shouldApplyCategorySafetyCheck() {
        KnowledgeDocument document = readyDocument(
            "KB-REPAIR",
            KnowledgeDocumentCategory.REPAIR,
            1
        );

        Document repairVector = vectorDocument(
            "vector-4",
            "KB-REPAIR",
            1,
            1,
            "Repair content",
            0.88d
        );

        when(vectorStore.similaritySearch(
            any(SearchRequest.class)
        )).thenReturn(List.of(repairVector));

        when(documentRepository
            .findByDocumentNoIgnoreCase("KB-REPAIR"))
            .thenReturn(Optional.of(document));

        KnowledgeSearchResponse response = service.search(
            "repair",
            KnowledgeDocumentCategory.WARRANTY,
            5,
            0.35d
        );

        assertThat(response.hits()).isEmpty();
    }

    @Test
    void shouldReturnEmptyResponseWhenVectorStoreHasNoHits() {
        when(vectorStore.similaritySearch(
            any(SearchRequest.class)
        )).thenReturn(List.of());

        KnowledgeSearchResponse response = service.search(
            "unknown policy",
            null,
            null,
            null
        );

        assertThat(response.query())
            .isEqualTo("unknown policy");
        assertThat(response.topK()).isEqualTo(5);
        assertThat(response.similarityThreshold())
            .isEqualTo(0.35d);
        assertThat(response.resultCount()).isZero();
        assertThat(response.hits()).isEmpty();
    }

    @Test
    void shouldRejectInvalidSearchParameters() {
        assertThatThrownBy(() ->
            service.search(" ", null, 5, 0.35d)
        )
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("query is required");

        assertThatThrownBy(() ->
            service.search("repair", null, 0, 0.35d)
        )
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("topK");

        assertThatThrownBy(() ->
            service.search("repair", null, 5, 1.5d)
        )
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("similarityThreshold");
    }

    @Test
    void shouldIgnoreMalformedVectorMetadata() {
        Document malformed = mock(Document.class);

        when(malformed.getMetadata()).thenReturn(Map.of(
            "document_no", "KB-001",
            "version_no", "invalid",
            "chunk_no", 1
        ));
        when(malformed.getText()).thenReturn("Content");

        when(vectorStore.similaritySearch(
            any(SearchRequest.class)
        )).thenReturn(List.of(malformed));

        KnowledgeSearchResponse response = service.search(
            "repair",
            null,
            5,
            0.35d
        );

        assertThat(response.hits()).isEmpty();
    }

    private KnowledgeDocument readyDocument(
            String documentNo,
            KnowledgeDocumentCategory category,
            int activeVersionNo) {
        KnowledgeDocument document = new KnowledgeDocument(
            documentNo,
            "Test document",
            category,
            "tester"
        );

        for (int i = 1; i <= activeVersionNo; i++) {
            int versionNo = document.beginNewVersion();
            document.markReady(versionNo);
        }

        return document;
    }

    private Document vectorDocument(
            String id,
            String documentNo,
            int versionNo,
            int chunkNo,
            String content,
            double score) {
        Document document = mock(Document.class);

        lenient().when(document.getId()).thenReturn(id);
        lenient().when(document.getMetadata()).thenReturn(Map.of(
            "document_no", documentNo,
            "version_no", versionNo,
            "chunk_no", chunkNo
        ));
        lenient().when(document.getText()).thenReturn(content);
        lenient().when(document.getScore()).thenReturn(score);

        return document;
    }
}