# GSTechStudent — Security + Branding update

Base: GSTechStudent v 20-09-2026, preserving the latest mobile branding changes.

## Mobile branding
- Login logo: `logo v5`
- Login typography/branding: refreshed GSTechStudent presentation
- Splash artwork: `Student-core_5 px3`
- Splash animation: subtle breathing scale animation
- Exact Library PNG assets copied into Android drawable resources

## Backend dependency/security changes
- NestJS remains on 11.x
- multer override: 2.4.0
- exceljs: 4.4.0
- bcrypt: 6.0.0
- nodemailer: 10.0.9
- uuid: 11.1.1
- targeted uuid override for gaxios
- targeted overrides for fast-uri, js-yaml and qs
- conservative multipart upload limits on document, justification and profile-image endpoints
- removed the incompatible `fieldArrayIndexLimit` TypeScript property
- `tsconfig.json` explicitly sets `rootDir` to `./src`
- stale `package-lock.json` removed; run `npm install` to regenerate it
