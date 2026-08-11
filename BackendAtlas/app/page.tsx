"use client";

import { useEffect, useMemo, useRef, useState } from "react";

type Method = "GET" | "POST" | "PUT" | "DELETE";

type Endpoint = {
  method: Method;
  path: string;
  group: string;
  title: string;
  beginner: string;
  auth: string;
  source: string;
};

const endpoints: Endpoint[] = [
  { method: "GET", path: "/api/health", group: "공통", title: "서버 상태 확인", beginner: "서버와 데이터베이스가 살아 있는지 확인해요.", auth: "없음", source: "common/health/controller/HealthController.java" },
  { method: "GET", path: "/api/regions", group: "집 찾기", title: "지역 목록", beginner: "시군구 코드에 속한 동네 이름을 가져와요.", auth: "없음", source: "house/controller/HouseController.java" },
  { method: "GET", path: "/api/houses", group: "집 찾기", title: "아파트 이름 검색", beginner: "입력한 글자가 포함된 아파트를 찾아요.", auth: "없음", source: "house/controller/HouseController.java" },
  { method: "GET", path: "/api/houses/search", group: "집 찾기", title: "거래 검색", beginner: "지역·기간·가격 조건으로 실제 거래를 검색해요.", auth: "없음", source: "house/controller/HouseController.java" },
  { method: "GET", path: "/api/houses/price-range", group: "집 찾기", title: "가격 범위", beginner: "검색 결과에서 가장 싼 값과 비싼 값을 알려줘요.", auth: "없음", source: "house/controller/HouseController.java" },
  { method: "GET", path: "/api/house-deals", group: "집 찾기", title: "저장된 거래", beginner: "특정 지역과 달에 저장된 거래를 모아 봐요.", auth: "없음", source: "house/controller/HouseController.java" },
  { method: "POST", path: "/api/public-data/apt-trades/import", group: "공공데이터", title: "거래 가져오기", beginner: "공공데이터에서 매매·전월세 거래를 가져와 저장해요.", auth: "없음", source: "publicdata/controller/PublicDataImportController.java" },
  { method: "POST", path: "/api/members", group: "회원", title: "회원가입", beginner: "새 계정을 만들어요.", auth: "없음", source: "member/controller/MemberController.java" },
  { method: "POST", path: "/api/auth/login", group: "회원", title: "로그인", beginner: "이메일과 비밀번호를 확인하고 로그인 쿠키를 만들어요.", auth: "없음", source: "member/controller/MemberController.java" },
  { method: "POST", path: "/api/auth/refresh", group: "회원", title: "로그인 연장", beginner: "만료된 로그인 정보를 안전하게 새로 만들어요.", auth: "Refresh 쿠키", source: "member/controller/MemberController.java" },
  { method: "POST", path: "/api/auth/logout", group: "회원", title: "로그아웃", beginner: "저장된 토큰과 브라우저 쿠키를 지워요.", auth: "선택", source: "member/controller/MemberController.java" },
  { method: "POST", path: "/api/auth/password-reset", group: "회원", title: "비밀번호 재설정", beginner: "비밀번호를 바꾸고 기존 로그인 정보를 모두 없애요.", auth: "없음", source: "member/controller/MemberController.java" },
  { method: "GET", path: "/api/members/me", group: "회원", title: "내 정보", beginner: "현재 로그인한 회원의 정보를 보여줘요.", auth: "로그인 필요", source: "member/controller/MemberController.java" },
  { method: "GET", path: "/api/members/search", group: "회원", title: "회원 검색", beginner: "이름·이메일·전화번호로 다른 회원을 찾아요.", auth: "로그인 필요", source: "member/controller/MemberController.java" },
  { method: "PUT", path: "/api/members/me", group: "회원", title: "내 정보 수정", beginner: "현재 회원의 이름과 전화번호를 바꿔요.", auth: "로그인 필요", source: "member/controller/MemberController.java" },
  { method: "DELETE", path: "/api/members/me", group: "회원", title: "회원 탈퇴", beginner: "계정과 로그인 정보를 함께 지워요.", auth: "로그인 필요", source: "member/controller/MemberController.java" },
  { method: "GET", path: "/api/interest-regions", group: "관심 지역", title: "내 관심 지역", beginner: "내가 저장한 관심 동네를 모아 봐요.", auth: "로그인 필요", source: "interest/controller/InterestRegionController.java" },
  { method: "POST", path: "/api/interest-regions", group: "관심 지역", title: "관심 지역 추가", beginner: "자주 보고 싶은 동네를 저장해요.", auth: "로그인 필요", source: "interest/controller/InterestRegionController.java" },
  { method: "DELETE", path: "/api/interest-regions/{interestRegionId}", group: "관심 지역", title: "관심 지역 삭제", beginner: "더 이상 보지 않을 동네를 목록에서 지워요.", auth: "로그인 필요", source: "interest/controller/InterestRegionController.java" },
  { method: "GET", path: "/api/notices", group: "공지", title: "공지 목록", beginner: "최근 공지를 순서대로 보여줘요.", auth: "없음", source: "notice/controller/NoticeController.java" },
  { method: "GET", path: "/api/notices/{noticeId}", group: "공지", title: "공지 상세", beginner: "선택한 공지의 제목과 내용을 보여줘요.", auth: "없음", source: "notice/controller/NoticeController.java" },
  { method: "POST", path: "/api/notices", group: "공지", title: "공지 작성", beginner: "관리자가 새 공지를 만들어요.", auth: "관리자", source: "notice/controller/NoticeController.java" },
  { method: "PUT", path: "/api/notices/{noticeId}", group: "공지", title: "공지 수정", beginner: "관리자가 기존 공지를 고쳐요.", auth: "관리자", source: "notice/controller/NoticeController.java" },
  { method: "DELETE", path: "/api/notices/{noticeId}", group: "공지", title: "공지 삭제", beginner: "관리자가 공지를 지워요.", auth: "관리자", source: "notice/controller/NoticeController.java" },
  { method: "POST", path: "/api/ai/assistant", group: "AI", title: "AI 도우미", beginner: "질문을 받고 답변이나 화면 이동 명령을 만들어요.", auth: "로그인 필요", source: "ai/controller/AiAssistantController.java" },
];

const blocks = [
  { id: "browser", icon: "01", name: "브라우저", easy: "사용자가 버튼을 누르는 곳" },
  { id: "controller", icon: "02", name: "Controller", easy: "요청을 처음 받는 안내 데스크" },
  { id: "service", icon: "03", name: "Service", easy: "업무 규칙을 실행하는 담당자" },
  { id: "adapter", icon: "04", name: "Adapter", easy: "데이터베이스 말로 번역하는 통역사" },
  { id: "database", icon: "05", name: "PostgreSQL", easy: "데이터를 오래 보관하는 창고" },
];

const tables = [
  { name: "regions", label: "지역", easy: "동네 이름과 위치", color: "mint", links: ["houses", "interest_regions"], columns: [["region_id", "지역 번호"], ["lawd_cd", "시군구 코드"], ["umd_nm", "읍면동 이름"], ["lat / lng", "위도와 경도"]] },
  { name: "houses", label: "주택", easy: "아파트 기본 정보", color: "blue", links: ["regions", "house_deals"], columns: [["house_id", "주택 번호"], ["region_id", "어느 지역인지"], ["apt_nm", "아파트 이름"], ["build_year", "지어진 해"]] },
  { name: "house_deals", label: "거래", easy: "매매·전월세 기록", color: "orange", links: ["houses"], columns: [["deal_id", "거래 번호"], ["deal_type", "매매·전세·월세"], ["deal_date", "거래 날짜"], ["api_row_hash", "중복을 막는 지문"], ["raw_response", "원본 데이터"]] },
  { name: "public_data_import_batches", label: "수집 기록", easy: "언제 얼마나 가져왔는지", color: "orange", links: [], columns: [["status", "진행 상태"], ["total_count", "전체 개수"], ["imported_count", "저장 개수"], ["skipped_count", "건너뛴 개수"]] },
  { name: "members", label: "회원", easy: "계정 기본 정보", color: "purple", links: ["member_refresh_tokens", "notices", "interest_regions"], columns: [["member_id", "회원 번호"], ["email", "로그인 이메일"], ["password_hash", "암호화된 비밀번호"], ["name", "이름"]] },
  { name: "member_refresh_tokens", label: "로그인 토큰", easy: "로그인을 연장하는 정보", color: "purple", links: ["members"], columns: [["member_id", "회원 번호"], ["token_hash", "토큰의 안전한 지문"], ["expires_at", "만료 시각"]] },
  { name: "notices", label: "공지", easy: "관리자가 쓴 알림", color: "blue", links: ["members"], columns: [["notice_id", "공지 번호"], ["member_id", "작성 회원"], ["title", "제목"], ["content", "내용"]] },
  { name: "interest_regions", label: "관심 지역", easy: "회원이 찜한 동네", color: "mint", links: ["members", "regions"], columns: [["interest_region_id", "관심 지역 번호"], ["member_id", "회원 번호"], ["region_id", "지역 번호"]] },
];

const pipelineStages = [
  { title: "가져오기", easy: "공공데이터 서버에서 XML을 받아요.", tech: "RestClient · 페이지당 최대 1,000건" },
  { title: "읽기", easy: "XML 태그를 자바 객체로 바꿔요.", tech: "AptTrade / AptRent XML Parser" },
  { title: "정리하기", easy: "금액, 날짜, 빈칸을 같은 모양으로 맞춰요.", tech: "ImportCommandFactory" },
  { title: "중복 찾기", easy: "각 거래에 고유한 지문을 만들어 겹치는지 봐요.", tech: "SHA-256 api_row_hash" },
  { title: "저장하기", easy: "지역 → 주택 → 거래 순서로 데이터베이스에 넣어요.", tech: "500건씩 묶어서 저장" },
  { title: "검색하기", easy: "조건에 맞는 거래를 정렬해서 화면에 돌려줘요.", tech: "Native SQL · 페이지 처리" },
];

const glossary = [
  ["Controller", "웹 요청을 가장 먼저 받는 클래스예요."],
  ["Service", "실제 업무 규칙과 처리 순서를 담당해요."],
  ["Transaction", "여러 저장 작업을 하나로 묶어요. 중간에 실패하면 모두 되돌려요."],
  ["Async", "사용자 응답을 기다리게 하지 않고 뒤에서 따로 처리하는 방식이에요."],
  ["ERD", "데이터 표들이 어떻게 연결되는지 보여주는 지도예요."],
  ["Hash", "긴 데이터를 짧고 일정한 지문으로 바꾸는 방법이에요."],
];

const errorResponses = [
  { status: "400", name: "잘못된 요청", when: "필수 값이 없거나 입력 규칙에 맞지 않을 때", code: "BAD_REQUEST" },
  { status: "401", name: "로그인 필요", when: "로그인 정보가 없거나 이메일·비밀번호가 틀릴 때", code: "UNAUTHORIZED" },
  { status: "403", name: "권한 부족", when: "일반 회원이 관리자 기능을 요청할 때", code: "FORBIDDEN" },
  { status: "404", name: "찾을 수 없음", when: "회원·공지·관심 지역이 존재하지 않을 때", code: "NOT_FOUND" },
  { status: "409", name: "이미 존재함", when: "이미 가입된 이메일로 회원가입할 때", code: "CONFLICT" },
  { status: "502", name: "외부 서버 오류", when: "공공데이터 제공 기관이 정상 응답하지 않을 때", code: "BAD_GATEWAY" },
  { status: "503", name: "외부 서비스 사용 불가", when: "공공데이터 키가 없거나 잘못됐거나 사용량을 넘었을 때", code: "SERVICE_UNAVAILABLE" },
  { status: "504", name: "외부 서버 시간 초과", when: "공공데이터 응답을 제한 시간 안에 받지 못할 때", code: "GATEWAY_TIMEOUT" },
];

const guideFiles = [
  { name: "HouseController.java", role: "검색 요청을 받고 HouseService에 전달해요.", path: "house/controller/HouseController.java" },
  { name: "HouseService.java", role: "주택 검색 조건과 업무 처리 순서를 맡아요.", path: "house/service/HouseService.java" },
  { name: "JpaHouseQueryAdapter.java", role: "검색 조건을 데이터베이스 조회로 바꿔요.", path: "house/persistence/JpaHouseQueryAdapter.java" },
  { name: "PublicDataImportService.java", role: "공공데이터 수집과 저장 흐름을 지휘해요.", path: "publicdata/service/PublicDataImportService.java" },
  { name: "PublicDataBatchPersistService.java", role: "두 작업 스레드로 데이터를 묶어서 저장해요.", path: "publicdata/service/PublicDataBatchPersistService.java" },
  { name: "GlobalExceptionHandler.java", role: "예외를 알맞은 HTTP 오류 응답으로 바꿔요.", path: "common/response/GlobalExceptionHandler.java" },
];

const githubBase = "https://github.com/cmsik1/no-home/blob/main/Backend/src/main/java/com/ssafy/home/";

export default function Home() {
  const [orderedBlocks, setOrderedBlocks] = useState(() => [blocks[0], blocks[2], blocks[1], blocks[4], blocks[3]]);
  const [dragIndex, setDragIndex] = useState<number | null>(null);
  const [answer, setAnswer] = useState<"idle" | "correct" | "wrong">("idle");
  const [dealType, setDealType] = useState<"sale" | "rent">("sale");
  const [itemCount, setItemCount] = useState(18);
  const [duplicateRate, setDuplicateRate] = useState(22);
  const [pipelineStep, setPipelineStep] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [query, setQuery] = useState("");
  const [method, setMethod] = useState<"ALL" | Method>("ALL");
  const [openApi, setOpenApi] = useState<string | null>(null);
  const [selectedTable, setSelectedTable] = useState("regions");
  const [flowMode, setFlowMode] = useState<"sync" | "async">("async");
  const [openTerm, setOpenTerm] = useState<number | null>(0);
  const [selectedError, setSelectedError] = useState(0);
  const [selectedFile, setSelectedFile] = useState(0);
  const labRef = useRef<HTMLElement>(null);

  useEffect(() => {
    if (!playing) return;
    const timer = window.setInterval(() => {
      setPipelineStep((current) => {
        if (current >= pipelineStages.length - 1) {
          setPlaying(false);
          return current;
        }
        return current + 1;
      });
    }, 1100);
    return () => window.clearInterval(timer);
  }, [playing]);

  const skipped = Math.round(itemCount * duplicateRate / 100);
  const imported = itemCount - skipped;
  const filteredEndpoints = useMemo(() => endpoints.filter((endpoint) => {
    const search = `${endpoint.path} ${endpoint.group} ${endpoint.title} ${endpoint.beginner}`.toLowerCase();
    return (method === "ALL" || endpoint.method === method) && search.includes(query.toLowerCase());
  }), [method, query]);
  const activeTable = tables.find((table) => table.name === selectedTable) ?? tables[0];

  function moveBlock(from: number, to: number) {
    if (from === to) return;
    setOrderedBlocks((current) => {
      const next = [...current];
      const [moved] = next.splice(from, 1);
      next.splice(to, 0, moved);
      return next;
    });
    setAnswer("idle");
  }

  function checkArchitecture() {
    const isCorrect = orderedBlocks.every((block, index) => block.id === blocks[index].id);
    setAnswer(isCorrect ? "correct" : "wrong");
  }

  return (
    <main>
      <header className="site-header">
        <a className="wordmark" href="#top" aria-label="NoHome Backend Atlas 처음으로">
          <span className="wordmark-icon">N</span>
          <span>NoHome <b>Backend Atlas</b></span>
        </a>
        <nav aria-label="주요 메뉴">
          <a href="#map">흐름 조립</a>
          <a href="#lab">파이프라인 실험</a>
          <a href="#api">API 찾기</a>
          <a href="#data">데이터 지도</a>
        </nav>
        <a className="header-action" href="#lab">직접 실행하기</a>
      </header>

      <section className="hero" id="top">
        <div className="hero-copy">
          <p className="eyebrow">초보자를 위한 인터랙티브 백엔드 학습 사이트</p>
          <h1><span>읽기만 하지 말고,</span><strong>직접 움직여 보세요.</strong></h1>
          <p className="hero-lead">버튼 한 번이 서버 안에서 어떤 길을 지나가는지,<br className="desktop-break" /> 블록을 옮기고 숫자를 바꾸며 눈으로 확인할 수 있어요.</p>
          <div className="hero-actions">
            <button onClick={() => labRef.current?.scrollIntoView({ behavior: "smooth" })}>파이프라인 실행하기 <span>↓</span></button>
            <a href="#map">요청 흐름 먼저 맞추기</a>
          </div>
          <div className="hero-proof" aria-label="코드에서 확인한 수치">
            <div><b>25</b><span>웹 요청 주소</span></div>
            <div><b>8</b><span>데이터 테이블</span></div>
            <div><b>175</b><span>통과한 테스트</span></div>
          </div>
        </div>
        <div className="hero-playground" aria-label="백엔드 흐름 미리보기">
          <div className="mini-window">
            <div className="window-top"><i /><i /><i /><span>GET /api/houses/search</span></div>
            <div className="route-preview">
              {blocks.map((block, index) => <div key={block.id} className={`route-node node-${index}`}><span>{block.icon}</span><b>{block.name}</b><small>{block.easy}</small></div>)}
            </div>
            <div className="moving-packet packet-one">요청</div>
            <div className="moving-packet packet-two">응답</div>
          </div>
          <div className="hint-card"><span>TIP</span><p>아래에서 이 블록들을<br />직접 드래그할 수 있어요.</p></div>
        </div>
      </section>

      <section className="learning-strip" aria-label="학습 순서">
        <span>오늘의 학습 경로</span>
        <ol><li>흐름을 맞춰요</li><li>데이터를 돌려요</li><li>API를 열어 봐요</li><li>테이블을 연결해요</li></ol>
      </section>

      <section className="section map-section" id="map">
        <SectionTitle number="01" label="요청 흐름 조립" title="블록을 끌어서 올바른 순서를 만들어 보세요" description="사용자가 검색 버튼을 누른 뒤 데이터베이스까지 가는 길이에요. 마우스로 끌거나 화살표 버튼으로 순서를 바꿀 수 있어요." />
        <div className="builder-layout">
          <div className="builder-board">
            <div className="board-label"><span>시작 · 사용자의 클릭</span><span>끝 · 데이터 찾기</span></div>
            <div className="sortable-list">
              {orderedBlocks.map((block, index) => (
                <div key={block.id} className="sortable-item" draggable onDragStart={() => setDragIndex(index)} onDragOver={(event) => event.preventDefault()} onDrop={() => { if (dragIndex !== null) moveBlock(dragIndex, index); setDragIndex(null); }}>
                  <span className="drag-handle" aria-hidden="true">⠿</span>
                  <span className="block-number">{block.icon}</span>
                  <div><b>{block.name}</b><small>{block.easy}</small></div>
                  <div className="move-buttons" aria-label={`${block.name} 위치 이동`}>
                    <button disabled={index === 0} onClick={() => moveBlock(index, index - 1)} aria-label={`${block.name} 왼쪽으로`}>←</button>
                    <button disabled={index === orderedBlocks.length - 1} onClick={() => moveBlock(index, index + 1)} aria-label={`${block.name} 오른쪽으로`}>→</button>
                  </div>
                </div>
              ))}
            </div>
            <div className="builder-actions">
              <button className="primary-button" onClick={checkArchitecture}>순서 확인하기</button>
              <button className="text-button" onClick={() => { setOrderedBlocks([blocks[0], blocks[2], blocks[1], blocks[4], blocks[3]]); setAnswer("idle"); }}>다시 섞기</button>
              <p className={`answer-message ${answer}`} aria-live="polite">{answer === "correct" ? "정답이에요! 요청은 이 순서로 이동합니다." : answer === "wrong" ? "거의 다 왔어요. 안내 데스크(Controller)가 업무 담당자(Service)보다 먼저예요." : ""}</p>
            </div>
          </div>
          <aside className="easy-note">
            <span className="note-label">한 문장으로 이해하기</span>
            <h3>Controller는 접수하고,<br />Service는 처리해요.</h3>
            <p>Adapter는 Service가 데이터베이스의 상세 문법을 몰라도 되게 중간에서 연결해 줍니다.</p>
            <div className="code-flow"><code>controller</code><i>→</i><code>service</code><i>→</i><code>persistence</code></div>
          </aside>
        </div>
      </section>

      <section className="section lab-section" id="lab" ref={labRef}>
        <SectionTitle number="02" label="파이프라인 실험실" title="숫자를 바꾸고 공공데이터를 직접 흘려보내세요" description="실제 서버는 외부 XML을 읽고, 모양을 정리하고, 중복을 제거한 뒤 저장해요. 아래 실험은 같은 규칙을 샘플 데이터로 보여 줍니다." light />
        <div className="lab-shell">
          <div className="lab-controls">
            <div className="segmented" aria-label="거래 종류">
              <button className={dealType === "sale" ? "selected" : ""} onClick={() => { setDealType("sale"); setPipelineStep(0); }}>매매</button>
              <button className={dealType === "rent" ? "selected" : ""} onClick={() => { setDealType("rent"); setPipelineStep(0); }}>월세</button>
            </div>
            <label className="range-control"><span><b>가져올 거래</b><output>{itemCount}건</output></span><input type="range" min="1" max="50" value={itemCount} onChange={(event) => setItemCount(Number(event.target.value))} /></label>
            <label className="range-control"><span><b>이미 있는 중복</b><output>{duplicateRate}%</output></span><input type="range" min="0" max="80" value={duplicateRate} onChange={(event) => setDuplicateRate(Number(event.target.value))} /></label>
            <button className="run-button" onClick={() => { setPipelineStep(0); setPlaying(true); }} disabled={playing}>{playing ? "실행 중…" : "처음부터 자동 실행"} <span>▶</span></button>
          </div>
          <div className="pipeline-stage-view">
            <div className="stage-rail" aria-label="파이프라인 단계">
              {pipelineStages.map((stage, index) => <button key={stage.title} className={`${index === pipelineStep ? "current" : ""} ${index < pipelineStep ? "passed" : ""}`} onClick={() => { setPlaying(false); setPipelineStep(index); }}><span>{String(index + 1).padStart(2, "0")}</span><b>{stage.title}</b></button>)}
            </div>
            <div className="stage-card" aria-live="polite">
              <div className="stage-copy"><span>STEP {pipelineStep + 1} / {pipelineStages.length}</span><h3>{pipelineStages[pipelineStep].title}</h3><p>{pipelineStages[pipelineStep].easy}</p><code>{pipelineStages[pipelineStep].tech}</code></div>
              <PipelineVisual step={pipelineStep} type={dealType} imported={imported} skipped={skipped} total={itemCount} />
            </div>
            <div className="stage-navigation"><button onClick={() => setPipelineStep(Math.max(0, pipelineStep - 1))} disabled={pipelineStep === 0}>← 이전</button><div className="result-summary"><span>전체 <b>{itemCount}</b></span><span>저장 <b>{imported}</b></span><span>중복 <b>{skipped}</b></span></div><button onClick={() => setPipelineStep(Math.min(pipelineStages.length - 1, pipelineStep + 1))} disabled={pipelineStep === pipelineStages.length - 1}>다음 →</button></div>
          </div>
        </div>
        <p className="lab-caption">체험용 숫자만 바뀝니다. 실제 코드의 순서와 중복 처리 규칙은 그대로 반영했어요.</p>
      </section>

      <section className="section api-section" id="api">
        <SectionTitle number="03" label="API 탐색기" title="원하는 기능을 검색하고 눌러서 펼쳐 보세요" description="API는 화면과 서버가 대화할 때 사용하는 주소예요. 영어 경로가 어렵다면 ‘회원’, ‘공지’, ‘거래’처럼 익숙한 단어로 찾아보세요." />
        <div className="api-toolbar">
          <label className="search-box"><span aria-hidden="true">⌕</span><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="예: 로그인, 거래, 공지" aria-label="API 검색" /><kbd>{filteredEndpoints.length}개</kbd></label>
          <div className="method-filter" aria-label="요청 방법 필터">{(["ALL", "GET", "POST", "PUT", "DELETE"] as const).map((item) => <button key={item} className={method === item ? "active" : ""} onClick={() => setMethod(item)}>{item === "ALL" ? "전체" : item}</button>)}</div>
        </div>
        <div className="api-list">
          {filteredEndpoints.map((endpoint) => {
            const key = `${endpoint.method}-${endpoint.path}`;
            const isOpen = openApi === key;
            return <article className={`api-row ${isOpen ? "open" : ""}`} key={key}>
              <button className="api-summary" onClick={() => setOpenApi(isOpen ? null : key)} aria-expanded={isOpen}>
                <span className={`method method-${endpoint.method.toLowerCase()}`}>{endpoint.method}</span>
                <code>{endpoint.path}</code>
                <span className="api-name"><b>{endpoint.title}</b><small>{endpoint.group}</small></span>
                <span className="expand-icon">{isOpen ? "−" : "+"}</span>
              </button>
              {isOpen && <div className="api-detail"><div><span>쉽게 말하면</span><p>{endpoint.beginner}</p></div><div><span>누가 쓸 수 있나요?</span><p>{endpoint.auth}</p></div><a href={`${githubBase}${endpoint.source}`} target="_blank" rel="noreferrer">실제 코드 열기 ↗</a></div>}
            </article>;
          })}
          {filteredEndpoints.length === 0 && <div className="empty-result">검색 결과가 없어요.<br />다른 쉬운 단어로 다시 찾아보세요.</div>}
        </div>
      </section>

      <section className="section code-guide-section" id="code-guide">
        <SectionTitle number="04" label="오류와 코드 안내서" title="오류 번호와 주요 파일을 눌러서 살펴보세요" description="오류는 실패 이유를 알려주는 신호이고, 주요 파일은 그 신호와 기능을 실제로 처리하는 장소예요. 아래 내용은 GlobalExceptionHandler와 현재 소스에서 확인했습니다." />
        <div className="code-guide-grid">
          <article className="error-explorer">
            <div className="guide-heading"><span>오류 응답</span><h3>숫자를 누르면 뜻이 열려요</h3></div>
            <div className="error-tabs" aria-label="오류 상태 선택">
              {errorResponses.map((error, index) => <button key={error.status} className={selectedError === index ? "active" : ""} onClick={() => setSelectedError(index)}>{error.status}</button>)}
            </div>
            <div className="error-card" aria-live="polite">
              <span>HTTP {errorResponses[selectedError].status} · {errorResponses[selectedError].code}</span>
              <h4>{errorResponses[selectedError].name}</h4>
              <p>{errorResponses[selectedError].when}</p>
              <pre>{`{\n  "success": false,\n  "message": "실패 이유",\n  "data": null\n}`}</pre>
              <a href={`${githubBase}common/response/GlobalExceptionHandler.java`} target="_blank" rel="noreferrer">오류 처리 코드 열기 ↗</a>
            </div>
          </article>
          <article className="file-explorer">
            <div className="guide-heading"><span>주요 파일</span><h3>책임을 먼저 읽고 코드를 열어요</h3></div>
            <div className="file-picker">
              {guideFiles.map((file, index) => <button key={file.name} className={selectedFile === index ? "active" : ""} onClick={() => setSelectedFile(index)}><code>{file.name}</code><span>{index + 1}</span></button>)}
            </div>
            <div className="file-role" aria-live="polite"><span>이 파일이 하는 일</span><p>{guideFiles[selectedFile].role}</p><a href={`${githubBase}${guideFiles[selectedFile].path}`} target="_blank" rel="noreferrer">GitHub에서 실제 파일 열기 ↗</a></div>
          </article>
        </div>
      </section>

      <section className="section data-section" id="data">
        <SectionTitle number="05" label="데이터 지도" title="테이블을 눌러 연결 관계를 따라가 보세요" description="테이블은 데이터를 종류별로 담는 표예요. 예를 들어 houses는 아파트 정보, house_deals는 거래 기록을 보관합니다." />
        <div className="data-map">
          <div className="table-constellation">
            <div className="connection-lines" aria-hidden="true"><i className="line l1" /><i className="line l2" /><i className="line l3" /><i className="line l4" /><i className="line l5" /></div>
            {tables.map((table, index) => <button key={table.name} className={`table-node table-${index} ${table.color} ${selectedTable === table.name ? "active" : ""}`} onClick={() => setSelectedTable(table.name)}><span>{table.label}</span><code>{table.name}</code><small>{table.easy}</small></button>)}
          </div>
          <aside className="table-inspector">
            <div className="inspector-top"><span>선택한 테이블</span><h3>{activeTable.label}</h3><code>{activeTable.name}</code><p>{activeTable.easy}을(를) 저장합니다.</p></div>
            <div className="column-list"><span>주요 칸</span>{activeTable.columns.map(([name, easy]) => <div key={name}><code>{name}</code><p>{easy}</p></div>)}</div>
            <div className="related-list"><span>연결된 테이블</span><div>{activeTable.links.length ? activeTable.links.map((link) => <button key={link} onClick={() => setSelectedTable(link)}>{tables.find((table) => table.name === link)?.label ?? link} →</button>) : <p>직접 연결된 테이블이 없어요.</p>}</div></div>
          </aside>
        </div>
      </section>

      <section className="section runtime-section" id="runtime">
        <SectionTitle number="06" label="처리 방식 비교" title="동기와 비동기를 전환해서 속도 차이를 느껴보세요" description="동기는 일이 끝날 때까지 기다리는 방식이고, 비동기는 답을 먼저 준 뒤 남은 일을 뒤에서 처리하는 방식이에요." light />
        <div className="runtime-toggle"><button className={flowMode === "sync" ? "active" : ""} onClick={() => setFlowMode("sync")}>동기 처리</button><button className={flowMode === "async" ? "active" : ""} onClick={() => setFlowMode("async")}>비동기 처리</button></div>
        <div className={`runtime-demo ${flowMode}`}>
          <div className="runtime-person"><span>사용자</span><i>검색</i></div>
          <div className="runtime-lanes">
            <div className="lane"><span>검색 결과 만들기</span><div className="track"><i className="runtime-dot dot-search" /></div><b>화면 응답</b></div>
            <div className="lane"><span>데이터 저장하기</span><div className="track"><i className="runtime-dot dot-save" /></div><b>DB 저장</b></div>
          </div>
          <div className="runtime-result"><b>{flowMode === "async" ? "먼저 보여줘요" : "저장까지 기다려요"}</b><p>{flowMode === "async" ? "검색 결과는 바로 돌려주고, 두 개의 작업 스레드가 뒤에서 저장해요." : "모든 저장이 끝난 다음 결과를 돌려줘요."}</p></div>
        </div>
      </section>

      <section className="section glossary-section" id="glossary">
        <SectionTitle number="07" label="쉬운 용어 사전" title="낯선 단어는 여기서 바로 풀어보세요" description="카드를 누르면 한 문장 설명이 열립니다. 외우기보다 위의 실험과 함께 다시 확인해 보세요." />
        <div className="glossary-grid">{glossary.map(([term, description], index) => <button key={term} className={openTerm === index ? "open" : ""} onClick={() => setOpenTerm(openTerm === index ? null : index)}><span><b>{term}</b><i>{openTerm === index ? "−" : "+"}</i></span>{openTerm === index && <p>{description}</p>}</button>)}</div>
      </section>

      <section className="closing-section">
        <p className="eyebrow">이제 실제 코드를 만나볼 차례예요</p>
        <h2>지도를 이해했다면,<br />한 파일씩 따라가 보세요.</h2>
        <p>Atlas의 모든 내용은 현재 NoHome 백엔드 코드와 Flyway 스키마에서 확인했습니다.</p>
        <div><a href="https://github.com/cmsik1/no-home/tree/main/Backend/src/main" target="_blank" rel="noreferrer">GitHub에서 백엔드 열기 ↗</a><a href="https://github.com/cmsik1/no-home/blob/main/Backend/src/main/resources/db/migration/V1__initial_schema.sql" target="_blank" rel="noreferrer">데이터베이스 설계 보기 ↗</a></div>
      </section>

      <footer><span>NoHome Backend Atlas</span><p>코드를 바꾸지 않고, 코드를 이해하기 위해 만든 학습 사이트</p><a href="#top">맨 위로 ↑</a></footer>
    </main>
  );
}

function SectionTitle({ number, label, title, description, light = false }: { number: string; label: string; title: string; description: string; light?: boolean }) {
  return <div className={`section-title ${light ? "light" : ""}`}><div><span>{number} / {label}</span><h2>{title}</h2></div><p>{description}</p></div>;
}

function PipelineVisual({ step, type, imported, skipped, total }: { step: number; type: "sale" | "rent"; imported: number; skipped: number; total: number }) {
  const sale = { aptNm: "한빛 아파트", amount: "120,000만원", date: "2026-07-15" };
  const rent = { aptNm: "푸른 아파트", amount: "보증금 30,000 / 월 120", date: "2026-07-09" };
  const sample = type === "sale" ? sale : rent;
  return <div className={`pipeline-visual visual-step-${step}`}>
    {step === 0 && <><div className="source-cloud">공공데이터 API</div><div className="xml-sheet"><code>&lt;aptNm&gt;{sample.aptNm}&lt;/aptNm&gt;</code><code>&lt;amount&gt;{sample.amount}&lt;/amount&gt;</code><code>&lt;date&gt;{sample.date}&lt;/date&gt;</code></div></>}
    {step === 1 && <div className="parser-view"><div className="xml-mini">XML</div><span>→</span><div className="object-mini">Java 객체</div></div>}
    {step === 2 && <div className="normalize-view"><div><small>이름</small><b>{sample.aptNm}</b></div><div><small>거래 종류</small><b>{type === "sale" ? "매매" : "월세"}</b></div><div><small>날짜</small><b>{sample.date}</b></div></div>}
    {step === 3 && <div className="hash-view"><span>거래 원본</span><i>→</i><code>9f6df8c3…e8a1</code><small>같은 지문이면 중복이에요</small></div>}
    {step === 4 && <div className="save-view"><div className="save-stack"><span>지역</span><span>주택</span><span>거래</span></div><div className="save-count"><b>{imported}</b><span>저장</span></div><div className="skip-count"><b>{skipped}</b><span>중복</span></div></div>}
    {step === 5 && <div className="search-result"><span className="result-type">{type === "sale" ? "매매" : "월세"}</span><h4>{sample.aptNm}</h4><p>서울특별시 강남구 · {sample.date}</p><b>{sample.amount}</b><small>전체 {total}건 중 예시 1건</small></div>}
  </div>;
}
