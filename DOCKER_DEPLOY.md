# GSTech Docker deployment

Repository layout expected by this setup:

```text
.
├── docker-compose.yml
├── .env
├── database/
│   └── Schema.sql
└── backend/
    ├── Dockerfile
    ├── .dockerignore
    ├── package.json
    └── src/
```

## First deployment on a fresh test server

```bash
cp .env.example .env
nano .env

docker compose up -d --build
```

## Verify

```bash
docker compose ps

docker compose logs -f backend

docker exec -it gstech-mysql mysql -u root -p -e "USE gestion_stagiaires; SHOW TABLES;"
```

The schema is mounted at `/docker-entrypoint-initdb.d/01-schema.sql` and is automatically executed when the MySQL volume is initialized for the first time.

## Reinitialize database (TEST ONLY - destroys database volume)

```bash
docker compose down -v
docker compose up -d --build
```
