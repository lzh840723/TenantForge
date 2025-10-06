# Demo Recording

Placeholder for demo video link (to be uploaded to your preferred storage):
- Link: <paste-here>
- Duration target: 3–5 minutes
- Contents: register → login → create project → list projects → weekly report (JSON/CSV)

How to produce quickly
1) Ensure backend domain (Railway): `railway domain -s "TenantForge" --environment production`
2) Set BASE_URL and run the scripted flow to capture artifacts:
   - `BASE_URL=https://<your-domain> ./scripts/demo_run.sh`
   - Outputs under `docs/evidence/api_demo/<timestamp>/`
3) Record with your screen tool while repeating the same steps (use Postman collection or curl).
4) Upload the video and paste the link above.
