# GSTech Object Storage

The current server structure keeps MySQL for metadata and uses S3-compatible object storage (MinIO) for binary files.

## Stored objects
- `documents/...` — Gestionnaire -> Directeur PDFs
- `justifications/...` — Stagiaire -> Formateur attachments
- `generated-documents/...` — generated Stagiaire document requests
- `profiles/<user-id>.<ext>` — profile images

## Docker
1. Copy `.env.example` to `.env` and set strong values for `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `JWT_SECRET`, `STORAGE_ACCESS_KEY`, and `STORAGE_SECRET_KEY`.
2. Start with `docker compose up -d --build`.
3. MinIO API is internal to Docker at `http://object-storage:9000`. The console is exposed on port 9001 by default.
4. The NestJS API is the only component the Android app should call. Do not put MinIO credentials in Android.

## Existing database
If MySQL already has the schema, run `database/migrations/001_object_storage.sql` once.

## Existing local uploads
When switching to `STORAGE_DRIVER=s3`, old filesystem paths remain readable when `STORAGE_FALLBACK_LOCAL=true`. New files are stored in MinIO. Migrate old files to object storage before deleting the old uploads volume.

## Environment without Docker
Use `STORAGE_DRIVER=s3`, set `STORAGE_ENDPOINT` to a reachable MinIO/S3 endpoint, and start NestJS normally.
