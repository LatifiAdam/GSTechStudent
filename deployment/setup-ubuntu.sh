#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is not installed. Install Docker Engine + Compose plugin first."
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose plugin is missing. Install docker-compose-plugin first."
  exit 1
fi

if [ ! -f .env ]; then
  cp .env.example .env
  chmod 600 .env
  echo "Created .env from .env.example. Edit passwords/secrets before continuing."
  exit 1
fi

if grep -q 'CHANGE_ME_' .env; then
  echo "ERROR: .env still contains CHANGE_ME_ placeholders. Edit .env first."
  exit 1
fi

echo "Building and starting GSTech..."
docker compose up -d --build

echo
echo "GSTech deployment started."
echo "Server IP(s):"
hostname -I || true
echo "API: http://<SERVER-IP>:${PORT:-3000}/api/v1/"
echo
docker compose ps
