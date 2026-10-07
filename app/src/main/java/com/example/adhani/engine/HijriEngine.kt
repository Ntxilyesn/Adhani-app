package com.example.adhani.engine

import com.example.adhani.model.HijriDate
import com.example.adhani.model.IslamicEvent
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt

object HijriEngine {

    private val ARABIC_MONTHS = arrayOf(
        "محرّم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    private val ENGLISH_MONTHS = arrayOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val ENGLISH_DAYS = arrayOf(
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    )

    private val ARABIC_DAYS = arrayOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    data class MoonPhaseInfo(
        val phaseName: String,
        val illuminationPercent: Int,
        val ageDays: Double,
        val iconType: MoonIconType
    )

    enum class MoonIconType {
        NEW_MOON,
        WAXING_CRESCENT,
        FIRST_QUARTER,
        WAXING_GIBBOUS,
        FULL_MOON,
        WANING_GIBBOUS,
        LAST_QUARTER,
        WANING_CRESCENT
    }

    /**
     * Converts a calendar day into a HijriDate with an optional manual adjustment in days (-2..+2).
     */
    fun calculateHijriDate(calendar: Calendar, manualDayOffset: Int = 0): HijriDate {
        val cal = (calendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, manualDayOffset)
        }

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday

        // Kuwaiti / Tabular astronomical Julian conversion
        var m = month
        var y = year
        if (m < 3) {
            y -= 1
            m += 12
        }

        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        val z = jd - 1948440.0 + 10632.0
        val n = floor((z - 1.0) / 10631.0)
        val zPrime = z - 10631.0 * n + 354.0
        val j = (floor((10985.0 - zPrime) / 5316.0)) * (floor((50.0 * zPrime) / 17719.0)) +
                (floor(zPrime / 5670.0)) * (floor((43.0 * zPrime) / 15238.0))
        val zDoublePrime = zPrime - (floor((30.0 - j) / 15.0)) * (floor((17719.0 * j) / 50.0)) -
                (floor(j / 16.0)) * (floor((15238.0 * j) / 43.0)) + 29.0
        val mHijri = floor((24.0 * zDoublePrime) / 709.0).toInt()
        val dHijri = (zDoublePrime - floor((709.0 * mHijri) / 24.0)).toInt()
        val yHijri = (30 * n + j - 30).toInt()

        val safeMonthIdx = (mHijri - 1).coerceIn(0, 11)

        return HijriDate(
            day = dHijri.coerceIn(1, 30),
            monthNameAr = ARABIC_MONTHS[safeMonthIdx],
            monthNameEn = ENGLISH_MONTHS[safeMonthIdx],
            year = yHijri,
            dayNameEn = ENGLISH_DAYS[dayOfWeekIndex.coerceIn(0, 6)],
            dayNameAr = ARABIC_DAYS[dayOfWeekIndex.coerceIn(0, 6)]
        )
    }

    /**
     * Calculates current astronomical Moon Phase information.
     */
    fun calculateMoonPhase(calendar: Calendar): MoonPhaseInfo {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Known new moon reference Julian date (Jan 6, 2000 18:14 UTC = JD 2451549.5)
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5

        val synodicMonth = 29.530588853
        val daysSinceNew = (jd - 2451549.5) % synodicMonth
        val normalizedAge = if (daysSinceNew < 0) daysSinceNew + synodicMonth else daysSinceNew

        // Illumination: 0% at new moon, 100% at full moon (approx 14.76 days)
        val phaseAngle = (normalizedAge / synodicMonth) * 2.0 * Math.PI
        val illumination = ((1.0 - cos(phaseAngle)) / 2.0 * 100.0).roundToInt()

        val (name, iconType) = when {
            normalizedAge < 1.84 -> "New Moon (Hilal Jadid)" to MoonIconType.NEW_MOON
            normalizedAge < 7.38 -> "Waxing Crescent (Hilal)" to MoonIconType.WAXING_CRESCENT
            normalizedAge < 9.23 -> "First Quarter (Tarbi' Awwal)" to MoonIconType.FIRST_QUARTER
            normalizedAge < 13.82 -> "Waxing Gibbous (Ahdab Mutazaid)" to MoonIconType.WAXING_GIBBOUS
            normalizedAge < 15.66 -> "Full Moon (Badr)" to MoonIconType.FULL_MOON
            normalizedAge < 20.26 -> "Waning Gibbous (Ahdab Mutanaqis)" to MoonIconType.WANING_GIBBOUS
            normalizedAge < 22.10 -> "Last Quarter (Tarbi' Thani)" to MoonIconType.LAST_QUARTER
            normalizedAge < 27.68 -> "Waning Crescent (Mihsaq)" to MoonIconType.WANING_CRESCENT
            else -> "New Moon (Hilal Jadid)" to MoonIconType.NEW_MOON
        }

        return MoonPhaseInfo(
            phaseName = name,
            illuminationPercent = illumination,
            ageDays = (normalizedAge * 10.0).roundToInt() / 10.0,
            iconType = iconType
        )
    }

    /**
     * List of key upcoming Islamic holy events with approximate countdowns.
     */
    fun getUpcomingEvents(currentHijri: HijriDate): List<IslamicEvent> {
        val staticEvents = listOf(
            IslamicEvent("1st of Ramadan", "أول رمضان", 1, 9, "Beginning of the Blessed Month of Fasting"),
            IslamicEvent("Laylat al-Qadr", "ليلة القدر", 27, 9, "Night of Power and Decree (Better than 1,000 months)"),
            IslamicEvent("Eid al-Fitr", "عيد الفطر المبارك", 1, 10, "Festival of Breaking the Fast"),
            IslamicEvent("Day of Arafah", "يوم عرفة", 9, 12, "Pinnacle of Hajj pilgrimage, supreme day of Du'a"),
            IslamicEvent("Eid al-Adha", "عيد الأضحى المبارك", 10, 12, "Feast of Sacrifice"),
            IslamicEvent("Islamic New Year", "رأس السنة الهجرية", 1, 1, "Beginning of the Hijri New Year"),
            IslamicEvent("Day of Ashura", "يوم عاشوراء", 10, 1, "Fasting day of Prophet Musa's liberation"),
            IslamicEvent("Mawlid an-Nabi", "المولد النبوي", 12, 3, "Birth of Prophet Muhammad (PBUH)"),
            IslamicEvent("Isra and Mi'raj", "الإسراء والمعراج", 27, 7, "Night Journey and Heavenly Ascension"),
            IslamicEvent("Mid-Sha'ban", "ليلة النصف من شعبان", 15, 8, "Night of Forgiveness and preparation for Ramadan")
        )

        return staticEvents.map { event ->
            val daysDiff = calculateHijriDayDiff(currentHijri, event.hijriMonth, event.hijriDay)
            event.copy(daysRemaining = daysDiff)
        }.sortedBy { it.daysRemaining }
    }

    private fun calculateHijriDayDiff(current: HijriDate, targetMonth: Int, targetDay: Int): Int {
        val currentTotalDays = (current.monthNameEnIndex() * 30) + current.day
        val targetTotalDays = ((targetMonth - 1) * 30) + targetDay
        var diff = targetTotalDays - currentTotalDays
        if (diff < 0) {
            diff += 354 // Days in Islamic Lunar year
        }
        return diff
    }

    private fun HijriDate.monthNameEnIndex(): Int {
        val idx = ENGLISH_MONTHS.indexOf(this.monthNameEn)
        return if (idx >= 0) idx else 0
    }
}
