# GSTech v1.7 — Backend compilation/hierarchy fixes

## Corrections
- Corrigé `classes.service.ts` : suppression de la chaîne mal échappée qui provoquait les erreurs syntaxiques en cascade.
- Implémenté `removeStudent()` et `assignments()` dans `ClassesService`, utilisés par `ClassesController`.
- Le Directeur ne peut pas affecter un Stagiaire à un groupe ; cette opération reste réservée au GS (et au DF).
- Corrigé `update-affectation.dto.ts` : suppression du conflit `PartialType`/`normalized()`.
- La normalisation des anciennes clés `idclasse`, `idcours`, `idformateur` est maintenant centralisée dans `AffectationsService`.
- Corrigé le contrôleur des annonces : la suppression utilise bien le troisième argument attendu par `AnnouncementsService.remove()` et ne transmet plus une expression `Role.ADMIN, Role.DF` invalide.
- Corrigée la création des comptes régionaux : SRIO et SCQ restent des rôles régionaux et ne sont pas rattachés à un EFP. En revanche, le GS créé par SRIO et le Directeur créé par SCQ doivent être rattachés à un établissement de la région de leur créateur.

## Hiérarchie fonctionnelle
SuperAdmin → DF → SRIO / SCQ → Directeur → GS → Stagiaire

- SRIO : gestion des GS de sa région.
- SCQ : gestion des Directeurs de sa région.
- Directeur : gestion des Formateurs, modules, affectations et emploi du temps.
- GS : gestion des groupes et des Stagiaires.
- SuperAdmin : maintenance technique uniquement.
