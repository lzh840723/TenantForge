# PG IT Evidence

Workflow
- GitHub Actions → PG Integration Evidence (`.github/workflows/pg-it.yml`)
- Trigger: workflow_dispatch or push to `.github/pg-it.trigger`

Artifacts
- `pg-it-evidence`:
  - `backend/target/artifacts/explain-time-entries.txt`
  - `backend/target/artifacts/view-week.txt`

Local Run
```
RUN_PG_IT=true mvn -f backend/pom.xml -Djacoco.skip=true verify
```

