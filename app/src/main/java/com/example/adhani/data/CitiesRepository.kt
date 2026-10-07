package com.example.adhani.data

import com.example.adhani.model.CityLocation

object CitiesRepository {

    val defaultCity = CityLocation("Makkah", "Saudi Arabia", 21.4225, 39.8262, 3.0)

    val popularCities: List<CityLocation> = listOf(
        CityLocation("Makkah", "Saudi Arabia", 21.4225, 39.8262, 3.0),
        CityLocation("Madinah", "Saudi Arabia", 24.5247, 39.5692, 3.0),
        CityLocation("Jerusalem (Al-Quds)", "Palestine", 31.7683, 35.2137, 3.0),
        CityLocation("Cairo", "Egypt", 30.0444, 31.2357, 2.0),
        CityLocation("Istanbul", "Turkey", 41.0082, 28.9784, 3.0),
        CityLocation("Dubai", "United Arab Emirates", 25.2048, 55.2708, 4.0),
        CityLocation("Riyadh", "Saudi Arabia", 24.7136, 46.6753, 3.0),
        CityLocation("Doha", "Qatar", 25.2854, 51.5310, 3.0),
        CityLocation("Kuwait City", "Kuwait", 29.3759, 47.9774, 3.0),
        CityLocation("Muscat", "Oman", 23.5880, 58.3829, 4.0),
        CityLocation("Casablanca", "Morocco", 33.5731, -7.5898, 1.0),
        CityLocation("Tunis", "Tunisia", 36.8065, 10.1815, 1.0),
        CityLocation("Algiers", "Algeria", 36.7538, 3.0588, 1.0),
        CityLocation("Amman", "Jordan", 31.9454, 35.9284, 3.0),
        CityLocation("Beirut", "Lebanon", 33.8938, 35.5018, 3.0),
        CityLocation("Baghdad", "Iraq", 33.3152, 44.3661, 3.0),
        CityLocation("Jakarta", "Indonesia", -6.2088, 106.8456, 7.0),
        CityLocation("Kuala Lumpur", "Malaysia", 3.1390, 101.6869, 8.0),
        CityLocation("Karachi", "Pakistan", 24.8607, 67.0011, 5.0),
        CityLocation("Lahore", "Pakistan", 31.5204, 74.3587, 5.0),
        CityLocation("Dhaka", "Bangladesh", 23.8103, 90.4125, 6.0),
        CityLocation("London", "United Kingdom", 51.5074, -0.1278, 1.0),
        CityLocation("Paris", "France", 48.8566, 2.3522, 2.0),
        CityLocation("Berlin", "Germany", 52.5200, 13.4050, 2.0),
        CityLocation("New York", "United States", 40.7128, -74.0060, -4.0),
        CityLocation("Chicago", "United States", 41.8781, -87.6298, -5.0),
        CityLocation("Los Angeles", "United States", 34.0522, -118.2437, -7.0),
        CityLocation("Toronto", "Canada", 43.6532, -79.3832, -4.0),
        CityLocation("Sydney", "Australia", -33.8688, 151.2093, 10.0),
        CityLocation("Tokyo", "Japan", 35.6762, 139.6503, 9.0),
        CityLocation("Cape Town", "South Africa", -33.9249, 18.4241, 2.0)
    )

    fun searchCities(query: String): List<CityLocation> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return popularCities
        return popularCities.filter {
            it.name.lowercase().contains(q) || it.country.lowercase().contains(q)
        }
    }
}
