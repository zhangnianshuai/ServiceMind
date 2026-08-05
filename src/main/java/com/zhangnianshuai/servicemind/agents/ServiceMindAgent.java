package com.zhangnianshuai.servicemind.agents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Component;

import com.zhangnianshuai.servicemind.application.ai.ServiceMindPrompt;
import com.zhangnianshuai.servicemind.application.ai.tool.AfterSalesQueryTools;
import com.zhangnianshuai.servicemind.application.ai.tool.KnowledgeQueryTools;
import com.zhangnianshuai.servicemind.application.ai.tool.OrderQueryTools;
import com.zhangnianshuai.servicemind.application.ai.tool.SupportTicketQueryTools;

@Component
public class ServiceMindAgent {

    private static final Logger log =
        LoggerFactory.getLogger(ServiceMindAgent.class);

    private final ChatClient chatClient;

    public ServiceMindAgent(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            OrderQueryTools orderQueryTools,
            AfterSalesQueryTools afterSalesQueryTools,
            SupportTicketQueryTools supportTicketQueryTools,
            KnowledgeQueryTools knowledgeQueryTools) {
        this.chatClient = chatClientBuilder
            .defaultSystem(ServiceMindPrompt.SYSTEM_PROMPT)
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

        log.info(
            "ServiceMindAgent initialized with read-only "
                + "business query and knowledge tools"
        );
    }

    public String handle(
            String chatId,
            String userMessage) {
        log.info(
            "ServiceMindAgent processing chat {}",
            chatId
        );

        String content = chatClient.prompt()
            .user(userMessage)
            .advisors(advisor -> advisor.param(
                ChatMemory.CONVERSATION_ID,
                chatId + "-servicemind"
            ))
            .call()
            .content();

        return content == null ? "" : content;
    }
}