package com.zhangnianshuai.servicemind.application.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.application.knowledge.KnowledgeRetrievalService;

@Component
public class KnowledgeQueryTools {

    private static final int DEFAULT_TOP_K = 4;

    private static final double
        DEFAULT_SIMILARITY_THRESHOLD = 0.35d;

    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeQueryTools(
            KnowledgeRetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Tool(
        name = "search_active_knowledge",
        description = """
            Search the currently active and published enterprise
            knowledge base.

            Use this tool for company policies, product guidance,
            warranty rules, return or exchange rules, repair
            procedures, payment guidance, account instructions
            and general customer-service processes.

            Do not use this tool to retrieve the current status
            of a specific order, after-sales application or
            support ticket. Use the corresponding business tool
            for those records.

            The result contains matched content and its source,
            including document title, document number, version
            number and chunk number.
            """
    )
    public BusinessToolResult searchActiveKnowledge(
            @ToolParam(
                description = """
                    A complete natural-language search query
                    describing the policy, rule, process or
                    guidance needed by the customer.
                    """
            )
            String query) {
        try {
            return BusinessToolResult.success(
                "search_active_knowledge",
                retrievalService.search(
                    query,
                    null,
                    DEFAULT_TOP_K,
                    DEFAULT_SIMILARITY_THRESHOLD
                )
            );
        }
        catch (ResponseStatusException exception) {
            return BusinessToolResult.failure(
                "search_active_knowledge",
                exception.getReason()
            );
        }
        catch (RuntimeException exception) {
            return BusinessToolResult.failure(
                "search_active_knowledge",
                safeMessage(exception)
            );
        }
    }

    private String safeMessage(
            RuntimeException exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return "Knowledge retrieval failed.";
        }

        message = message.replaceAll(
            "[\\r\\n\\t]+",
            " "
        );

        if (message.length() > 500) {
            return message.substring(0, 500);
        }

        return message;
    }
}