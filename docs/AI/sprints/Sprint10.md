# Sprint 10: Folder-Based Backend Frontend Artifact Split

## Manager Contract

- Goal: Reorganize the current single repository into three ordinary folders under the same Git repository: `Backend`, `Frontend`, and `Artifact`.
- Scope:
  - Move Spring Boot, Maven, DB, MyBatis, tests, Docker Compose, and backend environment example files into `Backend/`.
  - Move the Vue/Vite source from `frontend/` into `Frontend/`.
  - Move project documents and assignment artifacts into `Artifact/`.
  - Remove Spring Boot static Vue build artifacts from the backend source.
  - Switch local development to Backend API server plus Frontend Vite dev server with `/api` proxy to `http://localhost:8080`.
- Out of scope:
  - Git submodules.
  - Separate remote repositories.
  - Git history preservation.
  - Backend CORS changes unless the proxy approach fails.
- Completion criteria:
  - Repository root contains ordinary `Backend/`, `Frontend/`, and `Artifact/` folders.
  - `Frontend/package.json` no longer has `build:backend`.
  - `Frontend/vite.config.js` proxies `/api` to `http://localhost:8080`.
  - Backend tests pass from `Backend/`.
  - Frontend tests and production build pass from `Frontend/`.

## Generator Notes

- Moved backend files under `Backend/`.
- Moved frontend files under `Frontend/`.
- Moved documents and assignment artifacts under `Artifact/`.
- Removed the frontend static build files from backend resources.
- Added Vite `/api` proxy to `http://localhost:8080`.
- Added per-folder README files for Backend, Frontend, and Artifact.
- Verification:
  - `Backend`: `.\mvnw.cmd test` passed, 42 tests.
  - `Frontend`: `npm run test:auto-import` passed, 3 tests.
  - `Frontend`: `npm run build` passed.
- Residual local cleanup note:
  - Root `.mvn/wrapper/maven-wrapper.jar` is still physically present because another local process has the file locked. The backend copy exists at `Backend/.mvn/wrapper/maven-wrapper.jar`.

## Reviewer Checklist

- Confirm there is no `.gitmodules`.
- Confirm no root README is introduced.
- Confirm `Backend/src/main/resources/static` no longer contains Vue build artifacts.
- Confirm backend and frontend commands work from their new directories.
