# NOHOME-17 구현 계획

## 목표

Windows 로컬과 GitHub Ubuntu에서 재사용할 수 있는 배포 사전 검사 진입점을 만든다.

## 범위

- 필수·선택 환경변수 예시 계약
- Render Blueprint의 실행 방식, health check, 수동 배포와 Dashboard 비밀값 계약
- 안전한 임시 환경변수를 사용한 Docker Compose 구문 검사
- 기존 Vercel Rewrite 테스트와 현재 문서 링크 검사 연결
- Backend·Frontend 운영 파일의 알려진 비밀값 형식과 실제 클라우드 host 검사

Node 24 전환, 의존성 업데이트, GitHub Actions와 전체 컨테이너 기동은 후속 작업에서 진행한다.

## 구현 순서

1. 운영 파일만 대상으로 하는 비밀값·배포 host 검사기를 추가한다.
2. 환경변수·Render·Compose·Vercel·문서 검사를 하나의 사전 검사 명령으로 연결한다.
3. Compose가 기본적으로 기존 `.env`를 사용하되 검사에서는 임시 env 경로를 주입할 수 있게 한다.
4. 정상 저장소와 의도적으로 잘못된 임시 fixture를 검사한다.

## 인터페이스

- 새 명령: `pwsh -NoProfile -File scripts/check-deployment-preflight.ps1`
- 선택적 Compose 변수: `NOHOME_ENV_FILE`; 미지정 시 기존과 같이 `.env` 사용
- 애플리케이션 API와 DB schema 변경 없음

## 완료 기준

- Windows PowerShell 호환 문법과 GitHub `pwsh`에서 실행 가능한 문법을 사용한다.
- 정상 저장소의 모든 사전 검사가 통과한다.
- 누락된 계약과 알려진 비밀값·운영 host 패턴은 비밀값 자체를 출력하지 않고 실패한다.
- 실제 `.env`, 외부 API Key와 실행 중인 컨테이너를 사용하지 않는다.
- 검사 후 임시 파일이 남지 않는다.

## 자동 검증

- PowerShell 구문 분석
- 전체 사전 검사 실행
- 임시 fixture의 비밀값 및 운영 host 검출
- `docker compose config --quiet`
- Vercel 설정 테스트
- 현재 Markdown 링크 검사
- `git diff --check`

## 검증 결과

- Windows 전체 사전 검사: 통과
- Vercel 배포 설정 테스트 4개: 통과
- 운영 파일 221개 비밀값·배포 host 검사: 통과
- 현재 문서 로컬 링크 47개: 통과
- Ubuntu PowerShell 호환 검사: 통과
- 위험 fixture 2건 탐지 및 값 비노출 검사: 통과
- `git diff --check`: 통과

사용자가 결과를 승인했다. 실제 GitHub Actions 실행과 Node 24·의존성 변경은 이번 작업에 포함하지 않았다.
