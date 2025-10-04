# API Quickstart

Base URL
- Local: `http://localhost:8080`

Health & Docs
- Health: `curl -i {{base}}/api/health`
- OpenAPI: `curl -s {{base}}/v3/api-docs | jq .openapi`
- Swagger UI: `{{base}}/swagger-ui/index.html`

Register & Login
```bash
base=http://localhost:8080

# Register tenant + owner
curl -s $base/api/auth/register -H 'Content-Type: application/json' -d '{
  "tenantName":"demo-tenant",
  "email":"demo@tenantforge.dev",
  "password":"Password123!",
  "displayName":"Demo Owner"
}' | tee resp.json

ACCESS=$(jq -r .accessToken resp.json)

# Login (optional)
curl -s $base/api/auth/login -H 'Content-Type: application/json' -d '{
  "email":"demo@tenantforge.dev",
  "password":"Password123!"
}' | jq -r .accessToken
```

Projects
```bash
# Create project
curl -s $base/api/projects -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' -d '{
  "name":"Sample Project",
  "description":"demo"
}' | tee project.json

# List projects
curl -s "$base/api/projects?size=10" -H "Authorization: Bearer $ACCESS" | jq '.content | length'
```

Reports
```bash
# Weekly time report (JSON)
curl -s "$base/api/reports/time?period=week" -H "Authorization: Bearer $ACCESS" | jq '.[0]'

# Weekly time report (CSV)
curl -s "$base/api/reports/time?period=week&format=csv" -H "Authorization: Bearer $ACCESS"
```

