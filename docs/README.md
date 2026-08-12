# NoHome 문서 안내

이 디렉터리는 현재 프로젝트를 설명하는 기준 문서와 과거 산출물을 구분해 관리한다. 처음 프로젝트를 살펴본다면 아래의 현재 문서부터 읽고, 과거 의사결정이나 발표 자료가 필요할 때만 Archive를 참고한다.

## 현재 문서

- [프로젝트 실행 및 요청 처리 파이프라인](project-pipeline.md): 서버 기동, 사용자 요청, 데이터 변환과 백엔드 면접 대비 지식
- [리팩터링 아키텍처](refactoring-architecture.md): 현재 Frontend와 Backend의 계층, 책임과 설계 근거
- [AI 협업 개발 워크플로](ai-development-workflow.md): Jira, 작업 문서, Git을 연결하는 작업 방식
- [프로젝트 개선 마일스톤](project-improvement-milestones.md): 구조 정리부터 전체 검증까지의 진행 단계
- [작업 기록](work/): Jira 티켓별 계획과 구현·검증 결과

## Archive

- [Archive 안내](archive/README.md)
- [기존 Artifact 자료](archive/artifact/): 기획, 다이어그램, 발표와 AI 작업 기록
- [기존 Backend 문서](archive/backend/): 과거 Backend 작업 보고서, 이슈와 트러블슈팅 기록

Archive는 작성 당시의 코드와 경로를 전제로 한 역사 자료다. 현재 동작이나 실행 방법은 루트 [README](../README.md)와 위의 현재 문서를 기준으로 판단한다.

## 링크 검사

현재 문서의 로컬 Markdown·이미지 링크는 저장소 루트에서 다음 명령으로 검사한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-markdown-links.ps1
```

Archive 내부 문서는 원문 보존 대상이므로 기본 검사에서 제외한다. 필요하면 `-IncludeArchive` 옵션으로 과거 경로의 상태를 별도 점검할 수 있다.
