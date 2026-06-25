# NoHome AI 사용 보고서

> 프로젝트: **NoHome** — 공공데이터 기반 아파트 실거래가 검색 서비스
> 문서 버전: 1.0 / 작성일: 2026-06-24
> 범위: 서비스에 적용한 AI(LLM) 활용 — 기능·설계 의사결정·프롬프트·Tool calling·운영 정책
> 근거: `no-home-backend/docs/ai-chatbot/**` + 실제 코드(`com.ssafy.home.ai.*`, `application.properties`)
> 담당: 이정헌 (AI 어시스턴트)

---

## 1. 개요

NoHome은 서울 아파트 실거래가를 **자연어로 검색하고 화면까지 조작**하는 AI 어시스턴트를 제공한다. 사용자가 공공데이터 API나 필터 UI를 몰라도 "강남구 2024년 5월 전세 보여줘" 한마디로 검색·필터·페이지·지도를 다룰 수 있다.

| 항목 | 내용 |
| --- | --- |
| 프레임워크 | Spring AI 1.1.2 (ChatClient + Tool Calling) |
| 모델 | gpt-4o-mini (SSAFY GMS — OpenAI 프록시) |
| 엔드포인트 | 단일 `POST /api/ai/assistant` (로그인 전용) |
| 응답 계약 | `AssistantResponse { type: "answer"\|"command", answer, command, notice }` |
| 온도 | temperature 0.0 (결정적 응답) |

**두 가지 동작** — 하나의 엔드포인트에서 LLM이 tool calling으로 분기한다.
- **질문(answer)**: 실거래가/시세를 물으면 데이터 도구로 조회해 한국어로 요약 답변
- **조작(command)**: 검색·필터·페이지·매물선택·지도·초기화를 자연어로 지시하면 프론트가 실행할 명령 반환

> 근거: `ai/controller/AiAssistantController.java`, `ai/assistant/AssistantResponse.java`

---

## 2. 설계 진화와 재설계 의사결정 ⭐

AI 기능은 한 번에 완성되지 않았다. **에이전트 모드 초기 설계의 한계를 실사용에서 발견하고 구조를 재설계**한 과정이 이 프로젝트 AI의 핵심 의사결정이다.

### 2.1 단계별 진화

| 단계 | 시점 | 형태 | 상태 |
| --- | --- | --- | --- |
| 질문 모드 | 06-21 | `/api/ai/chat` — tool로 조회 후 텍스트 답변 | 대체됨 |
| 에이전트 MVP | 06-22 | `/api/ai/agent` — `.entity(AgentCommand)` 강제 구조화 출력(액션4·필터5) | 대체됨 |
| 에이전트 Phase2 | 06-23 | 필터·액션 확장(paginate/mapFocus/selectItem, sort/umdNm/price) | 대체됨 |
| 단기 대화기억 | 06-23~24 | InMemory + conversationId 세션 격리 | 유지 |
| **Tool Calling 혼합형 재설계** | **06-24** | **단일 `/api/ai/assistant`** — LLM이 tool calling으로 분기, 모드토글 제거 | **현재** |

### 2.2 초기 설계(분리형)와 드러난 한계

초기 에이전트 모드는 `/chat`(질문)·`/agent`(조작)를 **분리하고 모드 토글**을 두었으며, 조작 모드는 `.entity(AgentCommand.class)`로 LLM에게 **action을 강제로 생성**하게 했다. 실사용·검증에서 다음 한계가 드러났다.

1. **의도 오분류** — action을 무조건 생성하다 보니 불만·모호·평가성 발화도 억지로 action으로 채워졌다. 예: "이건 가장 싼 게 아닌데"(불만) → `paginate`로 분류.
2. **거래월 LLM 환각** — 거래월 유효 범위 판정을 프롬프트로 맡기자, 약한 모델이 정상 월(2025)도 "지원하지 않는다"고 거부했다.
3. **단일 약한 모델 의존** — 질문/조작/일반대화 분기를 gpt-4o-mini의 비결정적 판단에만 의존했다.
4. **capability drift** — 팀이 전월세 필터를 검색 UI·백엔드에 추가했으나 프론트 단일출처 `filterSchema`에 누락돼 **AI만 전월세를 지원하지 못하는** 불일치가 발생했다.

> 근거: `troubleshooting/2026-06-23-agent-intent-mapping-and-capability-drift.md`

### 2.3 타당성 검토와 재설계 결정

근본 원인은 "action을 강제 생성"하는 구조였다. 두 대안을 검토했다.

| 안 | 방식 | 판단 |
| --- | --- | --- |
| **A안 (채택)** | `@Tool` + `returnDirect` 혼합 — LLM이 도구를 고르되 **"아무 도구도 안 부르고 텍스트 답변"도 1급 선택지** | Phase 0 PoC로 실거동 확인 후 채택 |
| B안 | 수동 tool 루프(`ToolCallingManager`)로 직접 제어 | 구조 변경 규모 큼 → 보류 |

**Phase 0 PoC 게이트**: Spring AI 1.1.2에서 `@Tool(returnDirect=true)`가 실제로 어떻게 동작하는지 먼저 실측했다. 액션 도구 호출 시 응답의 `finishReason`이 정확히 `"returnDirect"`이고, 그때 content가 도구가 반환한 `AgentCommand`의 JSON임을 **결정적으로 식별**할 수 있음을 확인하고 A안을 확정했다.

```java
// AiAssistantController.java — 재설계 핵심: finishReason으로 답변/명령을 결정적 분기
private static final String FINISH_RETURN_DIRECT = "returnDirect";
// returnDirect면 content를 AgentCommand로 역직렬화(type=command), 아니면 텍스트(type=answer)
```

### 2.4 재설계 결과와 교훈

- **단일 `/assistant`로 통합**, 모드 토글·레거시 `/chat`·`/agent` 제거
- **"답변 안 함"을 1급 선택지화** → 모호·불만 발화가 억지 action으로 새지 않음(오분류 구조적 완화)
- **사실 판정을 서버로 이관** → 거래월·지역 유효성은 프롬프트가 아닌 서버 결정적 가드(`AgentCommandGuards`)가 판정
- **capability 단일출처 + 동기화 테스트**로 drift 재발 차단

> **교훈**: 프롬프트 튜닝은 비결정성이 남는 임시방편이고, 분기 구조 자체를 바꾸는 재설계가 근본 해결이었다. 원칙은 **"사실 판정은 LLM이 아니라 서버가 한다."**
> 근거: `proposals/2026-06-23-tool-calling-assistant.md`, `reports/2026-06-24-tool-calling-assistant-implementation.md`

---

## 3. 프롬프트 엔지니어링 ⭐

### 3.1 시스템 프롬프트 전문

런타임에 capability·현재 화면 상태를 주입(`%s`)해 구성한다. `AiAssistantController.java`의 `SYSTEM_PROMPT_TEMPLATE` 원문:

```
너는 'no-home' 서울 아파트 실거래가 서비스의 AI 어시스턴트다. 한 대화에서 '질문 답변'과 '페이지 조작'을 모두 처리한다.
- 특정 지역의 실거래가/시세를 '질문'하면 searchSeoulAptDeals tool로 조회한 뒤 한국어로 간결히 요약 답변하라.
  매매/전세/월세는 dealMode('sale'|'jeonse'|'monthly')로 구분한다.
- 사용자가 조건을 말하며 매물을 '검색/조회'하려 하면(예: '강남구 검색해줘', '서초구 전세로 찾아줘') applyFiltersAndSearch를 호출하라(검색 실행이 기본).
  '검색하지 말고 조건만'처럼 명시적으로 검색을 미룰 때만 setFilters를 쓴다. 페이지 이동은 paginate,
  매물 상세는 selectItem, 지도 표시는 mapFocus, 검색 초기화는 reset tool을 호출하라.
- 일반 대화·인사·모호·불만·평가성 발화는 어떤 tool도 호출하지 말고 한국어 텍스트로 답하라.
- 액션 tool의 filters에 쓸 수 있는 키는 정확히 다음뿐이다: %s. 목록에 없는 키는 만들지 마라.
- 값은 모두 문자열, 거래월은 'YYYY-MM'(예: '2024-05'), 자치구는 '강남구'처럼 '구'를 포함한다.
- 서울특별시 25개 자치구만 지원한다. 금액은 '만원/억원' 단위로 표기한다.
- 현재 화면 상태(참고용): filters=%s, page=%s, totalPages=%s. 존재하지 않는 페이지는 요청하지 마라.
```

### 3.2 설계 의도

| 규칙 | 의도 |
| --- | --- |
| 질문/조작/일반대화 3분기 | "일반 대화·불만은 도구 호출 없이 답하라" 명시 → 오분류 방지(§2와 연계) |
| applyFiltersAndSearch vs setFilters | "검색해줘/보여줘"는 검색 실행, "조건만"은 필터만 — 동사 기준 분기 |
| 고유명사·단위 규칙 | 거래월 `YYYY-MM`, 자치구 `구` 포함, 금액 만원/억원 — 입출력 일관성 |
| capability allow-list(`%s`) | 프론트가 지원 필터를 동적 주입 → "목록에 없는 키는 만들지 마라"로 drift 방지 |
| 현재 화면 상태 주입 | 무상태 서버가 페이지/필터 맥락을 받아 "없는 페이지 요청 금지" |

### 3.3 프롬프트로 해결한 문제

**(1) 인코딩 환각 — 고유명사만 매번 다르게 나옴**
- 증상: 거래 건수·평균가(숫자)는 정확한데 아파트명만 호출마다 엉뚱하게 바뀜
- 원인: 시드 데이터 UTF-8 이중 인코딩(mojibake)으로 DB에서 읽은 아파트명이 깨짐 → 모델이 깨진 입력을 그럴듯하게 "복원" 시도(숫자는 ASCII라 무손상)
- 해결: ① 근본 — 시드 데이터 재초기화(`SET NAMES utf8mb4`) ② 보강 — 프롬프트에 "고유명사는 도구 결과 글자를 그대로 복사" + temperature 0
- 교훈: LLM 환각처럼 보여도 **모델 입력(도구 결과) 검증이 먼저**
- 근거: `troubleshooting/2026-06-21-ai-prompt-and-hallucination.md`

**(2) 지역 선판단 거부 — "동작구"를 거부**
- 증상: "동작구 아파트 실거래가"는 거부(도구 미호출), "서울 동작구"는 정상
- 원인: 프롬프트가 "서울 외 미지원"만 지시 → 모델이 '서울' 없는 자치구를 도구 호출 전에 거부
- 해결: 서울 25개 자치구 명시 + **"지역 지원 여부를 단정 말고 먼저 도구를 호출, 도구가 거부할 때만 안내"**(선판단 금지)
- 교훈: 지역 같은 **사실 판정은 모델이 선판단하지 말고 도구(resolver)에 위임**
- 근거: `troubleshooting/2026-06-22-ai-region-prejudgment-refusal.md`

---

## 4. Tool Calling 구현

### 4.1 returnDirect 패턴

LLM에 두 종류의 도구를 제공하고, `returnDirect` 속성으로 응답 형태를 가른다.
- `returnDirect=false` (데이터 조회) → 도구 결과를 LLM이 받아 **텍스트 답변** 생성
- `returnDirect=true` (페이지 액션) → 도구가 반환한 `AgentCommand`를 **LLM 후처리 없이 그대로** 호출자에게 전달 → 프론트가 실행

### 4.2 데이터 조회 도구 — `HouseTools.searchSeoulAptDeals`

`returnDirect=false`. `@Tool` description 원문:

```
서울 아파트 실거래가를 조회해 요약한다. 매매(sale)·전세(jeonse)·월세(monthly)를 지원한다.
- 데이터는 서울특별시만 지원한다. 구 이름은 '강남구'처럼 '구'까지 포함해 전달한다.
- 거래 연월(dealYmd)은 가능하면 함께 전달한다. 형식은 'YYYYMM' 6자리, 2006년 이후이며 미래 월은 조회할 수 없다. 예: 2024년 5월 -> '202405'.
- dealMode: 'sale'(매매, 기본) | 'jeonse'(전세) | 'monthly'(월세). '전세 시세'면 'jeonse', '월세'면 'monthly'를 쓴다.
- 결과는 거래 건수, 대표 금액(매매가 또는 보증금/월세)의 평균/최저/최고, 대표 거래 목록을 포함한다.
```

- 파라미터: `sigungu`(필수), `umdNm`·`aptName`·`dealYmd`·`dealMode`(선택) — 각 `@ToolParam`에 예시 명시
- 동작: 자치구 → `SeoulLawdCodeResolver`로 lawdCd 변환 → 기존 `HouseService.searchHouseDeals`(자동임포트 포함) 재사용 → 한국어 요약 반환
- 근거: `ai/tool/HouseTools.java`

### 4.3 페이지 액션 도구 — `PageActionTools` (6종)

모두 `returnDirect=true`, 본문은 **부작용 없이** `AgentCommand` 조립·가드 후 반환. 실제 조작은 프론트가 수행(capability-driven).

| 도구 | action | @Tool description 요지 |
| --- | --- | --- |
| `applyFiltersAndSearch` | search | 조건 적용 + **검색 실행**("강남구 검색해줘"). 결과 목록 요청은 거의 이 도구 |
| `setFilters` | setFilters | 검색 미실행, **조건만** ("검색하지 말고 조건만") |
| `paginate` | paginate | 다음/이전 또는 특정 페이지 (`direction=next/prev`, `page=3`) |
| `selectItem` | selectItem | 특정 매물 상세 (`itemIndex` 1부터) |
| `mapFocus` | mapFocus | 특정 매물 지도 표시 (`itemIndex` 1부터) |
| `reset` | reset | 검색 조건 초기화 |

`applyFiltersAndSearch`/`setFilters`는 12개 필터 파라미터(지역·법정동·아파트명·거래월·정렬·dealMode·매매가·보증금·월세 상하한)를 받는다.

### 4.4 구조체

```java
// AgentCommand — filters를 의도적으로 Map<String,String>으로 둠(고정 record 금지)
//   → 메인 필터가 추가/변경돼도 record 무변경, 프론트가 인식 키만 적용(capability drift 방지)
record AgentCommand(String action, Map<String,String> filters, Integer page,
                    String direction, Integer itemIndex, String summary, String clarify)

// AssistantResponse — 컨트롤러가 finishReason으로 분기해 생성
record AssistantResponse(String type /* answer|command */, String answer,
                         AgentCommand command, String notice)
```

### 4.5 서버 결정적 가드 — `AgentCommandGuards`

§2의 "사실 판정은 서버가" 원칙의 구현. 액션 도구가 반환하기 전에 검증해 위반 시 `clarify`로 안전 강등한다.
- **지역**: `SeoulLawdCodeResolver`로 서울 25개 자치구만 허용
- **거래월**: `YYYYMM` 6자리, **2006년 이후 ~ 현재월**(미래 거부) — `HouseTools.dealYmdError` 재사용 (`MIN_DEAL_YEAR=2006`)
- **action**: 허용 목록(search/setFilters/reset/clarify/paginate/mapFocus/selectItem)만
- 근거: `ai/agent/AgentCommandGuards.java`, `ai/tool/PageActionTools.java`

---

## 5. 운영 정책 (대화기억·사용량·가용성·개인정보)

### 5.1 단기 대화기억

- 구성: `MessageWindowChatMemory`(InMemory, `maxMessages=10`) + `MessageChatMemoryAdvisor`를 중앙 ChatClient에 부착 → 모든 호출에 자동 적용
- 대화 키: `conversationId = memberId:<sessionStorage UUID>` — 회원으로 사용자 격리, UUID로 세션 분리
- 대화 원문은 **백엔드 JVM 힙에만** 존재, 브라우저엔 키(UUID)만, DB 영속 없음
- 저장소 4안 비교

  | 기준 | InMemory(채택) | localStorage | RDB | Redis |
  | --- | --- | --- | --- | --- |
  | 원문 위치 | 서버 메모리 | 클라이언트 | 서버 디스크 | 서버 메모리 |
  | 영속성 | ✗ 재기동 소멸 | △ | ✓ | ✓ TTL |
  | 프라이버시 | 높음(휘발) | XSS 위험 | 영속 책임 | 중(TTL) |
  | 운영 비용 | 없음 | 없음 | 스키마·정리 | 인프라 추가 |

- 채택 이유: "단일 backend + 닫으면 초기화 + 원문 비영속" 요구에 최적. 전환 시 `ChatMemoryRepository` 빈 한 곳만 교체(스케일아웃→Redis+TTL, 영구보존→RDB)
- 근거: `reports/2026-06-24-short-term-memory-implementation.md`, `ai/config/AiConfig.java`

### 5.2 사용량 제한·비용

- 알고리즘: 토큰 버킷 기반 sliding window (`AiChatRateLimiter`)
- 기본값: 분당 10회 · 메시지 500자 · 동시 요청 1 · 로그인 전용
- 타임아웃: connect 3s / read 25s, 모델 재시도 최대 2회
- 모델: gpt-4o-mini, temperature 0.0 (비용 효율 + 결정성)
- token 차감 기준

  | 결과 | HTTP | 차감 | 비고 |
  | --- | --- | --- | --- |
  | 정상 | 200 | ✅ | |
  | 빈/초과 입력 | 400 | ❌ | AI 처리 전 |
  | 미인증 | 401 | ❌ | AI 처리 전 |
  | 동시 요청 | 409 | ❌ | 기존 요청만 처리 |
  | 한도 초과 | 429 | ❌ | Retry-After 헤더 |
  | 모델·도구 장애 | 503 | ✅ | 자원 사용 가능성 |
  | 타임아웃 | 504 | ✅ | 자원 사용 가능성 |

- 근거: `ai/limit/AiChatRateLimiter.java`, `reports/2026-06-21-usage-limiter-ux-scenarios.md`

### 5.3 가용성 (graceful degradation)

- `AiKeyEnvironmentPostProcessor`: `SSAFY_GMS_API_KEY` 부재 시 모든 AI 모델 자동구성을 `none`으로, `app.ai.chat.available=false` 주입
- 효과: ChatClient 빈 미생성 → `/api/ai/assistant`만 503, **부동산 검색·회원 등 나머지 기능은 정상 기동**
- 근거: `ai/config/AiKeyEnvironmentPostProcessor.java`, `troubleshooting/2026-06-22-spring-ai-empty-key-graceful.md`

### 5.4 개인정보·로깅

- 기록 금지: 시스템 프롬프트·질문·답변 원문, 도구 입출력 원문(아파트명·가격·법정동), 회원ID·이메일·JWT, API 키
- 기록 허용: 메시지 개수, 도구 호출 여부, 공급자 token 수, 조회 건수·잘림 여부, HTTP 상태별 집계(원문 미포함)
- `SimpleLoggerAdvisor`는 기본 OFF(`AI_CHAT_DIAGNOSTIC_LOGGING_ENABLED=false`), 진단 시에도 메타데이터만
- 대화는 세션 휘발(§5.1) — DB 영속 없음
- 근거: `reports/2026-06-21-ai-logging-privacy-policy.md`

---

## 6. 검증

### 6.1 테스트 추이

| 단계 | 백엔드 | 프론트 | 비고 |
| --- | --- | --- | --- |
| 질문 모드 | 78 | 15 | E2E 9 |
| 에이전트 MVP | 105 | 28 | E2E 9 |
| Phase 2 | 113 | 44 | |
| 재설계(리포트 기준) | 144 | 44 | |
| **최신 master 실측(2026-06-24)** | **177** | **51** | 전체 그린, 오류 0 |

> 리포트별 수치는 작성 시점 기준이며, 최신 통합 master는 직접 측정값(BE 177 / FE 51)이다.

### 6.2 사용자 시나리오 (풀스택 검증, 발췌)

- 질문: "강남구 2024년 5월 평균 거래가" → 요약 답변 ✅
- 검색: 지역+월 / 기간범위 / 아파트명 / 가격대 / 정렬 / 읍면동 / **전월세(dealMode)** ✅
- 결과 탐색: 항목 선택(selectItem) / 지도 포커스(mapFocus) / 페이지 이동(paginate) / 경계 안내 ✅
- 제어·안전: 초기화(reset) / 서울 외 → clarify / 모호 요청 → clarify ✅
- 멀티턴: "마포구 시세" → "방금 그 지역?" → 맥락 추론 ✅
- 근거: `guides/2026-06-23-agent-mode-scenarios.md`

---

## 7. 한계·후속 (backlog)

| 항목 | 내용 |
| --- | --- |
| 다중 동작 | returnDirect가 호출당 1액션 → "3페이지 후 2번째 선택" 같은 순차 동작은 첫 동작만 실행 |
| 결과 기반 선택 | 현재 결과 목록이 모델에 미전달 → "면적 가장 작은 매물" 환각. 결과 요약 동봉 + 면적 정렬 필요 |
| 다중 인스턴스 | InMemory는 인스턴스별 분리 → 스케일아웃 시 Redis+TTL 전환 |
| 일일 한도 | 현재 분당 제한만 → 낮은 속도 반복 누적 대비 일일 한도·circuit breaker 필요 |

> 근거: `backlog/2026-06-24-backlog.md`

---

## 8. 부록

### 8.1 근거 파일
- 문서: `no-home-backend/docs/ai-chatbot/` (proposals · reports · troubleshooting · guides · backlog)
- 코드: `com.ssafy.home.ai` — `controller/AiAssistantController`, `tool/HouseTools`·`PageActionTools`, `agent/AgentCommand`·`AgentCommandGuards`, `assistant/AssistantResponse`, `config/AiConfig`·`AiKeyEnvironmentPostProcessor`, `limit/AiChatRateLimiter`

### 8.2 핵심 설정값 (`application.properties`)

| 키 | 기본값 |
| --- | --- |
| spring.ai.openai.chat.options.model | gpt-4o-mini |
| spring.ai.openai.base-url | https://gms.ssafy.io/gmsapi/api.openai.com/v1/ |
| ai.chat.memory.max-messages | 10 |
| ai.chat.max-message-length | 500 |
| ai.chat.rate-limit.requests / window | 10 / 1m |
| spring.http.client.connect-timeout / read-timeout | 3s / 25s |
| ai.chat.logging.diagnostics-enabled | false |

> 본 보고서의 용어·수치는 `requirements-spec.md` · `wbs-gantt.md` · 발표자료와 일관한다(단일 `/assistant`, BE 177 / FE 51).
