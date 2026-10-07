package com.example.adhani.ui.adhkar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.adhani.model.DhikrCategory
import com.example.adhani.model.DhikrItem
import com.example.adhani.ui.components.HapticUtil
import com.example.adhani.ui.components.LiquidGlassCard
import com.example.adhani.ui.components.LiquidProgressRing
import com.example.adhani.viewmodel.AdhaniUiState
import com.example.adhani.viewmodel.AdhaniViewModel
import com.example.ui.theme.ArabicTextStyle
import com.example.ui.theme.ArabicTitleStyle
import kotlinx.coroutines.launch

@Composable
fun AdhkarScreen(
    viewModel: AdhaniViewModel,
    uiState: AdhaniUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    if (uiState.isFullscreenTasbihOled) {
        FullscreenOledTasbihView(
            uiState = uiState,
            viewModel = viewModel,
            onClose = { viewModel.toggleFullscreenTasbihOled() }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("adhkar_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Digital Liquid Tasbih Hero Card
        item {
            DigitalLiquidTasbihCard(
                uiState = uiState,
                viewModel = viewModel
            )
        }

        // 2. Category Filter Pills
        item {
            CategoryFilterBar(
                selectedCategory = uiState.selectedDhikrCategory,
                onCategorySelect = { viewModel.setDhikrCategory(it) },
                primaryColor = primaryColor
            )
        }

        // 3. Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.selectedDhikrCategory.titleEn,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = uiState.selectedDhikrCategory.titleAr,
                    style = ArabicTitleStyle.copy(fontSize = 20.sp),
                    color = primaryColor
                )
            }
        }

        // 4. Dhikr items list
        items(uiState.adhkarList, key = { it.id }) { dhikr ->
            DhikrItemCard(
                dhikr = dhikr,
                isActive = uiState.activeTasbihDhikr?.id == dhikr.id,
                primaryColor = primaryColor,
                onStartTasbih = {
                    HapticUtil.playClick(context)
                    viewModel.openTasbihForDhikr(dhikr)
                }
            )
        }
    }
}

@Composable
private fun DigitalLiquidTasbihCard(
    uiState: AdhaniUiState,
    viewModel: AdhaniViewModel
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val scope = rememberCoroutineScope()
    val orbScale = remember { Animatable(1.0f) }

    LiquidGlassCard(
        cornerRadius = 28.dp,
        modifier = Modifier.fillMaxWidth().testTag("liquid_tasbih_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Controls Bar (Target selector, sound, fullscreen)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DIGITAL LIQUID TASBIH",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Today's Total: ${uiState.todayTasbihTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleTasbihSound() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.tasbihSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound toggle",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleFullscreenTasbihOled() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "OLED Fullscreen",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            HapticUtil.playClick(context)
                            viewModel.resetTasbih()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active dhikr title
            val activeTitle = uiState.activeTasbihDhikr?.transliteration?.take(36) ?: "SubhanAllah wa bihamdih"
            Text(
                text = activeTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Liquid Counter Button Orb
            val progressFraction = (uiState.tasbihCount.toFloat() / uiState.tasbihTarget.toFloat()).coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(orbScale.value)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 90.dp),
                        onClick = {
                            HapticUtil.playClick(context)
                            scope.launch {
                                orbScale.animateTo(0.92f, spring(stiffness = Spring.StiffnessHigh))
                                orbScale.animateTo(1.0f, spring(stiffness = Spring.StiffnessMedium))
                            }
                            viewModel.incrementTasbih()
                        }
                    )
                    .testTag("tasbih_counter_button"),
                contentAlignment = Alignment.Center
            ) {
                // Liquid outer ring
                LiquidProgressRing(
                    progress = progressFraction,
                    modifier = Modifier.fillMaxSize(),
                    primaryColor = primaryColor,
                    secondaryColor = MaterialTheme.colorScheme.tertiary
                ) {
                    // Center touch glass orb
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        primaryColor.copy(alpha = 0.35f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        Color.Black.copy(alpha = 0.5f)
                                    )
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${uiState.tasbihCount}",
                                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = Color.White
                            )
                            Text(
                                text = "of ${uiState.tasbihTarget}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = primaryColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Target Selector Chips (33, 100, 1000)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(33, 100, 1000).forEach { target ->
                    val isSelected = uiState.tasbihTarget == target
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .clickable { viewModel.setTasbihTarget(target) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$target",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterBar(
    selectedCategory: DhikrCategory,
    onCategorySelect: (DhikrCategory) -> Unit,
    primaryColor: Color
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(DhikrCategory.entries.toTypedArray()) { cat ->
            val isSelected = selectedCategory == cat
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) primaryColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                    .clickable { onCategorySelect(cat) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("filter_${cat.name.lowercase()}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = cat.titleEn,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun DhikrItemCard(
    dhikr: DhikrItem,
    isActive: Boolean,
    primaryColor: Color,
    onStartTasbih: () -> Unit
) {
    LiquidGlassCard(
        cornerRadius = 24.dp,
        borderAlpha = if (isActive) 0.5f else 0.2f,
        tintColor = if (isActive) primaryColor.copy(alpha = 0.12f) else null,
        modifier = Modifier.fillMaxWidth().testTag("dhikr_card_${dhikr.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Arabic Text (with diacritics)
            Text(
                text = dhikr.arabicText,
                style = ArabicTextStyle.copy(fontSize = 22.sp, lineHeight = 38.sp),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Transliteration
            Text(
                text = dhikr.transliteration,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Translation
            Text(
                text = dhikr.translation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Virtue & Source footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Virtue: ${dhikr.virtue}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = dhikr.source,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Button(
                    onClick = onStartTasbih,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.tertiary else primaryColor
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Count",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${dhikr.targetCount}x",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FullscreenOledTasbihView(
    uiState: AdhaniUiState,
    viewModel: AdhaniViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val scope = rememberCoroutineScope()
    val orbScale = remember { Animatable(1.0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
            .testTag("fullscreen_oled_tasbih"),
        contentAlignment = Alignment.Center
    ) {
        // Close button at top-right
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
        ) {
            Icon(Icons.Default.FullscreenExit, contentDescription = "Exit", tint = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "OLED NIGHT REMEMBRANCE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                color = Color.White.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Arabic text of active dhikr if present
            val arabic = uiState.activeTasbihDhikr?.arabicText ?: "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ"
            Text(
                text = arabic,
                style = ArabicTitleStyle.copy(fontSize = 24.sp, color = primaryColor),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Gigantic Touch Orb
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(orbScale.value)
                    .clip(CircleShape)
                    .background(Color(0xFF0A0A0A))
                    .border(2.dp, primaryColor, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false, radius = 120.dp),
                        onClick = {
                            HapticUtil.playClick(context)
                            scope.launch {
                                orbScale.animateTo(0.94f, spring(stiffness = Spring.StiffnessHigh))
                                orbScale.animateTo(1.0f, spring(stiffness = Spring.StiffnessMedium))
                            }
                            viewModel.incrementTasbih()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${uiState.tasbihCount}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Target: ${uiState.tasbihTarget}",
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Tap anywhere on the orb • Total today: ${uiState.todayTasbihTotal}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}
