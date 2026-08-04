package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.PlantEntity
import com.example.ui.components.PlantCard
import com.example.ui.theme.PlantPulseTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun plant_card_screenshot() {
    val dummyPlant = PlantEntity(
        id = 1,
        name = "Monstera Deliciosa",
        species = "Monstera deliciosa",
        room = "Living Room",
        waterIntervalDays = 7,
        fertilizeIntervalDays = 21,
        mistIntervalDays = 3,
        lastWateredDate = System.currentTimeMillis() - 86400000L * 3,
        seasonPresetName = "SPRING_AUTUMN",
        climateSettingName = "INDOOR",
        notes = "Thriving well!",
        iconKey = "monstera"
    )

    composeTestRule.setContent {
      PlantPulseTheme {
        PlantCard(
          plant = dummyPlant,
          onWaterClick = {},
          onSnoozeClick = {},
          onLogCareClick = {},
          onScrapbookClick = {},
          onCardClick = {}
        )
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/plant_card.png")
  }
}
