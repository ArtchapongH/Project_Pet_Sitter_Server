# Deploying the backend on Render

Create a Render Web Service using the Docker runtime and this repository. Render builds the included `Dockerfile`; set the health check path to `/health`.

Set these environment variables in the service:

| Variable | Value |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL, for example `jdbc:postgresql://HOST:5432/DATABASE?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password |
| `SUPABASE_URL` | Supabase project URL; required to validate access tokens |
| `SUPABASE_API_KEY` | Server-side Supabase key used for Storage uploads |
| `SUPABASE_BUCKET` | Storage bucket name; defaults to `uploads` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins for direct browser calls, without paths or trailing slashes; leave empty when all browser API calls use the Vercel `/api` rewrite |
| `SPRING_JPA_DDL_AUTO` | Defaults to `update`; review before production schema changes |
| `SPRING_JPA_SHOW_SQL` | Defaults to `false` |
| `APP_LOG_LEVEL` | Defaults to `INFO` |

Use the PostgreSQL internal connection details when the database is also on Render in the same region. Render supplies the web service `PORT`; the application already binds to it on `0.0.0.0`.

The Docker build skips test execution because the current Spring context test requires a configured database and Supabase URL. Run the project checks separately in CI with a test configuration.
