package com.zhangnianshuai.servicemind.application.model;

import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ByokChatService {

    private final ModelSessionStore sessionStore;
    private final ByokChatClientFactory chatClientFactory;

    public ByokChatService(
            ModelSessionStore sessionStore,
            ByokChatClientFactory chatClientFactory) {
        this.sessionStore = sessionStore;
        this.chatClientFactory = chatClientFactory;
    }

    public String chat(
            UUID modelSessionId,
            String chatId,
            String userMessage) {
        ModelSessionCredential credential =
            sessionStore.getRequired(modelSessionId);

        ChatClient chatClient =
            chatClientFactory.create(credential);

        String conversationId = chatId
            + "-byok-"
            + modelSessionId;

        String response = chatClient.prompt()
            .user(userMessage)
            .advisors(advisor -> advisor.param(
                ChatMemory.CONVERSATION_ID,
                conversationId
            ))
            .call()
            .content();

        return response == null ? "" : response;
    }
}