package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DefaultPlantPresets
import com.example.data.PlantPreset
import com.example.ui.components.getPlantEmoji
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantEncyclopediaSearchSheet(
    onSelectPreset: (PlantPreset) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = listOf("All", "Low Light Tolerant", "Pet-Friendly", "Bright Light", "High Humidity", "Easy Care")

    val filteredPresets = remember(searchQuery, selectedFilter) {
        DefaultPlantPresets.presets.filter { preset ->
            val matchesSearch = searchQuery.isBlank() ||
                    preset.name.contains(searchQuery, ignoreCase = true) ||
                    preset.species.contains(searchQuery, ignoreCase = true) ||
                    preset.description.contains(searchQuery, ignoreCase = true) ||
                    preset.lightRequirement.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Low Light Tolerant" -> preset.isLowLightTolerant
                "Pet-Friendly" -> preset.isPetFriendly
                "Bright Light" -> preset.lightRequirement.contains("Bright", ignoreCase = true)
                "Low Light" -> preset.isLowLightTolerant || preset.lightRequirement.contains("Low", ignoreCase = true) || preset.lightRequirement.contains("Indirect", ignoreCase = true)
                "High Humidity" -> preset.humidityRequirement.contains("High", ignoreCase = true) || preset.mistIntervalDays <= 3
                "Easy Care" -> preset.waterIntervalDays >= 10 || preset.description.contains("hardy", ignoreCase = true) || preset.description.contains("neglect", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📖 PLANT SPECIES ENCYCLOPEDIA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(
                        text = "Botanical Care Profiles",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search species by name, light, or care level...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ForestGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    cursorColor = ForestGreen
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintGreen,
                            selectedLabelColor = ForestGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Results List
            if (filteredPresets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌿", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No matching plant species found",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredPresets) { preset ->
                        PlantEncyclopediaCard(
                            preset = preset,
                            onSelect = { onSelectPreset(preset) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlantEncyclopediaCard(
    preset: PlantPreset,
    onSelect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MintGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getPlantEmoji(preset.iconKey), fontSize = 30.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = preset.species,
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Profile", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = preset.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Care requirements badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CareRequirementBadge(icon = "☀️", label = preset.lightRequirement, modifier = Modifier.weight(1f))
                CareRequirementBadge(icon = "💧", label = "Water: ${preset.waterIntervalDays}d", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CareRequirementBadge(icon = "🌫️", label = preset.humidityRequirement, modifier = Modifier.weight(1f))
                CareRequirementBadge(icon = "🧪", label = "Feed: ${preset.fertilizeIntervalDays}d", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val petText = if (preset.isPetFriendly) "🐾 Pet-Friendly" else "🚫 Not Pet-Friendly"
                val lightText = if (preset.isLowLightTolerant) "💡 Low Light Tolerant" else "☀️ Direct/Bright Light"
                CareRequirementBadge(icon = "", label = petText, modifier = Modifier.weight(1f))
                CareRequirementBadge(icon = "", label = lightText, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CareRequirementBadge(icon: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon.isNotEmpty()) {
                Text(icon, fontSize = 12.sp)
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
