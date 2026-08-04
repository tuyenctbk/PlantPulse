package com.example.ui.screens

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.MintGreen
import com.example.ui.theme.LightMint
import com.example.util.AppLanguage
import com.example.util.TransKey
import com.example.util.TranslationHelper

data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val iconColor: Color,
    val badgeText: String,
    val illustrationContent: @Composable () -> Unit
)

@Composable
fun OnboardingScreen(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        OnboardingPageData(
            title = TranslationHelper.translate(currentLanguage, TransKey.WELCOME_TITLE),
            subtitle = TranslationHelper.translate(currentLanguage, TransKey.WELCOME_SUBTITLE),
            description = TranslationHelper.translate(currentLanguage, TransKey.WELCOME_DESC),
            icon = Icons.Outlined.Grass,
            iconBgColor = LightMint,
            iconColor = ForestGreen,
            badgeText = TranslationHelper.translate(currentLanguage, TransKey.WELCOME_BADGE),
            illustrationContent = {
                WelcomeIllustration()
            }
        ),
        OnboardingPageData(
            title = TranslationHelper.translate(currentLanguage, TransKey.SEASONAL_TITLE),
            subtitle = TranslationHelper.translate(currentLanguage, TransKey.SEASONAL_SUBTITLE),
            description = TranslationHelper.translate(currentLanguage, TransKey.SEASONAL_DESC),
            icon = Icons.Outlined.WaterDrop,
            iconBgColor = Color(0xFFE0F2FE),
            iconColor = Color(0xFF0284C7),
            badgeText = TranslationHelper.translate(currentLanguage, TransKey.SEASONAL_BADGE),
            illustrationContent = {
                SeasonalSchedulingIllustration()
            }
        ),
        OnboardingPageData(
            title = TranslationHelper.translate(currentLanguage, TransKey.GROWTH_TITLE),
            subtitle = TranslationHelper.translate(currentLanguage, TransKey.GROWTH_SUBTITLE),
            description = TranslationHelper.translate(currentLanguage, TransKey.GROWTH_DESC),
            icon = Icons.Outlined.PhotoCamera,
            iconBgColor = Color(0xFFFCE7F3),
            iconColor = Color(0xFFDB2777),
            badgeText = TranslationHelper.translate(currentLanguage, TransKey.GROWTH_BADGE),
            illustrationContent = {
                ScrapbookIllustration()
            }
        )
    )

    val pageCount = pages.size
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        LightMint.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        val maxWidth = maxWidth
        val isWidescreen = maxWidth > 600.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PlantPulse 🪴",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Language Switcher Dropdown
                    var showLangMenu by remember { mutableStateOf(false) }
                    Box {
                        AssistChip(
                            onClick = { showLangMenu = true },
                            label = { Text("${currentLanguage.flag} ${currentLanguage.displayName}", style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = { Icon(Icons.Outlined.Language, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            AppLanguage.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text("${lang.flag} ${lang.displayName}") },
                                    onClick = {
                                        onLanguageChange(lang)
                                        showLangMenu = false
                                    }
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = onComplete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(TranslationHelper.translate(currentLanguage, TransKey.SKIP), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Adaptive Body Container with HorizontalPager
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Card for responsive visual limit
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .widthIn(max = 560.dp)
                        .fillMaxHeight(if (isWidescreen) 0.85f else 0.95f)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { index ->
                        val page = pages[index]
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Illustrated area
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .minHeight(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                page.illustrationContent()
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Badge
                            Surface(
                                shape = CircleShape,
                                color = page.iconBgColor,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = page.badgeText,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = page.iconColor,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            // Title & Description
                            Text(
                                text = page.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = page.subtitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = page.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }

            // Footer / Navigation controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pageCount) { i ->
                        val active = i == pagerState.currentPage
                        val width by animateDpAsState(targetValue = if (active) 24.dp else 8.dp, label = "DotWidth")
                        val color = if (active) ForestGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                        )
                    }
                }

                // Next / Finish Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pageCount - 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onComplete()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                    modifier = Modifier.height(52.dp)
                ) {
                    val isLast = pagerState.currentPage == pageCount - 1
                    AnimatedContent(
                        targetState = isLast,
                        label = "ButtonTextTransition"
                    ) { last ->
                        if (last) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(TranslationHelper.translate(currentLanguage, TransKey.GET_STARTED), fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(TranslationHelper.translate(currentLanguage, TransKey.NEXT), fontWeight = FontWeight.Bold, color = Color.White)
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeIllustration() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(160.dp)
    ) {
        // Decorative background concentric circles
        Surface(
            shape = CircleShape,
            color = LightMint.copy(alpha = 0.4f),
            modifier = Modifier.size(150.dp)
        ) {}
        Surface(
            shape = CircleShape,
            color = LightMint.copy(alpha = 0.8f),
            modifier = Modifier.size(110.dp)
        ) {}

        // Main floating botanical emblem
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ForestGreen),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.size(76.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Grass,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // Surrounding mini floating badges
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 16.dp, y = 16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🪴", fontSize = 18.sp)
                }
            }

            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-12).dp, y = (-12).dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🏡", fontSize = 18.sp)
                }
            }

            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-20).dp, y = 20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("✨", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun SeasonalSchedulingIllustration() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(160.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF0F9FF),
            modifier = Modifier.size(140.dp)
        ) {}

        // Central seasonal visual
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spring/Autumn
            SeasonalBadge(emoji = "🌸", label = "Spring", active = true)
            // Summer (High multiplier)
            SeasonalBadge(emoji = "☀️", label = "Summer", active = false)
            // Winter (Low multiplier)
            SeasonalBadge(emoji = "❄️", label = "Winter", active = false)
        }

        // Hydration drop icon overlay
        Surface(
            shape = CircleShape,
            color = Color(0xFF0284C7),
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-10).dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SeasonalBadge(emoji: String, label: String, active: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (active) LightMint else Color.White
        ),
        border = if (active) androidx.compose.foundation.BorderStroke(1.5.dp, ForestGreen) else null,
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.size(width = 46.dp, height = 64.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label.take(3),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (active) ForestGreen else Color.Gray,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
fun ScrapbookIllustration() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(160.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFFDF2F8),
            modifier = Modifier.size(140.dp)
        ) {}

        // overlapping visual polaroids / timeline
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .size(width = 70.dp, height = 80.dp)
                .offset(x = (-24).dp, y = (-12).dp)
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFEDD5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌱", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Day 1", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier
                .size(width = 80.dp, height = 90.dp)
                .offset(x = 18.dp, y = 14.dp)
        ) {
            Column(
                modifier = Modifier.padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(LightMint),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌿", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Day 30", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
            }
        }
    }
}

// Extension to allow custom minimum height
fun Modifier.minHeight(height: androidx.compose.ui.unit.Dp): Modifier = this.then(
    Modifier.heightIn(min = height)
)
