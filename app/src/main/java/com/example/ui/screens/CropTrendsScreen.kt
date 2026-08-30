package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LanguageManager
import com.example.data.model.*
import com.example.ui.components.CropTrendForecastChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.KisanViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropTrendsScreen(
    viewModel: KisanViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val selectedPriceUnit by viewModel.selectedPriceUnit.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()

    val searchQuery by viewModel.trendsSearchQuery.collectAsState()
    val selectedCategory by viewModel.trendsSelectedCategory.collectAsState()
    val selectedState by viewModel.trendsSelectedState.collectAsState()
    val selectedDistrict by viewModel.trendsSelectedDistrict.collectAsState()
    val selectedDateRange by viewModel.trendsSelectedDateRange.collectAsState()

    val selectedCrop by viewModel.selectedTrendCrop.collectAsState()
    val cropAnalyses by viewModel.cropForecastAnalyses.collectAsState()
    val activeAnalysis by viewModel.activeForecastAnalysis.collectAsState()
    val isRefreshing by viewModel.isTrendsRefreshing.collectAsState()
    val errorMessage by viewModel.trendsErrorMessage.collectAsState()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()

    var showStateFilterDialog by remember { mutableStateOf(false) }
    var showDistrictFilterDialog by remember { mutableStateOf(false) }

    val availableDistricts = remember(selectedState) {
        if (selectedState == "All States") {
            listOf("All Districts") + (STATE_DISTRICTS_MAP["Maharashtra"] ?: emptyList())
        } else {
            listOf("All Districts") + (STATE_DISTRICTS_MAP[selectedState] ?: emptyList())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aesthetic Subheader Bar (Integrated with AppTopBar)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedCrop != null) {
                        IconButton(
                            onClick = { viewModel.selectTrendCrop(null) },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("crop_trends_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to list",
                                tint = KisanGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = LanguageManager.getString("crop_trends_title", currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = LanguageManager.getString("crop_trends_subtitle", currentLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Audio Bulletin Button
                    IconButton(
                        onClick = {
                            if (isAudioPlaying) {
                                viewModel.stopAudio()
                            } else {
                                val bulletin = "Kisan Vani Crop Trends: Showing 30-day AI price forecast for currently selling crops."
                                viewModel.playAudio(bulletin)
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("crop_trends_audio_btn")
                    ) {
                        Icon(
                            imageVector = if (isAudioPlaying) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Spoken bulletin",
                            tint = if (isAudioPlaying) KisanSaffron else KisanGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = { viewModel.refreshTrends() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("crop_trends_refresh_btn")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = KisanGreenPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Data",
                                tint = KisanGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Offline Mode Notice if disconnected
            if (!isNetworkAvailable) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEF3C7)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Offline Mode: Showing official cached AGMARKNET benchmark data.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }
            }

            // 2. Price Unit Selector Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Price Unit:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PriceUnit.entries.forEach { unit ->
                            val isSelected = selectedPriceUnit == unit
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setPriceUnit(unit) },
                                label = {
                                    Text(
                                        text = unit.getSymbol(currentLanguage),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KisanGreenPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("price_unit_${unit.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // 3. Search & Filter Bar (Only when in list browsing mode)
            if (selectedCrop == null) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setTrendsSearchQuery(it) },
                        placeholder = {
                            Text(
                                text = "Search crop, mandi, district or state...",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setTrendsSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("crop_trends_search_input")
                    )
                }

                // Category Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(CropCategory.entries) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setTrendsCategory(cat) },
                                label = { Text(cat.getDisplayName(currentLanguage), fontSize = 12.sp) },
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KisanGreenPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // State, District & Date Range Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // State Selector Chip
                        OutlinedCard(
                            onClick = { showStateFilterDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("state_filter_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("State", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = selectedState,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }

                        // District Selector Chip
                        OutlinedCard(
                            onClick = { showDistrictFilterDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("district_filter_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("District", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = selectedDistrict,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Date Range Selector
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Timeline Window:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TrendDateRange.entries.forEach { range ->
                                val isSelected = selectedDateRange == range
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setTrendsDateRange(range) },
                                    label = { Text(range.getLabel(currentLanguage), fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 4. MAIN CONTENT AREA: DETAILED VIEW OR CROPS LIST

            // --- A. DETAILED CROP VIEW (When a crop is selected) ---
            if (selectedCrop != null && activeAnalysis != null) {
                val analysis = activeAnalysis!!
                val convertedCurrentPrice = selectedPriceUnit.convertPrice(analysis.currentOfficialPrice.toDouble())
                val convertedPeakPrice = selectedPriceUnit.convertPrice(analysis.predictedHighestPrice.toDouble())
                val convertedFloorPrice = selectedPriceUnit.convertPrice(analysis.predictedLowestPrice.toDouble())

                item {
                    // Crop Hero Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Name, Emoji, Mandi & Official Verification Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = analysis.emoji,
                                        fontSize = 32.sp
                                    )
                                    Column {
                                        Text(
                                            text = analysis.getCropName(currentLanguage),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${analysis.mandiName} • ${analysis.district}, ${analysis.state}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Official Tag
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = KisanGreenDark,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Official Data",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = KisanGreenDark
                                        )
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Key Forecast Metric Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Current Mandi Price
                                Column {
                                    Text(
                                        text = "Current Official Price",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "₹${convertedCurrentPrice.toInt()} ${selectedPriceUnit.getSymbol(currentLanguage)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = KisanGreenPrimaryDark
                                    )
                                }

                                // 30D Predicted Peak
                                Column {
                                    Text(
                                        text = "30D Predicted Peak",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "₹${convertedPeakPrice.toInt()} ${selectedPriceUnit.getSymbol(currentLanguage)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD97706)
                                    )
                                }

                                // Expected Trend Direction
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Expected Trend",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    val directionColor = Color(analysis.expectedDirection.colorHex)
                                    Surface(
                                        color = directionColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "${analysis.expectedDirection.symbol} ${if (analysis.percentChange30Days >= 0) "+${String.format(Locale.US, "%.1f", analysis.percentChange30Days)}%" else "${String.format(Locale.US, "%.1f", analysis.percentChange30Days)}%"}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = directionColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Dual-Series Line Chart
                item {
                    CropTrendForecastChart(
                        analysis = analysis,
                        selectedPriceUnit = selectedPriceUnit,
                        currentLanguage = currentLanguage
                    )
                }

                // AI Strategic Rationale & Market Drivers
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "AI Market Analysis & Selling Window",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Selling Window Callout
                            Surface(
                                color = Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = analysis.optimalSellingWindow,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }

                            Text(
                                text = analysis.getAiRationale(currentLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Key Market Drivers:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            analysis.keyDrivingFactors.forEach { factor ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("•", color = KisanGreenPrimary, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = factor,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Official Source & Mandatory Legal Disclaimer
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Data Source: ${analysis.officialDataSource}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = analysis.lastUpdatedTimestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = "“Predictions are estimates based on available market data and are not guaranteed selling prices.”",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Back Button
                item {
                    Button(
                        onClick = { viewModel.selectTrendCrop(null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Back to All Crops",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- B. ALL CURRENTLY SELLING CROPS LIST (Default state) ---
            else {
                // Header Count & Quick Summary
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${cropAnalyses.size} Crops Selling in Mandis",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap any crop to view 30D forecast",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Empty state if filtered results are 0
                if (cropAnalyses.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterAltOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "No crops match your selected filters.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Button(
                                    onClick = { viewModel.clearTrendsFilters() },
                                    colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Clear All Filters", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // List of all crops with current price and 30-day forecast cards
                    items(cropAnalyses, key = { it.cropId }) { analysis ->
                        val convertedPrice = selectedPriceUnit.convertPrice(analysis.currentOfficialPrice.toDouble())
                        val convertedPeak = selectedPriceUnit.convertPrice(analysis.predictedHighestPrice.toDouble())
                        val directionColor = Color(analysis.expectedDirection.colorHex)

                        Card(
                            onClick = { viewModel.selectTrendCropById(analysis.cropId) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("crop_trend_item_${analysis.cropId}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Top Row: Emoji, Name, Mandi & Official Tag
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = analysis.emoji, fontSize = 24.sp)
                                        Column {
                                            Text(
                                                text = analysis.getCropName(currentLanguage),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${analysis.mandiName} • ${analysis.district}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Direction Badge
                                    Surface(
                                        color = directionColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, directionColor.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "${analysis.expectedDirection.symbol} ${if (analysis.percentChange30Days >= 0) "+${String.format(Locale.US, "%.1f", analysis.percentChange30Days)}%" else "${String.format(Locale.US, "%.1f", analysis.percentChange30Days)}%"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = directionColor
                                            )
                                        }
                                    }
                                }

                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Price & 30D Forecast Metrics
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Current Price
                                    Column {
                                        Text(
                                            text = "Official Mandi Price",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = "₹${convertedPrice.toInt()} ${selectedPriceUnit.getSymbol(currentLanguage)}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = KisanGreenPrimaryDark
                                        )
                                    }

                                    // 30-Day AI Peak
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "30D AI Peak",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = "₹${convertedPeak.toInt()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }

                                    // Action Button
                                    Button(
                                        onClick = { viewModel.selectTrendCropById(analysis.cropId) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Forecast →",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Bottom Sub-bar: Official Source and Last Updated
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = analysis.officialDataSource,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Text(
                                        text = analysis.lastUpdatedTimestamp,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }

                // Mandatory Legal Disclaimer Box at list bottom
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "“Predictions are estimates based on available market data and are not guaranteed selling prices.”",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }

    // --- State Filter Selection Dialog ---
    if (showStateFilterDialog) {
        AlertDialog(
            onDismissRequest = { showStateFilterDialog = false },
            title = { Text("Select State", fontWeight = FontWeight.Bold) },
            text = {
                val stateList = listOf("All States") + INDIAN_STATES
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(stateList) { st ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTrendsState(st)
                                    showStateFilterDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = st,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (st == selectedState) FontWeight.Bold else FontWeight.Normal
                            )
                            if (st == selectedState) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = KisanGreenPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStateFilterDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // --- District Filter Selection Dialog ---
    if (showDistrictFilterDialog) {
        AlertDialog(
            onDismissRequest = { showDistrictFilterDialog = false },
            title = { Text("Select District / Mandi", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(280.dp)) {
                    items(availableDistricts) { dst ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTrendsDistrict(dst)
                                    showDistrictFilterDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dst,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (dst == selectedDistrict) FontWeight.Bold else FontWeight.Normal
                            )
                            if (dst == selectedDistrict) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = KisanGreenPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDistrictFilterDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
