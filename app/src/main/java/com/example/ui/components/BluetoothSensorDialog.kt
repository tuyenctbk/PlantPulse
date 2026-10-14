package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.PlantEntity
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGreen
import com.example.ui.theme.WaterBlue
import com.example.util.BluetoothPlantSensor
import com.example.util.BluetoothPlantSensorManager
import kotlinx.coroutines.launch

@Composable
fun BluetoothSensorDialog(
    plants: List<PlantEntity>,
    onWaterPlant: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var sensors by remember { mutableStateOf<List<BluetoothPlantSensor>>(emptyList()) }
    val scope = rememberCoroutineScope()

    fun scan() {
        scope.launch {
            isScanning = true
            sensors = emptyList()
            BluetoothPlantSensorManager.scanNearbySensors().collect { result ->
                sensors = result
                isScanning = false
            }
        }
    }

    LaunchedEffect(Unit) {
        scan()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.AutoMirrored.Filled.BluetoothSearching else Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = ForestGreen
                )
                Text("Bluetooth Plant Sensors", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Scan nearby Bluetooth soil moisture & sunlight sensors linked to your garden.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ForestGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Scanning for BLE soil sensors...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else if (sensors.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No sensors found. Tap Refresh to scan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.heightIn(max = 280.dp)
                    ) {
                        items(sensors) { sensor ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(sensor.name, fontWeight = FontWeight.Bold)
                                            Text("MAC: ${sensor.macAddress} • Battery: ${sensor.batteryPercent}%", style = MaterialTheme.typography.labelSmall)
                                        }
                                        Badge(containerColor = MintGreen, contentColor = ForestGreen) {
                                            Text("Active", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Soil Moisture: ${sensor.soilMoisturePercent}%", fontWeight = FontWeight.SemiBold, color = WaterBlue)
                                        Text("Temp: ${sensor.temperatureCelsius}°C | Light: ${sensor.sunlightLux} lx", style = MaterialTheme.typography.bodySmall)
                                    }

                                    if (sensor.soilMoisturePercent < 25) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.errorContainer,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("⚠️ Low Moisture Warning!", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { scan() }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
