package com.ssafy.home.ai.agent;

import java.util.Map;

/**
 * 통합 어시스턴트에서 LLM이 반환하는 구조화된 화면 명령.
 * <p>
 * {@code /api/ai/assistant}는 일반 질문에는 텍스트를, 페이지 조작 의도에는 이 명령을 반환하며
 * 실제 페이지 조작은 명령을 다시 검증한 프론트엔드가 수행한다.
 * <p>
 * {@code filters}는 의도적으로 {@code Map<String,String>} 제네릭 맵이다(고정 타입 record 금지).
 * 메인 페이지 검색 필터가 추가/변경되어도 이 record를 바꿀 필요가 없고, 프론트엔드가
 * 자신이 인식하는 키만 적용하고 모르는 키는 무시·보고한다(capability-driven).
 *
 * @param action    수행할 동작. {@code search|setFilters|reset|clarify|paginate|mapFocus|selectItem}
 * @param filters   적용할 검색 필터(키=프론트 capability 키, 값=문자열). 거래월은 'YYYY-MM'.
 * @param page      paginate용 절대 페이지 번호
 * @param direction paginate 방향 'next'|'prev'
 * @param itemIndex selectItem/mapFocus 대상 결과 인덱스
 * @param summary   수행 결과 한국어 요약(모델 생성). 프론트가 실제 적용 결과로 재생성하므로 fallback 용도.
 * @param clarify   {@code action=clarify}일 때 사용자에게 되묻는 한국어 질문
 */
public record AgentCommand(
        String action,
        Map<String, String> filters,
        Integer page,
        String direction,
        Integer itemIndex,
        String summary,
        String clarify
) {

    /** 모호/범위 밖/구조화 실패 시의 안전 폴백 명령을 만든다. */
    public static AgentCommand clarify(String question) {
        return new AgentCommand("clarify", Map.of(), null, null, null, question, question);
    }
}
