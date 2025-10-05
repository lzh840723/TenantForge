# TenantForge Demo Script

This script demonstrates a minimal end-to-end flow with the API.

Prerequisites
- Backend is running and reachable at `{{baseUrl}}` (default `http://localhost:8080`).
- Postman or curl installed. Optionally, run the scripted flow: `BASE_URL={{baseUrl}} ./scripts/demo_run.sh` to capture evidence automatically under `docs/evidence/api_demo/`.

Steps
1) Health and OpenAPI
   - GET `{{baseUrl}}/api/health` should return 200 OK.
   - GET `{{baseUrl}}/v3/api-docs` returns OpenAPI JSON.

2) Register Tenant and Owner
   - POST `{{baseUrl}}/api/auth/register` with body:
     {"tenantName":"demo-tenant","email":"demo@tenantforge.dev","password":"Password123!","displayName":"Demo Owner"}
   - Response includes `accessToken` and `refreshToken`.

3) Login (optional if you saved tokens from register)
   - POST `{{baseUrl}}/api/auth/login` with body:
     {"email":"demo@tenantforge.dev","password":"Password123!"}
   - Capture `accessToken`.

4) Create Project
   - POST `{{baseUrl}}/api/projects` with Authorization: `Bearer <accessToken>` and body:
     {"name":"Sample Project","description":"demo"}

5) List Projects
   - GET `{{baseUrl}}/api/projects?size=10` with Authorization header to verify the created record.

6) Time Report (JSON)
   - GET `{{baseUrl}}/api/reports/time?period=week` with Authorization header.

Postman
- Import `docs/postman/TenantForge.postman_collection.json`.
- Set `baseUrl` to your server and put `accessToken` into collection variables after register/login.
