package com.example.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import java.util.concurrent.TimeUnit
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.*
import com.example.ui.PlantViewModel
import com.example.ui.components.getPlantEmoji
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailSheet(
    plant: PlantEntity,
    viewModel: PlantViewModel,
    onDismiss: () -> Unit
) {
    var name by remember(plant) { mutableStateOf(plant.name) }
    var species by remember(plant) { mutableStateOf(plant.species) }
    var roomName by remember(plant) { mutableStateOf(plant.roomName) }
    var baseWaterInterval by remember(plant) { mutableStateOf(plant.waterIntervalDays.toString()) }
    var baseFertilizeInterval by remember(plant) { mutableStateOf(plant.fertilizeIntervalDays.toString()) }
    var baseMistInterval by remember(plant) { mutableStateOf(plant.mistIntervalDays.toString()) }
    var selectedSeason by remember(plant) { mutableStateOf(plant.getSeasonPreset()) }
    var selectedClimate by remember(plant) { mutableStateOf(plant.getClimateSetting()) }
    var notes by remember(plant) { mutableStateOf(plant.notes) }

    val careLogs by viewModel.getCareLogsForPlant(plant.id).collectAsState(initial = emptyList())
    val plantNotes by viewModel.getNotesForPlant(plant.id).collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsState()
    val weatherFactor = uiState.weatherAdjustmentFactor
    var newNoteText by remember { mutableStateOf("") }

    // Calculate effective interval live
    val currentBaseWater = baseWaterInterval.toIntOrNull() ?: plant.waterIntervalDays
    val effectiveInterval = (currentBaseWater * selectedSeason.multiplier * selectedClimate.multiplier).toInt().coerceAtLeast(1)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
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
                            Text(getPlantEmoji(plant.iconKey), fontSize = 28.sp)
                        }
                    }

                    Column {
                        Text(
                            text = plant.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = plant.roomName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Smart Seasonal & Climate Engine Section
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("☀️", fontSize = 18.sp)
                        Text(
                            text = "SMART SEASONAL ADJUSTMENT ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Current Season Preset:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SeasonPreset.values().forEach { season ->
                            FilterChip(
                                selected = selectedSeason == season,
                                onClick = { selectedSeason = season },
                                label = {
                                    Text(
                                        text = season.label.split(" ").first(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Room Microclimate:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ClimateSetting.values().forEach { climate ->
                            FilterChip(
                                selected = selectedClimate == climate,
                                onClick = { selectedClimate = climate },
                                label = {
                                    Text(
                                        text = climate.label.split(" ").first(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Adjusted Schedule: Every $effectiveInterval days\n(Base: $currentBaseWater days × ${selectedSeason.name.lowercase()} × ${selectedClimate.name.lowercase()})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val currentBaseFertilize = baseFertilizeInterval.toIntOrNull() ?: plant.fertilizeIntervalDays
                    val effectiveFertilizeInterval = (currentBaseFertilize * selectedSeason.multiplier).toInt().coerceAtLeast(1)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🧪", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Feeding Schedule: Every $effectiveFertilizeInterval days\n(Base: $currentBaseFertilize days × ${selectedSeason.name.lowercase()} multiplier)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Calendar Export Section
            Text(
                text = "CALENDAR INTEGRATION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val context = LocalContext.current
                Button(
                    onClick = {
                        val dueTimestamp = plant.getNextWaterDueTimestamp(weatherFactor)
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, "💧 Water ${plant.name}")
                            putExtra(CalendarContract.Events.DESCRIPTION, "Plant care reminder: Water ${plant.name} located in ${plant.roomName}.")
                            putExtra(CalendarContract.Events.EVENT_LOCATION, plant.roomName)
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dueTimestamp)
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dueTimestamp + 30 * 60 * 1000)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No calendar app available", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Water", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = {
                        val currentBaseFertilize = baseFertilizeInterval.toIntOrNull() ?: plant.fertilizeIntervalDays
                        val effectiveFertilizeInterval = (currentBaseFertilize * selectedSeason.multiplier).toInt().coerceAtLeast(1)
                        val fertilizeTimestamp = plant.lastFertilizedTimestamp + TimeUnit.DAYS.toMillis(effectiveFertilizeInterval.toLong())
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, "🧪 Fertilize ${plant.name}")
                            putExtra(CalendarContract.Events.DESCRIPTION, "Plant care reminder: Fertilize ${plant.name} located in ${plant.roomName}.")
                            putExtra(CalendarContract.Events.EVENT_LOCATION, plant.roomName)
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, fertilizeTimestamp)
                            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, fertilizeTimestamp + 30 * 60 * 1000)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No calendar app available", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Feed", style = MaterialTheme.typography.labelMedium, color = ForestGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Edit Plant Information
            Text(
                text = "PLANT SPECIFICATIONS & CARE PROTOCOLS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant Name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Species") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Room Group") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-care intervals
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = baseWaterInterval,
                    onValueChange = { baseWaterInterval = it },
                    label = { Text("💧 Water (Days)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = baseFertilizeInterval,
                    onValueChange = { baseFertilizeInterval = it },
                    label = { Text("🧪 Fertilize (Days)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = baseMistInterval,
                    onValueChange = { baseMistInterval = it },
                    label = { Text("🌫️ Mist (Days)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Care Notes (Lighting, Soil, etc.)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Growth Scrapbook Button
            Button(
                onClick = {
                    viewModel.setActivePlantForDetail(null)
                    viewModel.setActivePlantForScrapbook(plant)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("📸 Open Growth Scrapbook & Timelapse", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save & Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val updated = plant.copy(
                            name = name,
                            species = species,
                            room = roomName,
                            waterIntervalDays = baseWaterInterval.toIntOrNull() ?: plant.waterIntervalDays,
                            fertilizeIntervalDays = baseFertilizeInterval.toIntOrNull() ?: plant.fertilizeIntervalDays,
                            mistIntervalDays = baseMistInterval.toIntOrNull() ?: plant.mistIntervalDays,
                            seasonPresetName = selectedSeason.name,
                            climateSettingName = selectedClimate.name,
                            notes = notes
                        )
                        viewModel.saveCustomPlant(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.deletePlant(plant) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Plant Notes & Care Observations Journal
            Text(
                text = "PLANT OBSERVATIONS & CARE TIPS JOURNAL (${plantNotes.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        label = { Text("Add observation or care tip...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newNoteText.isNotBlank()) {
                                viewModel.addPlantNote(plant.id, newNoteText)
                                newNoteText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Note", style = MaterialTheme.typography.labelMedium)
                    }

                    if (plantNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            plantNotes.forEach { note ->
                                val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                                val formattedDate = dateFormat.format(Date(note.timestamp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = note.text,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = formattedDate,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deletePlantNote(note) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Note",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Care History Log Timeline
            Text(
                text = "CARE HISTORY TIMELINE (${careLogs.size})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (careLogs.isEmpty()) {
                Text(
                    text = "No care logs recorded yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    careLogs.take(10).forEach { log ->
                        val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                        val formattedDate = dateFormat.format(Date(log.timestamp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val emoji = when (log.careType) {
                                    CareType.WATER.name -> "💧"
                                    CareType.FERTILIZE.name -> "🧪"
                                    CareType.MIST.name -> "🌫️"
                                    else -> "🪴"
                                }
                                Text(emoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.note ?: "${log.careType} logged",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formattedDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
