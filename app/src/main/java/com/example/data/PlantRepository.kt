package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class PlantRepository(
    private val plantDao: PlantDao,
    private val context: Context
) {
    val allPlants: Flow<List<PlantEntity>> = plantDao.getAllPlants()
    val allCareLogs: Flow<List<CareLogEntity>> = plantDao.getAllCareLogs()

    fun getPlantById(id: Long): Flow<PlantEntity?> = plantDao.getPlantById(id)

    fun getCareLogsForPlant(plantId: Long): Flow<List<CareLogEntity>> =
        plantDao.getCareLogsForPlant(plantId)

    fun getGrowthPhotosForPlant(plantId: Long): Flow<List<GrowthPhotoEntity>> =
        plantDao.getGrowthPhotosForPlant(plantId)

    suspend fun seedInitialDataIfEmpty() {
        val current = allPlants.first()
        if (current.isEmpty()) {
            val seeds = DefaultPlantPresets.getInitialSeedPlants()
            seeds.forEach { plant ->
                val id = plantDao.insertPlant(plant)
                plantDao.insertCareLog(
                    CareLogEntity(
                        plantId = id,
                        careType = CareType.WATER.name,
                        timestamp = plant.lastWateredTimestamp,
                        note = "Initial registration watering"
                    )
                )
            }
        }
    }

    suspend fun insertPlant(plant: PlantEntity): Long {
        val id = plantDao.insertPlant(plant)
        plantDao.insertCareLog(
            CareLogEntity(
                plantId = id,
                careType = CareType.WATER.name,
                timestamp = plant.lastWateredTimestamp,
                note = "Added plant to garden"
            )
        )
        return id
    }

    suspend fun updatePlant(plant: PlantEntity) {
        plantDao.updatePlant(plant)
    }

    suspend fun deletePlant(plant: PlantEntity) {
        plantDao.deletePlant(plant)
    }

    suspend fun waterPlant(plantId: Long, note: String? = null) {
        val plant = plantDao.getPlantByIdSync(plantId) ?: return
        val now = System.currentTimeMillis()
        val updated = plant.copy(lastWateredDate = now)
        plantDao.updatePlant(updated)
        plantDao.insertCareLog(
            CareLogEntity(
                plantId = plantId,
                careType = CareType.WATER.name,
                timestamp = now,
                note = note ?: "Watered plant"
            )
        )
    }

    suspend fun waterAllDueInRoom(roomName: String) {
        val plants = allPlants.first().filter { it.roomName == roomName && it.isWaterDue() }
        val now = System.currentTimeMillis()
        plants.forEach { plant ->
            val updated = plant.copy(lastWateredDate = now)
            plantDao.updatePlant(updated)
            plantDao.insertCareLog(
                CareLogEntity(
                    plantId = plant.id,
                    careType = CareType.WATER.name,
                    timestamp = now,
                    note = "One-tap room water session"
                )
            )
        }
    }

    suspend fun waterAllDueTotal() {
        val plants = allPlants.first().filter { it.isWaterDue() }
        val now = System.currentTimeMillis()
        plants.forEach { plant ->
            val updated = plant.copy(lastWateredDate = now)
            plantDao.updatePlant(updated)
            plantDao.insertCareLog(
                CareLogEntity(
                    plantId = plant.id,
                    careType = CareType.WATER.name,
                    timestamp = now,
                    note = "One-tap total water all due"
                )
            )
        }
    }

    suspend fun snoozePlantWatering(plantId: Long, daysToSnooze: Int = 2) {
        val plant = plantDao.getPlantByIdSync(plantId) ?: return
        // Snooze pushes lastWateredDate forward by (daysToSnooze) so the next due calculation moves forward
        val snoozeMs = TimeUnit.DAYS.toMillis(daysToSnooze.toLong())
        val newLastWatered = plant.lastWateredTimestamp + snoozeMs
        val updated = plant.copy(lastWateredDate = newLastWatered)
        plantDao.updatePlant(updated)
        plantDao.insertCareLog(
            CareLogEntity(
                plantId = plantId,
                careType = CareType.WATER.name,
                timestamp = System.currentTimeMillis(),
                note = "Snoozed watering by $daysToSnooze days (Soil still moist)"
            )
        )
    }

    suspend fun logCare(plantId: Long, careType: CareType, note: String? = null) {
        val plant = plantDao.getPlantByIdSync(plantId) ?: return
        val now = System.currentTimeMillis()
        val updated = when (careType) {
            CareType.WATER -> plant.copy(lastWateredDate = now)
            CareType.FERTILIZE -> plant.copy(lastFertilizedTimestamp = now)
            CareType.MIST -> plant.copy(lastMistedTimestamp = now)
            CareType.REPOT -> plant.copy(lastRepottedTimestamp = now)
        }
        plantDao.updatePlant(updated)
        plantDao.insertCareLog(
            CareLogEntity(
                plantId = plantId,
                careType = careType.name,
                timestamp = now,
                note = note ?: "${careType.title} completed"
            )
        )
    }

    suspend fun addGrowthPhoto(plantId: Long, imagePath: String, note: String? = null) {
        plantDao.insertGrowthPhoto(
            GrowthPhotoEntity(
                plantId = plantId,
                imagePath = imagePath,
                timestamp = System.currentTimeMillis(),
                note = note
            )
        )
        // Set as main plant cover photo if plant imagePath is null
        val plant = plantDao.getPlantByIdSync(plantId)
        if (plant != null && plant.imagePath == null) {
            plantDao.updatePlant(plant.copy(imagePath = imagePath))
        }
    }

    suspend fun deleteGrowthPhoto(photo: GrowthPhotoEntity) {
        plantDao.deleteGrowthPhoto(photo)
    }

    suspend fun exportDataAsJson(): String {
        val plants = allPlants.first()
        val logs = allCareLogs.first()

        val rootObj = JSONObject()
        rootObj.put("version", 1)
        rootObj.put("appName", "PlantPulse")
        rootObj.put("exportedAt", System.currentTimeMillis())

        val plantsArr = JSONArray()
        plants.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("species", p.species)
                put("room", p.room)
                put("waterIntervalDays", p.waterIntervalDays)
                put("fertilizeIntervalDays", p.fertilizeIntervalDays)
                put("mistIntervalDays", p.mistIntervalDays)
                put("repotIntervalDays", p.repotIntervalDays)
                put("lastWateredDate", p.lastWateredDate)
                put("lastFertilizedTimestamp", p.lastFertilizedTimestamp)
                put("lastMistedTimestamp", p.lastMistedTimestamp)
                put("lastRepottedTimestamp", p.lastRepottedTimestamp)
                put("seasonPresetName", p.seasonPresetName)
                put("climateSettingName", p.climateSettingName)
                put("seasonalWateringFrequency", p.seasonalWateringFrequency)
                put("notes", p.notes)
                put("iconKey", p.iconKey)
            }
            plantsArr.put(obj)
        }
        rootObj.put("plants", plantsArr)

        val logsArr = JSONArray()
        logs.forEach { l ->
            val obj = JSONObject().apply {
                put("plantId", l.plantId)
                put("careType", l.careType)
                put("timestamp", l.timestamp)
                put("note", l.note)
            }
            logsArr.put(obj)
        }
        rootObj.put("careLogs", logsArr)

        return rootObj.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val rootObj = JSONObject(jsonStr)
            val plantsArr = rootObj.getJSONArray("plants")
            
            plantDao.clearPlants()
            plantDao.clearCareLogs()

            for (i in 0 until plantsArr.length()) {
                val p = plantsArr.getJSONObject(i)
                val plant = PlantEntity(
                    id = p.optLong("id", 0L),
                    name = p.getString("name"),
                    species = p.optString("species", ""),
                    room = p.optString("room", p.optString("roomName", "Living Room")),
                    waterIntervalDays = p.optInt("waterIntervalDays", 7),
                    fertilizeIntervalDays = p.optInt("fertilizeIntervalDays", 21),
                    mistIntervalDays = p.optInt("mistIntervalDays", 3),
                    repotIntervalDays = p.optInt("repotIntervalDays", 365),
                    lastWateredDate = p.optLong("lastWateredDate", p.optLong("lastWateredTimestamp", System.currentTimeMillis())),
                    lastFertilizedTimestamp = p.optLong("lastFertilizedTimestamp", System.currentTimeMillis()),
                    lastMistedTimestamp = p.optLong("lastMistedTimestamp", System.currentTimeMillis()),
                    lastRepottedTimestamp = p.optLong("lastRepottedTimestamp", System.currentTimeMillis()),
                    seasonPresetName = p.optString("seasonPresetName", SeasonPreset.SPRING_AUTUMN.name),
                    climateSettingName = p.optString("climateSettingName", ClimateSetting.INDOOR.name),
                    seasonalWateringFrequency = p.optString("seasonalWateringFrequency", "{\"SPRING_AUTUMN\":7,\"SUMMER\":5,\"WINTER\":10}"),
                    notes = p.optString("notes", ""),
                    iconKey = p.optString("iconKey", "monstera")
                )
                plantDao.insertPlant(plant)
            }

            if (rootObj.has("careLogs")) {
                val logsArr = rootObj.getJSONArray("careLogs")
                for (i in 0 until logsArr.length()) {
                    val l = logsArr.getJSONObject(i)
                    plantDao.insertCareLog(
                        CareLogEntity(
                            plantId = l.getLong("plantId"),
                            careType = l.getString("careType"),
                            timestamp = l.getLong("timestamp"),
                            note = if (l.has("note") && !l.isNull("note")) l.getString("note") else null
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
