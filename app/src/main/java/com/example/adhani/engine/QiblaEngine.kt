package com.example.adhani.engine

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object QiblaEngine {

    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the true Qibla bearing in degrees (0..360) from a given GPS location.
     */
    fun calculateQiblaBearing(userLat: Double, userLon: Double): Double {
        val lat1 = Math.toRadians(userLat)
        val lon1 = Math.toRadians(userLon)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val lon2 = Math.toRadians(KAABA_LONGITUDE)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        return (bearingDeg + 360.0) % 360.0
    }

    /**
     * Calculates the great-circle distance to the Holy Kaaba in kilometers.
     */
    fun calculateDistanceToKaabaKm(userLat: Double, userLon: Double): Double {
        val dLat = Math.toRadians(KAABA_LATITUDE - userLat)
        val dLon = Math.toRadians(KAABA_LONGITUDE - userLon)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LATITUDE)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }
}
