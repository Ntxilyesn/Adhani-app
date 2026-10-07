package com.example.adhani.model

enum class AppThemeMode {
    LIGHT,
    DARK,
    OLED,
    DYNAMIC
}

enum class AccentPalette(val displayName: String, val primaryHex: Long, val secondaryHex: Long) {
    EMERALD("Emerald Oasis", 0xFF10B981, 0xFF059669),
    ROYAL_GOLD("Royal Makkah Gold", 0xFFEAB308, 0xFFCA8A04),
    SAPPHIRE("Celestial Cyan", 0xFF06B6D4, 0xFF0891B2),
    AMETHYST("Twilight Violet", 0xFF8B5CF6, 0xFF7C3AED),
    ROSE("Sunset Crimson", 0xFFF43F5E, 0xFFE11D48),
    TEAL("Serene Teal", 0xFF14B8A6, 0xFF0D9488)
}

enum class PrayerName(val englishName: String, val arabicName: String) {
    FAJR("Fajr", "الفجر"),
    SUNRISE("Sunrise", "الشروق"),
    DHUHR("Dhuhr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Isha", "العشاء"),
    QIYAM("Qiyam al-Layl", "قيام الليل")
}

data class PrayerTimeItem(
    val name: PrayerName,
    val timeMillis: Long,
    val formattedTime: String,
    val isNext: Boolean = false,
    val isPast: Boolean = false
)

enum class CalculationMethod(
    val title: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutes: Int? = null
) {
    MWL("Muslim World League", 18.0, 17.0),
    ISNA("Islamic Society of North America (ISNA)", 15.0, 15.0),
    EGYPT("Egyptian General Authority", 19.5, 17.5),
    UMM_AL_QURA("Umm al-Qura University, Makkah", 18.5, 0.0, ishaMinutes = 90),
    KARACHI("Univ. of Islamic Sciences, Karachi", 18.0, 18.0),
    DUBAI("Dubai / UAE Awqaf", 18.2, 18.2),
    DIYANET("Diyanet (Turkey)", 18.0, 17.0),
    GULF("Gulf / Kuwait", 19.5, 0.0, ishaMinutes = 90),
    FRANCE("UOIF (France 12°)", 12.0, 12.0),
    SINGAPORE("MUIS (Singapore)", 20.0, 18.0),
    TEHRAN("Institute of Geophysics, Tehran", 17.7, 14.0)
}

enum class JuristicMethod(val title: String, val shadowMultiplier: Int) {
    STANDARD("Shafi'i, Maliki, Hanbali (Shadow 1x)", 1),
    HANAFI("Hanafi (Shadow 2x)", 2)
}

enum class HighLatitudeRule(val title: String) {
    MIDDLE_OF_NIGHT("Middle of the Night"),
    ONE_SEVENTH("One-Seventh of Night"),
    ANGLE_BASED("Angle-Based Method")
}

data class CityLocation(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneOffsetHours: Double
)

enum class AdhanVoice(val title: String, val origin: String) {
    MAKKAH("Makkah Al-Mukarramah", "Masjid al-Haram"),
    MADINAH("Madinah Al-Munawwarah", "Al-Masjid an-Nabawi"),
    AL_AQSA("Al-Quds Al-Sharif", "Al-Aqsa Mosque"),
    MUSTAFA_ISMAIL("Sheikh Mustafa Ismail", "Classic Egyptian"),
    GENTLE_TAKBEER("Gentle Takbeerat", "Soft Chime")
}

data class HijriDate(
    val day: Int,
    val monthNameAr: String,
    val monthNameEn: String,
    val year: Int,
    val dayNameEn: String,
    val dayNameAr: String
)

data class IslamicEvent(
    val titleEn: String,
    val titleAr: String,
    val hijriDay: Int,
    val hijriMonth: Int,
    val description: String,
    val daysRemaining: Int = 0
)

data class DhikrItem(
    val id: String,
    val category: DhikrCategory,
    val arabicText: String,
    val transliteration: String,
    val translation: String,
    val virtue: String,
    val source: String,
    val targetCount: Int
)

enum class DhikrCategory(val titleEn: String, val titleAr: String) {
    MORNING("Morning Adhkar", "أذكار الصباح"),
    EVENING("Evening Adhkar", "أذكار المساء"),
    POST_PRAYER("After Prayer", "أذكار بعد الصلاة"),
    SLEEP("Before Sleep", "أذكار النوم"),
    TRAVEL("Travel & Journey", "أذكار السفر"),
    DAILY("Daily Remembrance", "أدعية يومية")
}
