# Final Audit Checklist (TenantForge)

> Owner: Codex • Date: TBD • Scope: Backend API (Railway) + Docs

## Acceptance (User Journeys)
- [ ] Register tenant+owner (POST /api/auth/register) — Evidence: link to latest run in docs/evidence/api_demo/
- [ ] Login (POST /api/auth/login) — Evidence: api_demo sanitized output
- [ ] Create project (POST /api/projects) — Evidence: api_demo/project_created.json
- [ ] List projects (GET /api/projects) — Evidence: api_demo/projects_list.json
- [ ] Weekly report (GET /api/reports/time?period=week) — Evidence: api_demo/report_week.json
- [ ] Cross-tenant negative path (403/404) — Evidence: add E2E/API test logs (TBD)

## Non-Functional
- [ ] Performance: P95 ≤ 300ms @ 10 concurrency — Evidence: k6/JMeter report or actuator metrics snapshot (TBD)
- [x] Availability: Health endpoint 200 OK — Evidence: api_demo/health.json
- [x] Observability: /actuator/prometheus exposes core metrics — Evidence: curl output snapshot (attach)
- [x] Security: JWT tamper/expiry rejected — Evidence: unit/integration tests (link)
- [x] Security: Cross-tenant access rejected — Evidence: integration tests (link)

## CI/CD & Coverage
- [x] PR diff coverage ≥ 90% gate enabled — Evidence: CI logs, diff-coverage artifacts
- [x] Global coverage gate ≥ 80% (profile) — Evidence: backend/target/site/jacoco/jacoco.xml
- [x] Artifacts archived — Evidence: Actions artifacts (unit-test-reports, perf-artifacts)

## Docs & Deliverables
- [x] README/API Quickstart/Architecture updated
- [x] Postman collection present
- [ ] Demo recording uploaded — Evidence: docs/demo/recording.md link
- [x] Status board updated (status.md/json)

## Sign-off
- Product: ____  •  Tech Lead: ____  •  Date: ____

