# GSTechStudent Android — UI refresh

This build applies a visual refresh to the Android app using the supplied `better ui app.zip` screenshots as the reference direction.

## Visual changes

- Reworked the global Material 3 color system around a light blue-gray canvas, white surfaces, navy text, and GSTech blue accents.
- Added a shared rounded-card style with subtle outlines and reduced visual noise from elevation/shadows.
- Added reusable GSTech headers, screen headers, metric cards, and status pills.
- Refined the student home screen to match the reference structure: compact brand header, greeting, identity line, attendance card, daily classes, notification block, and quick actions.
- Refined teacher, gestionnaire, director/admin dashboard presentation with the same spacing, cards, metrics, and header language.
- Refined schedule, grades, and profile screen headers and card presentation.
- Refined bottom navigation appearance while keeping the existing destinations and navigation behavior.
- Refined login presentation and input borders to match the new visual system.

## Functional scope

Navigation routes, API calls, repositories, role logic, data models, and backend behavior were not intentionally changed as part of this UI pass.

## Validation note

The project sources were checked for obvious Kotlin syntax/reference issues in the modified files. A full Android Gradle build was not run in this environment because the supplied project archive does not include the Gradle wrapper executable/JAR and no Gradle installation is available here.
