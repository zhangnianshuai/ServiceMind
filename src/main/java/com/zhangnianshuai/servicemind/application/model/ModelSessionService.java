package com.zhangnianshuai.servicemind.application.model;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.zhangnianshuai.servicemind.api.model.dto.CreateModelSessionRequest;
import com.zhangnianshuai.servicemind.api.model.dto.ModelSessionResponse;

@Service
public class ModelSessionService {

    private final DashScopeCredentialValidator credentialValidator;
    private final ModelSessionStore sessionStore;

    public ModelSessionService(
            DashScopeCredentialValidator credentialValidator,
            ModelSessionStore sessionStore) {
        this.credentialValidator = credentialValidator;
        this.sessionStore = sessionStore;
    }

    public ModelSessionResponse createSession(
            CreateModelSessionRequest request) {
        credentialValidator.validate(
            request.provider(),
            request.apiKey(),
            request.model()
        );

        ModelSessionStore.Registration registration =
            sessionStore.create(
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

    public void revokeSession(UUID sessionId) {
        sessionStore.revoke(sessionId);
    }
}