# TenantForge — Status

Overview
- Branch: feat/backend-scaffold
- Scope: Chapter 3 (Multitenancy, Domain, Reports, Docs, CI/CD, Deliverables)

Progress
- 3.4 Multitenancy isolation: done
- 3.5 Domain (Projects/Tasks/Time): done
- 3.6 Reports & Export: SQL views/API done; performance evidence pending (manual PG IT)
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

Next Actions
- Trigger PG IT workflow to collect EXPLAIN/view evidence and mark 3.6 verification complete.
- Finalize 3.9 remaining items (readme/diagrams) and 3.10 final audit.

