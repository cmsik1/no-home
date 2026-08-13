# NOHOME-17 로컬 사전 검사 표준화

배포 설정을 변경한 뒤 실제 클라우드 리소스를 만들기 전에 저장소에서 계약 위반을 찾는 작업이다.

`scripts/check-deployment-preflight.ps1` 하나로 환경변수 예시, Render와 Vercel 설정, Docker Compose 구문, 현재 문서 링크, 운영 파일의 비밀값과 실제 클라우드 주소를 검사한다. Compose 검사는 사용자의 `.env`를 읽거나 덮어쓰지 않고 시스템 임시 파일의 안전한 값만 사용한다.

```powershell
pwsh -NoProfile -File scripts/check-deployment-preflight.ps1
```

Docker CLI와 Node.js가 PATH에 있어야 한다. Docker Compose 구문 검사에는 실행 중인 컨테이너나 외부 API Key가 필요하지 않다.

## 완료 결과

- Windows에서는 실제 Docker Compose와 Node.js를 사용한 전체 사전 검사가 통과했다.
- Ubuntu PowerShell 컨테이너에서는 경로·dotfile·인코딩 호환 검사가 통과했다.
- 임시 위험 fixture에서 비밀값과 실제 배포 host를 탐지하면서 해당 값을 로그에 노출하지 않음을 확인했다.
- Node 24 전환, 의존성 보안 기준과 GitHub Actions 연결은 후속 작업 `NOHOME-18~21`에 남겼다.
