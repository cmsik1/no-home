package com.ssafy.home.ai.assistant;

import java.util.List;
import java.util.Map;

public record AssistantRequest(
        String message,
        List<String> capabilities,
        Map<String, String> currentFilters,
        Integer currentPage,
        Integer totalPages,
        String conversationId
) {
}
