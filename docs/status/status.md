# TenantForge — Status

Overview
- Branch: feat/backend-scaffold
- Scope: Chapter 3 (Multitenancy, Domain, Reports, Docs, CI/CD, Deliverables)

Progress
- 3.4 Multitenancy isolation: done
- 3.5 Domain (Projects/Tasks/Time): done
- 3.6 Reports & Export: all done (views/API and performance evidence captured)
- 3.7 API Docs & Observability: done (OpenAPI UI, Actuator, Prometheus)
- 3.8 Tests & CI/CD: pipelines stable; coverage gates staged
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

Diff Coverage Gating (gradual)
- On PRs, CI always uploads a non-blocking diff coverage report (HTML/JSON/TXT).
- To enforce ≥90% on a specific PR, add label `cov-gate` to the PR.

Next Actions
- Optional: re-enable diff coverage gating once coverage stabilizes.
- Optional: expand E2E with real JWT and cross-tenant negative paths.
