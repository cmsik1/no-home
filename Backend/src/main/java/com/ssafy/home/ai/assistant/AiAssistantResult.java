package com.ssafy.home.ai.assistant;

public record AiAssistantResult(Status status, AssistantResponse response, String message, Long retryAfterSeconds) {

    public enum Status {
        OK,
        BAD_REQUEST,
        UNAUTHORIZED,
        CONFLICT,
        TOO_MANY_REQUESTS,
        SERVICE_UNAVAILABLE,
        GATEWAY_TIMEOUT
    }

    public static AiAssistantResult success(AssistantResponse response) {
        return new AiAssistantResult(Status.OK, response, "ok", null);
    }

    public static AiAssistantResult failure(Status status, String message) {
        return new AiAssistantResult(status, null, message, null);
    }

    public static AiAssistantResult rateLimited(String message, long retryAfterSeconds) {
        return new AiAssistantResult(Status.TOO_MANY_REQUESTS, null, message, retryAfterSeconds);
    }
}
