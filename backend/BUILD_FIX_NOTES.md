# Backend build fixes

The reported `TS2307` / `TS2580` errors for NestJS, TypeORM, class-validator,
Node (`process`, `Buffer`, `crypto`, `fs`, etc.) are dependency/type-environment
errors. This project already declares the required packages in `package.json`
and `package-lock.json`.

From the backend directory, install dependencies before compiling:

```bash
npm ci
npm run build
```

The following source-level DTO errors reported by TypeScript were fixed by
using explicit optional DTO fields instead of relying on mapped-type inference:

- `UpdateClasseDto.nomClasse`
- `UpdateCourseDto.nomCours`
- `UpdateCreneauDto.jourSemaine`, `heureDebut`, `heureFin`, `salle`, `dateDebut`, `dateFin`, `idAffectation`
- `UpdateUserDto` editable fields (`email`, `nom`, `prenom`, `cin`, `telephone`, `adresse`, `module`, `numerostagiaire`, `promotion`, `niveauAcces`, `region`, `idEtablissement`)

The runtime behavior of the existing update services/controllers is preserved.
