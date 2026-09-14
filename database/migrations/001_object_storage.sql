-- GSTech compatibility / object-storage migration
-- Run once against an existing MySQL database.
--
-- This migration is intentionally safe for databases created by older
-- GSTech versions. It fixes the old audit FK/table-name mismatch, converts
-- legacy French region names to the canonical region codes, and adds the
-- profile-image storage key used by object storage.

-- ------------------------------------------------------------
-- 1. Profile image object-storage metadata
-- ------------------------------------------------------------
ALTER TABLE utilisateur
    ADD COLUMN IF NOT EXISTS profile_image_key VARCHAR(512) NULL AFTER adresse;

-- ------------------------------------------------------------
-- 2. Normalize legacy region names to the canonical 10 codes.
--    This is compatible with the current FK because each code exists in
--    the reference table.
-- ------------------------------------------------------------
UPDATE utilisateur SET region = 'RSK' WHERE region IN ('RSK', 'Rabat-Salé-Kénitra');
UPDATE utilisateur SET region = 'CS'  WHERE region IN ('CS', 'Casablanca-Settat');
UPDATE utilisateur SET region = 'TTA' WHERE region IN ('TTA', 'Tanger-Tétouan-Al Hoceïma');
UPDATE utilisateur SET region = 'FM'  WHERE region IN ('FM', 'Fès-Meknès');
UPDATE utilisateur SET region = 'M'   WHERE region IN ('M', 'Marrakech-Safi');
UPDATE utilisateur SET region = 'OR'  WHERE region IN ('OR', 'Oriental');
UPDATE utilisateur SET region = 'BS'  WHERE region IN ('BS', 'Béni Mellal-Khénifra');
UPDATE utilisateur SET region = 'D'   WHERE region IN ('D', 'Drâa-Tafilalet');
UPDATE utilisateur SET region = 'SMD' WHERE region IN ('SMD', 'Souss-Massa');
UPDATE utilisateur SET region = 'GON' WHERE region IN ('GON', 'Guelmim-Oued Noun');

UPDATE etablissement SET region = 'RSK' WHERE region IN ('RSK', 'Rabat-Salé-Kénitra');
UPDATE etablissement SET region = 'CS'  WHERE region IN ('CS', 'Casablanca-Settat');
UPDATE etablissement SET region = 'TTA' WHERE region IN ('TTA', 'Tanger-Tétouan-Al Hoceïma');
UPDATE etablissement SET region = 'FM'  WHERE region IN ('FM', 'Fès-Meknès');
UPDATE etablissement SET region = 'M'   WHERE region IN ('M', 'Marrakech-Safi');
UPDATE etablissement SET region = 'OR'  WHERE region IN ('OR', 'Oriental');
UPDATE etablissement SET region = 'BS'  WHERE region IN ('BS', 'Béni Mellal-Khénifra');
UPDATE etablissement SET region = 'D'   WHERE region IN ('D', 'Drâa-Tafilalet');
UPDATE etablissement SET region = 'SMD' WHERE region IN ('SMD', 'Souss-Massa');
UPDATE etablissement SET region = 'GON' WHERE region IN ('GON', 'Guelmim-Oued Noun');

UPDATE audit_log SET region = 'RSK' WHERE region IN ('RSK', 'Rabat-Salé-Kénitra');
UPDATE audit_log SET region = 'CS'  WHERE region IN ('CS', 'Casablanca-Settat');
UPDATE audit_log SET region = 'TTA' WHERE region IN ('TTA', 'Tanger-Tétouan-Al Hoceïma');
UPDATE audit_log SET region = 'FM'  WHERE region IN ('FM', 'Fès-Meknès');
UPDATE audit_log SET region = 'M'   WHERE region IN ('M', 'Marrakech-Safi');
UPDATE audit_log SET region = 'OR'  WHERE region IN ('OR', 'Oriental');
UPDATE audit_log SET region = 'BS'  WHERE region IN ('BS', 'Béni Mellal-Khénifra');
UPDATE audit_log SET region = 'D'   WHERE region IN ('D', 'Drâa-Tafilalet');
UPDATE audit_log SET region = 'SMD' WHERE region IN ('SMD', 'Souss-Massa');
UPDATE audit_log SET region = 'GON' WHERE region IN ('GON', 'Guelmim-Oued Noun');

-- SRIO/SCQ tables exist in the full schema and may contain legacy names.
UPDATE srio SET region = 'RSK' WHERE region IN ('RSK', 'Rabat-Salé-Kénitra');
UPDATE srio SET region = 'CS'  WHERE region IN ('CS', 'Casablanca-Settat');
UPDATE srio SET region = 'TTA' WHERE region IN ('TTA', 'Tanger-Tétouan-Al Hoceïma');
UPDATE srio SET region = 'FM'  WHERE region IN ('FM', 'Fès-Meknès');
UPDATE srio SET region = 'M'   WHERE region IN ('M', 'Marrakech-Safi');
UPDATE srio SET region = 'OR'  WHERE region IN ('OR', 'Oriental');
UPDATE srio SET region = 'BS'  WHERE region IN ('BS', 'Béni Mellal-Khénifra');
UPDATE srio SET region = 'D'   WHERE region IN ('D', 'Drâa-Tafilalet');
UPDATE srio SET region = 'SMD' WHERE region IN ('SMD', 'Souss-Massa');
UPDATE srio SET region = 'GON' WHERE region IN ('GON', 'Guelmim-Oued Noun');

UPDATE scq SET region = 'RSK' WHERE region IN ('RSK', 'Rabat-Salé-Kénitra');
UPDATE scq SET region = 'CS'  WHERE region IN ('CS', 'Casablanca-Settat');
UPDATE scq SET region = 'TTA' WHERE region IN ('TTA', 'Tanger-Tétouan-Al Hoceïma');
UPDATE scq SET region = 'FM'  WHERE region IN ('FM', 'Fès-Meknès');
UPDATE scq SET region = 'M'   WHERE region IN ('M', 'Marrakech-Safi');
UPDATE scq SET region = 'OR'  WHERE region IN ('OR', 'Oriental');
UPDATE scq SET region = 'BS'  WHERE region IN ('BS', 'Béni Mellal-Khénifra');
UPDATE scq SET region = 'D'   WHERE region IN ('D', 'Drâa-Tafilalet');
UPDATE scq SET region = 'SMD' WHERE region IN ('SMD', 'Souss-Massa');
UPDATE scq SET region = 'GON' WHERE region IN ('GON', 'Guelmim-Oued Noun');

-- ------------------------------------------------------------
-- 3. Repair the audit actor FK from older schemas.
--    Older installations referenced the non-existent/legacy plural
--    table `utilisateurs`; the current table is `utilisateur`.
-- ------------------------------------------------------------
SET @old_audit_fk = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'audit_log'
      AND COLUMN_NAME = 'actor_id'
      AND REFERENCED_TABLE_NAME IN ('utilisateurs', 'utilisateur')
    LIMIT 1
);

SET @drop_sql = IF(
    @old_audit_fk IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE audit_log DROP FOREIGN KEY `', @old_audit_fk, '`')
);
PREPARE stmt FROM @drop_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE audit_log
    ADD CONSTRAINT fk_audit_actor
    FOREIGN KEY (actor_id)
    REFERENCES utilisateur(id_utilisateur)
    ON DELETE SET NULL;

-- Ensure the audit region FK points to the canonical region table.
SET @old_audit_region_fk = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'audit_log'
      AND COLUMN_NAME = 'region'
      AND REFERENCED_TABLE_NAME = 'region'
    LIMIT 1
);

SET @drop_region_sql = IF(
    @old_audit_region_fk IS NULL,
    'SELECT 1',
    CONCAT('ALTER TABLE audit_log DROP FOREIGN KEY `', @old_audit_region_fk, '`')
);
PREPARE stmt2 FROM @drop_region_sql;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

ALTER TABLE audit_log
    ADD CONSTRAINT fk_audit_region
    FOREIGN KEY (region)
    REFERENCES region(region)
    ON DELETE SET NULL
    ON UPDATE CASCADE;
