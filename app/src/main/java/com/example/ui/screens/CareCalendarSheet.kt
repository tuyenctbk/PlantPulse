package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

data class CalendarDayInfo(
    val dayNumber: Int,
    val timestamp: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val completedLogs: List<CareLogEntity>,
    val scheduledPlants: List<PlantEntity>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareCalendarSheet(
    plants: List<PlantEntity>,
    careLogs: List<CareLogEntity>,
    onWaterPlant: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }

    var selectedDayInfo by remember { mutableStateOf<CalendarDayInfo?>(null) }

    val monthHeaderStr = remember(calendarMonth) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(calendarMonth.time)
    }

    // Build days array for grid (42 cells: 6 weeks x 7 days)
    val daysList = remember(calendarMonth, plants, careLogs) {
        val list = mutableListOf<CalendarDayInfo>()
        val cal = calendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based index for Sunday
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val todayCal = Calendar.getInstance()

        // Days before start of month
        val prevCal = cal.clone() as Calendar
        prevCal.add(Calendar.MONTH, -1)
        val prevMaxDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in (prevMaxDays - firstDayOfWeek + 1)..prevMaxDays) {
            prevCal.set(Calendar.DAY_OF_MONTH, i)
            list.add(
                CalendarDayInfo(
                    dayNumber = i,
                    timestamp = prevCal.timeInMillis,
                    isCurrentMonth = false,
                    isToday = false,
                    completedLogs = emptyList(),
                    scheduledPlants = emptyList()
                )
            )
        }

        // Days in current month
        for (day in 1..maxDaysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayStart = cal.timeInMillis
            val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1)

            val isToday = todayCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                    todayCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)

            // Completed logs on this day
            val logsOnDay = careLogs.filter { log -> log.timestamp in dayStart until dayEnd }

            // Plants scheduled for watering on this day
            val scheduledOnDay = plants.filter { plant ->
                val intervalMs = TimeUnit.DAYS.toMillis(plant.getEffectiveWaterIntervalDays().toLong())
                val nextWaterDate = plant.lastWateredDate + intervalMs
                nextWaterDate in dayStart until dayEnd
            }

            list.add(
                CalendarDayInfo(
                    dayNumber = day,
                    timestamp = dayStart,
                    isCurrentMonth = true,
                    isToday = isToday,
                    completedLogs = logsOnDay,
                    scheduledPlants = scheduledOnDay
                )
            )
        }

        // Fill remaining grid to 42 cells
        val remaining = 42 - list.size
        val nextCal = cal.clone() as Calendar
        nextCal.add(Calendar.MONTH, 1)
        for (day in 1..remaining) {
            nextCal.set(Calendar.DAY_OF_MONTH, day)
            list.add(
                CalendarDayInfo(
                    dayNumber = day,
                    timestamp = nextCal.timeInMillis,
                    isCurrentMonth = false,
                    isToday = false,
                    completedLogs = emptyList(),
                    scheduledPlants = emptyList()
                )
            )
        }

        list
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = stringResource(R.string.monthly_care_calendar),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Month Navigator Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = calendarMonth.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    calendarMonth = newCal
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                }

                Text(
                    text = monthHeaderStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = {
                    val newCal = calendarMonth.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    calendarMonth = newCal
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                userScrollEnabled = false
            ) {
                items(daysList) { dayInfo ->
                    val isSelected = selectedDayInfo?.dayNumber == dayInfo.dayNumber && dayInfo.isCurrentMonth
                    val hasCompleted = dayInfo.completedLogs.isNotEmpty()
                    val hasScheduled = dayInfo.scheduledPlants.isNotEmpty()

                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> ForestGreen
                                    dayInfo.isToday -> MintGreen.copy(alpha = 0.4f)
                                    dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable(enabled = dayInfo.isCurrentMonth) {
                                selectedDayInfo = dayInfo
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = dayInfo.dayNumber.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (dayInfo.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White
                                    !dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )

                            if (dayInfo.isCurrentMonth) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    if (hasCompleted) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(if (isSelected) Color.White else ForestGreen, CircleShape)
                                        )
                                    }
                                    if (hasScheduled) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(if (isSelected) MintGreen else WaterBlue, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Details card for selected day
            selectedDayInfo?.let { dayInfo ->
                val dateStr = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date(dayInfo.timestamp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📅 $dateStr",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (dayInfo.completedLogs.isEmpty() && dayInfo.scheduledPlants.isEmpty()) {
                            Text(
                                text = "No care tasks logged or scheduled for this date.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            if (dayInfo.completedLogs.isNotEmpty()) {
                                Text(
                                    text = "✅ Care Logs Completed (${dayInfo.completedLogs.size}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                dayInfo.completedLogs.forEach { log ->
                                    val plantName = plants.find { it.id == log.plantId }?.name ?: "Plant #${log.plantId}"
                                    Text(
                                        text = "• ${log.careType} for $plantName",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (dayInfo.scheduledPlants.isNotEmpty()) {
                                Text(
                                    text = "⏰ Scheduled Waterings (${dayInfo.scheduledPlants.size}):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WaterBlue
                                )
                                dayInfo.scheduledPlants.forEach { plant ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💧 ${plant.name} (${plant.room})",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Button(
                                            onClick = { onWaterPlant(plant.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Water Now", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
