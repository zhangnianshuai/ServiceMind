package com.zhangnianshuai.servicemind.application.ai.tool;

public record BusinessToolResult(
    boolean success,
    String operation,
    Object data,
    String message
) {

    public static BusinessToolResult success(
            String operation,
            Object data) {
        return new BusinessToolResult(
            true,
            operation,
            data,
            null
        );
    }

    public static BusinessToolResult failure(
            String operation,
            String message) {
        String safeMessage =
            message == null || message.isBlank()
                ? "The business query failed."
                : message;

        return new BusinessToolResult(
            false,
            operation,
            null,
            safeMessage
        );
    }
}