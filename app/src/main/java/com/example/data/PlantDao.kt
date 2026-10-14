package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants ORDER BY createdAt DESC")
    fun getAllPlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants")
    suspend fun getAllPlantsSync(): List<PlantEntity>

    @Query("SELECT * FROM plants WHERE id = :id")
    fun getPlantById(id: Long): Flow<PlantEntity?>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlantByIdSync(id: Long): PlantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity): Long

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlantById(id: Long)

    @Query("SELECT * FROM care_logs WHERE plantId = :plantId ORDER BY timestamp DESC")
    fun getCareLogsForPlant(plantId: Long): Flow<List<CareLogEntity>>

    @Query("SELECT * FROM care_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllCareLogs(): Flow<List<CareLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareLog(log: CareLogEntity)

    @Query("DELETE FROM care_logs WHERE plantId = :plantId")
    suspend fun deleteCareLogsForPlant(plantId: Long)

    @Query("SELECT * FROM growth_photos WHERE plantId = :plantId ORDER BY timestamp ASC")
    fun getGrowthPhotosForPlant(plantId: Long): Flow<List<GrowthPhotoEntity>>

    @Query("SELECT * FROM growth_photos WHERE plantId = :plantId")
    suspend fun getGrowthPhotosForPlantSync(plantId: Long): List<GrowthPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrowthPhoto(photo: GrowthPhotoEntity): Long

    @Delete
    suspend fun deleteGrowthPhoto(photo: GrowthPhotoEntity)

    @Query("DELETE FROM growth_photos WHERE plantId = :plantId")
    suspend fun deleteGrowthPhotosForPlant(plantId: Long)

    @Query("SELECT * FROM plant_notes WHERE plantId = :plantId ORDER BY timestamp DESC")
    fun getNotesForPlant(plantId: Long): Flow<List<PlantNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantNote(note: PlantNoteEntity): Long

    @Delete
    suspend fun deletePlantNote(note: PlantNoteEntity)

    @Query("DELETE FROM plant_notes WHERE plantId = :plantId")
    suspend fun deleteNotesForPlant(plantId: Long)

    @Query("DELETE FROM plants")
    suspend fun clearPlants()

    @Query("DELETE FROM care_logs")
    suspend fun clearCareLogs()

    @Query("DELETE FROM growth_photos")
    suspend fun clearGrowthPhotos()

    @Query("DELETE FROM plant_notes")
    suspend fun clearPlantNotes()
}
