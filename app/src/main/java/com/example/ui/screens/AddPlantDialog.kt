package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.ClimateSetting
import com.example.data.PlantEntity
import com.example.data.SeasonPreset
import com.example.ui.components.getPlantEmoji
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGreen
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantDialog(
    onSavePlant: (PlantEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var roomName by remember { mutableStateOf("Living Room") }
    var customRoom by remember { mutableStateOf("") }
    var isCustomRoom by remember { mutableStateOf(false) }
    
    // Base care intervals
    var waterInterval by remember { mutableStateOf("7") }
    var fertilizeInterval by remember { mutableStateOf("21") }
    var mistInterval by remember { mutableStateOf("3") }
    
    // Seasonal watering frequency mapping (days)
    var springWaterInterval by remember { mutableStateOf("7") }
    var summerWaterInterval by remember { mutableStateOf("5") }
    var winterWaterInterval by remember { mutableStateOf("10") }

    var selectedIconKey by remember { mutableStateOf("monstera") }
    var notes by remember { mutableStateOf("") }
    var showTemplateMenu by remember { mutableStateOf(false) }

    val rooms = listOf("Living Room", "Bedroom", "Balcony", "Office Desk", "Kitchen", "Bathroom", "Hallway", "Other +")
    val icons = listOf("monstera", "snake_plant", "peace_lily", "pothos", "fiddle_leaf", "aloe", "calathea", "zz_plant", "orchid", "cactus", "palm", "flower", "herb", "bonsai", "citrus", "succulent", "ivy")

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
                Text(
                    text = "🪴 ${stringResource(R.string.add_plant)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Preset Template Selection
            Text(
                text = stringResource(R.string.quick_fill_preset),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box {
                OutlinedButton(
                    onClick = { showTemplateMenu = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (name.isBlank()) "Select Preconfigured Plant Template..." else "Template: $name",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                DropdownMenu(
                    expanded = showTemplateMenu,
                    onDismissRequest = { showTemplateMenu = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 300.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("✏️ Custom Plant (Blank Form)", fontWeight = FontWeight.Bold) },
                        onClick = {
                            name = ""
                            species = ""
                            notes = ""
                            showTemplateMenu = false
                        }
                    )
                    HorizontalDivider()
                    com.example.data.DefaultPlantPresets.presets.forEach { preset ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(getPlantEmoji(preset.iconKey), fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(preset.name, fontWeight = FontWeight.Bold)
                                        Text(preset.species, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            onClick = {
                                name = preset.name
                                species = preset.species
                                roomName = if (rooms.contains(preset.defaultRoom)) preset.defaultRoom else "Living Room"
                                waterInterval = preset.waterIntervalDays.toString()
                                springWaterInterval = preset.waterIntervalDays.toString()
                                fertilizeInterval = preset.fertilizeIntervalDays.toString()
                                mistInterval = preset.mistIntervalDays.toString()
                                selectedIconKey = preset.iconKey
                                notes = preset.description
                                showTemplateMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Icon Picker
            Text(
                text = "Choose Botanical Icon",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(icons) { key ->
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedIconKey == key) MintGreen else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedIconKey = key },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(getPlantEmoji(key), fontSize = 24.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant Name *") },
                placeholder = { Text("e.g. Monstera Deliciosa or My Custom Rose") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = species,
                onValueChange = { species = it },
                label = { Text("Species / Variety (Optional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Room Group Selection
            Text(
                text = "Location / Room",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(rooms) { r ->
                    FilterChip(
                        selected = if (r == "Other +") isCustomRoom else (roomName == r && !isCustomRoom),
                        onClick = {
                            if (r == "Other +") {
                                isCustomRoom = true
                            } else {
                                isCustomRoom = false
                                roomName = r
                            }
                        },
                        label = { Text(r, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            if (isCustomRoom) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customRoom,
                    onValueChange = { customRoom = it },
                    label = { Text("Custom Room Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Care Schedule Intervals (Days)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = waterInterval,
                    onValueChange = {
                        waterInterval = it
                        springWaterInterval = it
                    },
                    label = { Text("💧 Water") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = fertilizeInterval,
                    onValueChange = { fertilizeInterval = it },
                    label = { Text("🧪 Fertilize") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = mistInterval,
                    onValueChange = { mistInterval = it },
                    label = { Text("🌫️ Mist") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seasonal Watering Frequency Mapping
            Text(
                text = "Seasonal Watering Schedule Mapping (Days between waterings)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = springWaterInterval,
                    onValueChange = { springWaterInterval = it },
                    label = { Text("🌸 Spring/Autumn") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = summerWaterInterval,
                    onValueChange = { summerWaterInterval = it },
                    label = { Text("☀️ Summer") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = winterWaterInterval,
                    onValueChange = { winterWaterInterval = it },
                    label = { Text("❄️ Winter") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Care Notes (Optional)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val effectiveRoom = if (isCustomRoom && customRoom.isNotBlank()) customRoom else roomName
                        val springDays = springWaterInterval.toIntOrNull() ?: 7
                        val summerDays = summerWaterInterval.toIntOrNull() ?: 5
                        val winterDays = winterWaterInterval.toIntOrNull() ?: 10

                        val seasonalJson = JSONObject().apply {
                            put("SPRING_AUTUMN", springDays)
                            put("SUMMER", summerDays)
                            put("WINTER", winterDays)
                        }.toString()

                        val newPlant = PlantEntity(
                            name = name,
                            species = species,
                            room = effectiveRoom,
                            waterIntervalDays = waterInterval.toIntOrNull() ?: springDays,
                            fertilizeIntervalDays = fertilizeInterval.toIntOrNull() ?: 21,
                            mistIntervalDays = mistInterval.toIntOrNull() ?: 3,
                            lastWateredDate = System.currentTimeMillis(),
                            seasonPresetName = SeasonPreset.SPRING_AUTUMN.name,
                            climateSettingName = ClimateSetting.INDOOR.name,
                            seasonalWateringFrequency = seasonalJson,
                            notes = notes,
                            iconKey = selectedIconKey
                        )
                        onSavePlant(newPlant)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Save Plant to My Jungle", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
