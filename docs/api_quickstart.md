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

Tasks
```bash
# Create task (requires a projectId)
PROJECT_ID=$(jq -r .id project.json)
curl -s $base/api/tasks \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"projectId":"'"$PROJECT_ID"'","name":"Sample Task"}' | tee task.json

# List tasks (filter by project and/or status)
curl -s "$base/api/tasks?projectId=$PROJECT_ID&status=OPEN&size=10" \
  -H "Authorization: Bearer $ACCESS" | jq '.content | length'

# Update task (status values commonly used by UI: NEW/OPEN/CLOSED)
TASK_ID=$(jq -r .id task.json)
curl -s -X PUT $base/api/tasks/$TASK_ID \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"name":"Sample Task","status":"OPEN"}' | jq .status

# Delete task
curl -i -X DELETE $base/api/tasks/$TASK_ID -H "Authorization: Bearer $ACCESS" | head -n 1
```

Time Entries
```bash
# Create a time entry (ISO8601 timestamps)
START=$(date -u -v-1H "+%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%SZ)
END=$(date -u "+%Y-%m-%dT%H:%M:%SZ")
USER_ID=$(jq -r '.accessToken|split(".")[1]|@base64d|fromjson|.sub' resp.json)

curl -s $base/api/time-entries \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"taskId":"'"$TASK_ID"'","userId":"'"$USER_ID"'","startedAt":"'"$START"'","endedAt":"'"$END"'","notes":"demo"}' | tee te.json

# List time entries (by task or user and time range)
curl -s "$base/api/time-entries?taskId=$TASK_ID&size=10&sort=startedAt&order=desc" \
  -H "Authorization: Bearer $ACCESS" | jq '.content[0]'

# Update time entry
TE_ID=$(jq -r .id te.json)
curl -s -X PUT $base/api/time-entries/$TE_ID \
  -H "Authorization: Bearer $ACCESS" -H 'Content-Type: application/json' \
  -d '{"startedAt":"'"$START"'","endedAt":"'"$END"'","notes":"demo-updated"}' | jq .notes

# Delete time entry
curl -i -X DELETE $base/api/time-entries/$TE_ID -H "Authorization: Bearer $ACCESS" | head -n 1
```

Reports
```bash
# Weekly time report (JSON)
curl -s "$base/api/reports/time?period=week" -H "Authorization: Bearer $ACCESS" | jq '.[0]'

# Weekly time report (CSV)
curl -s "$base/api/reports/time?period=week&format=csv" -H "Authorization: Bearer $ACCESS"
```

Notes
- `Task.status` is not strictly validated by the API; the UI recognizes `NEW`, `OPEN`, and `CLOSED`.
- Timestamps must be ISO8601 (UTC recommended). The API treats the provided window as inclusive.
- For cross-tenant isolation Supabase RLS relies on the session GUC `app.tenant_id`; the backend sets it per-connection.
