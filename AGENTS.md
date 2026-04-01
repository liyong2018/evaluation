# Repository Guidelines

## Project Structure & Module Organization
- Backend (Spring Boot, Maven): `src/main/java/com/evaluate/...` organized by `controller`, `service`, `mapper`, `dto`, `entity`, `util`; resources in `src/main/resources` (profiles: `application.yml`, `application-test.yml`, `application-h2.yml`). Tests live in `src/test/java` (see `.../integration` for integration tests).
- SQL utilities and migrations: `sql/` (e.g., `diagnose_comprehensive_model.sql`, `fix_*`).
- Frontend (Vite + Vue 3 + TS): `frontend/` with `src/`, `public/`, `scripts/`.
- Docs and assets: `docs/`, `frontend/public/`. Build artifacts: `target/`.

## Build, Test, and Development Commands
- Backend
  - `mvn clean verify` — compile and run tests.
  - `mvn -DskipTests package` — build JAR to `target/`.
  - `mvn spring-boot:run` — run the app in dev; or `java -jar target/disaster-reduction-evaluation-1.0.0.jar` after packaging.
- Frontend (Node 20+)
  - `cd frontend && npm ci` — install deps.
  - `npm run dev` — Vite dev server.
  - `npm run build` — production build to `frontend/dist/`.
  - `npm run lint`, `npm run type-check` — lint and TS check.
- Database
  - Prefer H2 locally via `application-h2.yml`. Use scripts in `sql/` for diagnostics/fixes.

## Coding Style & Naming Conventions
- Java 8, UTF-8, 4-space indent. Packages `com.evaluate.*` (lowercase); classes `PascalCase`; methods/fields `camelCase`; constants `UPPER_SNAKE_CASE`.
- Keep controllers thin; put domain logic in services; use MyBatis-Plus mappers for persistence. Use Lombok for boilerplate.
- Frontend: TypeScript with ESLint (`frontend/eslint.config.ts`); components `PascalCase.vue`; prefer named exports.

## Testing Guidelines
- Framework: Spring Boot Test + JUnit. Name tests `*Test.java` mirroring package structure.
- Unit tests in `src/test/java/com/evaluate/...`; integration tests in `.../integration` with `@SpringBootTest` and the test or H2 profile.
- Run all tests: `mvn test`. For UI changes, provide screenshots or manual steps using `npm run dev`.

## Commit & Pull Request Guidelines
- Conventional prefixes: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:` (e.g., `fix(service): handle null inputs`). Keep titles ≤ 72 chars.
- PRs must include: clear description, linked issues, testing evidence (logs/screenshots), and notes for any `sql/` changes.

## Security & Configuration Tips
- Do not commit secrets. Use env vars or local overrides. Select profiles via Spring `--spring.profiles.active=test|h2`.
