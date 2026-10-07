package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adhani.ui.adhkar.AdhkarScreen
import com.example.adhani.ui.components.AppTab
import com.example.adhani.ui.components.HapticUtil
import com.example.adhani.ui.components.LiquidGlassBottomDock
import com.example.adhani.ui.hijri.HijriScreen
import com.example.adhani.ui.prayers.PrayersScreen
import com.example.adhani.ui.qibla.QiblaScreen
import com.example.adhani.ui.settings.SettingsScreen
import com.example.adhani.viewmodel.AdhaniViewModel
import com.example.ui.theme.AdhaniTheme
import com.example.ui.theme.ArabicTitleStyle
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdhaniApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhaniApp(viewModel: AdhaniViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Helper to resolve coordinates to city name and update ViewModel
    fun onLocationResolved(loc: Location) {
        var cityName: String? = null
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
            cityName = addresses?.firstOrNull()?.locality
                ?: addresses?.firstOrNull()?.subAdminArea
                ?: addresses?.firstOrNull()?.adminArea
        } catch (_: Exception) {}

        viewModel.setAutoGpsEnabled(
            enabled = true,
            detectedLat = loc.latitude,
            detectedLon = loc.longitude,
            cityName = cityName
        )
    }

    // Function to acquire location from FusedLocation or LocationManager
    fun requestLocationUpdate() {
        try {
            val fusedLocation = LocationServices.getFusedLocationProviderClient(context)
            fusedLocation.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc: Location? ->
                    if (loc != null) {
                        onLocationResolved(loc)
                    } else {
                        // Fallback to lastLocation
                        fusedLocation.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                onLocationResolved(lastLoc)
                            } else {
                                // Fallback to system LocationManager
                                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                                val gpsLoc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                                if (gpsLoc != null) {
                                    onLocationResolved(gpsLoc)
                                }
                            }
                        }
                    }
                }
        } catch (_: SecurityException) {}
    }

    // Permission launcher for Location and Notifications
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        } else true

        if (fineGranted || coarseGranted) {
            requestLocationUpdate()
        }
        if (notifGranted) {
            viewModel.updateNotification()
        }
    }

    LaunchedEffect(Unit) {
        val permsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine) {
            permissionsLauncher.launch(permsToRequest.toTypedArray())
        } else {
            requestLocationUpdate()
            viewModel.updateNotification()
        }
    }

    // Predictive Back Handling
    BackHandler(enabled = uiState.isFullscreenTasbihOled || uiState.currentTab != AppTab.PRAYERS) {
        if (uiState.isFullscreenTasbihOled) {
            viewModel.toggleFullscreenTasbihOled()
        } else {
            viewModel.selectTab(AppTab.PRAYERS)
        }
    }

    AdhaniTheme(
        themeMode = uiState.themeMode,
        accentPalette = uiState.accentPalette,
        glassOpacity = uiState.glassOpacity,
        glassBlurDp = uiState.glassBlurDp,
        motionSpeed = uiState.motionSpeedMultiplier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    if (!uiState.isFullscreenTasbihOled) {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Adhani",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "أَذَانِي",
                                        style = ArabicTitleStyle.copy(fontSize = 18.sp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                            ),
                            actions = {
                                IconButton(
                                    onClick = {
                                        HapticUtil.playClick(context)
                                        requestLocationUpdate()
                                    },
                                    modifier = Modifier.testTag("action_detect_location")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Detect Location",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        HapticUtil.playClick(context)
                                        viewModel.togglePrayerNotification(!uiState.isPrayerNotificationEnabled)
                                    },
                                    modifier = Modifier.testTag("action_toggle_notification")
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isPrayerNotificationEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                        contentDescription = "Toggle Notification",
                                        tint = if (uiState.isPrayerNotificationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                },
                bottomBar = {
                    if (!uiState.isFullscreenTasbihOled) {
                        LiquidGlassBottomDock(
                            currentTab = uiState.currentTab,
                            onTabSelected = { viewModel.selectTab(it) }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(
                        targetState = uiState.currentTab,
                        animationSpec = tween(durationMillis = (280 / uiState.motionSpeedMultiplier).toInt()),
                        label = "tab_crossfade"
                    ) { tab ->
                        when (tab) {
                            AppTab.PRAYERS -> PrayersScreen(viewModel = viewModel, uiState = uiState)
                            AppTab.QIBLA -> QiblaScreen(viewModel = viewModel, uiState = uiState)
                            AppTab.HIJRI -> HijriScreen(viewModel = viewModel, uiState = uiState)
                            AppTab.ADHKAR -> AdhkarScreen(viewModel = viewModel, uiState = uiState)
                            AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel, uiState = uiState)
                        }
                    }
                }
            }
        }
    }
}
