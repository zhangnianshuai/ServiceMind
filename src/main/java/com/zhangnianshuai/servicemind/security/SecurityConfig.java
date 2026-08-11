package com.zhangnianshuai.servicemind.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            ServiceMindUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(
                userDetailsService
            );

        provider.setPasswordEncoder(
            passwordEncoder
        );

        return new ProviderManager(
            List.of(provider)
        );
    }

    @Bean
    public SecurityContextRepository
            securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository =
            CookieCsrfTokenRepository
                .withHttpOnlyFalse();

        repository.setCookiePath("/");

        return repository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CsrfTokenRepository csrfTokenRepository,
            SecurityContextRepository securityContextRepository)
            throws Exception {

        CsrfTokenRequestAttributeHandler
            csrfRequestHandler =
                new CsrfTokenRequestAttributeHandler();

        http
            .securityContext(security -> security
                .securityContextRepository(
                    securityContextRepository
                )
                .requireExplicitSave(true)
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(
                    csrfTokenRepository
                )
                .csrfTokenRequestHandler(
                    csrfRequestHandler
                )
            )
            .formLogin(
                AbstractHttpConfigurer::disable
            )
            .httpBasic(
                AbstractHttpConfigurer::disable
            )
            .logout(
                AbstractHttpConfigurer::disable
            )
            .requestCache(
                AbstractHttpConfigurer::disable
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.IF_REQUIRED
                )
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(
                    (request, response, exception) ->
                        response.sendError(401)
                )
                .accessDeniedHandler(
                    (request, response, exception) ->
                        response.sendError(403)
                )
            )
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/auth/csrf"
                )
                .permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/login"
                )
                .permitAll()

                .requestMatchers(
                    "/error"
                )
                .permitAll()

                .requestMatchers(
                    HttpMethod.GET,
                    "/actuator/health"
                )
                .permitAll()

                .requestMatchers(
                    "/actuator/**"
                )
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/knowledge/search"
                )
                .hasAnyRole(
                    "AGENT",
                    "ADMIN"
                )

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/knowledge/documents",
                    "/api/knowledge/documents/**"
                )
                .hasAnyRole(
                    "AGENT",
                    "ADMIN"
                )

                .requestMatchers(
                    "/api/knowledge/documents",
                    "/api/knowledge/documents/**"
                )
                .hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/orders",
                    "/api/orders/**"
                )
                .hasAnyRole(
                    "AGENT",
                    "ADMIN"
                )

                .requestMatchers(
                    "/api/orders",
                    "/api/orders/**"
                )
                .hasRole("ADMIN")

                .requestMatchers(
                    "/api/after-sales",
                    "/api/after-sales/**"
                )
                .hasAnyRole(
                    "AGENT",
                    "ADMIN"
                )

                .requestMatchers(
                    "/api/tickets",
                    "/api/tickets/**"
                )
                .hasAnyRole(
                    "AGENT",
                    "ADMIN"
                )

                .requestMatchers(
                    "/api/customer-support",
                    "/api/customer-support/**"
                )
                .hasAnyRole(
                    "AGENT",
                    "ADMIN"
                )

                .requestMatchers(
                    "/api/model-sessions",
                    "/api/model-sessions/**"
                )
                .hasAnyRole(
                    "AGENT",
                    "ADMIN"
                )

                .anyRequest()
                .authenticated()
            );

        return http.build();
    }
}