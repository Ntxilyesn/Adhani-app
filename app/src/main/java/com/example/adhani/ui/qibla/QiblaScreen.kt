package com.example.adhani.ui.qibla

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adhani.ui.components.HapticUtil
import com.example.adhani.ui.components.LiquidGlassCard
import com.example.adhani.viewmodel.AdhaniUiState
import com.example.adhani.viewmodel.AdhaniViewModel
import com.example.ui.theme.ArabicTextStyle
import com.example.ui.theme.ArabicTitleStyle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    viewModel: AdhaniViewModel,
    uiState: AdhaniUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAligned = uiState.isAlignedWithQibla
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = MaterialTheme.colorScheme.tertiary

    // Alignment haptic trigger
    LaunchedEffect(isAligned) {
        if (isAligned) {
            HapticUtil.playSuccess(context)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("qibla_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title & Location Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Qibla Direction",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "اتجاه القِبلة الشريفة",
                    style = ArabicTitleStyle.copy(fontSize = 20.sp),
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${uiState.selectedCity.name} • Bearing: ${String.format("%.1f°", uiState.qiblaBearing)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Live Alignment Banner
        item {
            QiblaStatusBanner(
                isAligned = isAligned,
                primaryColor = primaryColor,
                goldColor = goldColor,
                heading = uiState.deviceHeading,
                targetBearing = uiState.qiblaBearing.toFloat()
            )
        }

        // Main Liquid Glass Compass Orb
        item {
            LiquidGlassCompassOrb(
                deviceHeading = uiState.deviceHeading,
                qiblaBearing = uiState.qiblaBearing.toFloat(),
                isAligned = isAligned,
                primaryColor = primaryColor,
                goldColor = goldColor
            )
        }

        // Metrics Row: Distance to Makkah & Coordinates
        item {
            QiblaMetricsRow(
                distanceKm = uiState.distanceToMakkahKm,
                qiblaBearing = uiState.qiblaBearing
            )
        }

        // Interactive Calibration & Sensor Simulation Card
        item {
            SensorSimulationCard(
                currentHeading = uiState.deviceHeading,
                qiblaBearing = uiState.qiblaBearing.toFloat(),
                isManualMode = uiState.isManualCompassMode,
                onHeadingChange = { viewModel.setManualHeading(it) },
                onCalibrateClick = { viewModel.toggleCalibrationHelper(true) }
            )
        }
    }

    if (uiState.showCalibrationHelper) {
        CalibrationHelperDialog(onDismiss = { viewModel.toggleCalibrationHelper(false) })
    }
}

@Composable
private fun QiblaStatusBanner(
    isAligned: Boolean,
    primaryColor: Color,
    goldColor: Color,
    heading: Float,
    targetBearing: Float
) {
    val bannerColor by animateColorAsState(
        targetValue = if (isAligned) goldColor.copy(alpha = 0.22f) else primaryColor.copy(alpha = 0.12f),
        label = "banner_color"
    )

    val diff = ((heading - targetBearing + 180f) % 360f) - 180f
    val turnDirection = when {
        isAligned -> "Directly Facing the Holy Kaaba"
        diff < 0 -> "Turn right by ${String.format("%.0f°", -diff)}"
        else -> "Turn left by ${String.format("%.0f°", diff)}"
    }

    LiquidGlassCard(
        cornerRadius = 20.dp,
        tintColor = bannerColor,
        borderAlpha = if (isAligned) 0.6f else 0.25f,
        modifier = Modifier.fillMaxWidth().testTag("qibla_status_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isAligned) goldColor else primaryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAligned) Icons.Outlined.CheckCircle else Icons.Default.NearMe,
                    contentDescription = null,
                    tint = if (isAligned) Color.Black else primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = if (isAligned) "ALIGNED WITH QIBLA" else "NAVIGATE TO KAABA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = if (isAligned) goldColor else primaryColor
                )
                Text(
                    text = turnDirection,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun LiquidGlassCompassOrb(
    deviceHeading: Float,
    qiblaBearing: Float,
    isAligned: Boolean,
    primaryColor: Color,
    goldColor: Color
) {
    val auraColor by animateColorAsState(
        targetValue = if (isAligned) goldColor.copy(alpha = 0.5f) else primaryColor.copy(alpha = 0.15f),
        animationSpec = tween(durationMillis = 500),
        label = "orb_aura"
    )

    val relativeAngle = (qiblaBearing - deviceHeading + 360f) % 360f

    val animatedRotation by animateFloatAsState(
        targetValue = -deviceHeading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "compass_rotation"
    )

    Box(
        modifier = Modifier
            .size(320.dp)
            .shadow(
                elevation = if (isAligned) 28.dp else 12.dp,
                shape = CircleShape,
                ambientColor = auraColor,
                spotColor = auraColor
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                        auraColor
                    )
                )
            )
            .border(
                width = if (isAligned) 2.5.dp else 1.5.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        Color.White.copy(alpha = 0.7f),
                        if (isAligned) goldColor else primaryColor,
                        Color.White.copy(alpha = 0.2f),
                        if (isAligned) goldColor else primaryColor,
                        Color.White.copy(alpha = 0.7f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Rotating Compass Ring (Canvas ticks & cardinals)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(animatedRotation)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 24.dp.toPx()

            // 360 degree ticks
            for (degree in 0 until 360 step 5) {
                val rad = Math.toRadians(degree.toDouble() - 90.0)
                val isMajor = degree % 30 == 0
                val tickLen = if (isMajor) 14.dp.toPx() else 6.dp.toPx()
                val strokeW = if (isMajor) 2.5f else 1.2f

                val startX = center.x + (radius - tickLen) * cos(rad).toFloat()
                val startY = center.y + (radius - tickLen) * sin(rad).toFloat()
                val endX = center.x + radius * cos(rad).toFloat()
                val endY = center.y + radius * sin(rad).toFloat()

                drawLine(
                    color = if (degree == 0) Color(0xFFEF4444) else Color.White.copy(alpha = if (isMajor) 0.6f else 0.25f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeW
                )
            }

            // Qibla Target Marker on outer ring
            val qiblaRad = Math.toRadians(qiblaBearing.toDouble() - 90.0)
            val markerRadius = radius + 6.dp.toPx()
            val markerX = center.x + markerRadius * cos(qiblaRad).toFloat()
            val markerY = center.y + markerRadius * sin(qiblaRad).toFloat()

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(goldColor, goldColor.copy(alpha = 0.4f)),
                    center = Offset(markerX, markerY),
                    radius = 18.dp.toPx()
                ),
                radius = 12.dp.toPx(),
                center = Offset(markerX, markerY)
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(markerX, markerY)
            )
        }

        // Center Kaaba stylized orb
        val kaabaResId = com.example.R.drawable.kaaba_icon_orb_1791397491853
        Box(
            modifier = Modifier
                .size(108.dp)
                .shadow(elevation = 16.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.8f))
                .border(2.dp, if (isAligned) goldColor else Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = kaabaResId),
                contentDescription = "Holy Kaaba",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
            )
        }

        // Top pointer indicator
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            // Triangle at top pointing down toward center
            val pointerLen = 16.dp.toPx()
            val pointerHalfW = 10.dp.toPx()
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(center.x, 8.dp.toPx())
                lineTo(center.x - pointerHalfW, 8.dp.toPx() + pointerLen)
                lineTo(center.x + pointerHalfW, 8.dp.toPx() + pointerLen)
                close()
            }
            drawPath(
                path = path,
                color = if (isAligned) goldColor else primaryColor
            )
        }
    }
}

@Composable
private fun QiblaMetricsRow(
    distanceKm: Double,
    qiblaBearing: Double
) {
    val distanceMiles = distanceKm * 0.621371

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LiquidGlassCard(
            modifier = Modifier.weight(1f),
            cornerRadius = 20.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DISTANCE TO KAABA",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${String.format("%,.0f", distanceKm)} km",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${String.format("%,.0f", distanceMiles)} mi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        LiquidGlassCard(
            modifier = Modifier.weight(1f),
            cornerRadius = 20.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TARGET BEARING",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%.1f° N", qiblaBearing),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "From True North",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun SensorSimulationCard(
    currentHeading: Float,
    qiblaBearing: Float,
    isManualMode: Boolean,
    onHeadingChange: (Float) -> Unit,
    onCalibrateClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Compass Sensor & Test",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(onClick = onCalibrateClick) {
                    Icon(Icons.Default.CompassCalibration, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Calibrate")
                }
            }

            Text(
                text = "On emulators or without magnetometer, rotate the test slider to align with ${String.format("%.0f°", qiblaBearing)}:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Slider(
                    value = currentHeading,
                    onValueChange = onHeadingChange,
                    valueRange = 0f..360f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Button(
                    onClick = { onHeadingChange(qiblaBearing) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Align", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CalibrationHelperDialog(onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Compass Sensor Calibration", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "If magnetic interference is detected near metal or laptops:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "Figure-8 motion",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Text(
                    text = "Wave your device in a smooth Figure-8 motion three times in the air to recalibrate internal magnetometer accuracy.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
