package com.zhangnianshuai.servicemind.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zhangnianshuai.servicemind.agents.ServiceMindAgent;
import com.zhangnianshuai.servicemind.application.model.ByokChatService;

@Service
public class ChatService {

    private static final Logger log =
        LoggerFactory.getLogger(ChatService.class);

    private final ServiceMindAgent serviceMindAgent;
    private final ByokChatService byokChatService;

    public ChatService(
            ServiceMindAgent serviceMindAgent,
            ByokChatService byokChatService) {
        this.serviceMindAgent = serviceMindAgent;
        this.byokChatService = byokChatService;

        log.info(
            "ChatService initialized with platform and BYOK modes"
        );
    }

    public String chat(
            String chatId,
            String userMessage,
            UUID modelSessionId) {
        if (modelSessionId == null) {
            log.info(
                "Processing chat {} using platform credentials",
                chatId
            );

            return serviceMindAgent.handle(
                chatId,
                userMessage
            );
        }

        log.info(
            "Processing chat {} using user model session",
            chatId
        );

        return byokChatService.chat(
            modelSessionId,
            chatId,
            userMessage
        );
    }

    public String chat(
            String chatId,
            String userMessage) {
        return chat(chatId, userMessage, null);
    }
}