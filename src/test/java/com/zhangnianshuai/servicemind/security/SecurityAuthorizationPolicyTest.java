package com.zhangnianshuai.servicemind.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = SecurityProbeController.class
)
@Import(SecurityConfig.class)
class SecurityAuthorizationPolicyTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceMindUserDetailsService userDetailsService;

    @Test
    void anonymousCannotReadOrders()
        throws Exception {

        mockMvc
            .perform(
                get("/api/orders")
            )
            .andExpect(
                status().isUnauthorized()
            );
    }

    @Test
    @WithMockUser(
        username = "agent",
        roles = "AGENT"
    )
    void agentCanReadOrders()
        throws Exception {

        mockMvc
            .perform(
                get("/api/orders")
            )
            .andExpect(
                status().isOk()
            );
    }

    @Test
    @WithMockUser(
        username = "agent",
        roles = "AGENT"
    )
    void agentCannotCreateOrders()
        throws Exception {

        mockMvc
            .perform(
                post("/api/orders")
                    .with(csrf())
            )
            .andExpect(
                status().isForbidden()
            );
    }

    @Test
    @WithMockUser(
        username = "admin",
        roles = "ADMIN"
    )
    void adminCanCreateOrders()
        throws Exception {

        mockMvc
            .perform(
                post("/api/orders")
                    .with(csrf())
            )
            .andExpect(
                status().isOk()
            );
    }

    @Test
    @WithMockUser(
        username = "admin",
        roles = "ADMIN"
    )
    void adminPostWithoutCsrfIsRejected()
        throws Exception {

        mockMvc
            .perform(
                post("/api/orders")
            )
            .andExpect(
                status().isForbidden()
            );
    }

    @Test
    @WithMockUser(
        username = "agent",
        roles = "AGENT"
    )
    void agentCanReadKnowledgeDocuments()
        throws Exception {

        mockMvc
            .perform(
                get("/api/knowledge/documents")
            )
            .andExpect(
                status().isOk()
            );
    }

    @Test
    @WithMockUser(
        username = "agent",
        roles = "AGENT"
    )
    void agentCannotCreateKnowledgeDocument()
        throws Exception {

        mockMvc
            .perform(
                post("/api/knowledge/documents")
                    .with(csrf())
            )
            .andExpect(
                status().isForbidden()
            );
    }

    @Test
    @WithMockUser(
        username = "admin",
        roles = "ADMIN"
    )
    void adminCanCreateKnowledgeDocument()
        throws Exception {

        mockMvc
            .perform(
                post("/api/knowledge/documents")
                    .with(csrf())
            )
            .andExpect(
                status().isOk()
            );
    }
}