package com.zhangnianshuai.servicemind.api.model.dto;

import com.zhangnianshuai.servicemind.domain.model.ModelProvider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateModelSessionRequest(

    @NotNull
    ModelProvider provider,

    @NotBlank
    @Size(max = 512)
    String apiKey,

    @NotBlank
    @Size(max = 64)
    @Pattern(
        regexp = "qwen-plus",
        message = "The current version only supports qwen-plus."
    )
    String model
) {

    public CreateModelSessionRequest {
        apiKey = apiKey == null ? null : apiKey.trim();
        model = model == null ? null : model.trim();
    }

    @Override
    public String toString() {
        return "CreateModelSessionRequest[provider="
            + provider
            + ", apiKey=***, model="
            + model
            + "]";
    }
}