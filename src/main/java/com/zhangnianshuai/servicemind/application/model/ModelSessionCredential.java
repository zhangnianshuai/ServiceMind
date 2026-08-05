package com.zhangnianshuai.servicemind.application.model;

import java.util.Arrays;

import com.zhangnianshuai.servicemind.domain.model.ModelProvider;

public final class ModelSessionCredential {

    private final ModelProvider provider;
    private final char[] apiKey;
    private final String model;

    ModelSessionCredential(
            ModelProvider provider,
            String apiKey,
            String model) {
        this.provider = provider;
        this.apiKey = apiKey.toCharArray();
        this.model = model;
    }

    public ModelProvider getProvider() {
        return provider;
    }

    public String copyApiKey() {
        return new String(apiKey);
    }

    public String getModel() {
        return model;
    }

    void destroy() {
        Arrays.fill(apiKey, '\0');
    }

    @Override
    public String toString() {
        return "ModelSessionCredential[provider="
            + provider
            + ", apiKey=***, model="
            + model
            + "]";
    }
}