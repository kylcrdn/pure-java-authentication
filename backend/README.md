# Backend setup

The backend requires a MySQL database and the following environment variables:

- `DB_URL`: JDBC URL for the database, for example `jdbc:mysql://localhost:3306/your_database_name`
- `DB_USER`: MySQL account used by the backend
- `DB_PASSWORD`: password for that MySQL account

For local development, copy `.env.example` to `.env` in this directory and replace the example values with your local settings. `.env` is ignored by Git; do not commit real credentials. The included IntelliJ run configuration loads this file. When running outside IntelliJ, configure the same three environment variables in the process environment.

The database must contain a `users` table with `id`, `email`, and `password_hash` columns before registration is used.
