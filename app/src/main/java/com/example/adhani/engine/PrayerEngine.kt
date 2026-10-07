package com.example.adhani.engine

import com.example.adhani.model.CalculationMethod
import com.example.adhani.model.HighLatitudeRule
import com.example.adhani.model.JuristicMethod
import com.example.adhani.model.PrayerName
import com.example.adhani.model.PrayerTimeItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object PrayerEngine {

    /**
     * Calculates Prayer times for a given calendar day, coordinates, timezone, method, juristic rule, and offsets.
     * Uses standard PrayTimes astronomical solar coordinates.
     */
    fun calculatePrayerTimes(
        calendar: Calendar,
        latitude: Double,
        longitude: Double,
        timezoneOffset: Double,
        method: CalculationMethod,
        juristicMethod: JuristicMethod = JuristicMethod.STANDARD,
        highLatRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
        offsets: Map<PrayerName, Int> = emptyMap()
    ): List<PrayerTimeItem> {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Date
        val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)

        // Solar parameters
        val solar = sunPosition(jd)
        val declination = solar.declination
        val eqOfTime = solar.equationOfTime // In hours

        // Solar transit / Noon
        val transit = 12.0 + timezoneOffset - (longitude / 15.0) - eqOfTime

        // Sunrise & Sunset (zenith = 90.833° accounting for atmospheric refraction and solar radius)
        val sunHalfDay = sunHourAngle(90.833, latitude, declination)
        val sunriseHours = transit - sunHalfDay
        val sunsetHours = transit + sunHalfDay

        // Fajr
        var fajrHalfDay = sunHourAngle(90.0 + method.fajrAngle, latitude, declination)
        if (fajrHalfDay.isNaN()) {
            fajrHalfDay = adjustHighLat(sunHalfDay, method.fajrAngle, highLatRule)
        }
        val fajrHours = transit - fajrHalfDay

        // Dhuhr (transit + safety margin of ~1 min)
        val dhuhrHours = transit + (1.0 / 60.0)

        // Asr (shadow altitude)
        val shadowRatio = juristicMethod.shadowMultiplier.toDouble()
        val deltaLatRad = Math.toRadians(abs(latitude - declination))
        val asrAltitudeDeg = Math.toDegrees(atan(1.0 / (shadowRatio + tan(deltaLatRad))))
        val asrZenith = 90.0 - asrAltitudeDeg
        val asrHalfDay = sunHourAngle(asrZenith, latitude, declination)
        val asrHours = transit + asrHalfDay

        // Maghrib
        val maghribHours = sunsetHours + (1.0 / 60.0)

        // Isha
        val ishaHours = if (method.ishaMinutes != null) {
            maghribHours + (method.ishaMinutes / 60.0)
        } else {
            var ishaHalfDay = sunHourAngle(90.0 + method.ishaAngle, latitude, declination)
            if (ishaHalfDay.isNaN()) {
                ishaHalfDay = adjustHighLat(sunHalfDay, method.ishaAngle, highLatRule)
            }
            transit + ishaHalfDay
        }

        // Qiyam al-Layl (Last third of the night)
        val nightDurationHours = (24.0 - sunsetHours) + sunriseHours
        val qiyamHours = sunsetHours + (nightDurationHours * (2.0 / 3.0))

        val timesMap = mapOf(
            PrayerName.FAJR to fajrHours,
            PrayerName.SUNRISE to sunriseHours,
            PrayerName.DHUHR to dhuhrHours,
            PrayerName.ASR to asrHours,
            PrayerName.MAGHRIB to maghribHours,
            PrayerName.ISHA to ishaHours,
            PrayerName.QIYAM to qiyamHours
        )

        val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
        val nowMillis = System.currentTimeMillis()

        // Base calendar at start of day
        val dayStart = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val rawItems = timesMap.map { (name, decimalHours) ->
            val adjustedDecHours = decimalHours + (offsets[name] ?: 0) / 60.0
            val normalizedHours = (adjustedDecHours + 24.0) % 24.0
            val totalSeconds = (normalizedHours * 3600.0).toLong()

            val prayerCal = (dayStart.clone() as Calendar).apply {
                add(Calendar.SECOND, totalSeconds.toInt())
            }
            val timeMillis = prayerCal.timeInMillis
            val formatted = timeFormatter.format(Date(timeMillis))

            PrayerTimeItem(
                name = name,
                timeMillis = timeMillis,
                formattedTime = formatted,
                isPast = timeMillis < nowMillis
            )
        }.sortedBy { it.timeMillis }

        // Find next prayer
        val nextIndex = rawItems.indexOfFirst { !it.isPast }
        return rawItems.mapIndexed { index, item ->
            item.copy(isNext = index == nextIndex || (nextIndex == -1 && index == 0))
        }
    }

    private fun adjustHighLat(sunHalfDay: Double, angle: Double, rule: HighLatitudeRule): Double {
        val nightPortion = 12.0 - sunHalfDay
        return when (rule) {
            HighLatitudeRule.MIDDLE_OF_NIGHT -> nightPortion / 2.0
            HighLatitudeRule.ONE_SEVENTH -> nightPortion / 7.0
            HighLatitudeRule.ANGLE_BASED -> (angle / 60.0) * nightPortion
        }
    }

    /**
     * Calculates the hour angle (in hours) for a given zenith angle, latitude, and declination.
     */
    private fun sunHourAngle(zenith: Double, latitude: Double, declination: Double): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val zenRad = Math.toRadians(zenith)

        val cosH = (cos(zenRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        if (cosH < -1.0 || cosH > 1.0) {
            return Double.NaN
        }
        val hDeg = Math.toDegrees(acos(cosH))
        return hDeg / 15.0 // Convert degrees to hours
    }

    private data class SunData(val declination: Double, val equationOfTime: Double)

    private fun sunPosition(jd: Double): SunData {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val decRad = asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l)))
        val declination = Math.toDegrees(decRad)

        var raRad = atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))
        var raHours = fixAngle(Math.toDegrees(raRad)) / 15.0

        val eqOfTimeHours = fixHour(q / 15.0 - raHours)
        return SunData(declination, eqOfTimeHours)
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double): Double {
        var res = a - 360.0 * floor(a / 360.0)
        if (res < 0) res += 360.0
        return res
    }

    private fun fixHour(h: Double): Double {
        var res = h - 24.0 * floor(h / 24.0)
        if (res < 0) res += 24.0
        if (res > 12.0) res -= 24.0
        return res
    }
}
