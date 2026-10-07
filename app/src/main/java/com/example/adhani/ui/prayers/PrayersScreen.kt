package com.example.adhani.ui.prayers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adhani.model.CityLocation
import com.example.adhani.model.PrayerTimeItem
import com.example.adhani.ui.components.HapticUtil
import com.example.adhani.ui.components.LiquidProgressRing
import com.example.adhani.viewmodel.AdhaniUiState
import com.example.adhani.viewmodel.AdhaniViewModel
import com.example.ui.theme.ArabicTextStyle
import com.example.ui.theme.ArabicTitleStyle

@Composable
fun PrayersScreen(
    viewModel: AdhaniViewModel,
    uiState: AdhaniUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCityPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("prayers_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Material 3 Location Bar with GPS Refresh and Hijri Badge
        item {
            PrayersLocationHeader(
                uiState = uiState,
                onLocationClick = { showCityPicker = true },
                onRefreshGps = {
                    HapticUtil.playClick(context)
                    viewModel.setAutoGpsEnabled(true)
                }
            )
        }

        // 2. Material 3 Hero Card: Live countdown + Next Prayer with Liquid Progress Ring
        item {
            HeroNextPrayerCard(
                uiState = uiState,
                onAdhanPreviewClick = {
                    HapticUtil.playClick(context)
                    viewModel.toggleAdhanPreview()
                }
            )
        }

        // 3. Notification Status Banner (Material 3 Outlined Card)
        item {
            NotificationQuickToggleCard(
                isEnabled = uiState.isPrayerNotificationEnabled,
                onToggle = {
                    HapticUtil.playClick(context)
                    viewModel.togglePrayerNotification(it)
                }
            )
        }

        // 4. Section Title: Today's Schedule
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Prayer Schedule",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "مواقيت اليوم",
                    style = ArabicTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 5. Prayer timetable items (Material 3 Cards)
        items(uiState.todayPrayers, key = { it.name.name }) { item ->
            PrayerItemM3Card(
                item = item,
                primaryColor = MaterialTheme.colorScheme.primary
            )
        }

        // 6. Real Recorded Adhan Audio Card
        item {
            RealAdhanAudioCard(
                uiState = uiState,
                onTogglePlay = {
                    HapticUtil.playClick(context)
                    viewModel.toggleAdhanPreview()
                }
            )
        }
    }

    if (showCityPicker) {
        CityPickerDialog(
            query = uiState.citySearchQuery,
            cities = uiState.filteredCities,
            selectedCity = uiState.selectedCity,
            onSearch = { viewModel.searchCity(it) },
            onSelect = {
                viewModel.selectCity(it)
                showCityPicker = false
            },
            onDismiss = { showCityPicker = false }
        )
    }
}

@Composable
private fun PrayersLocationHeader(
    uiState: AdhaniUiState,
    onLocationClick: () -> Unit,
    onRefreshGps: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().testTag("prayers_location_header")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location Badge & Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onLocationClick() }
                    .padding(vertical = 4.dp, horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${uiState.selectedCity.name}, ${uiState.selectedCity.country}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select City",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (uiState.isAutoGpsEnabled) "GPS Auto-Detected • ${uiState.calculationMethod.title.take(18)}..."
                        else "Manual • ${uiState.calculationMethod.title.take(18)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick GPS Refresh Button
            FilledTonalIconButton(
                onClick = onRefreshGps,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Refresh GPS",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroNextPrayerCard(
    uiState: AdhaniUiState,
    onAdhanPreviewClick: () -> Unit
) {
    val next = uiState.nextPrayer
    val primaryColor = MaterialTheme.colorScheme.primary

    ElevatedCard(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_prayer_card")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val heroResId = com.example.R.drawable.hero_masjid_night_1791397479579
            Image(
                painter = painterResource(id = heroResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(28.dp))
            )

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.45f),
                            0.5f to Color.Black.copy(alpha = 0.68f),
                            1.0f to Color.Black.copy(alpha = 0.90f)
                        )
                    )
            )

            // Content inside hero
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UPCOMING PRAYER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    AssistChip(
                        onClick = onAdhanPreviewClick,
                        label = {
                            Text(
                                text = if (uiState.isAdhanPreviewPlaying) "Stop Adhan" else "Real Adhan Voice",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (uiState.isAdhanPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(alpha = 0.18f)
                        ),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color.White.copy(alpha = 0.3f))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Circular Liquid Wave Progress Ring
                LiquidProgressRing(
                    progress = uiState.prayerProgressFraction,
                    modifier = Modifier.size(126.dp),
                    primaryColor = primaryColor,
                    secondaryColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = next?.name?.englishName ?: "Fajr",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = next?.name?.arabicName ?: "الفجر",
                            style = ArabicTitleStyle.copy(fontSize = 18.sp),
                            color = primaryColor
                        )
                        Text(
                            text = next?.formattedTime ?: "--:--",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "in ${uiState.countdownText}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationQuickToggleCard(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    OutlinedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier.fillMaxWidth().testTag("notification_toggle_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (isEnabled) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = "Notifications",
                        tint = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Prayer Times in Notification Bar",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEnabled) "Active: Showing live countdown & schedule" else "Turn on to show times in status bar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
    }
}

@Composable
private fun PrayerItemM3Card(
    item: PrayerTimeItem,
    primaryColor: Color
) {
    val isNext = item.isNext
    val isPast = item.isPast

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isNext) 6.dp else 1.5.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isNext) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_row_${item.name.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNext) primaryColor
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isNext) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Next Prayer",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    } else if (isPast) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Past Prayer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.name.englishName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = if (isNext) primaryColor else MaterialTheme.colorScheme.onSurface
                        )
                        if (isNext) {
                            Text(
                                text = "NEXT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(primaryColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = item.name.arabicName,
                        style = ArabicTextStyle.copy(fontSize = 15.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = item.formattedTime,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.SemiBold
                ),
                color = if (isNext) primaryColor else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = if (isPast) 0.6f else 1.0f
                )
            )
        }
    }
}

@Composable
private fun RealAdhanAudioCard(
    uiState: AdhaniUiState,
    onTogglePlay: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth().testTag("real_adhan_audio_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Real Adhan Voice",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Real Adhan: ${uiState.adhanVoice.title}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Authentic Audio • ${uiState.adhanVoice.origin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onTogglePlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (uiState.isAdhanPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Stop",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (uiState.isAdhanPreviewPlaying) "Stop" else "Listen",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun CityPickerDialog(
    query: String,
    cities: List<CityLocation>,
    selectedCity: CityLocation,
    onSearch: (String) -> Unit,
    onSelect: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Location",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onSearch,
                    placeholder = { Text("Search city or country...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cities) { city ->
                        val isCurrent = city.name == selectedCity.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainer
                                )
                                .clickable { onSelect(city) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = city.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = city.country,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isCurrent) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
