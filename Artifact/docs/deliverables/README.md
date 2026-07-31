# NoHome 제출 산출물

요구사항 명세서와 WBS·간트 차트 모음입니다. Markdown(버전관리·미리보기)과 Excel(제출·공유) 두 형식으로 제공합니다.

## 문서 목록

| 파일 | 내용 |
| --- | --- |
| [requirements-spec.md](requirements-spec.md) | 요구사항 명세서(SRS) — 개요·시스템·기능(필수+추가)·비기능·화면·API·데이터·외부I/F |
| [wbs-gantt.md](wbs-gantt.md) | WBS 계층 표 + Mermaid 간트(담당자 스윔레인) |
| [ai-usage-report.md](ai-usage-report.md) | AI 사용 보고서 — 기능·재설계 의사결정·프롬프트·Tool calling·운영 정책 |
| `no-home-deliverables.xlsx` | 통합 Excel 4시트: ①요구사항명세 ②WBS ③간트(담당자 색 막대) ④담당·일정 |

## 관련 자료
- 시스템 다이어그램: [클래스](../diagrams/class-diagrams.md) · [ERD](../diagrams/erd.md) · [유스케이스](../diagrams/usecase-diagrams.md) — 본 명세서의 데이터·기능·액터 정의와 정합
- 발표자료: [presentation/](../presentation/README.md)

## 핵심 기준

- **일정**: git 커밋 실측 **2026-06-12 ~ 06-24** (문서 Sprint 표기 5/29~6/14는 참조로 병기)
- **담당 3인**: 이정헌(AI 어시스턴트) · 최민식(공공데이터·검색) · 전효준(회원·인증·지도)
- 요구사항 ID는 PRD(F701~F720) + 구현 추가기능(F-AI·F-RENT·F-LIVE·F-INT·F-ADM·F-PWD)

## 재생성

Excel은 `_build_xlsx.py`(openpyxl)로 생성합니다. md 데이터 수정 시 스크립트의 해당 리스트도 갱신 후 재실행하세요.

```bash
python _build_xlsx.py
```

> 주: Windows 환경에서는 LibreOffice 소켓 재계산이 불가하여, 단일 합계 셀은 값으로 저장했습니다(정적 보고서). 수식 오류 0건 확인.
