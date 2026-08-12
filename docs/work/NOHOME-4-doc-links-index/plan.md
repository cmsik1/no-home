# NOHOME-4 세부 계획

## 범위

- 루트 README의 프로젝트 구조를 현재 디렉터리에 맞게 수정한다.
- `docs/README.md`와 `docs/archive/README.md`를 문서 진입점으로 만든다.
- 현재 문서에 남은 제거된 경로와 깨진 로컬 링크를 수정한다.
- 반복 가능한 Markdown·이미지 링크 검사 도구를 추가한다.

## 제외 범위

- 역사 기록인 `docs/archive` 문서 본문은 변경하지 않는다.
- Backend와 Frontend 운영 소스는 변경하지 않는다.

## 검증

- 현재 문서의 모든 로컬 Markdown·이미지 링크가 실제 파일 또는 디렉터리를 가리킨다.
- Archive 밖에 현재 경로로 오인될 `Artifact`와 `Backend/docs` 참조가 남지 않는다.
- `git diff --check`가 통과한다.

## 완료 기준

- [x] 루트 README와 `docs/README.md`에서 현재 문서 및 Archive를 탐색할 수 있다.
- [x] 현재 문서를 대상으로 한 자동 링크 검사가 통과한다.
- [x] 운영 소스 변경이 없다.
