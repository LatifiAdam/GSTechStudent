# GSTech — Ubuntu Server Deployment

## 1. Prepare Ubuntu Server

Install Ubuntu Server directly on the physical server. Give it a stable LAN IP (DHCP reservation is recommended).

Install Git and Docker Engine with the official Docker instructions for your Ubuntu release, including the Docker Compose plugin.

Verify:

```bash
docker --version
docker compose version
git --version
```

## 2. Get the project

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL> GSTech
cd GSTech
```

## 3. Configure secrets

```bash
cp .env.example .env
chmod 600 .env
nano .env
```

Replace every `CHANGE_ME_*` value with real secrets/passwords.

## 4. Start the stack

```bash
./deployment/setup-ubuntu.sh
```

The first MySQL startup initializes the database from `database/Schema.sql`. The database is persisted in the `mysql_data` Docker volume.

## 5. Check the services

```bash
docker compose ps
docker compose logs -f backend
```

The API is exposed on port `3000` by default:

```text
http://SERVER_LAN_IP:3000/api/v1/
```

## 6. Android LAN testing

Build the debug app with a LAN API URL without changing the tracked project files:

```bash
./gradlew assembleDebug -PGSTech_API_BASE_URL=http://SERVER_LAN_IP:3000/api/v1/
```

The default release URL is `https://api.gstech.ma/api/v1/`.

## 7. Production domain / HTTPS

Create DNS for `api.gstech.ma` pointing to the public IP of the network that exposes the server. Put an HTTPS reverse proxy (for example, Caddy or Nginx) in front of the NestJS container, terminate TLS there, and proxy to `backend:3000` on the Docker network.

Do not expose MySQL to the public Internet.

## 8. Update

```bash
./deployment/update.sh
```

## 9. Stop / start

```bash
./deployment/stop.sh
./deployment/start.sh
```

## 10. Database backup

```bash
./deployment/backup-database.sh
```

Backups are written to `backups/`, which is ignored by Git.
