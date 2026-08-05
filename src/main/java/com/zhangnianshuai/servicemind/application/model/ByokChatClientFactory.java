package com.zhangnianshuai.servicemind.application.model;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import com.zhangnianshuai.servicemind.application.ai.ServiceMindPrompt;
import com.zhangnianshuai.servicemind.application.ai.tool.AfterSalesQueryTools;
import com.zhangnianshuai.servicemind.application.ai.tool.KnowledgeQueryTools;
import com.zhangnianshuai.servicemind.application.ai.tool.OrderQueryTools;
import com.zhangnianshuai.servicemind.application.ai.tool.SupportTicketQueryTools;

@Component
public class ByokChatClientFactory {

    private static final String BASE_URL =
        "https://dashscope.aliyuncs.com/compatible-mode";

    private static final String COMPLETIONS_PATH =
        "/v1/chat/completions";

    private final ChatMemory chatMemory;
    private final OrderQueryTools orderQueryTools;
    private final AfterSalesQueryTools afterSalesQueryTools;
    private final SupportTicketQueryTools supportTicketQueryTools;
    private final KnowledgeQueryTools knowledgeQueryTools;

    public ByokChatClientFactory(
            ChatMemory chatMemory,
            OrderQueryTools orderQueryTools,
            AfterSalesQueryTools afterSalesQueryTools,
            SupportTicketQueryTools supportTicketQueryTools,
            KnowledgeQueryTools knowledgeQueryTools) {
        this.chatMemory = chatMemory;
        this.orderQueryTools = orderQueryTools;
        this.afterSalesQueryTools = afterSalesQueryTools;
        this.supportTicketQueryTools =
            supportTicketQueryTools;
        this.knowledgeQueryTools =
            knowledgeQueryTools;
    }

    public ChatClient create(
            ModelSessionCredential credential) {
        String apiKey = credential.copyApiKey();

        try {
            OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(BASE_URL)
                .apiKey(apiKey)
                .completionsPath(COMPLETIONS_PATH)
                .build();

            OpenAiChatOptions options =
                OpenAiChatOptions.builder()
                    .model(credential.getModel())
                    .temperature(0.0)
                    .build();

            OpenAiChatModel chatModel =
                OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(options)
                    .build();

            return ChatClient.builder(chatModel)
                .defaultSystem(
                    ServiceMindPrompt.SYSTEM_PROMPT
                )
                .defaultAdvisors(
                    MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build()
                )
                .defaultTools(
                    orderQueryTools,
                    afterSalesQueryTools,
                    supportTicketQueryTools,
                    knowledgeQueryTools
                )
                .build();
        }
        finally {
            apiKey = null;
        }
    }
}