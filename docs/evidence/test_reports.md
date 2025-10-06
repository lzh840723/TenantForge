# Test Reports

Artifacts (download from GitHub Actions for latest runs)
- Unit test reports: artifact `unit-test-reports` (surefire/failsafe XML)
- Diff coverage report: artifact `diff-coverage-report` (HTML/JSON/TXT)
- JaCoCo coverage XML: artifact `jacoco-xml-backend` (path: `backend/target/site/jacoco/jacoco.xml`)

Local paths (when running locally)
- `backend/target/surefire-reports/`
- `backend/target/failsafe-reports/` (only if ITs were executed)
- `backend/target/site/jacoco/jacoco.xml`

How to regenerate locally
```
# Unit tests only
mvn -f backend/pom.xml -DskipITs=true clean test

# Coverage with gates
mvn -f backend/pom.xml -Pcoverage-check clean verify
```

