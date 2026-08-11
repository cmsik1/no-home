# NoHome Backend Atlas

NoHome 백엔드를 초보자도 직접 조작하며 배울 수 있는 React 기반 인터랙티브 학습 사이트입니다.

## 할 수 있는 것

- 요청 흐름 블록을 드래그하거나 화살표로 옮겨 순서 맞추기
- 매매·월세, 거래 수, 중복률을 조절해 공공데이터 파이프라인 실행하기
- 25개 REST API를 쉬운 단어로 검색하고 설명 펼치기
- 8개 데이터베이스 테이블을 클릭해 컬럼과 연결 관계 탐색하기
- 동기·비동기 처리 방식을 전환해 응답 속도 차이 비교하기
- 낯선 백엔드 용어를 한 문장 설명으로 확인하기

모든 구조와 수치는 `Backend/src/main`과 Flyway 스키마에서 직접 확인했습니다. 백엔드 애플리케이션 코드는 변경하지 않습니다.

## 로컬 실행

```powershell
cd BackendAtlas
npm install
npm run dev
```

배포 빌드:

```powershell
npm run build
```

## 주요 파일

```text
BackendAtlas/
├── app/page.tsx       # 학습 콘텐츠와 모든 인터랙션
├── app/globals.css    # 반응형 화면, 애니메이션, 가독성
├── app/layout.tsx     # 메타데이터와 공유 이미지 설정
├── public/og.png      # 링크 공유용 대표 이미지
└── .openai/hosting.json
```
