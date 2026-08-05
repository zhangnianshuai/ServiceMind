package com.zhangnianshuai.servicemind.application.model;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.domain.model.ModelProvider;

@Component
public class DashScopeCredentialValidator {

    private static final String BASE_URL =
        "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private final RestClient restClient;

    public DashScopeCredentialValidator(RestClient.Builder builder) {
        this.restClient = builder
            .baseUrl(BASE_URL)
            .build();
    }

    public void validate(
            ModelProvider provider,
            String apiKey,
            String model) {
        if (provider != ModelProvider.DASHSCOPE) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported model provider."
            );
        }

        if (!"qwen-plus".equals(model)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The current version only supports qwen-plus."
            );
        }

        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of(
                    "role", "user",
                    "content", "Reply with OK."
                )
            ),
            "max_tokens", 1,
            "temperature", 0
        );

        try {
            restClient.post()
                .uri("/chat/completions")
                .headers(headers -> headers.setBearerAuth(apiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
        }
        catch (RestClientResponseException exception) {
            int statusCode = exception.getStatusCode().value();

            if (statusCode == 401 || statusCode == 403) {
                throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "The API Key is invalid or has no model permission."
                );
            }

            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "The model provider rejected the validation request."
            );
        }
        catch (RestClientException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "The model provider is temporarily unavailable."
            );
        }
    }
}