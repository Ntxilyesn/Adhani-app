package com.example

import com.example.adhani.engine.HijriEngine
import com.example.adhani.engine.PrayerEngine
import com.example.adhani.engine.QiblaEngine
import com.example.adhani.model.CalculationMethod
import com.example.adhani.model.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun prayerEngine_calculatesAllSixPrayersInOrder() {
        val calendar = Calendar.getInstance()
        // Cairo coordinates: 30.0444° N, 31.2357° E
        val times = PrayerEngine.calculatePrayerTimes(
            calendar = calendar,
            latitude = 30.0444,
            longitude = 31.2357,
            timezoneOffset = 2.0,
            method = CalculationMethod.EGYPT
        )

        assertEquals(7, times.size) // 6 prayers + Qiyam
        val names = times.map { it.name }
        assertTrue(names.contains(PrayerName.FAJR))
        assertTrue(names.contains(PrayerName.SUNRISE))
        assertTrue(names.contains(PrayerName.DHUHR))
        assertTrue(names.contains(PrayerName.ASR))
        assertTrue(names.contains(PrayerName.MAGHRIB))
        assertTrue(names.contains(PrayerName.ISHA))

        val fajr = times.first { it.name == PrayerName.FAJR }
        val sunrise = times.first { it.name == PrayerName.SUNRISE }
        val dhuhr = times.first { it.name == PrayerName.DHUHR }
        val asr = times.first { it.name == PrayerName.ASR }
        val maghrib = times.first { it.name == PrayerName.MAGHRIB }
        val isha = times.first { it.name == PrayerName.ISHA }

        assertTrue("Fajr must be before Sunrise", fajr.timeMillis < sunrise.timeMillis)
        assertTrue("Sunrise must be before Dhuhr", sunrise.timeMillis < dhuhr.timeMillis)
        assertTrue("Dhuhr must be before Asr", dhuhr.timeMillis < asr.timeMillis)
        assertTrue("Asr must be before Maghrib", asr.timeMillis < maghrib.timeMillis)
        assertTrue("Maghrib must be before Isha", maghrib.timeMillis < isha.timeMillis)
    }

    @Test
    fun qiblaEngine_calculatesBearingAccurately() {
        // From Cairo (30.0444° N, 31.2357° E) to Makkah (~136° South-East)
        val bearing = QiblaEngine.calculateQiblaBearing(30.0444, 31.2357)
        assertTrue("Bearing should be between 130 and 140 degrees", bearing in 130.0..140.0)

        // Distance from Cairo to Makkah (~1280 km)
        val distance = QiblaEngine.calculateDistanceToKaabaKm(30.0444, 31.2357)
        assertTrue("Distance should be around 1200-1400 km", distance in 1200.0..1400.0)
    }

    @Test
    fun hijriEngine_calculatesValidHijriDateAndMoonPhase() {
        val calendar = Calendar.getInstance()
        val hijriDate = HijriEngine.calculateHijriDate(calendar)

        assertTrue(hijriDate.day in 1..30)
        assertTrue(hijriDate.year >= 1445)
        assertNotNull(hijriDate.monthNameEn)
        assertNotNull(hijriDate.monthNameAr)

        val moon = HijriEngine.calculateMoonPhase(calendar)
        assertTrue(moon.illuminationPercent in 0..100)
        assertNotNull(moon.phaseName)
    }
}
