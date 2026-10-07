package com.example.adhani.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalGlassConfig
import kotlin.math.sin

enum class AppTab(val title: String, val arabicTitle: String, val icon: ImageVector) {
    PRAYERS("Prayers", "الصلاة", Icons.Default.AccessTime),
    QIBLA("Qibla", "القبلة", Icons.Default.Explore),
    HIJRI("Hijri", "التقويم", Icons.Default.CalendarMonth),
    ADHKAR("Adhkar", "الأذكار", Icons.AutoMirrored.Filled.MenuBook),
    SETTINGS("Settings", "الإعدادات", Icons.Default.Tune)
}

object HapticUtil {
    fun playClick(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(25)
            }
        }
    }

    fun playSuccess(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 50), -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(80)
            }
        }
    }
}

/**
 * Reusable frosted Liquid Glass Card with specular highlight border and translucent refractions.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    tintColor: Color? = null,
    borderAlpha: Float = 0.25f,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val glassConfig = LocalGlassConfig.current
    val effectiveTint = tintColor ?: MaterialTheme.colorScheme.surface
    val baseOpacity = if (glassConfig.isOled) 0.12f else glassConfig.opacity.coerceIn(0.12f, 0.45f)

    val surfaceColor = effectiveTint.copy(alpha = baseOpacity)
    val specularGradient = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = (borderAlpha * 1.5f).coerceAtMost(0.6f)),
        0.3f to Color.White.copy(alpha = borderAlpha * 0.4f),
        1.0f to Color.White.copy(alpha = 0.05f)
    )

    val shape = RoundedCornerShape(cornerRadius)

    val clickableMod = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = MaterialTheme.colorScheme.primary),
            role = Role.Button,
            onClick = onClick
        )
    } else Modifier

    Box(
        modifier = modifier
            .then(
                if (!glassConfig.isOled) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        surfaceColor,
                        surfaceColor.copy(alpha = (baseOpacity * 0.7f).coerceAtLeast(0.08f))
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = specularGradient,
                shape = shape
            )
            .then(clickableMod)
    ) {
        content()
    }
}

/**
 * Signature Liquid Glass Floating Dock with organic blob morphing animation across 5 tabs.
 */
@Composable
fun LiquidGlassBottomDock(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val tabs = remember { AppTab.entries.toTypedArray() }
    val selectedIndex = tabs.indexOf(currentTab)
    val glassConfig = LocalGlassConfig.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val isOled = glassConfig.isOled

    // Bouncy spring animation for fluid liquid blob positioning
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessLow * glassConfig.motionSpeedMultiplier
        ),
        label = "tab_blob_index"
    )

    // Liquid stretch factor: stretches wider horizontally when moving rapidly
    val stretchScale = remember { Animatable(1f) }
    LaunchedEffect(currentTab) {
        stretchScale.snapTo(1.22f)
        stretchScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .height(72.dp)
            .testTag("liquid_glass_dock"),
        contentAlignment = Alignment.Center
    ) {
        val dockShape = RoundedCornerShape(32.dp)
        val dockBorderBrush = Brush.verticalGradient(
            0.0f to Color.White.copy(alpha = 0.45f),
            0.2f to Color.White.copy(alpha = 0.20f),
            1.0f to Color.White.copy(alpha = 0.08f)
        )

        // Glass Dock backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isOled) {
                        Modifier.shadow(
                            elevation = 16.dp,
                            shape = dockShape,
                            ambientColor = Color.Black.copy(alpha = 0.4f),
                            spotColor = primaryColor.copy(alpha = 0.35f)
                        )
                    } else Modifier
                )
                .clip(dockShape)
                .background(
                    if (isOled) Color(0xFF090909).copy(alpha = 0.85f)
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                )
                .border(width = 1.2.dp, brush = dockBorderBrush, shape = dockShape)
        ) {
            // Specular light glow line along top edge
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 1.5f
                drawLine(
                    brush = Brush.horizontalGradient(
                        0.0f to Color.Transparent,
                        0.25f to Color.White.copy(alpha = 0.6f),
                        0.5f to Color.White.copy(alpha = 0.9f),
                        0.75f to Color.White.copy(alpha = 0.6f),
                        1.0f to Color.Transparent
                    ),
                    start = Offset(24f, strokeW),
                    end = Offset(size.width - 24f, strokeW),
                    strokeWidth = strokeW
                )
            }

            // Fluid Liquid Blob Indicator inside the dock
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val totalWidthPx = constraints.maxWidth.toFloat()
                val tabCount = tabs.size
                val itemWidthPx = totalWidthPx / tabCount
                val blobCenterPx = (animatedIndex + 0.5f) * itemWidthPx
                val blobWidthDp = 52.dp * stretchScale.value
                val blobHeightDp = 48.dp

                val blobCenterDp = with(density) { blobCenterPx.toDp() }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset {
                            val xOffset = blobCenterPx - (with(density) { blobWidthDp.toPx() } / 2f)
                            IntOffset(xOffset.toInt(), 0)
                        }
                        .size(width = blobWidthDp, height = blobHeightDp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.35f),
                                    primaryColor.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    primaryColor.copy(alpha = 0.3f)
                                )
                            ),
                            shape = RoundedCornerShape(26.dp)
                        )
                )
            }

            // Tab items row
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = currentTab == tab
                    val tabScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label = "tab_scale"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 28.dp),
                                onClick = {
                                    if (currentTab != tab) {
                                        HapticUtil.playClick(context)
                                        onTabSelected(tab)
                                    }
                                }
                            )
                            .testTag("tab_${tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            modifier = Modifier.size(if (isSelected) 24.dp else 22.dp)
                        )

                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = if (isSelected) 10.5.sp else 9.5.sp
                            ),
                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Liquid Wave Animated Circular Progress Ring for countdowns.
 */
@Composable
fun LiquidProgressRing(
    progress: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0.01f, 1.0f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress_ring"
    )

    val wavePhase = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            wavePhase.animateTo(
                targetValue = (2 * Math.PI).toFloat(),
                animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing)
            )
            wavePhase.snapTo(0f)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background glass track
            drawCircle(
                color = primaryColor.copy(alpha = 0.12f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Animated progress sweep arc
            val sweepAngle = 360f * animatedProgress
            drawArc(
                brush = Brush.sweepGradient(
                    0.0f to primaryColor.copy(alpha = 0.4f),
                    animatedProgress * 0.5f to secondaryColor,
                    animatedProgress to primaryColor,
                    center = center
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            // Small glowing orb indicator at the progress tip
            val tipAngleRad = Math.toRadians((-90.0 + sweepAngle))
            val tipX = center.x + radius * kotlin.math.cos(tipAngleRad).toFloat()
            val tipY = center.y + radius * kotlin.math.sin(tipAngleRad).toFloat()

            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.7f,
                center = Offset(tipX, tipY)
            )
            drawCircle(
                color = primaryColor.copy(alpha = 0.5f),
                radius = strokeWidth * 1.3f,
                center = Offset(tipX, tipY)
            )
        }

        content()
    }
}
