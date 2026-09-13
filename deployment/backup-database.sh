#!/usr/bin/env bash
set -euo pipefail
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

mkdir -p backups
timestamp="$(date +%Y%m%d_%H%M%S)"

set -a
source .env
set +a

docker compose exec -T mysql sh -c 'exec mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' > "backups/gestion_stagiaires_${timestamp}.sql"

echo "Backup written to backups/gestion_stagiaires_${timestamp}.sql"
