# TenantForge — Status

Overview
- Branch: main
- Scope: Chapter 3 (Multitenancy, Domain, Reports, Docs, CI/CD, Deliverables)

Progress
- 3.4 Multitenancy isolation: done
- 3.5 Domain (Projects/Tasks/Time): done
- 3.6 Reports & Export: all done (views/API and performance evidence captured)
- 3.7 API Docs & Observability: done (OpenAPI UI, Actuator, Prometheus)
- 3.8 Tests & CI/CD: pipelines stable; PR diff-coverage ≥90% enforced
- 3.9 Deliverables: Postman, demo script, release notes draft added
- 3.10 Close-out: status assets generated; changelog added; final audit pending

Artifacts
- Coverage: backend/target/site/jacoco/jacoco.xml
- Performance evidence (on PG IT run): backend/target/artifacts/
- Postman: docs/postman/TenantForge.postman_collection.json
- Demo: docs/demo_script.md

CI Workflows
- Main CI: .github/workflows/ci.yml (unit tests + jacoco artifact)
- PG IT (manual): .github/workflows/pg-it.yml (uploads evidence)
- Coverage (manual): .github/workflows/coverage.yml (global + diff coverage)

Diff Coverage Gating
- On PRs, CI enforces diff coverage ≥90% by default and uploads HTML/JSON/TXT artifacts for review.

Next Actions
- Optional: expand E2E with real JWT and cross-tenant negative paths.
- Finalize demo recording and close final audit checklist.
