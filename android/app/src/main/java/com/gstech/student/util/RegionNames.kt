package com.gstech.student.util

object RegionNames {
    private val names = mapOf(
        "1" to "Rabat-Salé-Kénitra", "rsk" to "Rabat-Salé-Kénitra",
        "2" to "Casablanca-Settat", "cs" to "Casablanca-Settat",
        "3" to "Tanger-Tétouan-Al Hoceïma", "tta" to "Tanger-Tétouan-Al Hoceïma",
        "4" to "Fès-Meknès", "fm" to "Fès-Meknès",
        "5" to "Marrakech-Safi", "m" to "Marrakech-Safi",
        "6" to "Oriental", "or" to "Oriental",
        "7" to "Béni Mellal-Khénifra", "bs" to "Béni Mellal-Khénifra",
        "8" to "Drâa-Tafilalet", "d" to "Drâa-Tafilalet",
        "9" to "Souss-Massa", "smd" to "Souss-Massa",
        "10" to "Guelmim-Oued Noun", "gon" to "Guelmim-Oued Noun"
    )

    fun display(value: String?): String {
        val clean = value?.trim().orEmpty()
        if (clean.isBlank()) return "—"
        return names[clean.lowercase()] ?: clean
    }
}
