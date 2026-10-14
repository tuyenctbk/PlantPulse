package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CityLocation
import com.example.data.WeatherResponse
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGreen

@Composable
fun WeatherWidgetCard(
    city: CityLocation,
    weather: WeatherResponse?,
    isLoading: Boolean,
    weatherAdjustmentFactor: Double,
    onOpenCitySelector: () -> Unit,
    onRefreshWeather: () -> Unit
) {
    val current = weather?.current
    val temp = current?.temperature_2m ?: 22.0
    val humidity = current?.relative_humidity_2m ?: 50
    val uv = current?.uv_index ?: 0.0
    val solarRad = current?.surface_solar_radiation ?: 0.0
    val precip = (current?.precipitation ?: 0.0) + (current?.rain ?: 0.0)

    val recommendationText = when {
        precip > 0.5 -> "🌧️ Rainy conditions (${precip}mm rain, ${humidity}% humidity) — Soil retains moisture. Reminders delayed by ~40% to prevent root rot."
        humidity >= 80 -> "💧 High humidity (${humidity}%) — Slow soil evaporation. Reminders extended."
        uv >= 6.0 || solarRad >= 500.0 || (temp >= 28.0 && humidity <= 40) -> "☀️ High UV (${uv}) & Solar Radiation (${solarRad.toInt()} W/m²) — Accelerated evaporation! Reminders sped up by 20%."
        weatherAdjustmentFactor < 0.9 -> "🌤️ Warm weather (${temp}°C, ${humidity}% humidity) — Slightly faster soil drying."
        weatherAdjustmentFactor > 1.15 -> "🌧️ Cool & Humid (${temp}°C, ${humidity}% humidity) — Extended watering intervals."
        else -> "⛅ Optimal environment (${temp}°C, ${humidity}% humidity, UV ${uv}) — Regular schedules applied."
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onOpenCitySelector)
                        .padding(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${city.name}, ${city.country}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen
                    )
                    Text(" ▾", color = ForestGreen, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ForestGreen)
                    } else {
                        IconButton(onClick = onRefreshWeather, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = ForestGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MintGreen.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "${temp}°C • ${humidity}% 💧",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time Environmental Chips from Open-Meteo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WeatherChip(label = "UV $uv", icon = "☀️")
                WeatherChip(label = "${solarRad.toInt()} W/m²", icon = "🛰️")
                WeatherChip(label = "${precip} mm rain", icon = "🌧️")
                WeatherChip(label = "${humidity}% rh", icon = "💧")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌤️", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = recommendationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherChip(label: String, icon: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = icon, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
