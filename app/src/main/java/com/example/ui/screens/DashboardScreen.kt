package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CareType
import com.example.data.PlantEntity
import com.example.ui.PlantUiState
import com.example.ui.PlantViewModel
import com.example.ui.components.PlantCard
import com.example.ui.components.WateringConsistencyDashboardCard
import com.example.ui.components.WeatherWidgetCard
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PlantViewModel,
    uiState: PlantUiState,
    onOpenAddPlant: () -> Unit,
    onOpenPresetPicker: () -> Unit,
    onOpenEncyclopedia: () -> Unit,
    onOpenScrapbook: (PlantEntity) -> Unit,
    onOpenPlantDetail: (PlantEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val allPlants = uiState.plants
    val selectedRoom = uiState.selectedRoomFilter
    val searchQuery = uiState.searchQuery

    val weatherFactor = uiState.weatherAdjustmentFactor

    var isBatchMode by remember { mutableStateOf(false) }
    var selectedPlantIds by remember { mutableStateOf(setOf<Long>()) }

    LaunchedEffect(selectedRoom, uiState.activeTab) {
        isBatchMode = false
        selectedPlantIds = emptySet()
    }

    val toggleSelection: (Long) -> Unit = { id ->
        selectedPlantIds = if (selectedPlantIds.contains(id)) {
            selectedPlantIds - id
        } else {
            selectedPlantIds + id
        }
    }

    // Derived counts
    val duePlants = remember(allPlants, weatherFactor) { allPlants.filter { it.isWaterDue(weatherFactor) } }
    val roomList = remember(allPlants) {
        listOf("All") + allPlants.map { it.roomName }.distinct().sorted()
    }

    // Filtered plants by room & search
    val filteredPlants = remember(allPlants, selectedRoom, searchQuery) {
        allPlants.filter { plant ->
            val matchesRoom = selectedRoom == null || selectedRoom == "All" || plant.roomName == selectedRoom
            val matchesSearch = searchQuery.isEmpty() ||
                    plant.name.contains(searchQuery, ignoreCase = true) ||
                    plant.species.contains(searchQuery, ignoreCase = true) ||
                    plant.roomName.contains(searchQuery, ignoreCase = true)
            matchesRoom && matchesSearch
        }
    }

    val dueInCurrentFilter = remember(filteredPlants, weatherFactor) { filteredPlants.filter { it.isWaterDue(weatherFactor) } }

    Scaffold(
        topBar = {
            var showThemeMenu by remember { mutableStateOf(false) }
            var showOverflowMenu by remember { mutableStateOf(false) }
            var showLanguageDialog by remember { mutableStateOf(false) }

            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_plant_logo),
                            contentDescription = "PlantPulse Logo",
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "PlantPulse",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Theme Switcher Menu
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(
                                imageVector = when (uiState.themeMode) {
                                    com.example.ui.theme.ThemeMode.SYSTEM -> Icons.Outlined.AutoMode
                                    com.example.ui.theme.ThemeMode.LIGHT -> Icons.Outlined.LightMode
                                    com.example.ui.theme.ThemeMode.DARK -> Icons.Outlined.DarkMode
                                },
                                contentDescription = stringResource(R.string.theme_mode)
                            )
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            com.example.ui.theme.ThemeMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.label) },
                                    onClick = {
                                        viewModel.setThemeMode(mode)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Preset Picker Button
                    IconButton(
                        onClick = onOpenPresetPicker,
                        modifier = Modifier.testTag("preset_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Botanical Presets",
                            tint = ForestGreen
                        )
                    }

                    // Encyclopedia Search Button
                    IconButton(
                        onClick = onOpenEncyclopedia,
                        modifier = Modifier.testTag("encyclopedia_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = "Plant Encyclopedia",
                            tint = ForestGreen
                        )
                    }

                    // Overflow Menu (Share, Backup)
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.settings_backup)
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.share_plantpulse)) },
                                leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.openShareAppPrompt(true)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings_backup)) },
                                leadingIcon = { Icon(Icons.Outlined.SettingsBackupRestore, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.openBackupDialog(true)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (uiState.activeTab < 2) {
                ExtendedFloatingActionButton(
                    onClick = onOpenAddPlant,
                    icon = { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_plant)) },
                    text = { Text(stringResource(R.string.add_plant), fontWeight = FontWeight.Bold) },
                    containerColor = ForestGreen,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_plant_fab")
                )
            }
        },
        bottomBar = {
            Column {
                AnimatedVisibility(visible = isBatchMode) {
                    Surface(
                        tonalElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${selectedPlantIds.size} selected",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                TextButton(
                                    onClick = {
                                        if (selectedPlantIds.size == filteredPlants.size) {
                                            selectedPlantIds = emptySet()
                                        } else {
                                            selectedPlantIds = filteredPlants.map { it.id }.toSet()
                                        }
                                    }
                                ) {
                                    Text(
                                        if (selectedPlantIds.size == filteredPlants.size && filteredPlants.isNotEmpty()) {
                                            stringResource(R.string.deselect_all)
                                        } else {
                                            stringResource(R.string.select_all)
                                        }
                                    )
                                }
                                Button(
                                    onClick = {
                                        viewModel.waterPlants(selectedPlantIds.toList())
                                        isBatchMode = false
                                        selectedPlantIds = emptySet()
                                    },
                                    enabled = selectedPlantIds.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${stringResource(R.string.water_selected)} (${selectedPlantIds.size})")
                                }
                                OutlinedButton(
                                    onClick = { isBatchMode = false; selectedPlantIds = emptySet() },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }
                            }
                        }
                    }
                }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = { Icon(if (uiState.activeTab == 0) Icons.Filled.Today else Icons.Outlined.Today, contentDescription = stringResource(R.string.tab_today)) },
                        label = { Text(stringResource(R.string.tab_today)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreen,
                            selectedTextColor = ForestGreen,
                            indicatorColor = MintGreen.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = uiState.activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = { Icon(if (uiState.activeTab == 1) Icons.Filled.LocalFlorist else Icons.Outlined.LocalFlorist, contentDescription = stringResource(R.string.tab_jungle)) },
                        label = { Text(stringResource(R.string.tab_jungle)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreen,
                            selectedTextColor = ForestGreen,
                            indicatorColor = MintGreen.copy(alpha = 0.3f)
                        )
                    )
                    NavigationBarItem(
                        selected = uiState.activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = { Icon(if (uiState.activeTab == 2) Icons.Filled.BarChart else Icons.Outlined.BarChart, contentDescription = stringResource(R.string.tab_analytics)) },
                        label = { Text(stringResource(R.string.tab_analytics)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ForestGreen,
                            selectedTextColor = ForestGreen,
                            indicatorColor = MintGreen.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            val columnsCount = when {
                maxWidth >= 960.dp -> 3
                maxWidth >= 600.dp -> 2
                else -> 1
            }
            val maxContentWidth = when {
                maxWidth >= 960.dp -> 1200.dp
                maxWidth >= 600.dp -> 840.dp
                else -> 600.dp
            }

            val heroInfiniteTransition = rememberInfiniteTransition(label = "heroPulse")
            val heroGlowAlpha by heroInfiniteTransition.animateFloat(
                initialValue = 0.85f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "heroGlowAlpha"
            )

            AnimatedContent(
                targetState = uiState.activeTab,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width / 4 } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { width -> -width / 4 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally { width -> -width / 4 } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { width -> width / 4 } + fadeOut(tween(200)))
                    }
                },
                label = "TabContentTransition",
                modifier = Modifier.fillMaxSize()
            ) { activeTab ->
                when (activeTab) {
                    0 -> {
                        // TAB 0: TODAY (Dashboard & Due Checklist)
                        LazyColumn(
                            contentPadding = PaddingValues(
                                top = 8.dp,
                                bottom = 88.dp,
                                start = 16.dp,
                                end = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = maxContentWidth)
                        ) {
                            // 1. Hero Status & Care Summary Card
                            item {
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        DeepEmerald.copy(alpha = if (duePlants.isNotEmpty()) heroGlowAlpha else 1f),
                                                        ForestGreen
                                                    )
                                                ),
                                                shape = RoundedCornerShape(24.dp)
                                            )
                                            .padding(20.dp)
                                    ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "TODAY'S JUNGLE CARE",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MintGreen.copy(alpha = 0.8f)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = if (duePlants.isNotEmpty())
                                                        "${duePlants.size} Plants Need Water 💧"
                                                    else
                                                        "All Plants Hydrated! ✨",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color.White.copy(alpha = 0.15f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "🪴 ${allPlants.size} Total",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // One-Tap Water All Due Banner Button
                                        if (duePlants.isNotEmpty()) {
                                            Button(
                                                onClick = { viewModel.waterAllDueTotal() },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MintGreen,
                                                    contentColor = DeepEmerald
                                                ),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp)
                                                    .testTag("water_all_due_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.WaterDrop,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Water All Due Today (${duePlants.size})",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp
                                                )
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = Color.White.copy(alpha = 0.1f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "🌿 All room hydration schedules are on track. No plants overwatered!",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MintGreen,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 1.5. Weather Widget & Auto-Adjustment Card
                        item {
                            WeatherWidgetCard(
                                city = uiState.selectedCity,
                                weather = uiState.currentWeather,
                                isLoading = uiState.isWeatherLoading,
                                weatherAdjustmentFactor = uiState.weatherAdjustmentFactor,
                                onOpenCitySelector = { viewModel.openCitySelector(true) },
                                onRefreshWeather = { viewModel.fetchWeather(uiState.selectedCity) }
                            )
                        }

                        // 2. Room Grouping Filter Pills (Today)
                        item {
                            RoomGroupingFilterSection(roomList, selectedRoom, allPlants, duePlants, viewModel, weatherFactor)
                        }

                        // Room-Specific Water All Button
                        if (selectedRoom != null && selectedRoom != "All" && dueInCurrentFilter.isNotEmpty()) {
                            item {
                                OutlinedButton(
                                    onClick = { viewModel.waterAllDueInRoom(selectedRoom) },
                                    shape = RoundedCornerShape(14.dp),
                                    border = ButtonDefaults.outlinedButtonBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(ForestGreen)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = ForestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Water All Due in $selectedRoom (${dueInCurrentFilter.size})",
                                        color = ForestGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 3. Today's Due Checklist Section
                        if (dueInCurrentFilter.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("💧", fontSize = 20.sp)
                                    Text(
                                        text = "DUE TODAY CHECKLIST (${dueInCurrentFilter.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RoseMist
                                    )
                                }
                            }

                            if (columnsCount > 1) {
                                val chunkedDue = dueInCurrentFilter.chunked(columnsCount)
                                items(chunkedDue, key = { "due_row_${it.first().id}_$columnsCount" }) { rowPlants ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        rowPlants.forEach { plant ->
                                            PlantCard(
                                                plant = plant,
                                                onWaterClick = { viewModel.waterPlant(plant.id) },
                                                onSnoozeClick = { viewModel.snoozeWatering(plant.id) },
                                                onLogCareClick = { careType -> viewModel.logCare(plant.id, careType) },
                                                onScrapbookClick = { onOpenScrapbook(plant) },
                                                onCardClick = { onOpenPlantDetail(plant) },
                                                isBatchMode = isBatchMode,
                                                isSelected = selectedPlantIds.contains(plant.id),
                                                onSelectToggle = { toggleSelection(plant.id) },
                                                weatherAdjustmentFactor = weatherFactor,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        val remaining = columnsCount - rowPlants.size
                                        if (remaining > 0) {
                                            repeat(remaining) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(dueInCurrentFilter, key = { "due_${it.id}" }) { plant ->
                                    PlantCard(
                                        plant = plant,
                                        onWaterClick = { viewModel.waterPlant(plant.id) },
                                        onSnoozeClick = { viewModel.snoozeWatering(plant.id) },
                                        onLogCareClick = { careType -> viewModel.logCare(plant.id, careType) },
                                        onScrapbookClick = { onOpenScrapbook(plant) },
                                        onCardClick = { onOpenPlantDetail(plant) },
                                        isBatchMode = isBatchMode,
                                        isSelected = selectedPlantIds.contains(plant.id),
                                        onSelectToggle = { toggleSelection(plant.id) },
                                        weatherAdjustmentFactor = weatherFactor
                                    )
                                }
                            }
                        } else {
                            item {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp)
                                    ) {
                                        Text("✨", fontSize = 44.sp)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "All Plants Hydrated & Happy!",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Everything in your botanical collection is fully taken care of for today. Relax or check 'My Jungle' to explore species parameters.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: MY JUNGLE (All Plants Grid)
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 88.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = maxContentWidth)
                    ) {
                        // 1. Search Bar
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text(stringResource(R.string.search_plants)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // 2. Room Filters (Jungle)
                        item {
                            RoomGroupingFilterSection(roomList, selectedRoom, allPlants, duePlants, viewModel, weatherFactor)
                        }

                        // 3. Section Title with quick presets button
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (selectedRoom != null && selectedRoom != "All") "PLANTS IN $selectedRoom" else "MY JUNGLE (${filteredPlants.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (filteredPlants.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = {
                                                isBatchMode = !isBatchMode
                                                if (!isBatchMode) selectedPlantIds = emptySet()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isBatchMode) Icons.Default.Close else Icons.Default.Checklist,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = ForestGreen
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isBatchMode) {
                                                    stringResource(R.string.exit_batch)
                                                } else {
                                                    stringResource(R.string.batch_select)
                                                },
                                                color = ForestGreen,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                    TextButton(onClick = onOpenPresetPicker) {
                                        Text(stringResource(R.string.quick_add_preset), color = ForestGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // 4. Grid / List rendering
                        if (filteredPlants.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp)
                                    ) {
                                        Text("🪴", fontSize = 48.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No plants found",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Tap + Add Plant or select a Botanical Preset to start tracking!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = onOpenPresetPicker,
                                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                                        ) {
                                            Text("Browse Presets")
                                        }
                                    }
                                }
                            }
                        } else {
                            if (columnsCount > 1) {
                                val chunkedPlants = filteredPlants.chunked(columnsCount)
                                items(chunkedPlants, key = { "plant_row_${it.first().id}_$columnsCount" }) { rowPlants ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        rowPlants.forEach { plant ->
                                            PlantCard(
                                                plant = plant,
                                                onWaterClick = { viewModel.waterPlant(plant.id) },
                                                onSnoozeClick = { viewModel.snoozeWatering(plant.id) },
                                                onLogCareClick = { careType -> viewModel.logCare(plant.id, careType) },
                                                onScrapbookClick = { onOpenScrapbook(plant) },
                                                onCardClick = { onOpenPlantDetail(plant) },
                                                isBatchMode = isBatchMode,
                                                isSelected = selectedPlantIds.contains(plant.id),
                                                onSelectToggle = { toggleSelection(plant.id) },
                                                weatherAdjustmentFactor = weatherFactor,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        val remaining = columnsCount - rowPlants.size
                                        if (remaining > 0) {
                                            repeat(remaining) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            } else {
                                items(filteredPlants, key = { "plant_${it.id}" }) { plant ->
                                    PlantCard(
                                        plant = plant,
                                        onWaterClick = { viewModel.waterPlant(plant.id) },
                                        onSnoozeClick = { viewModel.snoozeWatering(plant.id) },
                                        onLogCareClick = { careType -> viewModel.logCare(plant.id, careType) },
                                        onScrapbookClick = { onOpenScrapbook(plant) },
                                        onCardClick = { onOpenPlantDetail(plant) },
                                        isBatchMode = isBatchMode,
                                        isSelected = selectedPlantIds.contains(plant.id),
                                        onSelectToggle = { toggleSelection(plant.id) },
                                        weatherAdjustmentFactor = weatherFactor
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: ANALYTICS & CARE TIMELINE
                    val careLogs by viewModel.allCareLogs.collectAsState()
                    var logSearchQuery by remember { mutableStateOf("") }

                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 88.dp,
                            start = 16.dp,
                            end = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = maxContentWidth)
                    ) {
                        // 1. Stats Row Card
                        item {
                            StatsSummaryRow(
                                totalCareCount = uiState.totalCareActionsCount,
                                allPlants = allPlants,
                                weatherFactor = weatherFactor
                            )
                        }

                        // 2. Custom Weekly Activity Chart
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📊", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "WEEKLY HYDRATION ACTIVITY",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ForestGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))
                                    WeeklyCareChart(logs = careLogs)
                                }
                            }
                        }

                        // 3. 30-Day Watering Consistency & Schedule Analysis Dashboard
                        item {
                            WateringConsistencyDashboardCard(
                                plants = allPlants,
                                careLogs = careLogs
                            )
                        }

                        // 3. Unlocked Achievements / Milestones Row
                        item {
                            AchievementsSection(totalCareCount = careLogs.size, allPlants = allPlants, weatherFactor = weatherFactor)
                        }

                        // 4. Unified Care Feed Section Header
                        item {
                            Text(
                                text = "UNIFIED CARE LOG TIMELINE (${careLogs.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Search box for Logs
                        item {
                            OutlinedTextField(
                                value = logSearchQuery,
                                onValueChange = { logSearchQuery = it },
                                placeholder = { Text("Filter logs by plant name, notes, or types...") },
                                leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Filter Logs live
                        val filteredLogs = careLogs.filter { log ->
                            val plantName = allPlants.find { it.id == log.plantId }?.name ?: "Plant"
                            logSearchQuery.isEmpty() ||
                                    plantName.contains(logSearchQuery, ignoreCase = true) ||
                                    (log.note ?: "").contains(logSearchQuery, ignoreCase = true) ||
                                    log.careType.contains(logSearchQuery, ignoreCase = true)
                        }

                        if (filteredLogs.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No care logs matched your search.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(filteredLogs.take(50)) { log ->
                                val plant = allPlants.find { it.id == log.plantId }
                                val formattedDate = remember(log.timestamp) {
                                    SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(log.timestamp))
                                }
                                CareLogItem(log = log, plantName = plant?.name ?: "Deleted Plant", formattedDate = formattedDate)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomGroupingFilterSection(
    roomList: List<String>,
    selectedRoom: String?,
    allPlants: List<PlantEntity>,
    duePlants: List<PlantEntity>,
    viewModel: PlantViewModel,
    weatherFactor: Double = 1.0
) {
    Column {
        Text(
            text = "ROOM GROUPS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(roomList) { room ->
                val isSelected = (selectedRoom == null && room == "All") || selectedRoom == room
                val countInRoom = if (room == "All") allPlants.size else allPlants.count { it.roomName == room }
                val dueInRoom = if (room == "All") duePlants.size else allPlants.count { it.roomName == room && it.isWaterDue(weatherFactor) }

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectRoomFilter(room) },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = room)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "$countInRoom",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (dueInRoom > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("💧", fontSize = 11.sp)
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
fun WeeklyCareChart(logs: List<com.example.data.CareLogEntity>) {
    val currentMillis = System.currentTimeMillis()
    val oneDayMs = 24 * 60 * 60 * 1000L

    val daysData = remember(logs, currentMillis) {
        val list = mutableListOf<Pair<String, Int>>()
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            val dayStart = currentMillis - (i * oneDayMs)
            val dayEnd = dayStart + oneDayMs
            val dayLabel = sdf.format(Date(dayStart))

            // Count water/fertilize/mist activities logged on that day
            val count = logs.count { it.timestamp in (dayStart + 1)..dayEnd }
            list.add(dayLabel to count)
        }
        list
    }

    val maxCount = remember(daysData) { daysData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1 }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        daysData.forEach { (label, count) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .width(26.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val heightPercentage = count.toFloat() / maxCount
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(heightPercentage.coerceIn(0.08f, 1f))
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(MintGreen, ForestGreen)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (count > 0) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatsSummaryRow(totalCareCount: Int, allPlants: List<PlantEntity>, weatherFactor: Double = 1.0) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📈 LIFE ACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalCareCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Care events logged",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val dueCount = allPlants.count { it.isWaterDue(weatherFactor) }
        val healthScore = remember(allPlants, dueCount) {
            if (allPlants.isEmpty()) 100
            else ((allPlants.size - dueCount).toFloat() / allPlants.size * 100).toInt().coerceIn(0, 100)
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "💚 JUNGLE HEALTH",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepEmerald
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$healthScore%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = when {
                        healthScore == 100 -> "Perfect Hydration!"
                        healthScore >= 75 -> "Excellent care"
                        healthScore >= 50 -> "Doing good"
                        else -> "Needs attention"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AchievementsSection(totalCareCount: Int, allPlants: List<PlantEntity>, weatherFactor: Double = 1.0) {
    Column {
        Text(
            text = "BOTANICAL MILESTONES",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val unlocked = totalCareCount >= 5
                MilestoneBadge(
                    emoji = "🏆",
                    title = "Hydration Hero",
                    desc = "Log 5 care events",
                    unlocked = unlocked
                )
            }

            item {
                val unlocked = allPlants.size >= 3
                MilestoneBadge(
                    emoji = "🌿",
                    title = "Jungle Pioneer",
                    desc = "Cultivate 3 plants",
                    unlocked = unlocked
                )
            }

            item {
                val unlocked = allPlants.isNotEmpty() && allPlants.none { it.isWaterDue(weatherFactor) }
                MilestoneBadge(
                    emoji = "✨",
                    title = "Perfect Thumb",
                    desc = "0 due plants",
                    unlocked = unlocked
                )
            }

            item {
                val unlocked = totalCareCount >= 15
                MilestoneBadge(
                    emoji = "👑",
                    title = "Master Gardener",
                    desc = "Log 15 care events",
                    unlocked = unlocked
                )
            }
        }
    }
}

@Composable
fun MilestoneBadge(emoji: String, title: String, desc: String, unlocked: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (unlocked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (unlocked) androidx.compose.foundation.BorderStroke(1.dp, ForestGreen) else null,
        modifier = Modifier.width(152.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(emoji, fontSize = 22.sp)
                if (unlocked) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Unlocked",
                        tint = ForestGreen,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                fontSize = 10.sp,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun CareLogItem(log: com.example.data.CareLogEntity, plantName: String, formattedDate: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val (emoji, color) = remember(log.careType) {
                when (log.careType) {
                    CareType.WATER.name -> "💧" to ForestGreen
                    CareType.FERTILIZE.name -> "🧪" to Color(0xFF9C27B0)
                    CareType.MIST.name -> "🌫️" to Color(0xFF03A9F4)
                    else -> "🪴" to ForestGreen
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plantName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = log.note ?: "${log.careType} logged",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

