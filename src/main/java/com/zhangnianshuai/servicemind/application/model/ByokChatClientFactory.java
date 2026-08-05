package com.zhangnianshuai.servicemind.application.model;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

@Component
public class ByokChatClientFactory {

    private static final String BASE_URL =
        "https://dashscope.aliyuncs.com/compatible-mode";

    private static final String COMPLETIONS_PATH =
        "/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
        You are ServiceMind, an enterprise customer service assistant.

        Answer clearly, accurately, and concisely.
        Do not claim that an order, refund, after-sales request, or ticket
        has been modified unless a verified business tool has completed
        that operation.

        The BYOK mode currently provides general AI conversation only.
        When real business data or an operation is required, explain that
        the corresponding ServiceMind business tool is not connected to
        BYOK mode yet.
        """;

    private final ChatMemory chatMemory;

    public ByokChatClientFactory(ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
    }

    public ChatClient create(ModelSessionCredential credential) {
        String apiKey = credential.copyApiKey();

        try {
            OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(BASE_URL)
                .apiKey(apiKey)
                .completionsPath(COMPLETIONS_PATH)
                .build();

            OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(credential.getModel())
                .temperature(0.7)
                .build();

            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();

            return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                    MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build()
                )
                .build();
        }
        finally {
            apiKey = null;
        }
    }
}