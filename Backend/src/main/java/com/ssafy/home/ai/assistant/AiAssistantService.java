package com.ssafy.home.ai.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.home.ai.agent.AgentCommand;
import com.ssafy.home.ai.limit.AiChatRateLimiter;
import com.ssafy.home.ai.support.AiProviderErrors;
import com.ssafy.home.ai.support.AiRequests;
import com.ssafy.home.ai.tool.HouseTools;
import com.ssafy.home.ai.tool.PageActionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI 입력 검증, 회원별 요청 제한, 대화 memory와 tool calling을 한 유스케이스로 조율한다.
 * 모델 결과를 일반 답변 또는 Frontend가 실행할 구조화 {@link AgentCommand}로 정규화한다.
 */
@Service
public class AiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);
    private static final String TIMEOUT_MESSAGE = "AI 응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";
    private static final String UNAVAILABLE_MESSAGE = "AI 서비스에 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    private static final String AUTH_FAILURE_MESSAGE = "AI 서비스 인증에 문제가 있어 답변할 수 없습니다. 잠시 후 다시 시도해주세요.";
    private static final String DISABLED_MESSAGE = "AI 챗봇이 현재 비활성화되어 있습니다. 관리자에게 문의해주세요.";
    private static final String RATE_LIMIT_MESSAGE = "AI 질문 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
    private static final String CONCURRENT_REQUEST_MESSAGE = "이미 답변을 생성하고 있습니다. 현재 답변이 끝난 뒤 다시 질문해주세요.";
    private static final String DEFAULT_CLARIFY = "무엇을 도와드릴까요? 예: '강남구 2024년 5월 검색해줘'.";
    private static final String FINISH_RETURN_DIRECT = "returnDirect";
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            너는 'no-home' 서울 아파트 실거래가 서비스의 AI 어시스턴트다. 한 대화에서 '질문 답변'과 '페이지 조작'을 모두 처리한다.
            - 특정 지역의 실거래가/시세를 '질문'하면 searchSeoulAptDeals tool로 조회한 뒤 한국어로 간결히 요약 답변하라.
              매매/전세/월세는 dealMode('sale'|'jeonse'|'monthly')로 구분한다.
            - 사용자가 조건을 말하며 매물을 '검색/조회'하려 하면 applyFiltersAndSearch를 호출하라. 검색을 명시적으로 미룰 때만 setFilters를 쓴다.
            - 페이지 이동은 paginate, 매물 상세는 selectItem, 지도 표시는 mapFocus, 검색 초기화는 reset tool을 호출하라.
            - 일반 대화·인사·모호·불만·평가성 발화는 어떤 tool도 호출하지 말고 한국어 텍스트로 답하라.
            - 액션 tool의 filters에 쓸 수 있는 키는 정확히 다음뿐이다: %s. 목록에 없는 키는 만들지 마라.
            - 값은 모두 문자열, 거래월은 'YYYY-MM', 자치구는 '강남구'처럼 '구'를 포함한다.
            - 서울특별시 25개 자치구만 지원한다. 금액은 '만원/억원' 단위로 표기한다.
            - 현재 화면 상태: filters=%s, page=%s, totalPages=%s. 존재하지 않는 페이지는 요청하지 마라.
            """;

    private final ChatClient chatClient;
    private final HouseTools houseTools;
    private final PageActionTools pageActionTools;
    private final AiChatRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;
    private final int maxMessageLength;

    public AiAssistantService(
            @Nullable ChatClient chatClient,
            HouseTools houseTools,
            PageActionTools pageActionTools,
            AiChatRateLimiter rateLimiter,
            ObjectMapper objectMapper,
            @Value("${ai.chat.max-message-length:500}") int maxMessageLength
    ) {
        if (maxMessageLength <= 0) {
            throw new IllegalArgumentException("AI chat max message length must be positive.");
        }
        this.chatClient = chatClient;
        this.houseTools = houseTools;
        this.pageActionTools = pageActionTools;
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
        this.maxMessageLength = maxMessageLength;
    }

    /**
     * 사용자 메시지와 현재 화면 상태를 모델에 전달한다. rate limiter 획득 후에는 성공·실패와 무관하게
     * {@code finally}에서 점유를 해제해 한 회원의 후속 요청이 영구 차단되지 않게 한다.
     */
    public AiAssistantResult assist(AssistantRequest request, Long memberId) {
        String message = request == null || request.message() == null ? null : request.message().trim();
        if (message == null || message.isBlank()) {
            return AiAssistantResult.failure(AiAssistantResult.Status.BAD_REQUEST, "message is required.");
        }
        if (message.codePointCount(0, message.length()) > maxMessageLength) {
            return AiAssistantResult.failure(AiAssistantResult.Status.BAD_REQUEST,
                    "message must be " + maxMessageLength + " characters or fewer.");
        }
        if (chatClient == null) {
            return AiAssistantResult.failure(AiAssistantResult.Status.SERVICE_UNAVAILABLE, DISABLED_MESSAGE);
        }
        if (memberId == null) {
            return AiAssistantResult.failure(AiAssistantResult.Status.UNAUTHORIZED, "login is required.");
        }

        AiChatRateLimiter.Decision decision = rateLimiter.acquire(memberId);
        if (!decision.allowed()) {
            if (decision.reason() == AiChatRateLimiter.RejectionReason.CONCURRENT_REQUEST) {
                return AiAssistantResult.failure(AiAssistantResult.Status.CONFLICT, CONCURRENT_REQUEST_MESSAGE);
            }
            return AiAssistantResult.rateLimited(RATE_LIMIT_MESSAGE, decision.retryAfterSeconds());
        }

        try {
            String conversationId = AiRequests.resolveConversationId(request.conversationId(), memberId);
            ChatResponse response = chatClient.prompt()
                    .system(buildSystemPrompt(request))
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .tools(houseTools, pageActionTools)
                    .options(ChatOptions.builder().temperature(0.0).build())
                    .call()
                    .chatResponse();
            return AiAssistantResult.success(toAssistantResponse(response));
        } catch (RuntimeException exception) {
            if (AiProviderErrors.isTimeout(exception)) {
                return AiAssistantResult.failure(AiAssistantResult.Status.GATEWAY_TIMEOUT, TIMEOUT_MESSAGE);
            }
            if (AiProviderErrors.isAuthFailure(exception)) {
                log.warn("AI provider authentication failed — SSAFY_GMS_API_KEY may be invalid or expired.");
                return AiAssistantResult.failure(AiAssistantResult.Status.SERVICE_UNAVAILABLE, AUTH_FAILURE_MESSAGE);
            }
            log.debug("Assistant call failed.", exception);
            return AiAssistantResult.failure(AiAssistantResult.Status.SERVICE_UNAVAILABLE, UNAVAILABLE_MESSAGE);
        } finally {
            rateLimiter.release(memberId);
        }
    }

    private AssistantResponse toAssistantResponse(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResult() == null
                || chatResponse.getResult().getOutput() == null) {
            return AssistantResponse.answer(UNAVAILABLE_MESSAGE);
        }
        Generation generation = chatResponse.getResult();
        String content = generation.getOutput().getText();
        String finishReason = generation.getMetadata() == null ? null : generation.getMetadata().getFinishReason();
        if (FINISH_RETURN_DIRECT.equalsIgnoreCase(finishReason)) {
            try {
                AgentCommand command = objectMapper.readValue(content, AgentCommand.class);
                if (command != null && command.action() != null && !command.action().isBlank()) {
                    return AssistantResponse.command(command);
                }
            } catch (RuntimeException | JsonProcessingException exception) {
                log.debug("Failed to parse returnDirect content as AgentCommand; degrading to clarify.", exception);
            }
            return AssistantResponse.command(AgentCommand.clarify(DEFAULT_CLARIFY));
        }
        return content == null || content.isBlank()
                ? AssistantResponse.answer(UNAVAILABLE_MESSAGE)
                : AssistantResponse.answer(content);
    }

    private static String buildSystemPrompt(AssistantRequest request) {
        List<String> capabilities = request.capabilities() == null ? List.of() : request.capabilities();
        Map<String, String> filters = request.currentFilters() == null ? Map.of() : request.currentFilters();
        return SYSTEM_PROMPT_TEMPLATE.formatted(
                String.join(", ", capabilities), filters,
                request.currentPage() == null ? "-" : request.currentPage(),
                request.totalPages() == null ? "-" : request.totalPages());
    }
}
