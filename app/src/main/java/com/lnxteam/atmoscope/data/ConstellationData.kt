package com.lnxteam.atmoscope.data

data class ConstellationInfo(
    val name: String,
    val latinName: String,
    val emoji: String,
    val visibleMonths: List<Int>,
    val description: String,
    val starPoints: List<Pair<Float, Float>>,
    val lines: List<Pair<Int, Int>>
)

object ConstellationData {
    val all = listOf(
        ConstellationInfo("Orion", "Orion", "🏹", listOf(11, 12, 1, 2, 3),
            "Rasi pemburu dengan 3 bintang sabuk khas, paling gampang dikenali.",
            listOf(0.25f to 0.15f, 0.70f to 0.12f, 0.35f to 0.45f, 0.47f to 0.48f, 0.59f to 0.50f, 0.30f to 0.85f, 0.72f to 0.82f),
            listOf(0 to 2, 1 to 4, 2 to 3, 3 to 4, 2 to 5, 4 to 6)
        ),
        ConstellationInfo("Scorpius", "Scorpius", "🦂", listOf(5, 6, 7, 8),
            "Rasi kalajengking dengan bentuk melengkung khas, terang di langit malam musim kemarau.",
            listOf(0.5f to 0.1f, 0.45f to 0.3f, 0.4f to 0.5f, 0.35f to 0.65f, 0.45f to 0.78f, 0.6f to 0.85f),
            listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5)
        ),
        ConstellationInfo("Crux", "Crux (Salib Selatan)", "✝️", listOf(3, 4, 5, 6, 7, 8),
            "Rasi terkecil tapi paling terkenal di langit selatan, dipakai navigasi arah selatan.",
            listOf(0.5f to 0.1f, 0.5f to 0.9f, 0.15f to 0.5f, 0.85f to 0.5f),
            listOf(0 to 1, 2 to 3)
        ),
        ConstellationInfo("Ursa Major", "Ursa Major (Biduk)", "🐻", listOf(2, 3, 4, 5, 6),
            "Bentuk gayung tujuh bintang, muncul rendah di ufuk utara dari Indonesia.",
            listOf(0.05f to 0.7f, 0.25f to 0.62f, 0.45f to 0.55f, 0.6f to 0.5f, 0.8f to 0.4f, 0.78f to 0.2f, 0.55f to 0.27f),
            listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 3)
        ),
        ConstellationInfo("Leo", "Leo", "🦁", listOf(2, 3, 4, 5),
            "Rasi singa dengan pola sabit di kepala dan segitiga ekor.",
            listOf(0.2f to 0.3f, 0.35f to 0.35f, 0.5f to 0.4f, 0.65f to 0.45f, 0.85f to 0.35f, 0.3f to 0.6f, 0.55f to 0.65f),
            listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 2 to 6, 1 to 5)
        ),
        ConstellationInfo("Taurus", "Taurus", "🐂", listOf(11, 12, 1, 2),
            "Rasi banteng dengan bintang Aldebaran sebagai mata merah terangnya.",
            listOf(0.5f to 0.5f, 0.25f to 0.15f, 0.7f to 0.1f, 0.35f to 0.65f, 0.65f to 0.68f),
            listOf(1 to 0, 0 to 2, 0 to 3, 0 to 4)
        ),
        ConstellationInfo("Gemini", "Gemini", "👯", listOf(12, 1, 2, 3),
            "Rasi si kembar dengan dua garis bintang sejajar, Castor dan Pollux di kepalanya.",
            listOf(0.3f to 0.1f, 0.6f to 0.08f, 0.32f to 0.5f, 0.6f to 0.48f, 0.3f to 0.9f, 0.6f to 0.88f),
            listOf(0 to 2, 2 to 4, 1 to 3, 3 to 5, 2 to 3)
        ),
        ConstellationInfo("Canis Major", "Canis Major", "🐕", listOf(12, 1, 2, 3),
            "Rumah dari Sirius, bintang paling terang di langit malam.",
            listOf(0.5f to 0.2f, 0.45f to 0.45f, 0.55f to 0.5f, 0.3f to 0.8f, 0.7f to 0.78f),
            listOf(0 to 1, 1 to 2, 1 to 3, 2 to 4)
        ),
        ConstellationInfo("Cygnus", "Cygnus (Angsa)", "🦢", listOf(6, 7, 8, 9),
            "Berbentuk salib besar, dikenal juga sebagai Northern Cross.",
            listOf(0.5f to 0.1f, 0.5f to 0.45f, 0.5f to 0.9f, 0.2f to 0.4f, 0.8f to 0.4f),
            listOf(0 to 1, 1 to 2, 1 to 3, 1 to 4)
        ),
        ConstellationInfo("Lyra", "Lyra (Lira)", "🎵", listOf(6, 7, 8, 9),
            "Rasi kecil berbentuk jajar genjang, ditandai bintang terang Vega.",
            listOf(0.5f to 0.1f, 0.35f to 0.4f, 0.65f to 0.4f, 0.4f to 0.7f, 0.6f to 0.7f),
            listOf(0 to 1, 0 to 2, 1 to 3, 2 to 4, 3 to 4)
        ),
        ConstellationInfo("Aquila", "Aquila (Elang)", "🦅", listOf(6, 7, 8, 9),
            "Rasi elang dengan bintang Altair yang terang di tengah sayapnya.",
            listOf(0.5f to 0.5f, 0.2f to 0.35f, 0.8f to 0.35f, 0.5f to 0.85f),
            listOf(1 to 0, 0 to 2, 0 to 3)
        ),
        ConstellationInfo("Sagittarius", "Sagittarius (Pemanah)", "🏹", listOf(6, 7, 8),
            "Dikenal sebagai bentuk teko (teapot), mengarah ke pusat galaksi Bima Sakti.",
            listOf(0.2f to 0.3f, 0.4f to 0.25f, 0.55f to 0.3f, 0.6f to 0.5f, 0.45f to 0.6f, 0.25f to 0.55f, 0.7f to 0.4f),
            listOf(0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 0, 2 to 6)
        ),
        ConstellationInfo("Centaurus", "Centaurus", "🐎", listOf(3, 4, 5, 6),
            "Rasi besar di langit selatan, rumah dari Alpha Centauri, bintang tetangga terdekat Matahari.",
            listOf(0.7f to 0.5f, 0.5f to 0.55f, 0.4f to 0.3f, 0.6f to 0.25f, 0.3f to 0.8f),
            listOf(0 to 1, 1 to 2, 1 to 3, 1 to 4)
        )
    )

    fun forMonth(month: Int): List<ConstellationInfo> = all.filter { month in it.visibleMonths }
}