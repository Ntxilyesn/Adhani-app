package com.example.adhani.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.adhani.data.AdhkarRepository
import com.example.adhani.data.CitiesRepository
import com.example.adhani.engine.AdhanAudioEngine
import com.example.adhani.engine.AdhaniNotificationHelper
import com.example.adhani.engine.HijriEngine
import com.example.adhani.engine.PrayerEngine
import com.example.adhani.engine.QiblaEngine
import com.example.adhani.model.AccentPalette
import com.example.adhani.model.AdhanVoice
import com.example.adhani.model.AppThemeMode
import com.example.adhani.model.CalculationMethod
import com.example.adhani.model.CityLocation
import com.example.adhani.model.DhikrCategory
import com.example.adhani.model.DhikrItem
import com.example.adhani.model.HighLatitudeRule
import com.example.adhani.model.HijriDate
import com.example.adhani.model.IslamicEvent
import com.example.adhani.model.JuristicMethod
import com.example.adhani.model.PrayerName
import com.example.adhani.model.PrayerTimeItem
import com.example.adhani.ui.components.AppTab
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

data class AdhaniUiState(
    val currentTab: AppTab = AppTab.PRAYERS,

    // Theme & Styling
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val accentPalette: AccentPalette = AccentPalette.EMERALD,
    val glassOpacity: Float = 0.28f,
    val glassBlurDp: Float = 24f,
    val motionSpeedMultiplier: Float = 1.0f,

    // Location
    val selectedCity: CityLocation = CitiesRepository.defaultCity,
    val isAutoGpsEnabled: Boolean = true,
    val citySearchQuery: String = "",
    val filteredCities: List<CityLocation> = CitiesRepository.popularCities,

    // Prayer times
    val calculationMethod: CalculationMethod = CalculationMethod.MWL,
    val juristicMethod: JuristicMethod = JuristicMethod.STANDARD,
    val highLatRule: HighLatitudeRule = HighLatitudeRule.ANGLE_BASED,
    val prayerOffsets: Map<PrayerName, Int> = emptyMap(),
    val todayPrayers: List<PrayerTimeItem> = emptyList(),
    val nextPrayer: PrayerTimeItem? = null,
    val countdownText: String = "--:--:--",
    val countdownSecondsRemaining: Long = 0,
    val prayerProgressFraction: Float = 0.0f,
    val adhanVoice: AdhanVoice = AdhanVoice.MAKKAH,
    val isAdhanPreviewPlaying: Boolean = false,
    val preAdhanAlertMinutes: Int = 10,
    val isPrayerNotificationEnabled: Boolean = true,

    // Qibla Compass
    val deviceHeading: Float = 0f,
    val qiblaBearing: Double = 0.0,
    val distanceToMakkahKm: Double = 0.0,
    val isAlignedWithQibla: Boolean = false,
    val manualHeadingOverride: Float = 0f,
    val isManualCompassMode: Boolean = false,
    val magneticSensorAccuracy: Int = 3,
    val showCalibrationHelper: Boolean = false,

    // Hijri Calendar
    val currentHijriDate: HijriDate = HijriEngine.calculateHijriDate(Calendar.getInstance()),
    val hijriDayOffset: Int = 0,
    val moonPhase: HijriEngine.MoonPhaseInfo = HijriEngine.calculateMoonPhase(Calendar.getInstance()),
    val upcomingEvents: List<IslamicEvent> = emptyList(),
    val selectedCalendarMonthOffset: Int = 0,

    // Adhkar & Tasbih
    val selectedDhikrCategory: DhikrCategory = DhikrCategory.MORNING,
    val adhkarList: List<DhikrItem> = AdhkarRepository.getByCategory(DhikrCategory.MORNING),
    val activeTasbihDhikr: DhikrItem? = null,
    val tasbihCount: Int = 0,
    val tasbihTarget: Int = 33,
    val todayTasbihTotal: Int = 142,
    val tasbihSoundEnabled: Boolean = true,
    val isFullscreenTasbihOled: Boolean = false
)

class AdhaniViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val _uiState = MutableStateFlow(AdhaniUiState())
    val uiState: StateFlow<AdhaniUiState> = _uiState.asStateFlow()

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

    private var countdownJob: Job? = null
    private var notificationTickCounter = 0

    init {
        loadPreferences()
        recalculateAll()
        startLiveCountdownTicker()
        registerSensors()
    }

    private fun loadPreferences() {
        val prefs = getApplication<Application>().getSharedPreferences("adhani_prefs", Context.MODE_PRIVATE)
        val themeModeName = prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        val accentName = prefs.getString("accent_palette", AccentPalette.EMERALD.name) ?: AccentPalette.EMERALD.name
        val notificationEnabled = prefs.getBoolean("prayer_notification_enabled", true)
        val savedTasbihToday = prefs.getInt("today_tasbih_total", 142)

        val initialCity = if (prefs.contains("city_name")) {
            val cityName = prefs.getString("city_name", "Makkah") ?: "Makkah"
            CitiesRepository.popularCities.firstOrNull { it.name == cityName } ?: CitiesRepository.defaultCity
        } else {
            detectInitialDeviceCity()
        }

        _uiState.update {
            it.copy(
                themeMode = runCatching { AppThemeMode.valueOf(themeModeName) }.getOrDefault(AppThemeMode.DARK),
                accentPalette = runCatching { AccentPalette.valueOf(accentName) }.getOrDefault(AccentPalette.EMERALD),
                selectedCity = initialCity,
                isPrayerNotificationEnabled = notificationEnabled,
                todayTasbihTotal = savedTasbihToday
            )
        }
    }

    private fun detectInitialDeviceCity(): CityLocation {
        val tz = TimeZone.getDefault()
        val tzHours = tz.getOffset(System.currentTimeMillis()) / 3600000.0
        val tzId = tz.id.lowercase()
        val country = Locale.getDefault().country.lowercase()

        val matching = CitiesRepository.popularCities.firstOrNull {
            val name = it.name.lowercase()
            val c = it.country.lowercase()
            tzId.contains(name) || tzId.contains(c) || c.contains(country)
        } ?: CitiesRepository.popularCities.minByOrNull {
            abs(it.timezoneOffsetHours - tzHours)
        }

        return matching ?: CitiesRepository.defaultCity
    }

    private fun savePreferences() {
        val prefs = getApplication<Application>().getSharedPreferences("adhani_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("theme_mode", _uiState.value.themeMode.name)
            .putString("accent_palette", _uiState.value.accentPalette.name)
            .putString("city_name", _uiState.value.selectedCity.name)
            .putBoolean("prayer_notification_enabled", _uiState.value.isPrayerNotificationEnabled)
            .putInt("today_tasbih_total", _uiState.value.todayTasbihTotal)
            .apply()
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        savePreferences()
    }

    fun setAccentPalette(palette: AccentPalette) {
        _uiState.update { it.copy(accentPalette = palette) }
        savePreferences()
    }

    fun setGlassOpacity(opacity: Float) {
        _uiState.update { it.copy(glassOpacity = opacity) }
    }

    fun setGlassBlur(blurDp: Float) {
        _uiState.update { it.copy(glassBlurDp = blurDp) }
    }

    fun setMotionSpeed(multiplier: Float) {
        _uiState.update { it.copy(motionSpeedMultiplier = multiplier) }
    }

    fun selectCity(city: CityLocation) {
        _uiState.update { it.copy(selectedCity = city, isAutoGpsEnabled = false) }
        savePreferences()
        recalculateAll()
        updateNotification()
    }

    fun setAutoGpsEnabled(enabled: Boolean, detectedLat: Double? = null, detectedLon: Double? = null, cityName: String? = null) {
        if (enabled && detectedLat != null && detectedLon != null) {
            val tzOffset = (Calendar.getInstance().timeZone.getOffset(System.currentTimeMillis()) / 3600000.0)
            val gpsCity = CityLocation(
                name = cityName ?: "Local Area",
                country = "Current Location",
                latitude = detectedLat,
                longitude = detectedLon,
                timezoneOffsetHours = tzOffset
            )
            _uiState.update { it.copy(selectedCity = gpsCity, isAutoGpsEnabled = true) }
        } else {
            _uiState.update { it.copy(isAutoGpsEnabled = enabled) }
        }
        recalculateAll()
        updateNotification()
    }

    fun searchCity(query: String) {
        val filtered = CitiesRepository.searchCities(query)
        _uiState.update { it.copy(citySearchQuery = query, filteredCities = filtered) }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        _uiState.update { it.copy(calculationMethod = method) }
        recalculateAll()
        updateNotification()
    }

    fun setJuristicMethod(method: JuristicMethod) {
        _uiState.update { it.copy(juristicMethod = method) }
        recalculateAll()
        updateNotification()
    }

    fun setHighLatitudeRule(rule: HighLatitudeRule) {
        _uiState.update { it.copy(highLatRule = rule) }
        recalculateAll()
        updateNotification()
    }

    fun setPrayerOffset(prayerName: PrayerName, offsetMinutes: Int) {
        val currentOffsets = _uiState.value.prayerOffsets.toMutableMap()
        currentOffsets[prayerName] = offsetMinutes
        _uiState.update { it.copy(prayerOffsets = currentOffsets) }
        recalculateAll()
        updateNotification()
    }

    fun setAdhanVoice(voice: AdhanVoice) {
        _uiState.update { it.copy(adhanVoice = voice) }
    }

    fun toggleAdhanPreview() {
        val context = getApplication<Application>()
        if (_uiState.value.isAdhanPreviewPlaying) {
            AdhanAudioEngine.stop()
            _uiState.update { it.copy(isAdhanPreviewPlaying = false) }
        } else {
            _uiState.update { it.copy(isAdhanPreviewPlaying = true) }
            AdhanAudioEngine.playAdhan(context, _uiState.value.adhanVoice, viewModelScope) {
                _uiState.update { it.copy(isAdhanPreviewPlaying = false) }
            }
        }
    }

    fun togglePrayerNotification(enabled: Boolean) {
        _uiState.update { it.copy(isPrayerNotificationEnabled = enabled) }
        savePreferences()
        if (enabled) {
            updateNotification()
        } else {
            AdhaniNotificationHelper.cancelNotification(getApplication())
        }
    }

    fun updateNotification() {
        if (_uiState.value.isPrayerNotificationEnabled) {
            AdhaniNotificationHelper.showPrayerNotification(
                context = getApplication(),
                cityName = _uiState.value.selectedCity.name,
                nextPrayer = _uiState.value.nextPrayer,
                countdownText = _uiState.value.countdownText,
                allPrayers = _uiState.value.todayPrayers
            )
        }
    }

    fun setHijriOffset(offsetDays: Int) {
        _uiState.update { it.copy(hijriDayOffset = offsetDays.coerceIn(-2, 2)) }
        val cal = Calendar.getInstance()
        val hijri = HijriEngine.calculateHijriDate(cal, _uiState.value.hijriDayOffset)
        val events = HijriEngine.getUpcomingEvents(hijri)
        _uiState.update { it.copy(currentHijriDate = hijri, upcomingEvents = events) }
    }

    fun setCalendarMonthOffset(offset: Int) {
        _uiState.update { it.copy(selectedCalendarMonthOffset = offset) }
    }

    fun setDhikrCategory(category: DhikrCategory) {
        val list = AdhkarRepository.getByCategory(category)
        _uiState.update { it.copy(selectedDhikrCategory = category, adhkarList = list) }
    }

    fun openTasbihForDhikr(dhikr: DhikrItem?) {
        _uiState.update {
            it.copy(
                activeTasbihDhikr = dhikr,
                tasbihCount = 0,
                tasbihTarget = dhikr?.targetCount ?: 33
            )
        }
    }

    fun incrementTasbih() {
        _uiState.update { state ->
            val newCount = state.tasbihCount + 1
            val newTotal = state.todayTasbihTotal + 1
            val reachedTarget = newCount >= state.tasbihTarget
            state.copy(
                tasbihCount = if (reachedTarget) 0 else newCount,
                todayTasbihTotal = newTotal
            )
        }
        savePreferences()
    }

    fun resetTasbih() {
        _uiState.update { it.copy(tasbihCount = 0) }
    }

    fun setTasbihTarget(target: Int) {
        _uiState.update { it.copy(tasbihTarget = target) }
    }

    fun toggleTasbihSound() {
        _uiState.update { it.copy(tasbihSoundEnabled = !it.tasbihSoundEnabled) }
    }

    fun toggleFullscreenTasbihOled() {
        _uiState.update { it.copy(isFullscreenTasbihOled = !it.isFullscreenTasbihOled) }
    }

    fun setManualHeading(heading: Float) {
        _uiState.update {
            val normalized = (heading % 360f + 360f) % 360f
            val isAligned = abs(normalized - it.qiblaBearing.toFloat()) <= 3.5f
            it.copy(
                manualHeadingOverride = normalized,
                isManualCompassMode = true,
                deviceHeading = normalized,
                isAlignedWithQibla = isAligned
            )
        }
    }

    fun toggleCalibrationHelper(show: Boolean) {
        _uiState.update { it.copy(showCalibrationHelper = show) }
    }

    private fun recalculateAll() {
        val state = _uiState.value
        val cal = Calendar.getInstance()

        // Exact local timezone offset accounting for daylight savings
        val effectiveTzOffset = if (state.isAutoGpsEnabled) {
            cal.timeZone.getOffset(cal.timeInMillis) / 3600000.0
        } else {
            state.selectedCity.timezoneOffsetHours
        }

        // 1. Precise astronomical prayer times
        val times = PrayerEngine.calculatePrayerTimes(
            calendar = cal,
            latitude = state.selectedCity.latitude,
            longitude = state.selectedCity.longitude,
            timezoneOffset = effectiveTzOffset,
            method = state.calculationMethod,
            juristicMethod = state.juristicMethod,
            highLatRule = state.highLatRule,
            offsets = state.prayerOffsets
        )

        val next = times.firstOrNull { it.isNext }

        // 2. Qibla bearing & distance
        val bearing = QiblaEngine.calculateQiblaBearing(state.selectedCity.latitude, state.selectedCity.longitude)
        val distance = QiblaEngine.calculateDistanceToKaabaKm(state.selectedCity.latitude, state.selectedCity.longitude)

        // 3. Hijri Date & Events
        val hijri = HijriEngine.calculateHijriDate(cal, state.hijriDayOffset)
        val moon = HijriEngine.calculateMoonPhase(cal)
        val events = HijriEngine.getUpcomingEvents(hijri)

        _uiState.update {
            it.copy(
                todayPrayers = times,
                nextPrayer = next,
                qiblaBearing = bearing,
                distanceToMakkahKm = distance,
                currentHijriDate = hijri,
                moonPhase = moon,
                upcomingEvents = events
            )
        }
    }

    private fun startLiveCountdownTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                updateCountdown()
                delay(1000)
            }
        }
    }

    private fun updateCountdown() {
        val now = System.currentTimeMillis()
        val next = _uiState.value.nextPrayer

        if (next == null) {
            _uiState.update { it.copy(countdownText = "--:--:--", countdownSecondsRemaining = 0, prayerProgressFraction = 0f) }
            return
        }

        var diffMillis = next.timeMillis - now
        if (diffMillis < 0) {
            recalculateAll()
            diffMillis = (_uiState.value.nextPrayer?.timeMillis ?: now) - now
        }

        val totalSeconds = (diffMillis / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val formatted = String.format("%02dh %02dm %02ds", hours, minutes, seconds)

        val allTimes = _uiState.value.todayPrayers
        val nextIdx = allTimes.indexOfFirst { it.name == next.name }
        val prevIdx = if (nextIdx > 0) nextIdx - 1 else allTimes.size - 1
        val prevTimeMillis = allTimes.getOrNull(prevIdx)?.timeMillis ?: (next.timeMillis - 4 * 3600 * 1000)

        val totalInterval = (next.timeMillis - prevTimeMillis).coerceAtLeast(1)
        val elapsed = (now - prevTimeMillis).coerceAtLeast(0)
        val fraction = (elapsed.toFloat() / totalInterval.toFloat()).coerceIn(0.01f, 1.0f)

        _uiState.update {
            it.copy(
                countdownText = formatted,
                countdownSecondsRemaining = totalSeconds,
                prayerProgressFraction = fraction
            )
        }

        // Periodic notification sync (every 60 ticks)
        notificationTickCounter++
        if (notificationTickCounter >= 60) {
            notificationTickCounter = 0
            updateNotification()
        }
    }

    private fun registerSensors() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || _uiState.value.isManualCompassMode) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthRad = orientation[0]
            val azimuthDeg = (Math.toDegrees(azimuthRad.toDouble()).toFloat() + 360f) % 360f

            val qibla = _uiState.value.qiblaBearing.toFloat()
            val isAligned = abs(azimuthDeg - qibla) <= 3.5f

            _uiState.update {
                it.copy(
                    deviceHeading = azimuthDeg,
                    isAlignedWithQibla = isAligned,
                    magneticSensorAccuracy = event.accuracy
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _uiState.update { it.copy(magneticSensorAccuracy = accuracy) }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        AdhanAudioEngine.stop()
        countdownJob?.cancel()
    }
}
