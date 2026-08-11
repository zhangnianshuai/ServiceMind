package com.zhangnianshuai.servicemind.api.auth;

import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.mock.web.MockHttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.zhangnianshuai.servicemind.domain.auth.AppRole;
import com.zhangnianshuai.servicemind.domain.auth.AppUser;
import com.zhangnianshuai.servicemind.domain.auth.AppUserRepository;

import com.zhangnianshuai.servicemind.security.SecurityConfig;
import com.zhangnianshuai.servicemind.security.ServiceMindUserDetailsService;

@WebMvcTest(
    controllers = AuthController.class
)
@Import({
    SecurityConfig.class,
    ServiceMindUserDetailsService.class
})
class AuthSessionFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AppUserRepository userRepository;

    private AppUser adminUser;

    @BeforeEach
    void setUp() {

        adminUser = new AppUser(
            "admin",
            passwordEncoder.encode(
                "test-admin-password"
            ),
            "系统管理员",
            AppRole.ADMIN
        );

        when(
            userRepository
                .findByUsernameIgnoreCase(
                    "admin"
                )
        )
        .thenReturn(
            Optional.of(adminUser)
        );
    }

    @Test
    void csrfEndpointIsPublic()
        throws Exception {

        mockMvc
            .perform(
                get("/api/auth/csrf")
            )
            .andExpect(
                status().isOk()
            )
            .andExpect(
                jsonPath("$.headerName").isNotEmpty()
            )
            .andExpect(
                jsonPath(
                    "$.token"
                ).isNotEmpty()
            );
    }

    @Test
    void anonymousCannotAccessMe()
        throws Exception {

        mockMvc
            .perform(
                get("/api/auth/me")
            )
            .andExpect(
                status().isUnauthorized()
            );
    }

    @Test
    void loginWithoutCsrfIsRejected()
        throws Exception {

        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content(
                        """
                        {
                          "username": "admin",
                          "password": "test-admin-password"
                        }
                        """
                    )
            )
            .andExpect(
                status().isForbidden()
            );
    }

    @Test
    void wrongPasswordIsRejected()
        throws Exception {

        mockMvc
            .perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(
                        MediaType.APPLICATION_JSON
                    )
                    .content(
                        """
                        {
                          "username": "admin",
                          "password": "wrong-password"
                        }
                        """
                    )
            )
            .andExpect(
                status().isUnauthorized()
            );
    }

    @Test
    void loginCreatesSessionAndMeRestoresUser()
        throws Exception {

        MvcResult loginResult =
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .with(csrf())
                        .contentType(
                            MediaType.APPLICATION_JSON
                        )
                        .content(
                            """
                            {
                              "username": "admin",
                              "password": "test-admin-password"
                            }
                            """
                        )
                )
                .andExpect(
                    status().isOk()
                )
                .andExpect(
                    jsonPath(
                        "$.username"
                    ).value(
                        "admin"
                    )
                )
                .andExpect(
                    jsonPath(
                        "$.displayName"
                    ).value(
                        "系统管理员"
                    )
                )
                .andExpect(
                    jsonPath(
                        "$.role"
                    ).value(
                        "ADMIN"
                    )
                )
                .andReturn();

        MockHttpSession session =
            (MockHttpSession)
                loginResult
                    .getRequest()
                    .getSession(false);

        if (session == null) {
            throw new AssertionError(
                "Login did not create an HTTP session."
            );
        }

        mockMvc
            .perform(
                get("/api/auth/me")
                    .session(session)
            )
            .andExpect(
                status().isOk()
            )
            .andExpect(
                jsonPath(
                    "$.username"
                ).value(
                    "admin"
                )
            )
            .andExpect(
                jsonPath(
                    "$.displayName"
                ).value(
                    "系统管理员"
                )
            )
            .andExpect(
                jsonPath(
                    "$.role"
                ).value(
                    "ADMIN"
                )
            );
    }

    @Test
    void logoutInvalidatesAuthenticatedSession()
        throws Exception {

        MvcResult loginResult =
            mockMvc
                .perform(
                    post("/api/auth/login")
                        .with(csrf())
                        .contentType(
                            MediaType.APPLICATION_JSON
                        )
                        .content(
                            """
                            {
                              "username": "admin",
                              "password": "test-admin-password"
                            }
                            """
                        )
                )
                .andExpect(
                    status().isOk()
                )
                .andReturn();

        MockHttpSession session =
            (MockHttpSession)
                loginResult
                    .getRequest()
                    .getSession(false);

        if (session == null) {
            throw new AssertionError(
                "Login did not create an HTTP session."
            );
        }

        mockMvc
            .perform(
                post("/api/auth/logout")
                    .session(session)
                    .with(csrf())
            )
            .andExpect(
                status().isNoContent()
            );

        mockMvc
            .perform(
                get("/api/auth/me")
            )
            .andExpect(
                status().isUnauthorized()
            );
    }
}