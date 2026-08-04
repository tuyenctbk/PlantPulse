package com.example.ui.components

import androidx.compose.foundation.background
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

    val recommendationText = when {
        weatherAdjustmentFactor < 0.85 -> "☀️ Hot & Dry (${temp}°C, ${humidity}% humidity) — Soil dries 20% faster. Watering schedules adjusted proactively!"
        weatherAdjustmentFactor < 1.0 -> "🌤️ Warm conditions (${temp}°C, ${humidity}% humidity) — Plants need slightly more frequent watering."
        weatherAdjustmentFactor > 1.15 -> "🌧️ Cool & Humid (${temp}°C, ${humidity}% humidity) — Soil retains moisture. Watering intervals extended by 25%."
        else -> "⛅ Ideal Indoor Weather (${temp}°C, ${humidity}% humidity) — Standard plant care schedules in effect."
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
                    Text("🌦️", fontSize = 24.sp)
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
