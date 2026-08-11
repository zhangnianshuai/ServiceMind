package com.zhangnianshuai.servicemind.api.chat;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhangnianshuai.servicemind.api.chat.dto.ChatRequest;
import com.zhangnianshuai.servicemind.api.chat.dto.ChatResponse;
import com.zhangnianshuai.servicemind.service.ChatService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/customer-support")
public class CustomerSupportController {

    private final ChatService chatService;

    public CustomerSupportController(
            ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(
            @Valid
            @RequestBody
            ChatRequest request,
            Authentication authentication) {

        String chatId =
            resolveChatId(
                request.chatId()
            );

        String response =
            chatService.chat(
                authentication.getName(),
                chatId,
                request.text().trim(),
                request.modelSessionId()
            );

        String credentialSource =
            request.modelSessionId() == null
                ? "PLATFORM"
                : "USER_SESSION";

        return new ChatResponse(
            response,
            chatId,
            credentialSource
        );
    }

    private String resolveChatId(
            String requestedChatId) {

        if (StringUtils.hasText(
                requestedChatId)) {

            return requestedChatId.trim();
        }

        return UUID.randomUUID()
            .toString();
    }
}