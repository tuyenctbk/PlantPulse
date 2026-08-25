package com.example.util

import android.util.Base64
import com.example.data.CareLogEntity
import com.example.data.GrowthPhotoEntity
import com.example.data.PlantEntity
import com.example.data.PlantNoteEntity
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object EncryptedDatabaseBackupManager {

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_GEN_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256

    data class DecryptedBackup(
        val plants: List<PlantEntity>,
        val careLogs: List<CareLogEntity>,
        val growthPhotos: List<GrowthPhotoEntity>,
        val notes: List<PlantNoteEntity>
    )

    fun createEncryptedBackupJson(
        passphrase: String,
        plants: List<PlantEntity>,
        careLogs: List<CareLogEntity>,
        growthPhotos: List<GrowthPhotoEntity>,
        notes: List<PlantNoteEntity>
    ): String {
        // 1. Build Raw JSON
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val plantsArr = JSONArray()
        plants.forEach { plant ->
            plantsArr.put(JSONObject().apply {
                put("id", plant.id)
                put("name", plant.name)
                put("species", plant.species)
                put("room", plant.room)
                put("waterIntervalDays", plant.waterIntervalDays)
                put("lastWateredDate", plant.lastWateredDate)
                put("fertilizeIntervalDays", plant.fertilizeIntervalDays)
                put("lastFertilizedTimestamp", plant.lastFertilizedTimestamp)
                put("mistIntervalDays", plant.mistIntervalDays)
                put("lastMistedTimestamp", plant.lastMistedTimestamp)
                put("seasonPresetName", plant.seasonPresetName)
                put("climateSettingName", plant.climateSettingName)
                put("seasonalWateringFrequency", plant.seasonalWateringFrequency)
                put("notes", plant.notes)
                put("imagePath", plant.imagePath ?: "")
                put("iconKey", plant.iconKey)
                put("createdAt", plant.createdAt)
            })
        }
        root.put("plants", plantsArr)

        val logsArr = JSONArray()
        careLogs.forEach { log ->
            logsArr.put(JSONObject().apply {
                put("id", log.id)
                put("plantId", log.plantId)
                put("careType", log.careType)
                put("timestamp", log.timestamp)
                put("note", log.note ?: "")
            })
        }
        root.put("careLogs", logsArr)

        val photosArr = JSONArray()
        growthPhotos.forEach { photo ->
            photosArr.put(JSONObject().apply {
                put("id", photo.id)
                put("plantId", photo.plantId)
                put("imagePath", photo.imagePath)
                put("timestamp", photo.timestamp)
                put("note", photo.note ?: "")
            })
        }
        root.put("growthPhotos", photosArr)

        val notesArr = JSONArray()
        notes.forEach { note ->
            notesArr.put(JSONObject().apply {
                put("id", note.id)
                put("plantId", note.plantId)
                put("text", note.text)
                put("timestamp", note.timestamp)
            })
        }
        root.put("notes", notesArr)

        val rawJson = root.toString()

        // 2. Encrypt using PBKDF2 + AES-256-CBC
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secretKeyFactory = SecretKeyFactory.getInstance(KEY_GEN_ALGORITHM)
        val secretKeyBytes = secretKeyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val encryptedBytes = cipher.doFinal(rawJson.toByteArray(Charsets.UTF_8))

        // 3. Envelope Package
        val packageObj = JSONObject().apply {
            put("type", "PLANT_PULSE_ENCRYPTED_BACKUP")
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            put("data", Base64.encodeToString(encryptedBytes, Base64.NO_WRAP))
        }

        return packageObj.toString()
    }

    fun decryptBackupJson(passphrase: String, encryptedPackageStr: String): DecryptedBackup {
        val packageObj = JSONObject(encryptedPackageStr)

        if (!packageObj.has("type") || packageObj.optString("type") != "PLANT_PULSE_ENCRYPTED_BACKUP") {
            throw IllegalArgumentException("Invalid backup file format.")
        }

        val salt = Base64.decode(packageObj.getString("salt"), Base64.NO_WRAP)
        val iv = Base64.decode(packageObj.getString("iv"), Base64.NO_WRAP)
        val encryptedData = Base64.decode(packageObj.getString("data"), Base64.NO_WRAP)

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val secretKeyFactory = SecretKeyFactory.getInstance(KEY_GEN_ALGORITHM)
        val secretKeyBytes = secretKeyFactory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")

        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        val decryptedBytes = cipher.doFinal(encryptedData)
        val rawJson = String(decryptedBytes, Charsets.UTF_8)

        val root = JSONObject(rawJson)

        val plantsList = mutableListOf<PlantEntity>()
        val plantsArr = root.optJSONArray("plants") ?: JSONArray()
        for (i in 0 until plantsArr.length()) {
            val obj = plantsArr.getJSONObject(i)
            plantsList.add(
                PlantEntity(
                    id = obj.optLong("id", 0),
                    name = obj.getString("name"),
                    species = obj.optString("species", ""),
                    room = obj.optString("room", "Living Room"),
                    waterIntervalDays = obj.optInt("waterIntervalDays", 7),
                    lastWateredDate = obj.optLong("lastWateredDate", System.currentTimeMillis()),
                    fertilizeIntervalDays = obj.optInt("fertilizeIntervalDays", 30),
                    lastFertilizedTimestamp = obj.optLong("lastFertilizedTimestamp", System.currentTimeMillis()),
                    mistIntervalDays = obj.optInt("mistIntervalDays", 3),
                    lastMistedTimestamp = obj.optLong("lastMistedTimestamp", System.currentTimeMillis()),
                    seasonPresetName = obj.optString("seasonPresetName", "SPRING_AUTUMN"),
                    climateSettingName = obj.optString("climateSettingName", "INDOOR"),
                    seasonalWateringFrequency = obj.optString("seasonalWateringFrequency", ""),
                    notes = obj.optString("notes", ""),
                    imagePath = obj.optString("imagePath").ifBlank { null },
                    iconKey = obj.optString("iconKey", "monstera"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        val careLogsList = mutableListOf<CareLogEntity>()
        val logsArr = root.optJSONArray("careLogs") ?: JSONArray()
        for (i in 0 until logsArr.length()) {
            val obj = logsArr.getJSONObject(i)
            careLogsList.add(
                CareLogEntity(
                    id = obj.optLong("id", 0),
                    plantId = obj.getLong("plantId"),
                    careType = obj.getString("careType"),
                    timestamp = obj.getLong("timestamp"),
                    note = obj.optString("note").ifBlank { null }
                )
            )
        }

        val growthPhotosList = mutableListOf<GrowthPhotoEntity>()
        val photosArr = root.optJSONArray("growthPhotos") ?: JSONArray()
        for (i in 0 until photosArr.length()) {
            val obj = photosArr.getJSONObject(i)
            growthPhotosList.add(
                GrowthPhotoEntity(
                    id = obj.optLong("id", 0),
                    plantId = obj.getLong("plantId"),
                    imagePath = obj.getString("imagePath"),
                    timestamp = obj.getLong("timestamp"),
                    note = obj.optString("note").ifBlank { null }
                )
            )
        }

        val notesList = mutableListOf<PlantNoteEntity>()
        val notesArr = root.optJSONArray("notes") ?: JSONArray()
        for (i in 0 until notesArr.length()) {
            val obj = notesArr.getJSONObject(i)
            notesList.add(
                PlantNoteEntity(
                    id = obj.optLong("id", 0),
                    plantId = obj.getLong("plantId"),
                    text = obj.getString("text"),
                    timestamp = obj.getLong("timestamp")
                )
            )
        }

        return DecryptedBackup(
            plants = plantsList,
            careLogs = careLogsList,
            growthPhotos = growthPhotosList,
            notes = notesList
        )
    }
}
