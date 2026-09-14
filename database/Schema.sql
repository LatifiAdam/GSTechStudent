
CREATE DATABASE IF NOT EXISTS gestion_stagiaires
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE gestion_stagiaires;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- SUPPRESSION DES TABLES EXISTANTES
-- ============================================================

DROP VIEW IF EXISTS v_attendance_details;
DROP VIEW IF EXISTS v_formateur_home;
DROP VIEW IF EXISTS v_student_attendance_summary;
DROP VIEW IF EXISTS v_formateur_course_student_count;
DROP VIEW IF EXISTS v_course_student_count;
DROP VIEW IF EXISTS v_formateur_schedule;
DROP VIEW IF EXISTS v_student_schedule;
DROP VIEW IF EXISTS v_schedule;

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS role_permission;
DROP TABLE IF EXISTS permission;

DROP TABLE IF EXISTS device_token;
DROP TABLE IF EXISTS note;
DROP TABLE IF EXISTS annonce;
DROP TABLE IF EXISTS demande_document;
DROP TABLE IF EXISTS document;
DROP TABLE IF EXISTS notification;

DROP TABLE IF EXISTS justification;
DROP TABLE IF EXISTS presence;
DROP TABLE IF EXISTS appel;

DROP TABLE IF EXISTS creneau;
DROP TABLE IF EXISTS affectation;
DROP TABLE IF EXISTS cours;

DROP TABLE IF EXISTS stagiaire;
DROP TABLE IF EXISTS classe;
DROP TABLE IF EXISTS formateur;
DROP TABLE IF EXISTS gestionnaire;

DROP TABLE IF EXISTS directeur;
DROP TABLE IF EXISTS scq;
DROP TABLE IF EXISTS srio;
DROP TABLE IF EXISTS administrateur;

DROP TABLE IF EXISTS etablissement;
DROP TABLE IF EXISTS annee_formation;
DROP TABLE IF EXISTS utilisateur;
DROP TABLE IF EXISTS region;

-- ============================================================
-- REGIONS
-- ============================================================

CREATE TABLE region (
    region VARCHAR(30) PRIMARY KEY,

    code VARCHAR(30) NOT NULL UNIQUE,

    nom VARCHAR(150) NOT NULL UNIQUE,

    actif BOOLEAN NOT NULL DEFAULT TRUE,

    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

INSERT INTO region (code, nom) VALUES
('RSK', 'Rabat-Salé-Kénitra'),
('CS',  'Casablanca-Settat'),
('TTA', 'Tanger-Tétouan-Al Hoceïma'),
('FM',  'Fès-Meknès'),
('M',   'Marrakech-Safi'),
('OR',  'Oriental'),
('BS',  'Béni Mellal-Khénifra'),
('D',   'Drâa-Tafilalet'),
('SMD', 'Souss-Massa'),
('GON', 'Guelmim-Oued Noun');

-- ============================================================
-- ANNEES DE FORMATION
-- ============================================================

CREATE TABLE annee_formation (
    id_annee INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    libelle VARCHAR(20) NOT NULL UNIQUE,

    date_debut DATE NOT NULL,

    date_fin DATE NOT NULL,

    active BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_annee_dates
        CHECK (date_fin > date_debut)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- UTILISATEURS
-- ============================================================

CREATE TABLE utilisateur (
    id_utilisateur CHAR(36) NOT NULL,

    nom VARCHAR(100) NOT NULL,

    prenom VARCHAR(100) NOT NULL,

    email VARCHAR(255) NOT NULL UNIQUE,

    mot_de_passe VARCHAR(255) NOT NULL,

    role ENUM(
        'superadmin',
        'df',
        'srio',
        'scq',
        'directeur',
        'gestionnaire',
        'formateur',
        'stagiaire'
    ) NOT NULL DEFAULT 'stagiaire',

    region VARCHAR(30) NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Temporary account used only to bootstrap the first real Super Admin.
    -- It is automatically deleted when a real Super Admin is created.
    is_bootstrap BOOLEAN NOT NULL DEFAULT FALSE,

    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_modification DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    cin VARCHAR(20) NULL UNIQUE,

    telephone VARCHAR(30) NULL,

    adresse VARCHAR(255) NULL,

    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,

    two_factor_code_hash VARCHAR(255) NULL,

    two_factor_code_expires_at DATETIME NULL,

    PRIMARY KEY (id_utilisateur),

    KEY idx_user_region (region),

    KEY idx_user_region_active (region, is_active),

    KEY idx_user_role_active (role, is_active),

    CONSTRAINT fk_utilisateur_region
        FOREIGN KEY (region)
        REFERENCES region(region)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- SUPER ADMIN
-- Maintenance technique uniquement
-- ============================================================

CREATE TABLE administrateur (
    id_utilisateur CHAR(36) NOT NULL,

    niveau_acces VARCHAR(30) NOT NULL DEFAULT 'technical',

    PRIMARY KEY (id_utilisateur),

    CONSTRAINT fk_administrateur_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- SRIO
-- 1 SRIO maximum par région
-- ============================================================

CREATE TABLE srio (
    id_utilisateur CHAR(36) NOT NULL,

    region VARCHAR(30) NOT NULL,

    PRIMARY KEY (id_utilisateur),

    UNIQUE KEY uq_srio_region (region),

    KEY idx_srio_region (region),

    CONSTRAINT fk_srio_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE,

    CONSTRAINT fk_srio_region
        FOREIGN KEY (region)
        REFERENCES region(region)
        ON DELETE RESTRICT
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- SCQ
-- 1 SCQ maximum par région
-- ============================================================

CREATE TABLE scq (
    id_utilisateur CHAR(36) NOT NULL,

    region VARCHAR(30) NOT NULL,

    PRIMARY KEY (id_utilisateur),

    UNIQUE KEY uq_scq_region (region),

    KEY idx_scq_region (region),

    CONSTRAINT fk_scq_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE,

    CONSTRAINT fk_scq_region
        FOREIGN KEY (region)
        REFERENCES region(region)
        ON DELETE RESTRICT
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- DIRECTEURS
-- ============================================================

CREATE TABLE directeur (
    id_utilisateur CHAR(36) NOT NULL,

    PRIMARY KEY (id_utilisateur),

    CONSTRAINT fk_directeur_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- ETABLISSEMENTS / EFP
-- ============================================================

CREATE TABLE etablissement (
    id_etablissement CHAR(36) NULL,

    nom_etablissement VARCHAR(200) NOT NULL,

    region VARCHAR(30) NOT NULL,

    id_directeur CHAR(36) NULL UNIQUE,

    actif BOOLEAN NOT NULL DEFAULT TRUE,

    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_modification DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id_etablissement),

    UNIQUE KEY uq_etab_nom_region (
        region,
        nom_etablissement
    ),

    KEY idx_etab_region (
        region
    ),

    CONSTRAINT fk_etablissement_region
        FOREIGN KEY (region)
        REFERENCES region(region)
        ON DELETE RESTRICT,

    CONSTRAINT fk_etablissement_directeur
        FOREIGN KEY (id_directeur)
        REFERENCES directeur(id_utilisateur)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- GESTIONNAIRES / GS
-- ============================================================

CREATE TABLE gestionnaire (
    id_utilisateur CHAR(36) NOT NULL,

    id_etablissement CHAR(36) NULL,

    PRIMARY KEY (id_utilisateur),

    KEY idx_gestionnaire_etab (
        id_etablissement
    ),

    CONSTRAINT fk_gestionnaire_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE,

    CONSTRAINT fk_gestionnaire_etablissement
        FOREIGN KEY (id_etablissement)
        REFERENCES etablissement(id_etablissement)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- FORMATEURS
-- ============================================================

CREATE TABLE formateur (
    id_utilisateur CHAR(36) NOT NULL,

    id_etablissement CHAR(36) NULL,

    module VARCHAR(100) NULL,

    PRIMARY KEY (id_utilisateur),

    KEY idx_formateur_etab (
        id_etablissement
    ),

    CONSTRAINT fk_formateur_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE,

    CONSTRAINT fk_formateur_etablissement
        FOREIGN KEY (id_etablissement)
        REFERENCES etablissement(id_etablissement)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- GROUPES / CLASSES
-- Créés et gérés par le GS
-- ============================================================

CREATE TABLE classe (
    id_classe CHAR(36) NOT NULL,

    id_etablissement CHAR(36) NOT NULL,

    id_annee_formation INT UNSIGNED NULL,

    nom_classe VARCHAR(100) NOT NULL,

    description VARCHAR(255) NULL,

    actif BOOLEAN NOT NULL DEFAULT TRUE,

    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_modification DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id_classe),

    UNIQUE KEY uq_classe_etab_nom (
        id_etablissement,
        nom_classe
    ),

    KEY idx_classe_etab (
        id_etablissement
    ),

    KEY idx_classe_annee (
        id_annee_formation
    ),

    CONSTRAINT fk_classe_etablissement
        FOREIGN KEY (id_etablissement)
        REFERENCES etablissement(id_etablissement)
        ON DELETE RESTRICT,

    CONSTRAINT fk_classe_annee
        FOREIGN KEY (id_annee_formation)
        REFERENCES annee_formation(id_annee)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- STAGIAIRES
-- ============================================================

CREATE TABLE stagiaire (
    id_utilisateur CHAR(36) NOT NULL,

    numero_stagiaire VARCHAR(30) NULL UNIQUE,

    promotion VARCHAR(50) NULL,

    id_etablissement CHAR(36) NULL,

    id_classe CHAR(36) NULL,

    PRIMARY KEY (id_utilisateur),

    KEY idx_stagiaire_etab (
        id_etablissement
    ),

    KEY idx_stagiaire_classe (
        id_classe
    ),

    CONSTRAINT fk_stagiaire_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE,

    CONSTRAINT fk_stagiaire_etablissement
        FOREIGN KEY (id_etablissement)
        REFERENCES etablissement(id_etablissement)
        ON DELETE RESTRICT,

    CONSTRAINT fk_stagiaire_classe
        FOREIGN KEY (id_classe)
        REFERENCES classe(id_classe)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- MODULES / COURS
-- ============================================================

CREATE TABLE cours (
    id_cours CHAR(36) NOT NULL,

    id_etablissement CHAR(36) NOT NULL,

    id_annee_formation INT UNSIGNED NULL,

    nom_cours VARCHAR(150) NOT NULL,

    description VARCHAR(500) NULL,

    actif BOOLEAN NOT NULL DEFAULT TRUE,

    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_modification DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id_cours),

    KEY idx_cours_etab (
        id_etablissement
    ),

    KEY idx_cours_annee (
        id_annee_formation
    ),

    CONSTRAINT fk_cours_etablissement
        FOREIGN KEY (id_etablissement)
        REFERENCES etablissement(id_etablissement)
        ON DELETE RESTRICT,

    CONSTRAINT fk_cours_annee
        FOREIGN KEY (id_annee_formation)
        REFERENCES annee_formation(id_annee)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- AFFECTATION
-- Directeur affecte le Formateur à un groupe/module
-- Le groupe est créé par le GS
-- ============================================================

CREATE TABLE affectation (
    id_affectation CHAR(36) NOT NULL,

    id_classe CHAR(36) NOT NULL,

    id_cours CHAR(36) NOT NULL,

    id_formateur CHAR(36) NOT NULL,

    date_debut DATE NULL,

    date_fin DATE NULL,

    actif BOOLEAN NOT NULL DEFAULT TRUE,

    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_affectation),

    UNIQUE KEY uq_affectation_classe_cours (
        id_classe,
        id_cours
    ),

    KEY idx_affectation_classe (
        id_classe
    ),

    KEY idx_affectation_cours (
        id_cours
    ),

    KEY idx_affectation_formateur (
        id_formateur
    ),

    CONSTRAINT fk_affectation_classe
        FOREIGN KEY (id_classe)
        REFERENCES classe(id_classe)
        ON DELETE RESTRICT,

    CONSTRAINT fk_affectation_cours
        FOREIGN KEY (id_cours)
        REFERENCES cours(id_cours)
        ON DELETE RESTRICT,

    CONSTRAINT fk_affectation_formateur
        FOREIGN KEY (id_formateur)
        REFERENCES formateur(id_utilisateur)
        ON DELETE RESTRICT,

    CONSTRAINT chk_affectation_dates
        CHECK (
            date_fin IS NULL
            OR date_debut IS NULL
            OR date_fin >= date_debut
        )
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- CRENEAUX / EMPLOI DU TEMPS
-- Créés par le Directeur
-- ============================================================

CREATE TABLE creneau (
    id_creneau CHAR(36) NOT NULL,

    jour_semaine ENUM(
        'lundi',
        'mardi',
        'mercredi',
        'jeudi',
        'vendredi',
        'samedi',
        'dimanche'
    ) NOT NULL,

    heure_debut TIME NOT NULL,

    heure_fin TIME NOT NULL,

    salle VARCHAR(50) NOT NULL,

    id_affectation CHAR(36) NOT NULL,

    date_debut DATE NULL,

    date_fin DATE NULL,

    actif BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id_creneau),

    KEY idx_creneau_affectation (
        id_affectation
    ),

    KEY idx_creneau_jour (
        jour_semaine,
        heure_debut
    ),

    CONSTRAINT fk_creneau_affectation
        FOREIGN KEY (id_affectation)
        REFERENCES affectation(id_affectation)
        ON DELETE CASCADE,

    CONSTRAINT chk_creneau_heures
        CHECK (heure_fin > heure_debut),

    CONSTRAINT chk_creneau_dates
        CHECK (
            date_fin IS NULL
            OR date_debut IS NULL
            OR date_fin >= date_debut
        )
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- PRESENCES / ABSENCES
-- ============================================================

CREATE TABLE appel (
    id_appel CHAR(36) NOT NULL,

    date_heure DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    valide BOOLEAN NOT NULL DEFAULT FALSE,

    id_creneau CHAR(36) NOT NULL,

    id_formateur CHAR(36) NOT NULL,

    PRIMARY KEY (id_appel),

    KEY idx_appel_creneau (
        id_creneau
    ),

    KEY idx_appel_formateur (
        id_formateur
    ),

    CONSTRAINT fk_appel_creneau
        FOREIGN KEY (id_creneau)
        REFERENCES creneau(id_creneau)
        ON DELETE CASCADE,

    CONSTRAINT fk_appel_formateur
        FOREIGN KEY (id_formateur)
        REFERENCES formateur(id_utilisateur)
        ON DELETE RESTRICT
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================

CREATE TABLE presence (
    id_presence CHAR(36) NOT NULL,

    statut ENUM(
        'present',
        'absent',
        'retard'
    ) NOT NULL DEFAULT 'present',

    id_appel CHAR(36) NOT NULL,

    id_stagiaire CHAR(36) NOT NULL,

    PRIMARY KEY (id_presence),

    UNIQUE KEY uq_presence_appel_stagiaire (
        id_appel,
        id_stagiaire
    ),

    KEY idx_presence_stagiaire (
        id_stagiaire
    ),

    CONSTRAINT fk_presence_appel
        FOREIGN KEY (id_appel)
        REFERENCES appel(id_appel)
        ON DELETE CASCADE,

    CONSTRAINT fk_presence_stagiaire
        FOREIGN KEY (id_stagiaire)
        REFERENCES stagiaire(id_utilisateur)
        ON DELETE CASCADE
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================

CREATE TABLE justification (
    id_justification CHAR(36) NOT NULL,

    motif TEXT NOT NULL,

    piece_jointe VARCHAR(512) NULL,

    statut_justification ENUM(
        'en_attente',
        'acceptee',
        'refusee'
    ) NOT NULL DEFAULT 'en_attente',

    date_envoi DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    id_presence CHAR(36) NOT NULL UNIQUE,

    PRIMARY KEY (id_justification),

    CONSTRAINT fk_justification_presence
        FOREIGN KEY (id_presence)
        REFERENCES presence(id_presence)
        ON DELETE CASCADE
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- NOTIFICATIONS
-- ============================================================

CREATE TABLE notification (
    id_notification CHAR(36) NOT NULL,

    type VARCHAR(50) NOT NULL,

    message TEXT NOT NULL,

    lue BOOLEAN NOT NULL DEFAULT FALSE,

    date_envoi DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    id_utilisateur CHAR(36) NOT NULL,

    PRIMARY KEY (id_notification),

    KEY idx_notification_utilisateur (
        id_utilisateur
    ),

    CONSTRAINT fk_notification_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- DOCUMENTS
-- ============================================================

CREATE TABLE document (
    id_document CHAR(36) NOT NULL,

    nom_document VARCHAR(255) NOT NULL,

    type_document VARCHAR(100) NOT NULL,

    fichier VARCHAR(512) NOT NULL,

    statut ENUM(
        'en_attente',
        'approuve',
        'refuse'
    ) NOT NULL DEFAULT 'en_attente',

    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_validation DATETIME NULL,

    motif_refus VARCHAR(500) NULL,

    id_gestionnaire CHAR(36) NOT NULL,

    id_directeur CHAR(36) NULL,

    PRIMARY KEY (id_document),

    KEY idx_document_gestionnaire (
        id_gestionnaire
    ),

    KEY idx_document_directeur (
        id_directeur
    ),

    CONSTRAINT fk_document_gestionnaire
        FOREIGN KEY (id_gestionnaire)
        REFERENCES gestionnaire(id_utilisateur)
        ON DELETE RESTRICT,

    CONSTRAINT fk_document_directeur
        FOREIGN KEY (id_directeur)
        REFERENCES directeur(id_utilisateur)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- DEMANDES DE DOCUMENTS
-- ============================================================

CREATE TABLE demande_document (
    id_demande CHAR(36) NOT NULL,

    type_document ENUM(
        'certificat_scolarite',
        'releve_notes',
        'attestation_reussite',
        'bulletin'
    ) NULL,

    id_document CHAR(36) NULL,

    id_gestionnaire CHAR(36) NULL,

    id_etablissement CHAR(36) NULL,

    statut ENUM(
        'en_attente',
        'en_cours',
        'delivree',
        'refusee'
    ) NOT NULL DEFAULT 'en_attente',

    date_demande DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_traitement DATETIME NULL,

    fichier_genere VARCHAR(512) NULL,

    id_stagiaire CHAR(36) NOT NULL,

    id_administrateur CHAR(36) NULL,

    PRIMARY KEY (id_demande),

    KEY idx_demande_stagiaire (
        id_stagiaire
    ),

    KEY idx_demande_gestionnaire (
        id_gestionnaire
    ),

    KEY idx_demande_etab (
        id_etablissement
    ),

    CONSTRAINT fk_demande_stagiaire
        FOREIGN KEY (id_stagiaire)
        REFERENCES stagiaire(id_utilisateur)
        ON DELETE CASCADE,

    CONSTRAINT fk_demande_document
        FOREIGN KEY (id_document)
        REFERENCES document(id_document)
        ON DELETE SET NULL,

    CONSTRAINT fk_demande_gestionnaire
        FOREIGN KEY (id_gestionnaire)
        REFERENCES gestionnaire(id_utilisateur)
        ON DELETE SET NULL,

    CONSTRAINT fk_demande_etablissement
        FOREIGN KEY (id_etablissement)
        REFERENCES etablissement(id_etablissement)
        ON DELETE SET NULL,

    CONSTRAINT fk_demande_administrateur
        FOREIGN KEY (id_administrateur)
        REFERENCES administrateur(id_utilisateur)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- ANNONCES
-- ============================================================

CREATE TABLE annonce (
    id_annonce CHAR(36) NOT NULL,

    titre VARCHAR(150) NOT NULL,

    contenu TEXT NOT NULL,

    type_annonce ENUM(
        'examen',
        'controle',
        'generale'
    ) NOT NULL,

    date_publication DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_evenement DATETIME NULL,

    id_cours CHAR(36) NULL,

    id_classe CHAR(36) NULL,

    id_auteur CHAR(36) NOT NULL,

    PRIMARY KEY (id_annonce),

    KEY idx_annonce_cours (
        id_cours
    ),

    KEY idx_annonce_classe (
        id_classe
    ),

    KEY idx_annonce_auteur (
        id_auteur
    ),

    CONSTRAINT fk_annonce_cours
        FOREIGN KEY (id_cours)
        REFERENCES cours(id_cours)
        ON DELETE SET NULL,

    CONSTRAINT fk_annonce_classe
        FOREIGN KEY (id_classe)
        REFERENCES classe(id_classe)
        ON DELETE SET NULL,

    CONSTRAINT fk_annonce_auteur
        FOREIGN KEY (id_auteur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- NOTES
-- ============================================================

CREATE TABLE note (
    id_note CHAR(36) NOT NULL,

    id_stagiaire CHAR(36) NOT NULL,

    id_affectation CHAR(36) NOT NULL,

    note1 DECIMAL(5,2) NULL,

    note2 DECIMAL(5,2) NULL,

    note3 DECIMAL(5,2) NULL,

    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_modification DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id_note),

    UNIQUE KEY uq_note_stagiaire_affectation (
        id_stagiaire,
        id_affectation
    ),

    KEY idx_note_stagiaire (
        id_stagiaire
    ),

    KEY idx_note_affectation (
        id_affectation
    ),

    CONSTRAINT fk_note_stagiaire
        FOREIGN KEY (id_stagiaire)
        REFERENCES stagiaire(id_utilisateur)
        ON DELETE CASCADE,

    CONSTRAINT fk_note_affectation
        FOREIGN KEY (id_affectation)
        REFERENCES affectation(id_affectation)
        ON DELETE CASCADE,

    CONSTRAINT chk_note1
        CHECK (
            note1 IS NULL
            OR (note1 >= 0 AND note1 <= 20)
        ),

    CONSTRAINT chk_note2
        CHECK (
            note2 IS NULL
            OR (note2 >= 0 AND note2 <= 20)
        ),

    CONSTRAINT chk_note3
        CHECK (
            note3 IS NULL
            OR (note3 >= 0 AND note3 <= 20)
        )
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- DEVICE TOKENS
-- ============================================================

CREATE TABLE device_token (
    id_device_token CHAR(36) NOT NULL,

    token VARCHAR(255) NOT NULL UNIQUE,

    plateforme ENUM(
        'ios',
        'android'
    ) NOT NULL,

    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    date_derniere_utilisation DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    id_utilisateur CHAR(36) NOT NULL,

    PRIMARY KEY (id_device_token),

    KEY idx_device_user (
        id_utilisateur
    ),

    CONSTRAINT fk_device_token_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE CASCADE
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- PERMISSIONS
-- ============================================================

CREATE TABLE permission (
    id_permission INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    code VARCHAR(80) NOT NULL UNIQUE,

    description VARCHAR(255) NOT NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- ROLE / PERMISSION
-- ============================================================

CREATE TABLE role_permission (
    role ENUM(
        'superadmin',
        'df',
        'srio',
        'scq',
        'directeur',
        'gestionnaire',
        'formateur',
        'stagiaire'
    ) NOT NULL,

    id_permission INT UNSIGNED NOT NULL,

    PRIMARY KEY (
        role,
        id_permission
    ),

    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (id_permission)
        REFERENCES permission(id_permission)
        ON DELETE CASCADE
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- PERMISSIONS DISPONIBLES
-- ============================================================

INSERT INTO permission (code, description) VALUES

(
    'TECHNICAL_MAINTENANCE',
    'Maintenance technique du logiciel'
),

(
    'MANAGE_FUNCTIONAL_GLOBAL',
    'Administration fonctionnelle nationale'
),

(
    'MANAGE_SRIO',
    'Créer et supprimer les SRIO'
),

(
    'MANAGE_SCQ',
    'Créer et supprimer les SCQ'
),

(
    'MANAGE_REGIONAL_GS',
    'Créer et supprimer les GS de sa région'
),

(
    'MANAGE_REGIONAL_DIRECTEURS',
    'Créer et supprimer les Directeurs de sa région'
),

(
    'MANAGE_FORMATEURS',
    'Créer et supprimer les Formateurs de son EFP'
),

(
    'MANAGE_MODULES',
    'Gérer les modules de son EFP'
),

(
    'MANAGE_SCHEDULE',
    'Créer et gérer l emploi du temps de son EFP'
),

(
    'MANAGE_AFFECTATIONS',
    'Affecter les Formateurs aux groupes et modules'
),

(
    'MANAGE_GROUPS',
    'Créer, modifier et supprimer les groupes de son EFP'
),

(
    'MANAGE_STAGIAIRES',
    'Ajouter et supprimer les Stagiaires de son EFP'
),

(
    'VIEW_REGIONAL_DATA',
    'Consulter les données de sa région'
),

(
    'VIEW_EFP_DATA',
    'Consulter les données de son EFP'
),

(
    'VIEW_OWN_DATA',
    'Consulter ses propres données'
);

-- ============================================================
-- SUPER ADMIN
-- Technique uniquement
-- ============================================================

INSERT INTO role_permission (role, id_permission)

SELECT
    'superadmin',
    id_permission

FROM permission

WHERE code = 'TECHNICAL_MAINTENANCE';

-- ============================================================
-- DF
-- Tous les droits fonctionnels
-- PAS de maintenance technique
-- ============================================================

INSERT INTO role_permission (role, id_permission)

SELECT
    'df',
    id_permission

FROM permission

WHERE code <> 'TECHNICAL_MAINTENANCE';

-- ============================================================
-- SRIO
-- ============================================================

INSERT INTO role_permission (role, id_permission)

SELECT
    'srio',
    id_permission

FROM permission

WHERE code IN (
    'MANAGE_REGIONAL_GS',
    'VIEW_REGIONAL_DATA'
);

-- ============================================================
-- SCQ
-- ============================================================

INSERT INTO role_permission (role, id_permission)

SELECT
    'scq',
    id_permission

FROM permission

WHERE code IN (
    'MANAGE_REGIONAL_DIRECTEURS',
    'VIEW_REGIONAL_DATA'
);

-- ============================================================
-- DIRECTEUR
-- ============================================================

INSERT INTO role_permission (role, id_permission)

SELECT
    'directeur',
    id_permission

FROM permission

WHERE code IN (
    'MANAGE_FORMATEURS',
    'MANAGE_MODULES',
    'MANAGE_SCHEDULE',
    'MANAGE_AFFECTATIONS',
    'VIEW_EFP_DATA'
);

-- ============================================================
-- GESTIONNAIRE / GS
-- ============================================================

INSERT INTO role_permission (role, id_permission)

SELECT
    'gestionnaire',
    id_permission

FROM permission

WHERE code IN (
    'MANAGE_GROUPS',
    'MANAGE_STAGIAIRES',
    'VIEW_EFP_DATA'
);

-- ============================================================
-- FORMATEUR
-- ============================================================

INSERT INTO role_permission (role, id_permission)

SELECT
    'formateur',
    id_permission

FROM permission

WHERE code IN (
    'VIEW_EFP_DATA',
    'VIEW_OWN_DATA'
);

-- ============================================================
-- STAGIAIRE
-- ============================================================

INSERT INTO role_permission (role, id_permission)

SELECT
    'stagiaire',
    id_permission

FROM permission

WHERE code = 'VIEW_OWN_DATA';

-- ============================================================
-- AUDIT LOG
-- ============================================================

CREATE TABLE audit_log (
    id_audit CHAR(36) NOT NULL,

    actor_id CHAR(36) NULL,

    actor_role VARCHAR(30) NULL,

    action VARCHAR(80) NOT NULL,

    entity_type VARCHAR(80) NOT NULL,

    entity_id CHAR(36) NULL,

    region VARCHAR(30) NULL,

    id_etablissement CHAR(36) NULL,

    old_value JSON NULL,

    new_value JSON NULL,

    ip_address VARCHAR(64) NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_audit),

    KEY idx_audit_actor_date (
        actor_id,
        created_at
    ),

    KEY idx_audit_scope (
        region,
        id_etablissement,
        created_at
    ),

    KEY idx_audit_entity (
        entity_type,
        entity_id
    ),

    CONSTRAINT fk_audit_actor
        FOREIGN KEY (actor_id)
        REFERENCES utilisateur(id_utilisateur)
        ON DELETE SET NULL,

    CONSTRAINT fk_audit_region
        FOREIGN KEY (region)
        REFERENCES region(region)
        ON DELETE SET NULL,

    CONSTRAINT fk_audit_etablissement
        FOREIGN KEY (id_etablissement)
        REFERENCES etablissement(id_etablissement)
        ON DELETE SET NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- ============================================================
-- VUE : EMPLOI DU TEMPS
-- ============================================================

CREATE OR REPLACE VIEW v_schedule AS

SELECT

    cr.id_creneau,

    cr.jour_semaine,

    cr.heure_debut,

    cr.heure_fin,

    cr.salle,

    cr.date_debut,

    cr.date_fin,

    af.id_affectation,

    af.id_classe,

    cl.nom_classe,

    af.id_cours,

    co.nom_cours,

    af.id_formateur,

    u.nom AS formateur_nom,

    u.prenom AS formateur_prenom,

    cl.id_etablissement

FROM creneau cr

JOIN affectation af
    ON cr.id_affectation = af.id_affectation

JOIN classe cl
    ON af.id_classe = cl.id_classe

JOIN cours co
    ON af.id_cours = co.id_cours

JOIN formateur f
    ON af.id_formateur = f.id_utilisateur

JOIN utilisateur u
    ON f.id_utilisateur = u.id_utilisateur;

-- ============================================================
-- VUE : EMPLOI DU TEMPS STAGIAIRE
-- ============================================================

CREATE OR REPLACE VIEW v_student_schedule AS

SELECT

    s.id_utilisateur AS id_stagiaire,

    s.id_classe,

    cl.nom_classe,

    cr.id_creneau,

    cr.jour_semaine,

    cr.heure_debut,

    cr.heure_fin,

    cr.salle,

    co.id_cours,

    co.nom_cours,

    f.id_utilisateur AS id_formateur,

    u.nom AS formateur_nom,

    u.prenom AS formateur_prenom

FROM stagiaire s

JOIN classe cl
    ON s.id_classe = cl.id_classe

JOIN affectation af
    ON af.id_classe = cl.id_classe

JOIN creneau cr
    ON cr.id_affectation = af.id_affectation

JOIN cours co
    ON co.id_cours = af.id_cours

JOIN formateur f
    ON f.id_utilisateur = af.id_formateur

JOIN utilisateur u
    ON u.id_utilisateur = f.id_utilisateur;

-- ============================================================
-- VUE : EMPLOI DU TEMPS FORMATEUR
-- ============================================================

CREATE OR REPLACE VIEW v_formateur_schedule AS

SELECT

    af.id_formateur,

    af.id_affectation,

    cr.id_creneau,

    cr.jour_semaine,

    cr.heure_debut,

    cr.heure_fin,

    cr.salle,

    af.id_classe,

    cl.nom_classe,

    af.id_cours,

    co.nom_cours,

    cl.id_etablissement

FROM affectation af

JOIN creneau cr
    ON cr.id_affectation = af.id_affectation

JOIN classe cl
    ON cl.id_classe = af.id_classe

JOIN cours co
    ON co.id_cours = af.id_cours;

-- ============================================================
-- VUE : NOMBRE DE STAGIAIRES PAR COURS/GROUPE
-- ============================================================

CREATE OR REPLACE VIEW v_course_student_count AS

SELECT

    af.id_cours,

    co.nom_cours,

    af.id_classe,

    cl.nom_classe,

    COUNT(s.id_utilisateur) AS total_stagiaires

FROM affectation af

JOIN cours co
    ON af.id_cours = co.id_cours

JOIN classe cl
    ON af.id_classe = cl.id_classe

LEFT JOIN stagiaire s
    ON s.id_classe = af.id_classe

GROUP BY

    af.id_cours,
    co.nom_cours,
    af.id_classe,
    cl.nom_classe;

-- ============================================================
-- VUE : FORMATEUR / COURS / STAGIAIRES
-- ============================================================

CREATE OR REPLACE VIEW v_formateur_course_student_count AS

SELECT

    af.id_formateur,

    af.id_cours,

    co.nom_cours,

    af.id_classe,

    cl.nom_classe,

    COUNT(s.id_utilisateur) AS total_stagiaires

FROM affectation af

JOIN cours co
    ON af.id_cours = co.id_cours

JOIN classe cl
    ON af.id_classe = cl.id_classe

LEFT JOIN stagiaire s
    ON s.id_classe = af.id_classe

GROUP BY

    af.id_formateur,
    af.id_cours,
    co.nom_cours,
    af.id_classe,
    cl.nom_classe;

-- ============================================================
-- VUE : PRESENCE STAGIAIRE
-- ============================================================

CREATE OR REPLACE VIEW v_student_attendance_summary AS

SELECT

    p.id_stagiaire,

    COUNT(p.id_presence) AS total_classes,

    SUM(p.statut = 'present') AS present_count,

    SUM(p.statut = 'absent') AS absent_count,

    SUM(p.statut = 'retard') AS late_count,

    ROUND(
        SUM(p.statut = 'present')
        / NULLIF(COUNT(p.id_presence), 0)
        * 100,
        2
    ) AS attendance_percentage

FROM presence p

GROUP BY p.id_stagiaire;

-- ============================================================
-- VUE : ACCUEIL FORMATEUR
-- ============================================================

CREATE OR REPLACE VIEW v_formateur_home AS

SELECT

    af.id_formateur,

    af.id_affectation,

    af.id_cours,

    co.nom_cours,

    af.id_classe,

    cl.nom_classe,

    COUNT(s.id_utilisateur) AS total_stagiaires

FROM affectation af

JOIN cours co
    ON af.id_cours = co.id_cours

JOIN classe cl
    ON af.id_classe = cl.id_classe

LEFT JOIN stagiaire s
    ON s.id_classe = af.id_classe

GROUP BY

    af.id_formateur,
    af.id_affectation,
    af.id_cours,
    co.nom_cours,
    af.id_classe,
    cl.nom_classe;

-- ============================================================
-- VUE : DETAILS PRESENCES
-- ============================================================

CREATE OR REPLACE VIEW v_attendance_details AS

SELECT

    a.id_appel,

    a.date_heure,

    a.valide,

    a.id_formateur,

    cr.id_creneau,

    cr.jour_semaine,

    cr.heure_debut,

    cr.heure_fin,

    cr.salle,

    af.id_affectation,

    af.id_classe,

    cl.nom_classe,

    af.id_cours,

    co.nom_cours,

    p.id_presence,

    p.id_stagiaire,

    p.statut,

    u.nom AS stagiaire_nom,

    u.prenom AS stagiaire_prenom,

    s.numero_stagiaire,

    cl.id_etablissement,

    e.region

FROM appel a

JOIN creneau cr
    ON a.id_creneau = cr.id_creneau

JOIN affectation af
    ON cr.id_affectation = af.id_affectation

JOIN classe cl
    ON af.id_classe = cl.id_classe

JOIN cours co
    ON af.id_cours = co.id_cours

JOIN presence p
    ON p.id_appel = a.id_appel

JOIN stagiaire s
    ON p.id_stagiaire = s.id_utilisateur

JOIN utilisateur u
    ON s.id_utilisateur = u.id_utilisateur

JOIN etablissement e
    ON cl.id_etablissement = e.id_etablissement;

-- ============================================================
-- FIN
-- ============================================================

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- VERIFICATION
-- ============================================================

SELECT
    'Base gestion_stagiaires créée avec succès' AS resultat;

SELECT
    COUNT(*) AS nombre_regions
FROM region;
-- Migration for existing databases: allow optional student profile fields.
-- Execute these statements once on an already-created database if the columns
-- are still NOT NULL there.
ALTER TABLE stagiaire
    MODIFY numero_stagiaire VARCHAR(30) NULL UNIQUE,
    MODIFY promotion VARCHAR(50) NULL,
    MODIFY id_etablissement CHAR(36) NULL;
