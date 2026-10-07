package com.example.adhani.ui.hijri

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adhani.engine.HijriEngine
import com.example.adhani.model.IslamicEvent
import com.example.adhani.ui.components.LiquidGlassCard
import com.example.adhani.viewmodel.AdhaniUiState
import com.example.adhani.viewmodel.AdhaniViewModel
import com.example.ui.theme.ArabicTextStyle
import com.example.ui.theme.ArabicTitleStyle
import java.util.Calendar

@Composable
fun HijriScreen(
    viewModel: AdhaniViewModel,
    uiState: AdhaniUiState,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val hijri = uiState.currentHijriDate
    val moon = uiState.moonPhase

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("hijri_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header: Current Hijri Date Card
        item {
            HijriHeaderCard(
                hijri = hijri,
                offset = uiState.hijriDayOffset,
                onOffsetChange = { viewModel.setHijriOffset(it) }
            )
        }

        // 2. Moon Phase Hero Card
        item {
            MoonPhaseCard(moon = moon, primaryColor = primaryColor)
        }

        // 3. Interactive Monthly Calendar Grid
        item {
            HijriMonthCalendarGrid(
                currentHijriDay = hijri.day,
                monthNameEn = hijri.monthNameEn,
                monthNameAr = hijri.monthNameAr,
                year = hijri.year,
                primaryColor = primaryColor
            )
        }

        // 4. Section Title: Upcoming Islamic Events
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Islamic Holy Days",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "المناسبات الإسلامية",
                    style = ArabicTextStyle.copy(fontSize = 18.sp),
                    color = primaryColor
                )
            }
        }

        // 5. Holy Days List
        items(uiState.upcomingEvents, key = { it.titleEn }) { event ->
            IslamicEventCard(event = event, primaryColor = primaryColor)
        }
    }
}

@Composable
private fun HijriHeaderCard(
    hijri: com.example.adhani.model.HijriDate,
    offset: Int,
    onOffsetChange: (Int) -> Unit
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        modifier = Modifier.fillMaxWidth().testTag("hijri_header_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${hijri.dayNameEn} • ${hijri.dayNameAr}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${hijri.day} ${hijri.monthNameEn} ${hijri.year} AH",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${hijri.day} ${hijri.monthNameAr} ${hijri.year} هـ",
                style = ArabicTitleStyle.copy(fontSize = 22.sp),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Manual Moon-sighting Offset Selector (-2 to +2 days)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Moon-sighting Offset:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                IconButton(
                    onClick = { onOffsetChange(offset - 1) },
                    modifier = Modifier.size(24.dp),
                    enabled = offset > -2
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Decrease offset", modifier = Modifier.size(18.dp))
                }

                Text(
                    text = if (offset > 0) "+$offset day" else if (offset < 0) "$offset day" else "Exact (0)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = { onOffsetChange(offset + 1) },
                    modifier = Modifier.size(24.dp),
                    enabled = offset < 2
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Increase offset", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MoonPhaseCard(
    moon: HijriEngine.MoonPhaseInfo,
    primaryColor: Color
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        modifier = Modifier.fillMaxWidth().testTag("moon_phase_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LUNAR CYCLE & MOON PHASE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = moon.phaseName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Illumination: ${moon.illuminationPercent}% • Age: ${moon.ageDays} days",
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor
                )
            }

            // Visual Moon graphic canvas
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(54.dp)) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Moon base dark disk
                    drawCircle(color = Color(0xFF1E293B), radius = radius, center = center)

                    // Crescent / Gibbous illumination simulation
                    val illumFract = (moon.illuminationPercent / 100f).coerceIn(0.05f, 1.0f)
                    drawCircle(
                        color = Color(0xFFFDE68A),
                        radius = radius * illumFract,
                        center = center
                    )
                }
            }
        }
    }
}

@Composable
private fun HijriMonthCalendarGrid(
    currentHijriDay: Int,
    monthNameEn: String,
    monthNameAr: String,
    year: Int,
    primaryColor: Color
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        modifier = Modifier.fillMaxWidth().testTag("hijri_month_grid")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$monthNameEn $year AH",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$monthNameAr $year هـ",
                    style = ArabicTextStyle.copy(fontSize = 17.sp),
                    color = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of week headers (Sun..Sat)
            val weekDays = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                weekDays.forEach { dayName ->
                    val isFriday = dayName == "Fr"
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isFriday) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 30 days grid (5 rows x 7 cols)
            val days = (1..30).toList()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until 5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (col in 0 until 7) {
                            val dayNum = row * 7 + col + 1
                            if (dayNum <= 30) {
                                val isToday = dayNum == currentHijriDay
                                val isFriday = col == 5 // Friday column

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isToday -> primaryColor
                                                isFriday -> primaryColor.copy(alpha = 0.12f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday) 1.5.dp else 0.dp,
                                            color = if (isToday) Color.White.copy(alpha = 0.8f) else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isToday || isFriday) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = when {
                                            isToday -> Color.Black
                                            isFriday -> primaryColor
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IslamicEventCard(
    event: IslamicEvent,
    primaryColor: Color
) {
    LiquidGlassCard(
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth().testTag("event_card_${event.hijriMonth}_${event.hijriDay}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = event.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = event.titleAr,
                        style = ArabicTextStyle.copy(fontSize = 16.sp),
                        color = primaryColor
                    )
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }

            // Countdown Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (event.daysRemaining <= 30) primaryColor.copy(alpha = 0.25f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                    .border(
                        1.dp,
                        if (event.daysRemaining <= 30) primaryColor.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (event.daysRemaining == 0) "TODAY" else "in ${event.daysRemaining} days",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (event.daysRemaining <= 30) primaryColor else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
