package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CareLogEntity
import com.example.data.PlantEntity
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGreen
import com.example.ui.theme.WaterBlue
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun WateringFrequencyChartCard(
    plants: List<PlantEntity>,
    careLogs: List<CareLogEntity>,
    modifier: Modifier = Modifier
) {
    var selectedPlantId by remember { mutableStateOf<Long?>(null) } // null = All Plants

    val selectedPlant = plants.find { it.id == selectedPlantId }

    val filteredLogs = remember(careLogs, selectedPlantId) {
        careLogs.filter { log ->
            log.careType == "WATER" && (selectedPlantId == null || log.plantId == selectedPlantId)
        }
    }

    // Past 30 Days Bucket (30 days ago to today)
    val now = System.currentTimeMillis()
    val thirtyDaysAgo = now - TimeUnit.DAYS.toMillis(30)

    val dailyCounts = remember(filteredLogs) {
        val calendar = Calendar.getInstance()
        val counts = IntArray(30) { 0 }

        filteredLogs.forEach { log ->
            if (log.timestamp in thirtyDaysAgo..now) {
                val dayDiff = ((now - log.timestamp) / TimeUnit.DAYS.toMillis(1)).toInt()
                if (dayDiff in 0..29) {
                    counts[29 - dayDiff]++
                }
            }
        }
        counts
    }

    val totalWaterings = dailyCounts.sum()
    val maxCount = (dailyCounts.maxOrNull() ?: 1).coerceAtLeast(1)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
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
                Column {
                    Text(
                        text = stringResource(R.string.past_30_days_watering_frequency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (selectedPlant == null) "All Garden Plants ($totalWaterings waters logged)" else "${selectedPlant.name} ($totalWaterings waters)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Plant Filter Menu
                var expanded by remember { mutableStateOf(false) }
                Box {
                    FilterChip(
                        selected = selectedPlantId != null,
                        onClick = { expanded = true },
                        label = {
                            Text(
                                text = selectedPlant?.name ?: "All Plants 🪴",
                                maxLines = 1
                            )
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Garden Plants 🪴") },
                            onClick = {
                                selectedPlantId = null
                                expanded = false
                            }
                        )
                        plants.forEach { plant ->
                            DropdownMenuItem(
                                text = { Text(plant.name) },
                                onClick = {
                                    selectedPlantId = plant.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                val lineColor = WaterBlue
                val gradientBottom = WaterBlue.copy(alpha = 0.05f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / 29f

                    val path = Path()
                    val fillPath = Path()

                    dailyCounts.forEachIndexed { index, count ->
                        val x = index * stepX
                        val normalizedY = count.toFloat() / maxCount.toFloat()
                        val y = height - (normalizedY * (height - 30.dp.toPx()) + 15.dp.toPx())

                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        // Draw bar/point dot
                        if (count > 0) {
                            drawCircle(
                                color = lineColor,
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }

                    fillPath.lineTo(width, height)
                    fillPath.close()

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(WaterBlue.copy(alpha = 0.35f), gradientBottom)
                        )
                    )

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("30 Days Ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("15 Days Ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
