package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.CareType
import com.example.data.PlantEntity
import com.example.ui.theme.*
import java.io.File

@Composable
fun PlantCard(
    plant: PlantEntity,
    onWaterClick: () -> Unit,
    onSnoozeClick: () -> Unit,
    onLogCareClick: (CareType) -> Unit,
    onScrapbookClick: () -> Unit,
    onCardClick: () -> Unit,
    isBatchMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDue = plant.isWaterDue()
    val daysUntilWater = plant.getDaysUntilWaterDue()
    var expanded by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val cardContainerColor = if (isSelected) {
        MintGreen.copy(alpha = 0.3f)
    } else if (isDue) {
        if (isDark) Color(0xFF1E3A2F) else LightMint
    } else {
        MaterialTheme.colorScheme.surface
    }

    val cardContentColor = if (isDue && !isDark) {
        CharcoalText
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardContainerColor,
            contentColor = cardContentColor
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ForestGreen)
        ) else if (isDue) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isDark) MintGreen else ForestGreen)
        ) else CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDue || isSelected) 3.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { if (isBatchMode) onSelectToggle() else onCardClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Plant Image or Botanical Icon Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isDue) (if (isDark) ForestGreen else MintGreen) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!plant.imagePath.isNullOrEmpty() && File(plant.imagePath).exists()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(plant.imagePath))
                                .crossfade(true)
                                .build(),
                            contentDescription = plant.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = getPlantEmoji(plant.iconKey),
                            fontSize = 32.sp
                        )
                    }
                }

                // Plant Information
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = plant.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = cardContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        // Room Tag
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF0D47A1).copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = plant.roomName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF90CAF9) else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        if (isBatchMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onSelectToggle() },
                                colors = CheckboxDefaults.colors(checkedColor = ForestGreen),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    if (plant.species.isNotEmpty()) {
                        Text(
                            text = plant.species,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDue && !isDark) Color(0xFF334155) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Water Status Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isDue) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF881337).copy(alpha = 0.5f) else RoseMist.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "💧 WATER DUE TODAY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFECDD3) else RoseMist
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDark) Color(0xFF075985).copy(alpha = 0.4f) else WaterBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (daysUntilWater <= 1) "💧 Water Tomorrow" else "💧 Water in $daysUntilWater days",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color(0xFF7DD3FC) else WaterBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Season Indicator badge
                        val season = plant.getSeasonPreset()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = season.name.take(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    val isFertilizeDue = plant.isFertilizeDue()
                    if (isFertilizeDue) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD97706).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "🧪 FERTILIZE DUE TODAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Water Now Button
                Button(
                    onClick = onWaterClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDue) ForestGreen else WaterBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "Water Now",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDue) "Water Now" else "Watered",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                // Snooze Button
                OutlinedButton(
                    onClick = onSnoozeClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Snooze,
                        contentDescription = "Snooze",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+2d", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Growth Scrapbook Button
                IconButton(
                    onClick = onScrapbookClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Growth Photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Expand details toggle
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "More Actions"
                    )
                }
            }

            // Expanded multi-care protocols bar
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = "MULTI-CARE PROTOCOLS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CareType.values().forEach { care ->
                            AssistChip(
                                onClick = { onLogCareClick(care) },
                                label = {
                                    Text(
                                        text = "${care.emoji} ${care.title}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getPlantEmoji(key: String): String {
    return when (key) {
        "monstera" -> "🪴"
        "snake_plant" -> "🌵"
        "peace_lily" -> "🪷"
        "pothos" -> "🌿"
        "fiddle_leaf" -> "🍃"
        "aloe" -> "🪴"
        "calathea" -> "🌱"
        "zz_plant" -> "🌿"
        else -> "🪴"
    }
}
