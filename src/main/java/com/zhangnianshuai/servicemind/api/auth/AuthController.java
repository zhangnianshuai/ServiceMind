package com.zhangnianshuai.servicemind.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.auth.dto.AuthUserResponse;
import com.zhangnianshuai.servicemind.api.auth.dto.CsrfResponse;
import com.zhangnianshuai.servicemind.api.auth.dto.LoginRequest;
import com.zhangnianshuai.servicemind.domain.auth.AppUser;
import com.zhangnianshuai.servicemind.domain.auth.AppUserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager
        authenticationManager;

    private final AppUserRepository
        userRepository;

    private final SecurityContextRepository
        securityContextRepository;

    private final CsrfTokenRepository
        csrfTokenRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            AppUserRepository userRepository,
            SecurityContextRepository
                securityContextRepository,
            CsrfTokenRepository csrfTokenRepository) {

        this.authenticationManager =
            authenticationManager;

        this.userRepository =
            userRepository;

        this.securityContextRepository =
            securityContextRepository;

        this.csrfTokenRepository =
            csrfTokenRepository;
    }

    @GetMapping("/csrf")
    public CsrfResponse csrf(
            CsrfToken csrfToken) {

        return new CsrfResponse(
            csrfToken.getHeaderName(),
            csrfToken.getToken()
        );
    }

    @PostMapping("/login")
    public AuthUserResponse login(
            @Valid
            @RequestBody
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Authentication authentication;

        try {
            authentication =
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken
                        .unauthenticated(
                            request.username().trim(),
                            request.password()
                        )
                );
        }
        catch (AuthenticationException exception) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password."
            );
        }

        HttpSession existingSession =
            httpRequest.getSession(false);

        if (existingSession != null) {
            httpRequest.changeSessionId();
        }

        SecurityContext context =
            SecurityContextHolder
                .createEmptyContext();

        context.setAuthentication(
            authentication
        );

        SecurityContextHolder.setContext(
            context
        );

        securityContextRepository.saveContext(
            context,
            httpRequest,
            httpResponse
        );

        csrfTokenRepository.saveToken(
            null,
            httpRequest,
            httpResponse
        );

        return currentUserResponse(
            authentication.getName()
        );
    }

    @GetMapping("/me")
    public AuthUserResponse me(
            Authentication authentication) {

        return currentUserResponse(
            authentication.getName()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {

        SecurityContextLogoutHandler handler =
            new SecurityContextLogoutHandler();

        handler.logout(
            request,
            response,
            authentication
        );

        csrfTokenRepository.saveToken(
            null,
            request,
            response
        );

        return ResponseEntity
            .noContent()
            .build();
    }

    private AuthUserResponse currentUserResponse(
            String username) {

        AppUser user = userRepository
            .findByUsernameIgnoreCase(username)
            .orElseThrow(
                () -> new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user no longer exists."
                )
            );

        return new AuthUserResponse(
            user.getUsername(),
            user.getDisplayName(),
            user.getRole().name()
        );
    }
}