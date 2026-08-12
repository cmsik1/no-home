# NOHOME-10 세부 계획

## 완료 기준

- [x] Backend 전체 test와 package가 통과한다.
- [x] Frontend 전체 test와 production build가 통과한다.
- [x] 삭제·이동 대상과 현재 문서 링크 검사가 통과한다.
- [x] `.env.example` 기반 Docker Compose 구성 검사가 통과한다.
- [x] API·schema·생성 파일 무변경과 사용자 `.gitignore` 보존을 확인한다.
- [x] 전체 결과와 남은 위험을 현재 문서에 기록한다.

## 검증 명령

- `mvn test`
- `mvn -DskipTests package`
- `npm test`
- `npm run build`
- `docker compose config --quiet`
- `powershell -File scripts/check-markdown-links.ps1`
- `git diff --check`
