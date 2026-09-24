-- Allow student requests for the personalized OFPPT registration/training attestation.
ALTER TABLE demande_document
  MODIFY COLUMN type_document ENUM(
    'certificat_scolarite',
    'releve_notes',
    'attestation_reussite',
    'bulletin',
    'attestation_inscription'
  ) NULL;
