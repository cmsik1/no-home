# NOHOME-4 문서 링크와 인덱스 정리

## 목적

문서 통합 이후 처음 프로젝트를 보는 사람이 루트 README와 문서 인덱스에서 현재 문서와 과거 기록을 구분해 탐색할 수 있게 한다.

## 변경 결과

- 루트 README의 모노레포 구조를 `Backend`, `Frontend`, `docs` 기준으로 갱신했다.
- `docs/README.md`에서 현재 문서, Jira 작업 기록과 Archive를 탐색할 수 있게 했다.
- `docs/archive/README.md`에 역사 자료의 성격과 현재 기준 문서의 위치를 명시했다.
- 현재 문서 링크를 반복 검사하는 `scripts/check-markdown-links.ps1`을 추가했다.

## 검증 결과

- 현재 문서의 로컬 Markdown·이미지 링크 10개가 모두 유효하다.
- Archive 밖에서 제거된 이전 문서 경로를 가리키는 링크는 0개다.
- Archive의 기존 문서 본문은 변경하지 않았다. 별도 검사에서 확인된 과거 링크 39개는 원문 보존 정책에 따라 현재 문서의 합격 기준에서 제외한다.
- Backend와 Frontend 운영 소스 변경은 0개다.
- `git diff --check`가 통과했다.
