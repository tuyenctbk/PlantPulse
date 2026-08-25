package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.concurrent.TimeUnit

enum class SeasonPreset(val label: String, val multiplier: Double) {
    SPRING_AUTUMN("Spring / Autumn (Standard)", 1.0),
    SUMMER("Summer Growth (+20% Water)", 0.8),
    WINTER("Winter Resting (+40% Longer)", 1.4)
}

enum class ClimateSetting(val label: String, val multiplier: Double) {
    INDOOR("Indoor Room (Normal)", 1.0),
    BALCONY_SUN("Sunny Balcony (+20% Fast Dry)", 0.8),
    HIGH_HUMIDITY("High Humidity / Shaded", 1.25)
}

enum class CareType(val title: String, val emoji: String) {
    WATER("Water", "💧"),
    FERTILIZE("Fertilize", "🧪"),
    MIST("Mist", "🌫️"),
    REPOT("Repot", "🪴")
}

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val species: String = "",
    val room: String = "Living Room",
    val waterIntervalDays: Int = 7,
    val fertilizeIntervalDays: Int = 21,
    val mistIntervalDays: Int = 3,
    val repotIntervalDays: Int = 365,
    val lastWateredDate: Long = System.currentTimeMillis(),
    val lastFertilizedTimestamp: Long = System.currentTimeMillis(),
    val lastMistedTimestamp: Long = System.currentTimeMillis(),
    val lastRepottedTimestamp: Long = System.currentTimeMillis(),
    val seasonPresetName: String = SeasonPreset.SPRING_AUTUMN.name,
    val climateSettingName: String = ClimateSetting.INDOOR.name,
    val seasonalWateringFrequency: String = "{\"SPRING_AUTUMN\":7,\"SUMMER\":5,\"WINTER\":10}",
    val notes: String = "",
    val imagePath: String? = null,
    val iconKey: String = "monstera",
    val createdAt: Long = System.currentTimeMillis()
) {
    val roomName: String get() = room
    val lastWateredTimestamp: Long get() = lastWateredDate

    fun getSeasonPreset(): SeasonPreset {
        return runCatching { SeasonPreset.valueOf(seasonPresetName) }.getOrDefault(SeasonPreset.SPRING_AUTUMN)
    }

    fun getClimateSetting(): ClimateSetting {
        return runCatching { ClimateSetting.valueOf(climateSettingName) }.getOrDefault(ClimateSetting.INDOOR)
    }

    fun getSeasonalWaterInterval(seasonKey: String, defaultDays: Int): Int {
        if (seasonalWateringFrequency.isNotBlank()) {
            try {
                val json = org.json.JSONObject(seasonalWateringFrequency)
                if (json.has(seasonKey)) {
                    return json.optInt(seasonKey, defaultDays)
                }
            } catch (e: Exception) {
                // fallback
            }
        }
        return defaultDays
    }

    fun getEffectiveWaterIntervalDays(weatherMultiplier: Double = 1.0): Int {
        val seasonKey = seasonPresetName
        var baseDays = waterIntervalDays
        if (seasonalWateringFrequency.isNotBlank()) {
            try {
                val json = org.json.JSONObject(seasonalWateringFrequency)
                if (json.has(seasonKey)) {
                    baseDays = json.optInt(seasonKey, waterIntervalDays)
                } else {
                    baseDays = (waterIntervalDays * getSeasonPreset().multiplier).toInt()
                }
            } catch (e: Exception) {
                baseDays = (waterIntervalDays * getSeasonPreset().multiplier).toInt()
            }
        } else {
            baseDays = (waterIntervalDays * getSeasonPreset().multiplier).toInt()
        }
        val mult = getClimateSetting().multiplier
        return (baseDays * mult * weatherMultiplier).toInt().coerceAtLeast(1)
    }

    fun getNextWaterDueTimestamp(weatherMultiplier: Double = 1.0): Long {
        return lastWateredDate + TimeUnit.DAYS.toMillis(getEffectiveWaterIntervalDays(weatherMultiplier).toLong())
    }

    fun isWaterDue(weatherMultiplier: Double = 1.0): Boolean {
        return System.currentTimeMillis() >= getNextWaterDueTimestamp(weatherMultiplier)
    }

    fun getDaysUntilWaterDue(weatherMultiplier: Double = 1.0): Int {
        val diffMs = getNextWaterDueTimestamp(weatherMultiplier) - System.currentTimeMillis()
        return (diffMs / TimeUnit.DAYS.toMillis(1)).toInt()
    }

    fun getEffectiveFertilizeIntervalDays(): Int {
        return (fertilizeIntervalDays * getSeasonPreset().multiplier).toInt().coerceAtLeast(1)
    }

    fun getNextFertilizeDueTimestamp(): Long {
        return lastFertilizedTimestamp + TimeUnit.DAYS.toMillis(getEffectiveFertilizeIntervalDays().toLong())
    }

    fun isFertilizeDue(): Boolean {
        return System.currentTimeMillis() >= getNextFertilizeDueTimestamp()
    }

    fun isAnyCareDue(): Boolean {
        return isWaterDue() || isFertilizeDue()
    }
}

typealias PlantEntity = Plant

@Entity(tableName = "care_logs")
data class CareLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val careType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

@Entity(tableName = "growth_photos")
data class GrowthPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val imagePath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)

@Entity(tableName = "plant_notes")
data class PlantNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

