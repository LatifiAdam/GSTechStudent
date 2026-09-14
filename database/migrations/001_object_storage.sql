-- GSTech Object Storage migration
-- Run once against an existing MySQL database.
ALTER TABLE utilisateur
    ADD COLUMN IF NOT EXISTS profile_image_key VARCHAR(512) NULL AFTER adresse;
