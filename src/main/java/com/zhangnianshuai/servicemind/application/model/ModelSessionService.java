package com.zhangnianshuai.servicemind.application.model;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zhangnianshuai.servicemind.api.model.dto.CreateModelSessionRequest;
import com.zhangnianshuai.servicemind.api.model.dto.ModelSessionResponse;
import com.zhangnianshuai.servicemind.security.PublicDemoUsageGuard;

@Service
public class ModelSessionService {

    private final DashScopeCredentialValidator credentialValidator;
    private final ModelSessionStore sessionStore;
    private final PublicDemoUsageGuard usageGuard;

    public ModelSessionService(
            DashScopeCredentialValidator credentialValidator,
            ModelSessionStore sessionStore,
            PublicDemoUsageGuard usageGuard) {

        this.credentialValidator =
            credentialValidator;

        this.sessionStore =
            sessionStore;

        this.usageGuard =
            usageGuard;
    }

    public ModelSessionResponse createSession(
            String username,
            CreateModelSessionRequest request) {

        usageGuard.checkModelSessionCreate(
            username
        );

        credentialValidator.validate(
            request.provider(),
            request.apiKey(),
            request.model()
        );

        ModelSessionStore.Registration registration =
            sessionStore.create(
                username,
                request.provider(),
                request.apiKey(),
                request.model()
            );

        return new ModelSessionResponse(
            registration.sessionId(),
            request.provider(),
            request.model(),
            registration.expiresAt()
        );
    }

    public void revokeSession(
            String username,
            UUID sessionId) {

        sessionStore.revoke(
            sessionId,
            username
        );
    }
}