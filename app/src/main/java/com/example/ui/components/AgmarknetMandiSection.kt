package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LanguageManager
import com.example.data.model.AgmarknetFilters
import com.example.data.model.AgmarknetMandiRecord
import com.example.data.model.AgmarknetUiState
import com.example.data.model.AppLanguage
import com.example.data.model.INDIAN_STATES
import com.example.data.model.STATE_DISTRICTS_MAP
import com.example.ui.theme.*

// Extended Indian States for Filter 1 (Includes 'All')
val MANDI_INDIAN_STATES = listOf("All") + INDIAN_STATES

// Major Maharashtra Districts (backward compatibility reference)
val MAHARASHTRA_DISTRICTS = listOf("All Districts") + (STATE_DISTRICTS_MAP["Maharashtra"] ?: emptyList())

// Common Agri Commodities for Filter 4
val POPULAR_COMMODITIES = listOf(
    "All Commodities",
    "Onion",
    "Cotton",
    "Soybean",
    "Wheat",
    "Tomato",
    "Potato",
    "Paddy / Dhan",
    "Chana / Gram",
    "Tur / Arhar",
    "Maize / Corn",
    "Groundnut",
    "Green Chilli",
    "Garlic",
    "Ginger",
    "Mustard",
    "Cumin / Jeera",
    "Turmeric",
    "Pomegranate",
    "Banana",
    "Grapes",
    "Sugarcane"
)

fun cleanCommodityName(raw: String): String {
    return raw
        .replace("_", " ")
        .replace("Paddy(Dhan)(Common)", "Paddy (Dhan)")
        .replace("Gram Raw(Chhana)", "Chana / Gram")
        .replace("Arhar (Tur/Red Gram)(Whole)", "Tur / Arhar (Red Gram)")
        .replace("Ginger(Green)", "Green Ginger")
        .replace("Jowar(Sorghum)", "Jowar (Sorghum)")
        .replace("Bajra(Pearl Millet/Cumbu)", "Bajra (Pearl Millet)")
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun getCommodityEmoji(commodity: String): String {
    val lower = commodity.lowercase()
    return when {
        lower.contains("onion") -> "🧅"
        lower.contains("tomato") -> "🍅"
        lower.contains("potato") -> "🥔"
        lower.contains("cotton") -> "🧵"
        lower.contains("soybean") -> "🌱"
        lower.contains("wheat") -> "🌾"
        lower.contains("paddy") || lower.contains("rice") || lower.contains("dhan") -> "🌾"
        lower.contains("chana") || lower.contains("gram") -> "🫘"
        lower.contains("tur") || lower.contains("arhar") -> "🫘"
        lower.contains("garlic") -> "🧄"
        lower.contains("ginger") -> "🫚"
        lower.contains("chilli") || lower.contains("mirchi") -> "🌶️"
        lower.contains("banana") -> "🍌"
        lower.contains("pomegranate") -> "🍎"
        lower.contains("grapes") -> "🍇"
        lower.contains("groundnut") || lower.contains("peanut") -> "🥜"
        lower.contains("mustard") || lower.contains("sarson") -> "🌼"
        lower.contains("cumin") || lower.contains("jeera") -> "🌿"
        lower.contains("turmeric") || lower.contains("haldi") -> "✨"
        lower.contains("sugarcane") || lower.contains("gur") -> "🎋"
        lower.contains("maize") || lower.contains("corn") -> "🌽"
        else -> "🌾"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgmarknetMandiSection(
    uiState: AgmarknetUiState,
    currentLanguage: AppLanguage,
    onRefresh: () -> Unit,
    onStateChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onMarketChange: (String) -> Unit,
    onCommodityChange: (String) -> Unit,
    onVarietyChange: (String) -> Unit,
    onClearFilters: () -> Unit,
    onSpeakRecord: (AgmarknetMandiRecord) -> Unit,
    isPlayingAudio: Boolean
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var stateDropdownExpanded by remember { mutableStateOf(false) }
    var districtDropdownExpanded by remember { mutableStateOf(false) }
    var commodityDropdownExpanded by remember { mutableStateOf(false) }

    var searchMarketQuery by remember { mutableStateOf(uiState.filters.market) }
    var searchVarietyQuery by remember { mutableStateOf(uiState.filters.variety) }

    // Sync search fields when filters are cleared or changed externally
    LaunchedEffect(uiState.filters.market) {
        searchMarketQuery = uiState.filters.market
    }
    LaunchedEffect(uiState.filters.variety) {
        searchVarietyQuery = uiState.filters.variety
    }

    // Dynamically compute the district list for the currently chosen state
    val currentDistricts = remember(uiState.filters.state, uiState.records) {
        val selectedState = uiState.filters.state.trim()
        val baseList = when {
            selectedState.isBlank() || selectedState.equals("All", ignoreCase = true) -> {
                // If "All States" is selected, provide All Districts + union of popular districts
                listOf("All Districts") + STATE_DISTRICTS_MAP.values.flatten().distinct().sorted()
            }
            STATE_DISTRICTS_MAP.containsKey(selectedState) -> {
                listOf("All Districts") + STATE_DISTRICTS_MAP[selectedState].orEmpty()
            }
            else -> {
                listOf("All Districts")
            }
        }
        // Also seamlessly merge any extra districts that exist in incoming live records for this state
        val recordDistricts = uiState.records
            .filter {
                (selectedState.isBlank() || selectedState.equals("All", ignoreCase = true) || it.state.equals(selectedState, ignoreCase = true)) &&
                        it.district.isNotBlank() && it.district != "N/A"
            }
            .map { it.district }
            .distinct()

        (baseList + recordDistricts).distinct()
    }

    var isFiltersCollapsed by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("agmarknet_records_list"),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Official Source & Status Header Card (Scrolls upward when exploring records)
        item(key = "agmarknet_header") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(KisanGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "Govt of India",
                                tint = KisanGold,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "GOI AGMARKNET LIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = KisanGold,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text = if (uiState.lastFetchedIst.isNotBlank()) "Fetched: ${uiState.lastFetchedIst}" else "Official daily arrival records",
                                fontSize = 8.5.sp,
                                color = Slate300,
                                maxLines = 1
                            )
                        }
                    }

                    // Refresh Button with Cooldown Debouncing
                    val isCooldown = uiState.cooldownSecondsRemaining > 0
                    Button(
                        onClick = onRefresh,
                        enabled = !uiState.isLoading && !isCooldown,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KisanGreenPrimary,
                            disabledContainerColor = Slate700
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("refresh_agmarknet_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(11.dp),
                                    strokeWidth = 1.5.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    modifier = Modifier.size(12.dp),
                                    tint = if (isCooldown) Slate400 else Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isCooldown) "${uiState.cooldownSecondsRemaining}s" else "Refresh",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCooldown) Slate400 else Color.White
                            )
                        }
                    }
                }
            }
        }

        // 2. Delayed Data Banner (if arrival date > 2 days)
        if (uiState.isDelayed && uiState.delayNotice != null) {
            item(key = "agmarknet_delayed_banner") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Delay notice",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.delayNotice ?: "Data may be delayed from government records.",
                            fontSize = 11.sp,
                            color = Color(0xFF991B1B),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // 3. Hierarchical Filter Controls (Scrolls upward seamlessly when exploring records)
        item(key = "agmarknet_filters") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { isFiltersCollapsed = !isFiltersCollapsed }
                                .padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = KisanGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AGMARKNET Filters (1 to 5)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isFiltersCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = if (isFiltersCollapsed) "Expand" else "Collapse",
                                tint = Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (uiState.filters.hasActiveFilters()) {
                            TextButton(
                                onClick = onClearFilters,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = LanguageManager.getString("clear_all_filters", currentLanguage),
                                    fontSize = 11.sp,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    if (!isFiltersCollapsed) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Row 1: State & District Dropdowns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Filter 1: State
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { stateDropdownExpanded = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("filter_state_dropdown"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (uiState.filters.state.isBlank() || uiState.filters.state == "All") "1. All States" else "1. ${uiState.filters.state}",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = stateDropdownExpanded,
                                    onDismissRequest = { stateDropdownExpanded = false }
                                ) {
                                    INDIAN_STATES.forEach { state ->
                                        DropdownMenuItem(
                                            text = { Text(state, fontSize = 12.sp) },
                                            onClick = {
                                                onStateChange(state)
                                                stateDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Filter 2: District
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { districtDropdownExpanded = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("filter_district_dropdown"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (uiState.filters.district.isBlank() || uiState.filters.district == "All Districts") "2. All Districts" else "2. ${uiState.filters.district}",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = districtDropdownExpanded,
                                    onDismissRequest = { districtDropdownExpanded = false },
                                    modifier = Modifier
                                        .heightIn(max = 350.dp)
                                        .testTag("district_dropdown_menu")
                                ) {
                                    currentDistricts.forEach { district ->
                                        DropdownMenuItem(
                                            text = { Text(district, fontSize = 12.sp) },
                                            onClick = {
                                                onDistrictChange(if (district == "All Districts") "" else district)
                                                districtDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Row 2: Commodity & Mandi / Market
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Filter 3: Mandi / Market Search Field
                            OutlinedTextField(
                                value = searchMarketQuery,
                                onValueChange = {
                                    searchMarketQuery = it
                                    onMarketChange(it)
                                },
                                placeholder = { Text("3. Mandi / Market", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("filter_market_input"),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                trailingIcon = {
                                    if (searchMarketQuery.isNotBlank()) {
                                        IconButton(onClick = {
                                            searchMarketQuery = ""
                                            onMarketChange("")
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            )

                            // Filter 4: Commodity Select Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { commodityDropdownExpanded = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("filter_commodity_dropdown"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (uiState.filters.commodity.isBlank() || uiState.filters.commodity == "All Commodities") "4. All Commodities" else "4. ${uiState.filters.commodity}",
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = commodityDropdownExpanded,
                                    onDismissRequest = { commodityDropdownExpanded = false }
                                ) {
                                    POPULAR_COMMODITIES.forEach { comm ->
                                        DropdownMenuItem(
                                            text = { Text(comm, fontSize = 12.sp) },
                                            onClick = {
                                                onCommodityChange(if (comm == "All Commodities") "" else comm)
                                                commodityDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Filter 5: Variety (Optional)
                        OutlinedTextField(
                            value = searchVarietyQuery,
                            onValueChange = {
                                searchVarietyQuery = it
                                onVarietyChange(it)
                            },
                            placeholder = { Text("5. Variety (e.g. Red, Shankar-6, Hybrid) - Optional", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("filter_variety_input"),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                if (searchVarietyQuery.isNotBlank()) {
                                    IconButton(onClick = {
                                        searchVarietyQuery = ""
                                        onVarietyChange("")
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        )

                        // Quick Commodity Chips
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val chips = listOf("Onion", "Cotton", "Soybean", "Wheat", "Tomato", "Gram Raw(Chhana)")
                            chips.forEach { chip ->
                                val isSelected = uiState.filters.commodity.equals(chip, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        val next = if (isSelected) "" else chip
                                        onCommodityChange(next)
                                    },
                                    label = { Text(chip, fontSize = 10.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KisanGreenContainer,
                                        selectedLabelColor = KisanGreenPrimary
                                    ),
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Results Count & Header Bar
        item(key = "agmarknet_results_bar") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${uiState.records.size} Government APMC Records",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Unit: ₹ / Quintal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = KisanGreenPrimary
                )
            }
        }

        // 5. Results / Skeletons / Errors / Empty States
        when {
            // Loading State (Shimmer Skeleton Cards)
            uiState.isLoading -> {
                items(5, key = { "skeleton_$it" }) {
                    AgmarknetSkeletonCard()
                }
            }

            // Empty State (No records matched)
            uiState.records.isEmpty() -> {
                item(key = "agmarknet_no_results") {
                    AgmarknetNoResultsCard(
                        currentLanguage = currentLanguage,
                        onClearFilters = onClearFilters
                    )
                }
            }

            // Success: Results List
            else -> {
                items(
                    items = uiState.records,
                    key = { it.id }
                ) { record ->
                    AgmarknetRecordCard(
                        record = record,
                        currentLanguage = currentLanguage,
                        onSpeak = { onSpeakRecord(record) },
                        isPlayingAudio = isPlayingAudio
                    )
                }
            }
        }
    }
}

/**
 * Mobile-First Responsive Record Card displaying Government AGMARKNET data
 */
@Composable
fun AgmarknetRecordCard(
    record: AgmarknetMandiRecord,
    currentLanguage: AppLanguage,
    onSpeak: () -> Unit,
    isPlayingAudio: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("agmark_card_${record.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Commodity & Variety + Speak Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val emoji = getCommodityEmoji(record.commodity)
                        val cleanedCommodity = cleanCommodityName(record.commodity)
                        Text(
                            text = "$emoji $cleanedCommodity",
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (record.variety.isNotBlank() && record.variety != "Standard") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KisanGreenContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = record.variety.replace("_", " "),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KisanGreenPrimary
                                )
                            }
                        }
                    }

                    // Location: Mandi, District, State
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Mandi",
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${record.market} APMC • ${record.district}, ${record.state}",
                            fontSize = 11.5.sp,
                            color = Slate600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Audio Broadcast Button
                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(KisanGreenContainer)
                ) {
                    Icon(
                        imageVector = if (isPlayingAudio) Icons.Default.VolumeUp else Icons.Outlined.VolumeUp,
                        contentDescription = "Listen price in local language",
                        tint = KisanGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Column Price Breakdown (Min, Modal, Max)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 8.dp, horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Min Price
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = LanguageManager.getString("min_price_label", currentLanguage),
                            fontSize = 10.sp,
                            color = Slate500
                        )
                        Text(
                            text = if (record.minPrice > 0) "₹${record.minPrice.toInt()}" else "—",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Slate300)
                    )

                    // Modal Price (Hero)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = LanguageManager.getString("modal_price_label", currentLanguage),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KisanGreenPrimary
                        )
                        Text(
                            text = if (record.modalPrice > 0) "₹${record.modalPrice.toInt()}" else "₹${record.maxPrice.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = KisanGreenPrimary
                        )
                        Text(
                            text = "per Quintal",
                            fontSize = 9.sp,
                            color = Slate500
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Slate300)
                    )

                    // Max Price
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = LanguageManager.getString("max_price_label", currentLanguage),
                            fontSize = 10.sp,
                            color = Slate500
                        )
                        Text(
                            text = if (record.maxPrice > 0) "₹${record.maxPrice.toInt()}" else "—",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Arrival Date & Govt Quality Grade
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Arrival Date",
                        tint = Slate400,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Arrival: ${record.arrivalDate}",
                        fontSize = 11.sp,
                        color = Slate600,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "Grade: ${record.grade}",
                    fontSize = 11.sp,
                    color = Slate500
                )
            }
        }
    }
}

/**
 * Skeleton Card for loading feedback
 */
@Composable
fun AgmarknetSkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(140.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Slate300)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Slate300)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate200)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Slate300)
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Slate300)
                )
            }
        }
    }
}

/**
 * Friendly card shown when DATA_GOV_API_KEY is not configured
 */
@Composable
fun AgmarknetApiKeyMissingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF93C5FD))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = "API Key",
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "data.gov.in API Key Setup Required",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E40AF)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "To access official Government of India AGMARKNET daily mandi feeds, configure your DATA_GOV_API_KEY in the AI Studio Secrets panel or .env file.",
                fontSize = 12.sp,
                color = Color(0xFF1E3A8A),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDBEAFE))
                    .padding(8.dp)
            ) {
                Text(
                    text = "1. Register free at data.gov.in\n2. Open My Account -> API Key\n3. Add DATA_GOV_API_KEY in Secrets panel",
                    fontSize = 11.sp,
                    color = Color(0xFF1E40AF),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Error state with retry
 */
@Composable
fun AgmarknetErrorStateCard(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCA5A5))
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Error",
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Unable to connect to data.gov.in",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF991B1B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                fontSize = 11.5.sp,
                color = Color(0xFFB91C1C),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retry Connection", fontSize = 12.sp)
            }
        }
    }
}

/**
 * No records found state
 */
@Composable
fun AgmarknetNoResultsCard(
    currentLanguage: AppLanguage,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = "No records",
                tint = Slate400,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = LanguageManager.getString("no_records_msg", currentLanguage),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Try clearing district, commodity, or variety filters to view broader market arrivals.",
                fontSize = 11.5.sp,
                color = Slate500,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onClearFilters,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Reset Filters to Maharashtra All", fontSize = 12.sp)
            }
        }
    }
}
