# GSTech Student

Application de gestion des stagiaires développée avec **Android (Kotlin / Jetpack Compose)** pour l'application mobile et **NestJS + MySQL** pour le backend et la base de données.

L'application gère plusieurs rôles et leurs fonctionnalités associées : Super Admin, DF, SRIO, SCQ, Directeur, Gestionnaire, Formateur et Stagiaire.

---

## 1. Structure générale du projet

La structure actuelle du projet est organisée comme suit :

```text
GSTechStudent/
├── android/
│   └── GSTechStudent/
│       ├── app/
│       │   ├── build.gradle.kts
│       │   ├── proguard-rules.pro
│       │   └── src/
│       │       ├── debug/
│       │       │   └── AndroidManifest.xml
│       │       └── main/
│       │           ├── AndroidManifest.xml
│       │           ├── java/
│       │           │   └── com/gstech/student/
│       │           │       ├── data/
│       │           │       ├── model/
│       │           │       ├── ui/
│       │           │       └── util/
│       │           └── res/
│       │               ├── drawable/
│       │               ├── mipmap-anydpi-v26/
│       │               ├── values/
│       │               ├── values-night/
│       │               └── xml/
│       ├── build.gradle.kts
│       ├── gradle.properties
│       ├── gradle/
│       │   └── wrapper/
│       ├── settings.gradle.kts
│       └── local.properties
│
├── backend/
│   ├── database/
│   │   └── Schema.sql
│   ├── src/
│   │   ├── affectations/
│   │   ├── announcements/
│   │   ├── attendance/
│   │   ├── auth/
│   │   ├── classes/
│   │   ├── common/
│   │   ├── courses/
│   │   ├── document-requests/
│   │   ├── documents/
│   │   ├── entities/
│   │   ├── etablissements/
│   │   ├── grading/
│   │   ├── justifications/
│   │   ├── notifications/
│   │   ├── reports/
│   │   ├── schedule/
│   │   ├── users/
│   │   ├── app.controller.ts
│   │   ├── app.module.ts
│   │   ├── app.service.ts
│   │   ├── bootstrap.service.ts
│   │   └── main.ts
│   ├── Dockerfile
│   ├── package.json
│   ├── package-lock.json
│   ├── .env.example
│   └── tsconfig.json
│
├── docker-compose.yml
├── .env.example
├── SERVER_SETUP.md
├── TEST_REPORT_v2.1.md
├── CHANGELOG_BOOTSTRAP_AND_LAN.md
└── CHANGELOG_ROLE_MANAGEMENT_V2.2.md
```

> Pour afficher l'arborescence complète avec les chemins des fichiers depuis la racine du projet :
>
> ```bash
> tree -f
> ```
>
> Sous Windows, si `tree` est disponible :
>
> ```cmd
> tree /F
> ```

---

## 2. Composants principaux

### Application Android

Dossier :

```text
android/GSTechStudent/
```

Technologies principales :

- Kotlin
- Jetpack Compose
- Retrofit
- OkHttp
- Moshi
- Architecture Repository / ViewModel
- JWT pour l'authentification

Les différents écrans sont organisés principalement dans :

```text
android/GSTechStudent/app/src/main/java/com/gstech/student/ui/
```

Les appels API sont centralisés dans :

```text
android/GSTechStudent/app/src/main/java/com/gstech/student/data/remote/
```

Les repositories sont dans :

```text
android/GSTechStudent/app/src/main/java/com/gstech/student/data/repository/
```

---

## 3. Backend

Dossier :

```text
backend/
```

Technologies :

- Node.js
- NestJS
- TypeScript
- TypeORM
- MySQL
- JWT
- bcrypt

Le backend expose l'API sous :

```text
/api/v1
```

Dans `main.ts`, l'application NestJS écoute sur toutes les interfaces réseau :

```text
0.0.0.0:3000
```

L'API démarre donc par défaut sur :

```text
http://localhost:3000/api/v1
```

Pour un appareil connecté au même réseau local, utiliser l'adresse IP du serveur.

Exemple :

```text
http://192.168.1.13:3000/api/v1/
```

> L'adresse IP dépend de la machine qui héberge le backend.

---

## 4. Base de données

La base de données utilisée est :

```text
MySQL
```

Nom par défaut :

```text
gestion_stagiaires
```

Le schéma principal est :

```text
backend/database/Schema.sql
```

Le backend utilise les variables suivantes :

```env
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=
DB_NAME=gestion_stagiaires
```

Lorsque Docker est utilisé, le backend se connecte au service MySQL avec :

```text
DB_HOST=mysql
DB_PORT=3306
```

---

## 5. Configuration de l'environnement

Le fichier réel `.env` ne doit pas être envoyé sur GitHub.

Utiliser :

```text
backend/.env.example
```

comme modèle.

Exemple :

```env
NODE_ENV=development

PORT=3000

DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=YOUR_PASSWORD
DB_NAME=gestion_stagiaires

JWT_SECRET=YOUR_SECRET

CORS_ORIGIN=*

BOOTSTRAP_SUPERADMIN_EMAIL=bootstrap@gstech.ma
BOOTSTRAP_SUPERADMIN_PASSWORD=CHANGE_ME
BOOTSTRAP_SUPERADMIN_ACCESS_LEVEL=technical
```

---

## 6. Installation du backend

Entrer dans le dossier backend :

```bash
cd backend
```

Installer les dépendances :

```bash
npm ci
```

ou, si aucun `package-lock.json` n'est disponible :

```bash
npm install
```

Créer le fichier `.env` à partir de `.env.example`.

Puis compiler :

```bash
npm run build
```

---

## 7. Démarrage du backend

### Mode développement

```bash
npm run start:dev
```

### Mode normal

```bash
npm run start
```

### Mode production

```bash
npm run build
npm run start:prod
```

L'API est exposée sur :

```text
http://localhost:3000/api/v1
```

Le backend écoute sur :

```text
0.0.0.0:3000
```

ce qui permet aux autres appareils du réseau local d'accéder à l'API.

---

## 8. Configuration de l'application Android

L'application Android utilise une variable de build pour définir l'URL de l'API :

```text
GSTech_API_BASE_URL
```

Pour un test local avec un serveur ayant l'adresse :

```text
192.168.1.13
```

l'URL sera :

```text
http://192.168.1.13:3000/api/v1/
```

### Important

Depuis un téléphone physique, ne pas utiliser :

```text
http://localhost:3000/api/v1/
```

car `localhost` désigne le téléphone lui-même.

Utiliser l'adresse IP LAN du serveur :

```text
http://IP_DU_SERVEUR:3000/api/v1/
```

Exemple :

```text
http://192.168.1.13:3000/api/v1/
```

Le téléphone et le serveur doivent être accessibles sur le même réseau local ou disposer d'un routage approprié.

---

## 9. Test sur réseau local

Exemple :

```text
Routeur
   │
   ├── Serveur / VM
   │     └── 192.168.1.13
   │          └── NestJS :3000
   │
   └── Téléphone
         └── 192.168.1.x
```

Le téléphone doit pouvoir accéder à :

```text
http://192.168.1.13:3000
```

avant de tester l'application Android.

Si l'API n'est pas accessible :

1. Vérifier l'adresse IP du serveur.
2. Vérifier que NestJS écoute sur `0.0.0.0`.
3. Vérifier le pare-feu Windows.
4. Vérifier que le téléphone et le serveur sont sur le même réseau.
5. Vérifier le port `3000`.

---

## 10. Docker

Le projet contient :

```text
docker-compose.yml
```

Deux services principaux sont définis :

```text
mysql
backend
```

Le service MySQL utilise :

```text
MySQL 8.4
```

et expose le port :

```text
3307:3306
```

Le backend expose :

```text
3000:3000
```

### Démarrage Docker

Depuis la racine :

```bash
docker compose up -d
```

Vérifier les conteneurs :

```bash
docker compose ps
```

Voir les logs :

```bash
docker compose logs -f backend
```

Arrêter les services :

```bash
docker compose down
```

Les données MySQL sont conservées dans le volume Docker :

```text
mysql_data
```

---

## 11. Premier compte Super Admin

Lorsqu'une base de données neuve ne contient encore aucun utilisateur, le backend dispose d'un mécanisme de bootstrap pour créer un compte **Super Admin temporaire**.

Les paramètres sont configurables dans `.env` :

```env
BOOTSTRAP_SUPERADMIN_EMAIL=bootstrap@gstech.ma
BOOTSTRAP_SUPERADMIN_PASSWORD=CHANGE_ME
BOOTSTRAP_SUPERADMIN_ACCESS_LEVEL=technical
```

Le compte Bootstrap est destiné à l'initialisation du système.

Une fois le véritable Super Admin créé, le compte Bootstrap est désactivé/supprimé selon le workflow prévu par l'application.

---

## 12. Rôles principaux

La hiérarchie fonctionnelle actuelle est :

```text
Super Admin
     │
     ▼
DF
 ├── SRIO
 ├── SCQ
 └── ...
      │
      ▼
Directeur
      │
      ▼
Gestionnaire
      │
      ├── Stagiaires
      └── Groupes

Formateur

Stagiaire
```

Les rôles sont gérés dans le backend ainsi que dans la navigation Android.

---

## 13. API

Le préfixe global est :

```text
/api/v1
```

Exemple :

```text
POST /api/v1/auth/login
```

Autres domaines principaux :

```text
/api/v1/users
/api/v1/classes
/api/v1/courses
/api/v1/affectations
/api/v1/attendance
/api/v1/documents
/api/v1/document-requests
/api/v1/announcements
/api/v1/notifications
/api/v1/etablissements
/api/v1/reports
/api/v1/schedule
```

Pour les tests d'API, une collection Postman est présente dans :

```text
backend/Gestion_Stagiaires.postman_collection.json
```

ainsi que :

```text
backend/postman/Gestion_Stagiaires.postman_collection.json
```

---

## 14. Authentification

L'authentification utilise :

- JWT Access Token
- Refresh Token
- bcrypt pour le hachage des mots de passe
- possibilité d'utiliser le 2FA
- gestion des rôles et autorisations

Le token d'accès est utilisé par l'application Android pour les requêtes protégées.

---

## 15. Fichiers à ne pas versionner

Ne pas envoyer sur Git :

```text
.env
node_modules/
dist/
build/
.gradle/
.idea/
local.properties
uploads/
```

En particulier, ne jamais publier :

- mots de passe MySQL
- JWT secrets
- clés Firebase privées
- clés/signatures Android
- autres secrets de production

Le fichier recommandé pour partager la configuration est :

```text
.env.example
```

---

## 16. Ordre recommandé pour lancer le projet

### Avec MySQL local

```text
1. Démarrer MySQL
2. Créer/importer la base gestion_stagiaires
3. Configurer backend/.env
4. cd backend
5. npm ci
6. npm run start:dev
7. Configurer l'URL API Android
8. Lancer l'application Android
```

### Avec Docker

```text
1. Configurer les variables nécessaires
2. docker compose up -d
3. Vérifier docker compose ps
4. Vérifier les logs du backend
5. Configurer l'URL API Android
6. Lancer l'application Android
```

---

## 17. Développement Android

Ouvrir le dossier :

```text
android/GSTechStudent/
```

dans Android Studio.

La compilation et l'installation de l'application peuvent ensuite être effectuées depuis Android Studio ou avec Gradle.

---

## 18. Documentation complémentaire

Le projet contient également :

```text
SERVER_SETUP.md
TEST_REPORT_v2.1.md
CHANGELOG_BOOTSTRAP_AND_LAN.md
CHANGELOG_ROLE_MANAGEMENT_V2.2.md
backend/README.md
backend/README_SERVER_TESTS.md
backend/BUILD_FIX_NOTES.md
```

Ces fichiers contiennent des informations supplémentaires concernant le déploiement, les tests, le bootstrap Super Admin et les corrections du backend.

---

## 19. Résumé rapide

```text
Android App
    │
    │ HTTP / JSON
    ▼
NestJS API
    │
    │ TypeORM
    ▼
MySQL
```

### Développement local

```text
Android
   ↓
http://localhost:3000/api/v1/
   ↓
NestJS
   ↓
MySQL
```

### Test avec téléphone sur réseau local

```text
Téléphone
   ↓
Wi-Fi / LAN
   ↓
Serveur
   ↓
http://IP_DU_SERVEUR:3000/api/v1/
   ↓
NestJS
   ↓
MySQL
```

### Production

Pour un déploiement public, utiliser de préférence :

```text
https://api.gstech.ma/api/v1/
```

avec HTTPS et une configuration serveur sécurisée.