package com.zhangnianshuai.servicemind.api.model;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.zhangnianshuai.servicemind.api.model.dto.CreateModelSessionRequest;
import com.zhangnianshuai.servicemind.api.model.dto.ModelSessionResponse;
import com.zhangnianshuai.servicemind.application.model.ModelSessionService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/model-sessions")
public class ModelSessionController {

    private final ModelSessionService modelSessionService;

    public ModelSessionController(
            ModelSessionService modelSessionService) {
        this.modelSessionService =
            modelSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ModelSessionResponse createSession(
            @Valid
            @RequestBody
            CreateModelSessionRequest request,
            Authentication authentication) {

        return modelSessionService.createSession(
            authentication.getName(),
            request
        );
    }

    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeSession(
            @PathVariable UUID sessionId,
            Authentication authentication) {

        modelSessionService.revokeSession(
            authentication.getName(),
            sessionId
        );
    }
}