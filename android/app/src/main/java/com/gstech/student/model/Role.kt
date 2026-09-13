package com.gstech.student.model

enum class Role {
    SUPER_ADMIN, DF, SRIO, SCQ, DIRECTEUR, GESTIONNAIRE, FORMATEUR, ETUDIANT;

    companion object {
        fun fromClaim(value: String?): Role? = when (value?.lowercase()) {
            "superadmin", "admin", "administrateur" -> SUPER_ADMIN
            "df" -> DF
            "srio" -> SRIO
            "scq" -> SCQ
            "directeur" -> DIRECTEUR
            "gestionnaire" -> GESTIONNAIRE
            "formateur" -> FORMATEUR
            "etudiant", "stagiaire" -> ETUDIANT
            else -> null
        }
    }
}
