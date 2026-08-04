package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareLogEntity
import com.example.data.PlantEntity
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WateringConsistencyDashboardCard(
    plants: List<PlantEntity>,
    careLogs: List<CareLogEntity>
) {
    val currentMillis = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L
    val thirtyDaysMs = 30 * oneDayMs

    // Calculate 30-day stats
    val past30DaysLogs = remember(careLogs, currentMillis) {
        careLogs.filter { it.timestamp >= (currentMillis - thirtyDaysMs) && it.careType == "WATER" }
    }

    val totalWaterCount = past30DaysLogs.size

    // Average target interval across all plants
    val avgTargetInterval = remember(plants) {
        if (plants.isEmpty()) 7f
        else plants.map { it.getEffectiveWaterIntervalDays().toFloat() }.average().toFloat()
    }

    // Consistency score estimation
    val consistencyScore = remember(past30DaysLogs, plants) {
        if (plants.isEmpty()) 100
        else {
            // Expected waterings in 30 days = sum(30 / effectiveInterval)
            val expectedTotal = plants.sumOf { 30.0 / it.getEffectiveWaterIntervalDays().coerceAtLeast(1) }
            if (expectedTotal <= 0.0) 100
            else ((totalWaterCount.toDouble() / expectedTotal) * 100).toInt().coerceIn(0, 100)
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌊", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "30-DAY WATERING & SCHEDULE ANALYSIS",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        color = ForestGreen
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MintGreen.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "$consistencyScore% Consistency",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Comparing actual hydration frequency over the past month against seasonal plant targets (Avg Target: ~${avgTargetInterval.toInt()} days).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 30-Day Activity Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val primaryColor = ForestGreen
                val accentColor = MintGreen
                val surfaceColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                // Group watering logs by day for the past 30 days
                val dailyWaterCounts = remember(past30DaysLogs, currentMillis) {
                    val map = LinkedHashMap<Int, Int>() // dayIndex (0 to 29) -> count
                    for (i in 29 downTo 0) {
                        map[i] = 0
                    }
                    val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    past30DaysLogs.forEach { log ->
                        val diffDays = ((currentMillis - log.timestamp) / oneDayMs).toInt()
                        if (diffDays in 0..29) {
                            val index = 29 - diffDays
                            map[index] = (map[index] ?: 0) + 1
                        }
                    }
                    map.values.toList()
                }

                val maxVal = remember(dailyWaterCounts) { dailyWaterCounts.maxOfOrNull { it }?.coerceAtLeast(3) ?: 3 }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / 29f

                    // Draw baseline grid lines
                    for (i in 0..3) {
                        val y = height * (i / 3f)
                        drawLine(
                            color = surfaceColor,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Build path for line chart
                    val path = Path()
                    val points = mutableListOf<Offset>()

                    dailyWaterCounts.forEachIndexed { index, count ->
                        val x = index * stepX
                        val y = height - (count.toFloat() / maxVal * (height - 20f)) - 10f
                        points.add(Offset(x, y))
                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // Draw smooth gradient fill under the line
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw line
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw dots
                    points.forEach { pt ->
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("30 Days Ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Total Water Events: $totalWaterCount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ForestGreen)
                Text("Today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
