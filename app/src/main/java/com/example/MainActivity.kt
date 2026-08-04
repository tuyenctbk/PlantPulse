package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.PlantViewModel
import com.example.ui.screens.*
import com.example.ui.theme.PlantPulseTheme

import com.example.ui.components.RateAppDialog
import com.example.ui.components.ShareAppDialog
import com.example.ui.components.CitySelectorDialog

class MainActivity : ComponentActivity() {
    private val viewModel: PlantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.util.PlantNotificationScheduler.schedulePeriodicReminders(applicationContext)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            PlantPulseTheme(themeMode = uiState.themeMode) {
                val snackbarHostState = remember { SnackbarHostState() }

                // Display toast notifications
                LaunchedEffect(uiState.userToast) {
                    uiState.userToast?.let { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                if (!uiState.hasCompletedOnboarding) {
                    OnboardingScreen(
                        currentLanguage = uiState.appLanguage,
                        onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                        onComplete = { viewModel.completeOnboarding() },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        DashboardScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            onOpenAddPlant = { viewModel.openAddPlantSheet(true) },
                            onOpenPresetPicker = { viewModel.openPresetPicker(true) },
                            onOpenEncyclopedia = { viewModel.openEncyclopedia(true) },
                            onOpenScrapbook = { plant -> viewModel.setActivePlantForScrapbook(plant) },
                            onOpenPlantDetail = { plant -> viewModel.setActivePlantForDetail(plant) },
                            modifier = Modifier.padding(innerPadding)
                        )

                        // Sheet Overlays
                        if (uiState.isAddPlantSheetOpen) {
                            AddPlantDialog(
                                onSavePlant = { plant -> viewModel.saveCustomPlant(plant) },
                                onDismiss = { viewModel.openAddPlantSheet(false) }
                            )
                        }

                        if (uiState.isPresetPickerOpen) {
                            PresetPickerSheet(
                                onSelectPreset = { preset -> viewModel.addPlantFromPreset(preset) },
                                onDismiss = { viewModel.openPresetPicker(false) }
                            )
                        }

                        if (uiState.isEncyclopediaOpen) {
                            PlantEncyclopediaSearchSheet(
                                onSelectPreset = { preset -> viewModel.addPlantFromPreset(preset) },
                                onDismiss = { viewModel.openEncyclopedia(false) }
                            )
                        }

                        if (uiState.isBackupDialogOpen) {
                            BackupRestoreDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.openBackupDialog(false) }
                            )
                        }

                        if (uiState.isCitySelectorOpen) {
                            CitySelectorDialog(
                                selectedCity = uiState.selectedCity,
                                onSelectCity = { city -> viewModel.fetchWeather(city) },
                                onDismiss = { viewModel.openCitySelector(false) }
                            )
                        }

                        if (uiState.isRateAppDialogPromptOpen) {
                            RateAppDialog(
                                totalCareCount = uiState.totalCareActionsCount,
                                totalPlantsCount = uiState.plants.size,
                                onRateSubmitted = { rating -> viewModel.markAppRated(rating) },
                                onDismiss = { viewModel.openRateAppPrompt(false) }
                            )
                        }

                        if (uiState.isShareAppDialogPromptOpen) {
                            ShareAppDialog(
                                onShareConfirmed = { viewModel.markAppShared() },
                                onDismiss = { viewModel.openShareAppPrompt(false) }
                            )
                        }

                        uiState.activePlantForScrapbook?.let { plant ->
                            GrowthScrapbookSheet(
                                plant = plant,
                                viewModel = viewModel,
                                onDismiss = { viewModel.setActivePlantForScrapbook(null) }
                            )
                        }

                        uiState.activePlantForDetail?.let { plant ->
                            PlantDetailSheet(
                                plant = plant,
                                viewModel = viewModel,
                                onDismiss = { viewModel.setActivePlantForDetail(null) }
                            )
                        }
                    }
                }
            }
        }
    }
}
