package com.gstech.student.util

object RegionNames {
    data class Option(val code: String, val name: String)

    // IMPORTANT: code is the value sent to the backend / stored in the FK.
    // The French name is only the display label.
    val options = listOf(
        Option("RSK", "Rabat-Salé-Kénitra"),
        Option("CS", "Casablanca-Settat"),
        Option("TTA", "Tanger-Tétouan-Al Hoceïma"),
        Option("FM", "Fès-Meknès"),
        Option("M", "Marrakech-Safi"),
        Option("OR", "Oriental"),
        Option("BS", "Béni Mellal-Khénifra"),
        Option("D", "Drâa-Tafilalet"),
        Option("SMD", "Souss-Massa"),
        Option("GON", "Guelmim-Oued Noun"),
    )

    private val names = buildMap {
        options.forEach { option ->
            put(option.code.lowercase(), option.name)
            put(option.name.lowercase(), option.name)
        }
    }

    fun display(value: String?): String {
        val clean = value?.trim().orEmpty()
        if (clean.isBlank()) return "—"
        return names[clean.lowercase()] ?: clean
    }
}
