#!/usr/bin/env bash
set -euo pipefail
cd "$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

git pull --ff-only origin main
docker compose up -d --build

docker compose ps
