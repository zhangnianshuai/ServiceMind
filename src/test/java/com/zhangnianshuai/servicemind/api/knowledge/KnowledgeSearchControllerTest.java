package com.zhangnianshuai.servicemind.api.knowledge;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeSearchHitResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeSearchResponse;
import com.zhangnianshuai.servicemind.application.knowledge.KnowledgeRetrievalService;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;

@ExtendWith(MockitoExtension.class)
class KnowledgeSearchControllerTest {

    @Mock
    private KnowledgeRetrievalService retrievalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(
                new KnowledgeSearchController(
                    retrievalService
                )
            )
            .build();
    }

    @Test
    void shouldReturnKnowledgeSearchResponse()
            throws Exception {
        KnowledgeSearchHitResponse hit =
            new KnowledgeSearchHitResponse(
                "vector-1",
                "KB-001",
                "Repair process",
                KnowledgeDocumentCategory.REPAIR,
                2,
                3,
                0.82d,
                "Raise the support ticket priority."
            );

        KnowledgeSearchResponse response =
            new KnowledgeSearchResponse(
                "repair delay",
                KnowledgeDocumentCategory.REPAIR,
                3,
                0.40d,
                1,
                List.of(hit)
            );

        when(retrievalService.search(
            "repair delay",
            KnowledgeDocumentCategory.REPAIR,
            3,
            0.40d
        )).thenReturn(response);

        mockMvc.perform(get("/api/knowledge/search")
                .param("query", "repair delay")
                .param("category", "REPAIR")
                .param("topK", "3")
                .param("similarityThreshold", "0.40"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.query")
                .value("repair delay"))
            .andExpect(jsonPath("$.category")
                .value("REPAIR"))
            .andExpect(jsonPath("$.resultCount")
                .value(1))
            .andExpect(jsonPath("$.hits[0].documentNo")
                .value("KB-001"))
            .andExpect(jsonPath("$.hits[0].versionNo")
                .value(2))
            .andExpect(jsonPath("$.hits[0].chunkNo")
                .value(3));

        verify(retrievalService).search(
            "repair delay",
            KnowledgeDocumentCategory.REPAIR,
            3,
            0.40d
        );
    }

    @Test
    void shouldApplyControllerDefaults()
            throws Exception {
        KnowledgeSearchResponse response =
            new KnowledgeSearchResponse(
                "repair",
                null,
                5,
                0.35d,
                0,
                List.of()
            );

        when(retrievalService.search(
            "repair",
            null,
            5,
            0.35d
        )).thenReturn(response);

        mockMvc.perform(get("/api/knowledge/search")
                .param("query", "repair"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.topK").value(5))
            .andExpect(jsonPath("$.similarityThreshold")
                .value(0.35d))
            .andExpect(jsonPath("$.resultCount")
                .value(0));
    }

    @Test
    void shouldReturnBadRequestForServiceValidation()
            throws Exception {
        when(retrievalService.search(
            "test",
            null,
            0,
            0.35d
        )).thenThrow(new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "topK must be between 1 and 10."
        ));

        mockMvc.perform(get("/api/knowledge/search")
                .param("query", "test")
                .param("topK", "0"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForInvalidCategory()
            throws Exception {
        mockMvc.perform(get("/api/knowledge/search")
                .param("query", "repair")
                .param("category", "INVALID_CATEGORY"))
            .andExpect(status().isBadRequest());
    }
}