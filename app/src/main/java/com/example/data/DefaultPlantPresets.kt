package com.example.data

data class PlantPreset(
    val name: String,
    val species: String,
    val defaultRoom: String,
    val waterIntervalDays: Int,
    val fertilizeIntervalDays: Int,
    val mistIntervalDays: Int,
    val iconKey: String,
    val lightRequirement: String,
    val humidityRequirement: String,
    val description: String,
    val isLowLightTolerant: Boolean = false,
    val isPetFriendly: Boolean = false
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
            lightRequirement = "Bright Indirect Sunlight",
            humidityRequirement = "High Humidity (>60%)",
            description = "Loves indirect bright sunlight & humid air. Water when top 2 inches dry.",
            isLowLightTolerant = false,
            isPetFriendly = false
        ),
        PlantPreset(
            name = "Snake Plant",
            species = "Sansevieria trifasciata",
            defaultRoom = "Bedroom",
            waterIntervalDays = 14,
            fertilizeIntervalDays = 30,
            mistIntervalDays = 14,
            iconKey = "snake_plant",
            lightRequirement = "Low to Bright Indirect",
            humidityRequirement = "Low Humidity Tolerant",
            description = "Ultra hardy & air purifying. Let soil dry out completely between waterings.",
            isLowLightTolerant = true,
            isPetFriendly = false
        ),
        PlantPreset(
            name = "Peace Lily",
            species = "Spathiphyllum",
            defaultRoom = "Office Desk",
            waterIntervalDays = 5,
            fertilizeIntervalDays = 14,
            mistIntervalDays = 2,
            iconKey = "peace_lily",
            lightRequirement = "Medium Indirect Light",
            humidityRequirement = "High Humidity / Misting",
            description = "Droops dramatically when thirsty. Prefers moist soil and frequent leaf misting.",
            isLowLightTolerant = true,
            isPetFriendly = false
        ),
        PlantPreset(
            name = "Golden Pothos",
            species = "Epipremnum aureum",
            defaultRoom = "Living Room",
            waterIntervalDays = 6,
            fertilizeIntervalDays = 21,
            mistIntervalDays = 4,
            iconKey = "pothos",
            lightRequirement = "Low to Bright Indirect",
            humidityRequirement = "Average Indoor Humidity",
            description = "Fast trailing vine. Very forgiving, thrives in almost any indoor lighting.",
            isLowLightTolerant = true,
            isPetFriendly = false
        ),
        PlantPreset(
            name = "Fiddle Leaf Fig",
            species = "Ficus lyrata",
            defaultRoom = "Living Room",
            waterIntervalDays = 8,
            fertilizeIntervalDays = 21,
            mistIntervalDays = 5,
            iconKey = "fiddle_leaf",
            lightRequirement = "Bright Filtered Sunlight",
            humidityRequirement = "Moderate to High",
            description = "Requires consistent light & watering rhythm. Wipe dust from wide glossy leaves.",
            isLowLightTolerant = false,
            isPetFriendly = false
        ),
        PlantPreset(
            name = "Aloe Vera",
            species = "Aloe barbadensis",
            defaultRoom = "Sunny Balcony",
            waterIntervalDays = 12,
            fertilizeIntervalDays = 30,
            mistIntervalDays = 0,
            iconKey = "aloe",
            lightRequirement = "Direct Sunlight",
            humidityRequirement = "Low Humidity (Dry Air)",
            description = "Sun-loving succulent. Stores water in fleshy leaves—avoid overwatering!",
            isLowLightTolerant = false,
            isPetFriendly = false
        ),
        PlantPreset(
            name = "Calathea Rattlesnake",
            species = "Goeppertia insignis",
            defaultRoom = "Bedroom",
            waterIntervalDays = 6,
            fertilizeIntervalDays = 21,
            mistIntervalDays = 2,
            iconKey = "calathea",
            lightRequirement = "Medium Indirect Light",
            humidityRequirement = "High Humidity (>65%)",
            description = "Prayer plant with striking foliage. Loves high humidity & distilled water.",
            isLowLightTolerant = true,
            isPetFriendly = true
        ),
        PlantPreset(
            name = "ZZ Plant",
            species = "Zamioculcas zamiifolia",
            defaultRoom = "Hallway",
            waterIntervalDays = 18,
            fertilizeIntervalDays = 30,
            mistIntervalDays = 14,
            iconKey = "zz_plant",
            lightRequirement = "Low to Bright Indirect",
            humidityRequirement = "Low Humidity Tolerant",
            description = "Indestructible waxy leaves. Thrives on neglect and low ambient light.",
            isLowLightTolerant = true,
            isPetFriendly = false
        ),
        PlantPreset(
            name = "Spider Plant",
            species = "Chlorophytum comosum",
            defaultRoom = "Living Room",
            waterIntervalDays = 7,
            fertilizeIntervalDays = 21,
            mistIntervalDays = 3,
            iconKey = "pothos",
            lightRequirement = "Bright Indirect Light",
            humidityRequirement = "Average to High",
            description = "Produces baby spiderettes. Excellent pet-safe air purifier.",
            isLowLightTolerant = true,
            isPetFriendly = true
        ),
        PlantPreset(
            name = "Boston Fern",
            species = "Nephrolepis exaltata",
            defaultRoom = "Bathroom",
            waterIntervalDays = 4,
            fertilizeIntervalDays = 14,
            mistIntervalDays = 1,
            iconKey = "peace_lily",
            lightRequirement = "Indirect Diffused Light",
            humidityRequirement = "Very High Humidity",
            description = "Lush green fronds that thrive in humid bathroom environments.",
            isLowLightTolerant = true,
            isPetFriendly = true
        ),
        PlantPreset(
            name = "Rubber Tree",
            species = "Ficus elastica",
            defaultRoom = "Living Room",
            waterIntervalDays = 10,
            fertilizeIntervalDays = 30,
            mistIntervalDays = 5,
            iconKey = "fiddle_leaf",
            lightRequirement = "Bright Filtered Light",
            humidityRequirement = "Moderate Indoor Humidity",
            description = "Stately tree with thick burgundy-green leaves. Let topsoil dry.",
            isLowLightTolerant = false,
            isPetFriendly = false
        ),
        PlantPreset(
            name = "Chinese Money Plant",
            species = "Pilea peperomioides",
            defaultRoom = "Office Desk",
            waterIntervalDays = 7,
            fertilizeIntervalDays = 21,
            mistIntervalDays = 4,
            iconKey = "aloe",
            lightRequirement = "Bright Indirect Light",
            humidityRequirement = "Average Indoor Humidity",
            description = "Distinctive round coin-like leaves. Rotate weekly for even growth.",
            isLowLightTolerant = false,
            isPetFriendly = true
        )
    )

    fun getInitialSeedPlants(): List<PlantEntity> {
        val now = System.currentTimeMillis()
        val oneDayMs = 86400000L

        return listOf(
            PlantEntity(
                name = "Monstera Deliciosa",
                species = "Monstera deliciosa",
                room = "Living Room",
                waterIntervalDays = 7,
                fertilizeIntervalDays = 21,
                mistIntervalDays = 3,
                lastWateredDate = now - (7 * oneDayMs),
                lastFertilizedTimestamp = now - (21 * oneDayMs),
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
                lastWateredDate = now - (14 * oneDayMs),
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
                lastWateredDate = now - (5 * oneDayMs),
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
                lastWateredDate = now - (2 * oneDayMs),
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
                lastWateredDate = now - (8 * oneDayMs),
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
