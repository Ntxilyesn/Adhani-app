package com.example.adhani.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adhani.model.AccentPalette
import com.example.adhani.model.AdhanVoice
import com.example.adhani.model.AppThemeMode
import com.example.adhani.model.CalculationMethod
import com.example.adhani.model.HighLatitudeRule
import com.example.adhani.model.JuristicMethod
import com.example.adhani.model.PrayerName
import com.example.adhani.ui.components.HapticUtil
import com.example.adhani.ui.components.LiquidGlassCard
import com.example.adhani.viewmodel.AdhaniUiState
import com.example.adhani.viewmodel.AdhaniViewModel
import com.example.ui.theme.ArabicTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AdhaniViewModel,
    uiState: AdhaniUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Preferences & Studio",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تخصيص التطبيق والمواقيت",
                    style = ArabicTextStyle.copy(fontSize = 18.sp),
                    color = primaryColor
                )
            }
        }

        // 1. Theme Engine & Custom Studio Card
        item {
            ThemeStudioCard(
                themeMode = uiState.themeMode,
                activePalette = uiState.accentPalette,
                glassOpacity = uiState.glassOpacity,
                motionSpeed = uiState.motionSpeedMultiplier,
                onThemeModeChange = { viewModel.setThemeMode(it) },
                onPaletteChange = { viewModel.setAccentPalette(it) },
                onOpacityChange = { viewModel.setGlassOpacity(it) },
                onMotionChange = { viewModel.setMotionSpeed(it) }
            )
        }

        // 2. Prayer Calculation Engine Settings
        item {
            CalculationSettingsCard(
                method = uiState.calculationMethod,
                juristic = uiState.juristicMethod,
                highLat = uiState.highLatRule,
                onMethodChange = { viewModel.setCalculationMethod(it) },
                onJuristicChange = { viewModel.setJuristicMethod(it) },
                onHighLatChange = { viewModel.setHighLatitudeRule(it) }
            )
        }

        // 3. Prayer Offsets Fine-Tuning
        item {
            PrayerOffsetsCard(
                offsets = uiState.prayerOffsets,
                onOffsetChange = { name, offset -> viewModel.setPrayerOffset(name, offset) }
            )
        }

        // 4. Adhan Audio & Alerts
        item {
            AdhanPreferencesCard(
                activeVoice = uiState.adhanVoice,
                isPlaying = uiState.isAdhanPreviewPlaying,
                onVoiceChange = { viewModel.setAdhanVoice(it) },
                onTogglePlay = {
                    HapticUtil.playClick(context)
                    viewModel.toggleAdhanPreview()
                }
            )
        }

        // 5. Notifications Manager
        item {
            NotificationSettingsCard(
                isEnabled = uiState.isPrayerNotificationEnabled,
                onToggle = {
                    HapticUtil.playClick(context)
                    viewModel.togglePrayerNotification(it)
                },
                onTestPush = {
                    HapticUtil.playClick(context)
                    viewModel.updateNotification()
                }
            )
        }

        // 6. About & Islamic Astronomy Card
        item {
            AboutCard(primaryColor = primaryColor)
        }
    }
}

@Composable
private fun ThemeStudioCard(
    themeMode: AppThemeMode,
    activePalette: AccentPalette,
    glassOpacity: Float,
    motionSpeed: Float,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onPaletteChange: (AccentPalette) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onMotionChange: (Float) -> Unit
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        modifier = Modifier.fillMaxWidth().testTag("theme_studio_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Theme Studio & Liquid Glass",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Theme Mode Selector (Light, Dark, OLED, Dynamic)
            Text(
                text = "Surface Appearance",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair(AppThemeMode.LIGHT, "Light"),
                    Pair(AppThemeMode.DARK, "Dark"),
                    Pair(AppThemeMode.OLED, "OLED"),
                    Pair(AppThemeMode.DYNAMIC, "Auto")
                ).forEach { (mode, label) ->
                    val isSelected = themeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { onThemeModeChange(mode) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accent Palette Swatches
            Text(
                text = "Accent Palette: ${activePalette.displayName}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AccentPalette.entries.forEach { palette ->
                    val isSelected = activePalette == palette
                    val color = Color(palette.primaryHex)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onPaletteChange(palette) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glass Opacity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Glass Frost Opacity",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${(glassOpacity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = glassOpacity,
                onValueChange = onOpacityChange,
                valueRange = 0.12f..0.45f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            // Fluid Motion Speed Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Fluid Dock Animation Speed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "${String.format("%.1f", motionSpeed)}x",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = motionSpeed,
                onValueChange = onMotionChange,
                valueRange = 0.5f..1.8f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculationSettingsCard(
    method: CalculationMethod,
    juristic: JuristicMethod,
    highLat: HighLatitudeRule,
    onMethodChange: (CalculationMethod) -> Unit,
    onJuristicChange: (JuristicMethod) -> Unit,
    onHighLatChange: (HighLatitudeRule) -> Unit
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        modifier = Modifier.fillMaxWidth().testTag("calculation_settings_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Calculation Standards",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Method Picker dropdown
            var methodExpanded by remember { mutableStateOf(false) }
            Text(
                text = "Astronomical Method",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = methodExpanded,
                onExpandedChange = { methodExpanded = it }
            ) {
                OutlinedTextField(
                    value = method.title,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                ExposedDropdownMenu(
                    expanded = methodExpanded,
                    onDismissRequest = { methodExpanded = false }
                ) {
                    CalculationMethod.entries.forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m.title) },
                            onClick = {
                                onMethodChange(m)
                                methodExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Asr Juristic Method Toggle
            Text(
                text = "Asr Shadow Factor (Fiqh)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JuristicMethod.entries.forEach { j ->
                    val isSelected = juristic == j
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { onJuristicChange(j) }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (j == JuristicMethod.STANDARD) "Standard (1x Shadow)" else "Hanafi (2x Shadow)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrayerOffsetsCard(
    offsets: Map<PrayerName, Int>,
    onOffsetChange: (PrayerName, Int) -> Unit
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        modifier = Modifier.fillMaxWidth().testTag("prayer_offsets_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Manual Offsets (Minutes)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            listOf(PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA).forEach { prayer ->
                val currentOffset = offsets[prayer] ?: 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prayer.englishName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { onOffsetChange(prayer, (currentOffset - 1).coerceAtLeast(-15)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = if (currentOffset > 0) "+$currentOffset min" else "$currentOffset min",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (currentOffset != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { onOffsetChange(prayer, (currentOffset + 1).coerceAtMost(15)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdhanPreferencesCard(
    activeVoice: AdhanVoice,
    isPlaying: Boolean,
    onVoiceChange: (AdhanVoice) -> Unit,
    onTogglePlay: () -> Unit
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        modifier = Modifier.fillMaxWidth().testTag("adhan_prefs_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Adhan Reciter Voice",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onTogglePlay,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Test",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPlaying) "Stop" else "Test", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            AdhanVoice.entries.forEach { voice ->
                val isSelected = activeVoice == voice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else Color.Transparent
                        )
                        .clickable { onVoiceChange(voice) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = voice.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = voice.origin,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationSettingsCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onTestPush: () -> Unit
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        modifier = Modifier.fillMaxWidth().testTag("notification_settings_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Prayer Times Notification",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Show ongoing notification with live countdown to next prayer and today's full timetable directly in the Android status bar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (isEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onTestPush,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Update / Show Notification Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AboutCard(primaryColor: Color) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        modifier = Modifier.fillMaxWidth().testTag("about_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = primaryColor)
                Text(
                    text = "About Adhani",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Adhani is crafted with modern Material 3 design and a signature Liquid Glass experience. Features high-precision astronomical solar positioning, great-circle Qibla mathematics, Umm al-Qura Hijri lunar tracking, and Hisn al-Muslim supplications with an interactive haptic digital Tasbih.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Version 1.0 • Offline Ready & Privacy Respecting",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = primaryColor
            )
        }
    }
}
