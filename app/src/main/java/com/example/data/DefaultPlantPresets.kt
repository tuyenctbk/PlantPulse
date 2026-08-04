package com.example.data

data class PlantPreset(
    val name: String,
    val species: String,
    val defaultRoom: String,
    val waterIntervalDays: Int,
    val fertilizeIntervalDays: Int,
    val mistIntervalDays: Int,
    val iconKey: String,
    val description: String
)

object DefaultPlantPresets {
    val presets = listOf(
        PlantPreset(
            name = "Monstera Deliciosa",
            species = "Monstera deliciosa",
            defaultRoom = "Living Room",
            waterIntervalDays = 7,
            fertilizeIntervalDays = 21,
            mistIntervalDays = 3,
            iconKey = "monstera",
            description = "Loves indirect bright sunlight & humid air. Water when top 2 inches dry."
        ),
        PlantPreset(
            name = "Snake Plant",
            species = "Sansevieria trifasciata",
            defaultRoom = "Bedroom",
            waterIntervalDays = 14,
            fertilizeIntervalDays = 30,
            mistIntervalDays = 14,
            iconKey = "snake_plant",
            description = "Ultra hardy & air purifying. Let soil dry out completely between waterings."
        ),
        PlantPreset(
            name = "Peace Lily",
            species = "Spathiphyllum",
            defaultRoom = "Office Desk",
            waterIntervalDays = 5,
            fertilizeIntervalDays = 14,
            mistIntervalDays = 2,
            iconKey = "peace_lily",
            description = "Droops dramatically when thirsty. Prefers moist soil and frequent leaf misting."
        ),
        PlantPreset(
            name = "Golden Pothos",
            species = "Epipremnum aureum",
            defaultRoom = "Living Room",
            waterIntervalDays = 6,
            fertilizeIntervalDays = 21,
            mistIntervalDays = 4,
            iconKey = "pothos",
            description = "Fast trailing vine. Very forgiving, thrives in almost any indoor lighting."
        ),
        PlantPreset(
            name = "Fiddle Leaf Fig",
            species = "Ficus lyrata",
            defaultRoom = "Living Room",
            waterIntervalDays = 8,
            fertilizeIntervalDays = 21,
            mistIntervalDays = 5,
            iconKey = "fiddle_leaf",
            description = "Requires consistent light & watering rhythm. Wipe dust from wide glossy leaves."
        ),
        PlantPreset(
            name = "Aloe Vera",
            species = "Aloe barbadensis",
            defaultRoom = "Sunny Balcony",
            waterIntervalDays = 12,
            fertilizeIntervalDays = 30,
            mistIntervalDays = 0,
            iconKey = "aloe",
            description = "Sun-loving succulent. Stores water in fleshy leaves—avoid overwatering!"
        ),
        PlantPreset(
            name = "Calathea Rattlesnake",
            species = "Goeppertia insignis",
            defaultRoom = "Bedroom",
            waterIntervalDays = 6,
            fertilizeIntervalDays = 21,
            mistIntervalDays = 2,
            iconKey = "calathea",
            description = "Prayer plant with striking foliage. Loves high humidity & distilled water."
        ),
        PlantPreset(
            name = "ZZ Plant",
            species = "Zamioculcas zamiifolia",
            defaultRoom = "Hallway",
            waterIntervalDays = 18,
            fertilizeIntervalDays = 30,
            mistIntervalDays = 14,
            iconKey = "zz_plant",
            description = "Indestructible waxy leaves. Thrives on neglect and low ambient light."
        )
    )

    fun getInitialSeedPlants(): List<PlantEntity> {
        val now = System.currentTimeMillis()
        val oneDayMs = 86400000L
        val threeDaysMs = 3 * oneDayMs
        val sixDaysMs = 6 * oneDayMs

        return listOf(
            PlantEntity(
                name = "Monstera Deliciosa",
                species = "Monstera deliciosa",
                room = "Living Room",
                waterIntervalDays = 7,
                fertilizeIntervalDays = 21,
                mistIntervalDays = 3,
                lastWateredDate = now - (7 * oneDayMs), // Due today!
                lastFertilizedTimestamp = now - (21 * oneDayMs), // Due today!
                lastMistedTimestamp = now - oneDayMs,
                seasonPresetName = SeasonPreset.SPRING_AUTUMN.name,
                climateSettingName = ClimateSetting.INDOOR.name,
                notes = "Near south window with sheer curtain.",
                iconKey = "monstera"
            ),
            PlantEntity(
                name = "Snake Plant",
                species = "Sansevieria trifasciata",
                room = "Office Desk",
                waterIntervalDays = 14,
                fertilizeIntervalDays = 30,
                mistIntervalDays = 14,
                lastWateredDate = now - (14 * oneDayMs), // Due today!
                lastFertilizedTimestamp = now - (10 * oneDayMs),
                lastMistedTimestamp = now - (5 * oneDayMs),
                seasonPresetName = SeasonPreset.SPRING_AUTUMN.name,
                climateSettingName = ClimateSetting.INDOOR.name,
                notes = "Super low maintenance desk buddy.",
                iconKey = "snake_plant"
            ),
            PlantEntity(
                name = "Peace Lily",
                species = "Spathiphyllum",
                room = "Living Room",
                waterIntervalDays = 5,
                fertilizeIntervalDays = 14,
                mistIntervalDays = 2,
                lastWateredDate = now - (5 * oneDayMs), // Due today!
                lastFertilizedTimestamp = now - (2 * oneDayMs),
                lastMistedTimestamp = now - oneDayMs,
                seasonPresetName = SeasonPreset.SUMMER.name,
                climateSettingName = ClimateSetting.INDOOR.name,
                notes = "Mist leaves frequently.",
                iconKey = "peace_lily"
            ),
            PlantEntity(
                name = "Golden Pothos",
                species = "Epipremnum aureum",
                room = "Balcony",
                waterIntervalDays = 6,
                fertilizeIntervalDays = 21,
                mistIntervalDays = 4,
                lastWateredDate = now - (2 * oneDayMs), // 4 days remaining
                lastFertilizedTimestamp = now - (5 * oneDayMs),
                lastMistedTimestamp = now,
                seasonPresetName = SeasonPreset.SPRING_AUTUMN.name,
                climateSettingName = ClimateSetting.BALCONY_SUN.name,
                notes = "Trailing down shelf.",
                iconKey = "pothos"
            ),
            PlantEntity(
                name = "Aloe Vera",
                species = "Aloe barbadensis",
                room = "Balcony",
                waterIntervalDays = 12,
                fertilizeIntervalDays = 30,
                mistIntervalDays = 0,
                lastWateredDate = now - (8 * oneDayMs), // 4 days remaining
                lastFertilizedTimestamp = now - (15 * oneDayMs),
                lastMistedTimestamp = now,
                seasonPresetName = SeasonPreset.SPRING_AUTUMN.name,
                climateSettingName = ClimateSetting.BALCONY_SUN.name,
                notes = "Loves afternoon sunshine.",
                iconKey = "aloe"
            )
        )
    }
}
