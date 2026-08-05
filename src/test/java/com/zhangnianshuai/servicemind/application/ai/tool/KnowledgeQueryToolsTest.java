package com.zhangnianshuai.servicemind.application.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeSearchResponse;
import com.zhangnianshuai.servicemind.application.knowledge.KnowledgeRetrievalService;

@ExtendWith(MockitoExtension.class)
class KnowledgeQueryToolsTest {

    @Mock
    private KnowledgeRetrievalService retrievalService;

    private KnowledgeQueryTools tools;

    @BeforeEach
    void setUp() {
        tools = new KnowledgeQueryTools(retrievalService);
    }

    @Test
    void shouldReturnSuccessfulToolResult() {
        KnowledgeSearchResponse response =
            new KnowledgeSearchResponse(
                "repair",
                null,
                4,
                0.35d,
                0,
                List.of()
            );

        when(retrievalService.search(
            "repair",
            null,
            4,
            0.35d
        )).thenReturn(response);

        BusinessToolResult result =
            tools.searchActiveKnowledge("repair");

        assertThat(result.success()).isTrue();
        assertThat(result.operation())
            .isEqualTo("search_active_knowledge");
        assertThat(result.data()).isSameAs(response);
        assertThat(result.message()).isNull();
    }

    @Test
    void shouldConvertResponseStatusExceptionToFailure() {
        when(retrievalService.search(
            " ",
            null,
            4,
            0.35d
        )).thenThrow(new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Knowledge search query is required."
        ));

        BusinessToolResult result =
            tools.searchActiveKnowledge(" ");

        assertThat(result.success()).isFalse();
        assertThat(result.data()).isNull();
        assertThat(result.message())
            .isEqualTo("Knowledge search query is required.");
    }

    @Test
    void shouldSanitizeRuntimeExceptionMessage() {
        when(retrievalService.search(
            "repair",
            null,
            4,
            0.35d
        )).thenThrow(new RuntimeException(
            "line one\nline two\tline three"
        ));

        BusinessToolResult result =
            tools.searchActiveKnowledge("repair");

        assertThat(result.success()).isFalse();
        assertThat(result.message())
            .isEqualTo("line one line two line three");
    }

    @Test
    void shouldUseSafeMessageWhenExceptionHasNoMessage() {
        when(retrievalService.search(
            "repair",
            null,
            4,
            0.35d
        )).thenThrow(new RuntimeException());

        BusinessToolResult result =
            tools.searchActiveKnowledge("repair");

        assertThat(result.success()).isFalse();
        assertThat(result.message())
            .isEqualTo("Knowledge retrieval failed.");
    }
}