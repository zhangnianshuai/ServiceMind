package com.zhangnianshuai.servicemind.api.knowledge;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeSearchResponse;
import com.zhangnianshuai.servicemind.application.knowledge.KnowledgeRetrievalService;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;

@Validated
@RestController
@RequestMapping("/api/knowledge/search")
public class KnowledgeSearchController {

    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeSearchController(
            KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @GetMapping
    public KnowledgeSearchResponse search(
            @RequestParam
            String query,

            @RequestParam(required = false)
            KnowledgeDocumentCategory category,

            @RequestParam(
                defaultValue = "5"
            )
            Integer topK,

            @RequestParam(
                defaultValue = "0.35"
            )
            Double similarityThreshold) {
        return retrievalService.search(
            query,
            category,
            topK,
            similarityThreshold
        );
    }
}