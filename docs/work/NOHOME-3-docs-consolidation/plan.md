# NOHOME-3 구현 계획

## 목표

`Artifact`와 `Backend/docs`를 루트 `docs/archive` 아래로 이동하여 문서 위치를 통합한다.

## 대상 구조

```text
docs/
  archive/
    artifact/
    backend/
  work/
  현재 기준 문서
```

## 범위

- `Artifact`의 89개 파일을 `docs/archive/artifact`로 이동
- `Backend/docs`의 44개 파일을 `docs/archive/backend`로 이동
- 한글 Markdown 파일명 8개 영문화
- 과거 `AGENTS.md`를 `AGENTS.legacy.md`로 변경

## 제외 범위

- 문서 본문 수정
- Markdown 및 이미지 링크 수정
- 이미지, PDF, XLSX 등 비 Markdown 파일명 변경
- Backend와 Frontend 운영 소스 변경
- 현재 루트 `docs` 문서 변경

## 검증

- 이동 전후 133개 파일의 SHA-256 비교
- 이전 디렉터리 부재 확인
- Markdown 파일명 정책 검사
- Backend와 Frontend 운영 소스 무변경 확인
- `git diff --check`

## 완료 결과

- 이동 전후 파일 수: 133개
- SHA-256 불일치: 0개
- PDF 3개와 XLSX 1개를 실제 파서로 열어 정상 구조를 확인했다.
- `Artifact`와 `Backend/docs`의 부재를 확인했다.
- Backend와 Frontend 운영 소스 변경이 없고 `git diff --check`가 통과했다.

## 다음 작업

이동으로 바뀐 문서 경로와 기존 깨진 링크는 NOHOME-4에서 정리한다.
