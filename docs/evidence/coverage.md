# Coverage

- Global coverage gate: enforced in `coverage.yml` workflow (`-Pcoverage-check`).
- Diff coverage: non-blocking report on PRs; enforce ≥90% by adding PR label `cov-gate`.

Artifacts
- `jacoco-xml-backend` → `backend/target/site/jacoco/jacoco.xml`
- `diff-coverage-report` → `diff-coverage.html / .json / .txt`

Local Commands
```
mvn -f backend/pom.xml -Pcoverage-check clean verify
```

