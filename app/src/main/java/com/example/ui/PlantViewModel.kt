package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.ImageStorageHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import android.content.Context
import com.example.ui.theme.ThemeMode
import com.example.util.FirebaseManager

data class PlantUiState(
    val plants: List<PlantEntity> = emptyList(),
    val selectedRoomFilter: String? = null,
    val searchQuery: String = "",
    val activeTab: Int = 0, // 0: Dashboard/Today, 1: My Jungle, 2: Care History & Analytics
    val isAddPlantSheetOpen: Boolean = false,
    val isPresetPickerOpen: Boolean = false,
    val isEncyclopediaOpen: Boolean = false,
    val isBackupDialogOpen: Boolean = false,
    val isRateAppDialogPromptOpen: Boolean = false,
    val isShareAppDialogPromptOpen: Boolean = false,
    val selectedCity: CityLocation = DefaultCities.cities[0],
    val currentWeather: WeatherResponse? = null,
    val isWeatherLoading: Boolean = false,
    val weatherAdjustmentFactor: Double = 1.0,
    val isCitySelectorOpen: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val totalCareActionsCount: Int = 0,
    val hasRatedApp: Boolean = false,
    val hasSharedApp: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val activePlantForScrapbook: PlantEntity? = null,
    val activePlantForDetail: PlantEntity? = null,
    val userToast: String? = null
)

class PlantViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PlantRepository
    private val prefs = application.getSharedPreferences("plantpulse_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(PlantUiState())
    val uiState: StateFlow<PlantUiState> = _uiState.asStateFlow()

    val allCareLogs: StateFlow<List<CareLogEntity>> by lazy {
        repository.allCareLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PlantRepository(db.plantDao(), application)

        // Load persisted settings
        val savedThemeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val savedTheme = runCatching { ThemeMode.valueOf(savedThemeStr) }.getOrDefault(ThemeMode.SYSTEM)
        val savedCareCount = prefs.getInt("care_actions_count", 0)
        val savedHasRated = prefs.getBoolean("has_rated_app", false)
        val savedHasShared = prefs.getBoolean("has_shared_app", false)
        val savedHasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)
        val savedCityName = prefs.getString("selected_city_name", "New York") ?: "New York"
        val foundCity = DefaultCities.cities.find { it.name == savedCityName } ?: DefaultCities.cities[0]

        _uiState.update {
            it.copy(
                themeMode = savedTheme,
                totalCareActionsCount = savedCareCount,
                hasRatedApp = savedHasRated,
                hasSharedApp = savedHasShared,
                hasCompletedOnboarding = savedHasCompletedOnboarding,
                selectedCity = foundCity
            )
        }

        fetchWeather(foundCity)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        viewModelScope.launch {
            repository.allPlants.collect { plantList ->
                _uiState.update { it.copy(plants = plantList) }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString("theme_mode", mode.name).apply()
        _uiState.update { it.copy(themeMode = mode) }
        FirebaseManager.logThemeChanged(getApplication(), mode.name)
        showToast("Theme changed to ${mode.label}")
    }

    fun openRateAppPrompt(open: Boolean) {
        _uiState.update { it.copy(isRateAppDialogPromptOpen = open) }
    }

    fun openShareAppPrompt(open: Boolean) {
        _uiState.update { it.copy(isShareAppDialogPromptOpen = open) }
    }

    fun markAppRated(rating: Int) {
        prefs.edit().putBoolean("has_rated_app", true).apply()
        _uiState.update { it.copy(hasRatedApp = true, isRateAppDialogPromptOpen = false) }
        FirebaseManager.logAppRated(getApplication(), rating)
        showToast("Thank you for rating 🌟")
    }

    fun markAppShared() {
        prefs.edit().putBoolean("has_shared_app", true).apply()
        _uiState.update { it.copy(hasSharedApp = true, isShareAppDialogPromptOpen = false) }
        FirebaseManager.logAppShared(getApplication())
        showToast("Thank you for sharing PlantPulse! 🪴")
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("has_completed_onboarding", true).apply()
        _uiState.update { it.copy(hasCompletedOnboarding = true) }
        FirebaseManager.logEvent(getApplication(), "onboarding_completed")
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean("has_completed_onboarding", false).apply()
        _uiState.update { it.copy(hasCompletedOnboarding = false) }
        FirebaseManager.logEvent(getApplication(), "onboarding_reset")
        showToast("Onboarding reset! Welcome back. 👋")
    }

    private fun checkSmartRatingTrigger() {
        val hasRated = _uiState.value.hasRatedApp
        if (hasRated) return

        val careCount = _uiState.value.totalCareActionsCount
        val plants = _uiState.value.plants

        // Smart conditions to capture perfect "Happy State"
        // 1. Decent user engagement (at least 5 successful care logs)
        // 2. Added at least 2 plants to track
        val isEngaged = careCount >= 5 && plants.size >= 2

        // 3. Perfect state (triumph): all registered plants have zero due tasks!
        val allPlantsHappy = plants.isNotEmpty() && plants.none { it.isAnyCareDue() }

        val lastPrompted = prefs.getInt("last_prompted_care_count", 0)
        // Cooldown: at least 8 care actions between automatic prompts if they clicked 'Later'
        val cooldownPassed = (careCount - lastPrompted) >= 8 || lastPrompted == 0

        if (isEngaged && allPlantsHappy && cooldownPassed) {
            prefs.edit().putInt("last_prompted_care_count", careCount).apply()
            _uiState.update { it.copy(isRateAppDialogPromptOpen = true) }
            FirebaseManager.logEvent(getApplication(), "smart_rating_prompt_triggered")
        }
    }

    private fun incrementCareActionAndCheckPrompts() {
        val newCount = _uiState.value.totalCareActionsCount + 1
        prefs.edit().putInt("care_actions_count", newCount).apply()
        _uiState.update { it.copy(totalCareActionsCount = newCount) }

        val hasRated = _uiState.value.hasRatedApp
        val hasShared = _uiState.value.hasSharedApp

        if (!hasRated) {
            checkSmartRatingTrigger()
        } else if (!hasShared && newCount >= 8) {
            _uiState.update { it.copy(isShareAppDialogPromptOpen = true) }
        }
    }

    fun selectRoomFilter(roomName: String?) {
        _uiState.update { it.copy(selectedRoomFilter = if (roomName == "All") null else roomName) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun openAddPlantSheet(open: Boolean) {
        _uiState.update { it.copy(isAddPlantSheetOpen = open) }
    }

    fun openPresetPicker(open: Boolean) {
        _uiState.update { it.copy(isPresetPickerOpen = open) }
    }

    fun openEncyclopedia(open: Boolean) {
        _uiState.update { it.copy(isEncyclopediaOpen = open) }
    }

    fun openBackupDialog(open: Boolean) {
        _uiState.update { it.copy(isBackupDialogOpen = open) }
    }

    fun openCitySelector(open: Boolean) {
        _uiState.update { it.copy(isCitySelectorOpen = open) }
    }

    fun fetchWeather(city: CityLocation) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWeatherLoading = true, selectedCity = city, isCitySelectorOpen = false) }
            prefs.edit().putString("selected_city_name", city.name).apply()
            try {
                val response = WeatherApiService.instance.getCurrentWeather(lat = city.latitude, lon = city.longitude)
                val current = response.current
                var factor = 1.0
                if (current != null) {
                    val temp = current.temperature_2m
                    val humidity = current.relative_humidity_2m
                    val uv = current.uv_index ?: 0.0
                    val solarRad = current.surface_solar_radiation ?: 0.0
                    val precip = (current.precipitation ?: 0.0) + (current.rain ?: 0.0)

                    factor = when {
                        // Rainy day or heavy rain: Delay watering reminders by 40% (soil wet)
                        precip > 0.5 || humidity >= 85 -> 1.4
                        // High humidity or cold weather: Extend watering interval by 25%
                        humidity >= 75 || temp <= 15.0 -> 1.25
                        // High UV index, high solar radiation, or heatwave: Accelerate watering by 25%
                        (temp >= 30.0 || uv >= 6.0 || solarRad >= 500.0) && humidity <= 40 -> 0.75
                        // Warm/Sunny: Accelerate watering schedule by 15%
                        temp >= 25.0 || uv >= 5.0 || humidity <= 45 -> 0.85
                        else -> 1.0
                    }
                }
                _uiState.update { it.copy(currentWeather = response, weatherAdjustmentFactor = factor, isWeatherLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isWeatherLoading = false) }
            }
        }
    }

    fun setActivePlantForScrapbook(plant: PlantEntity?) {
        _uiState.update { it.copy(activePlantForScrapbook = plant) }
    }

    fun setActivePlantForDetail(plant: PlantEntity?) {
        _uiState.update { it.copy(activePlantForDetail = plant) }
    }

    fun clearToast() {
        _uiState.update { it.copy(userToast = null) }
    }

    fun waterPlant(plantId: Long, note: String? = null) {
        viewModelScope.launch {
            repository.waterPlant(plantId, note)
            val plant = _uiState.value.plants.find { it.id == plantId }
            FirebaseManager.logPlantCare(getApplication(), "WATER", plant?.name ?: "Plant")
            showToast("💧 Plant watered successfully!")
            incrementCareActionAndCheckPrompts()
        }
    }

    fun waterAllDueInRoom(roomName: String) {
        viewModelScope.launch {
            repository.waterAllDueInRoom(roomName)
            FirebaseManager.logPlantCare(getApplication(), "WATER_ROOM", roomName)
            showToast("🚿 Watered all due plants in $roomName!")
            incrementCareActionAndCheckPrompts()
        }
    }

    fun waterAllDueTotal() {
        viewModelScope.launch {
            repository.waterAllDueTotal()
            FirebaseManager.logPlantCare(getApplication(), "WATER_ALL", "Jungle")
            showToast("🚿 All due plants in your jungle watered!")
            incrementCareActionAndCheckPrompts()
        }
    }

    fun waterPlants(plantIds: List<Long>) {
        if (plantIds.isEmpty()) return
        viewModelScope.launch {
            repository.waterPlants(plantIds)
            FirebaseManager.logPlantCare(getApplication(), "WATER_BATCH", "${plantIds.size} plants")
            showToast("🚿 Watered ${plantIds.size} selected plants!")
            incrementCareActionAndCheckPrompts()
        }
    }

    fun snoozeWatering(plantId: Long, days: Int = 2) {
        viewModelScope.launch {
            repository.snoozePlantWatering(plantId, days)
            showToast("⏳ Snoozed watering for $days days (Soil still moist)")
        }
    }

    fun getNotesForPlant(plantId: Long): Flow<List<PlantNoteEntity>> =
        repository.getNotesForPlant(plantId)

    fun addPlantNote(plantId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addPlantNote(plantId, text.trim())
            showToast("📝 Note recorded!")
        }
    }

    fun deletePlantNote(note: PlantNoteEntity) {
        viewModelScope.launch {
            repository.deletePlantNote(note)
            showToast("🗑️ Note deleted")
        }
    }

    fun logCare(plantId: Long, careType: CareType, note: String? = null) {
        viewModelScope.launch {
            repository.logCare(plantId, careType, note)
            val plant = _uiState.value.plants.find { it.id == plantId }
            FirebaseManager.logPlantCare(getApplication(), careType.name, plant?.name ?: "Plant")
            showToast("${careType.emoji} ${careType.title} logged!")
            incrementCareActionAndCheckPrompts()
        }
    }

    fun addGrowthPhoto(plantId: Long, uri: Uri, note: String? = null) {
        viewModelScope.launch {
            val savedPath = ImageStorageHelper.saveCompressedImage(getApplication(), uri)
            if (savedPath != null) {
                repository.addGrowthPhoto(plantId, savedPath, note)
                showToast("📸 Growth photo added to scrapbook!")
            } else {
                showToast("Failed to save photo")
            }
        }
    }

    fun deleteGrowthPhoto(photo: GrowthPhotoEntity) {
        viewModelScope.launch {
            repository.deleteGrowthPhoto(photo)
            showToast("Photo deleted from scrapbook")
        }
    }

    fun addPlantFromPreset(preset: PlantPreset) {
        viewModelScope.launch {
            val newPlant = PlantEntity(
                name = preset.name,
                species = preset.species,
                room = preset.defaultRoom,
                waterIntervalDays = preset.waterIntervalDays,
                fertilizeIntervalDays = preset.fertilizeIntervalDays,
                mistIntervalDays = preset.mistIntervalDays,
                lastWateredDate = System.currentTimeMillis(),
                lastFertilizedTimestamp = System.currentTimeMillis(),
                lastMistedTimestamp = System.currentTimeMillis(),
                seasonPresetName = SeasonPreset.SPRING_AUTUMN.name,
                climateSettingName = ClimateSetting.INDOOR.name,
                notes = preset.description,
                iconKey = preset.iconKey
            )
            repository.insertPlant(newPlant)
            _uiState.update { it.copy(isPresetPickerOpen = false, isEncyclopediaOpen = false) }
            showToast("🪴 ${preset.name} added to your garden!")
        }
    }

    fun saveCustomPlant(plant: PlantEntity) {
        viewModelScope.launch {
            if (plant.id == 0L) {
                repository.insertPlant(plant)
                showToast("🪴 Added ${plant.name} to garden")
            } else {
                repository.updatePlant(plant)
                showToast("Updated ${plant.name}")
            }
            _uiState.update { it.copy(isAddPlantSheetOpen = false, activePlantForDetail = null) }
        }
    }

    fun deletePlant(plant: PlantEntity) {
        viewModelScope.launch {
            repository.deletePlant(plant)
            _uiState.update { it.copy(activePlantForDetail = null) }
            showToast("Deleted ${plant.name}")
        }
    }

    fun getCareLogsForPlant(plantId: Long): Flow<List<CareLogEntity>> {
        return repository.getCareLogsForPlant(plantId)
    }

    fun getGrowthPhotosForPlant(plantId: Long): Flow<List<GrowthPhotoEntity>> {
        return repository.getGrowthPhotosForPlant(plantId)
    }

    fun exportBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportDataAsJson()
            onResult(json)
        }
    }

    fun exportEncryptedBackup(passphrase: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val encrypted = repository.exportEncryptedBackup(passphrase)
            onResult(encrypted)
        }
    }

    fun importEncryptedBackup(passphrase: String, encryptedData: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importEncryptedBackup(passphrase, encryptedData)
            if (success) {
                showToast("🔐 Encrypted backup restored successfully!")
            } else {
                showToast("❌ Decryption failed! Invalid password or corrupted data.")
            }
            onComplete(success)
        }
    }

    fun importBackupJson(jsonStr: String) {
        viewModelScope.launch {
            val success = repository.importDataFromJson(jsonStr)
            if (success) {
                showToast("Data restored successfully!")
            } else {
                showToast("Failed to parse backup JSON")
            }
        }
    }

    private fun showToast(msg: String) {
        _uiState.update { it.copy(userToast = msg) }
    }
}
