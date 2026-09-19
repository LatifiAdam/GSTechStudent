# GSTechStudent stable fixes

Applied to the stable baseline:
- EFP region dropdown with canonical region codes.
- Super Admin EFP access.
- SRIO/SCQ regional account inheritance.
- Android PDF validation/upload hardening.
- Formateur announcement quick action already routed to Announcements.
- Existing secure profile-image MinIO/S3 workflow retained and verified.

## 2026-09-19 — Region + profile-photo fixes
- Centralized the 10 official region choices in Android and always sends the canonical backend region code.
- Backend now normalizes region code or French region name for every user creation path, including Directeur.
- Directeur creation now exposes and requires a Region selector before later EFP assignment.
- Profile-photo upload/editor is reachable from the Student, Super Admin, DF, SRIO, SCQ, Director, Gestionnaire and Formateur app flows.
- Preserved the bootstrap Super Admin audit-log foreign-key fix.
