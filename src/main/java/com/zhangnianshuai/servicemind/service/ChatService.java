package com.zhangnianshuai.servicemind.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.zhangnianshuai.servicemind.agents.ServiceMindAgent;
import com.zhangnianshuai.servicemind.application.model.ByokChatService;
import com.zhangnianshuai.servicemind.security.PublicDemoUsageGuard;

@Service
public class ChatService {

    private static final Logger log =
        LoggerFactory.getLogger(
            ChatService.class
        );

    private final ServiceMindAgent serviceMindAgent;
    private final ByokChatService byokChatService;
    private final PublicDemoUsageGuard usageGuard;

    public ChatService(
            ServiceMindAgent serviceMindAgent,
            ByokChatService byokChatService,
            PublicDemoUsageGuard usageGuard) {

        this.serviceMindAgent =
            serviceMindAgent;

        this.byokChatService =
            byokChatService;

        this.usageGuard =
            usageGuard;

        log.info(
            "ChatService initialized with platform and BYOK modes"
        );
    }

    public String chat(
            String username,
            String chatId,
            String userMessage,
            UUID modelSessionId) {

        if (modelSessionId == null) {

            usageGuard.checkPlatformChat(
                username
            );

            log.info(
                "Processing chat {} using platform credentials",
                chatId
            );

            return serviceMindAgent.handle(
                chatId,
                userMessage
            );
        }

        usageGuard.checkByokChat(
            username
        );

        log.info(
            "Processing chat {} using user model session",
            chatId
        );

        return byokChatService.chat(
            username,
            modelSessionId,
            chatId,
            userMessage
        );
    }
}