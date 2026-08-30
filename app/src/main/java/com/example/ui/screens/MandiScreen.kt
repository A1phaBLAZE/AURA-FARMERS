package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LanguageManager
import com.example.data.model.AgmarknetMandiRecord
import com.example.data.model.AgmarknetUiState
import com.example.data.model.AppLanguage
import com.example.data.model.CropCategory
import com.example.data.model.CropItem
import com.example.service.LiveMarketMonitorState
import com.example.ui.components.AgmarknetMandiSection
import com.example.ui.components.PriceHistoryChart
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MandiScreen(
    crops: List<CropItem>,
    selectedCategory: CropCategory,
    onCategorySelect: (CropCategory) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    currentLanguage: AppLanguage,
    selectedPriceUnit: com.example.data.model.PriceUnit = com.example.data.model.PriceUnit.QUINTAL,
    onPriceUnitChange: (com.example.data.model.PriceUnit) -> Unit = {},
    selectedCrop: CropItem?,
    onCropSelect: (CropItem?) -> Unit,
    marketMonitorState: LiveMarketMonitorState = LiveMarketMonitorState(),
    onSyncLivePrices: () -> Unit = {},
    isPlayingAudio: Boolean,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    agmarknetUiState: AgmarknetUiState = AgmarknetUiState(),
    onFetchAgmarknetPrices: (forceRefresh: Boolean) -> Unit = {},
    onAgmarknetStateChange: (String) -> Unit = {},
    onAgmarknetDistrictChange: (String) -> Unit = {},
    onAgmarknetMarketChange: (String) -> Unit = {},
    onAgmarknetCommodityChange: (String) -> Unit = {},
    onAgmarknetVarietyChange: (String) -> Unit = {},
    onClearAgmarknetFilters: () -> Unit = {},
    onSpeakAgmarknetRecord: (AgmarknetMandiRecord) -> Unit = {}
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    var mandiMode by remember { mutableIntStateOf(0) } // 0: Live AGMARKNET (Govt), 1: APMC Directory

    // Auto-fetch AGMARKNET records if initial list is empty
    LaunchedEffect(Unit) {
        if (agmarknetUiState.records.isEmpty() && !agmarknetUiState.isLoading) {
            onFetchAgmarknetPrices(false)
        }
    }

    LaunchedEffect(selectedCrop) {
        if (selectedCrop != null) {
            showDetailSheet = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Mandi Navigation Switcher (Official AGMARKNET Live vs APMC Directory)
        PrimaryTabRow(
            selectedTabIndex = mandiMode,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = KisanGreenPrimary,
            modifier = Modifier.fillMaxWidth().height(42.dp)
        ) {
            Tab(
                selected = mandiMode == 0,
                onClick = { mandiMode = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (mandiMode == 0) KisanGreenPrimary else Slate400
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LanguageManager.getString("mandi_view_agmarknet", currentLanguage),
                            fontWeight = if (mandiMode == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_agmarknet_live")
            )

            Tab(
                selected = mandiMode == 1,
                onClick = { mandiMode = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (mandiMode == 1) KisanGreenPrimary else Slate400
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = LanguageManager.getString("mandi_view_directory", currentLanguage),
                            fontWeight = if (mandiMode == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_apmc_directory")
            )
        }

            if (mandiMode == 0) {
                // Official Government of India AGMARKNET Live Prices View
                AgmarknetMandiSection(
                    uiState = agmarknetUiState,
                    currentLanguage = currentLanguage,
                    onRefresh = { onFetchAgmarknetPrices(true) },
                    onStateChange = onAgmarknetStateChange,
                    onDistrictChange = onAgmarknetDistrictChange,
                    onMarketChange = onAgmarknetMarketChange,
                    onCommodityChange = onAgmarknetCommodityChange,
                    onVarietyChange = onAgmarknetVarietyChange,
                    onClearFilters = onClearAgmarknetFilters,
                    onSpeakRecord = onSpeakAgmarknetRecord,
                    isPlayingAudio = isPlayingAudio
                )
            } else {
                // APMC Benchmark Directory View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    // Demo Data Notice & APMC Benchmark Overview Bar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFCD34D))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Demo Data",
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Benchmark Directory — Reference Profiles",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "National APMC Mandi commodities with historical price curves and sale recommendations.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }


            // Search Bar & Filter Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = LanguageManager.getString("search_crop_hint", currentLanguage),
                            fontSize = 13.sp,
                            color = Slate500
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Slate400
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Slate500
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(KisanGreenContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GO",
                                    color = KisanGreenPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("crop_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KisanGreenPrimary,
                        unfocusedBorderColor = Slate200,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips Flow Row (Wraps cleanly on mobile without horizontal scroll overflow)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf(
                        CropCategory.ALL to "filter_all",
                        CropCategory.VEGETABLES to "filter_veg",
                        CropCategory.GRAINS_PULSES to "filter_grains",
                        CropCategory.CASH_CROPS to "filter_cash",
                        CropCategory.FRUITS to "filter_fruits",
                        CropCategory.SPICES to "filter_spices"
                    )

                    categories.forEach { (cat, strKey) ->
                        val isSelected = (selectedCategory == cat)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategorySelect(cat) },
                            label = {
                                Text(
                                    text = LanguageManager.getString(strKey, currentLanguage),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KisanGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .bounceClick { onCategorySelect(cat) }
                                .testTag("cat_chip_${cat.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Unit Conversion & Selection Bar (Kg Minimal, Quintal Standard, Ton Bulk)
                com.example.ui.components.UnitConversionBar(
                    currentLanguage = currentLanguage,
                    selectedPriceUnit = selectedPriceUnit,
                    onUnitSelect = onPriceUnitChange
                )
            }

            // Audio Mandi Bulletin Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .bounceClick {
                        val bulletinText = when (currentLanguage) {
                            AppLanguage.MR -> "किसान वाणी आजचे प्रमुख बाजार भाव: लासलगाव कांदा २,८५० रुपये प्रति क्विंटल, वाढ ६.४ टक्के. नारायणगाव टोमॅटो २,१०० रुपये. लातूर पिवळा सोयाबीन ४,६८० रुपये. अकोला कापूस ७,४५० रुपये. सोलापूर तूर १०,४५० रुपये. सविस्तर माहितीसाठी पिकावर टॅप करा."
                            AppLanguage.HI -> "किसान वाणी आज के मुख्य मंडी भाव: लासलगांव प्याज ₹2,850 प्रति क्विंटल। नारायणगांव टमाटर ₹2,100। लातूर सोयाबीन ₹4,680। अकोला कपास ₹7,450। सोलापूर तुअर ₹10,450। अधिक जानकारी के लिए फसल पर टैप करें।"
                            AppLanguage.GU -> "કિસાન વાણી આજના મુખ્ય બજાર ભાવ: નાસિક ડુંગળી ₹2,850 પ્રતિ ક્વિન્ટલ, સોયાબીન ₹4,680, કપાસ ₹7,450. વિગત માટે પાક પર ક્લિક કરો."
                            AppLanguage.EN -> "Kisan Vani Mandi Bulletin: Onion at Lasalgaon ₹2,850/qtl (+6.4%). Soybean at Latur ₹4,680/qtl. Cotton at Akola ₹7,450/qtl. Tur pulse at Solapur ₹10,450/qtl. Tap on any crop for price trend chart."
                            else -> "किसान वाणी आज के मुख्य मंडी भाव: लासलगांव प्याज ₹2,850 प्रति क्विंटल। लातूर सोयाबीन ₹4,680। अकोला कपास ₹7,450।"
                        }
                        if (isPlayingAudio) onStopAudio() else onPlayAudio(bulletinText)
                    }
                    .testTag("mandi_audio_bulletin_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = KisanGreenContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isPlayingAudio) KisanSaffron else KisanGreenPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = "Play Audio",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = LanguageManager.getString("listen_bulletin", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = KisanGreenPrimary
                                )
                                if (isPlayingAudio) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AudioWaveformBars(isPlaying = true, barColor = KisanGreenDark, barCount = 4)
                                }
                            }
                            Text(
                                text = if (isPlayingAudio) "Playing summary... Tap to stop" else "1-Tap voice summary of APMC benchmark prices",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Crops & Vegetables Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${crops.size} Commodities",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tap to view price trends",
                    fontSize = 11.sp,
                    color = KisanGreenPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Crops Responsive Vertical Column (Zero horizontal scroll overflow, optimized for mobile)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                crops.forEach { crop ->
                    CropCard(
                        crop = crop,
                        currentLanguage = currentLanguage,
                        selectedPriceUnit = selectedPriceUnit,
                        onClick = {
                            onCropSelect(crop)
                            showDetailSheet = true
                        }
                    )
                }
            }
        }
    }
}

    // Detailed Crop Modal Sheet (Price history chart, sale window, mandi details)
    if (showDetailSheet && selectedCrop != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showDetailSheet = false
                onCropSelect(null)
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            CropDetailContent(
                crop = selectedCrop,
                currentLanguage = currentLanguage,
                selectedPriceUnit = selectedPriceUnit,
                onClose = {
                    showDetailSheet = false
                    onCropSelect(null)
                },
                onPlayAudio = onPlayAudio
            )
        }
    }
}

@Composable
fun CropCard(
    crop: CropItem,
    currentLanguage: AppLanguage,
    selectedPriceUnit: com.example.data.model.PriceUnit = com.example.data.model.PriceUnit.QUINTAL,
    onClick: () -> Unit
) {
    val displayPriceFormatted = when (selectedPriceUnit) {
        com.example.data.model.PriceUnit.KG -> "₹${String.format(java.util.Locale.US, "%.1f", crop.currentPrice / 100.0)}"
        com.example.data.model.PriceUnit.QUINTAL -> "₹${String.format(java.util.Locale.US, "%,d", crop.currentPrice)}"
        com.example.data.model.PriceUnit.TON -> "₹${String.format(java.util.Locale.US, "%,d", crop.currentPrice * 10)}"
    }

    val displayUnitLabel = when (selectedPriceUnit) {
        com.example.data.model.PriceUnit.KG -> "/ Kg (Minimal)"
        com.example.data.model.PriceUnit.QUINTAL -> "/ Quintal (100 kg)"
        com.example.data.model.PriceUnit.TON -> "/ Metric Ton (Bulk)"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
            .testTag("crop_card_${crop.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(KisanGreenContainer)
                            .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = crop.emoji,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = crop.getName(currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Mandi",
                                tint = Slate500,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${crop.mandiName} • ${crop.district}",
                                fontSize = 11.sp,
                                color = Slate500,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Price & Trend Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = displayPriceFormatted,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = KisanGreenPrimary
                    )
                    Text(
                        text = displayUnitLabel,
                        fontSize = 9.5.sp,
                        color = Slate400,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (crop.trendPercent >= 0) Color(0xFFDCFCE7)
                                else Color(0xFFFEE2E2)
                            )
                            .border(
                                1.dp,
                                if (crop.trendPercent >= 0) Color(0xFF86EFAC)
                                else Color(0xFFFCA5A5),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = (if (crop.trendPercent >= 0) "▲ +" else "▼ ") + "${crop.trendPercent}%",
                            color = if (crop.trendPercent >= 0) TrendGreen else TrendRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arrival Volume & Range Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100)
                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Range: ₹${crop.minPrice} - ₹${crop.maxPrice}",
                    fontSize = 11.sp,
                    color = Slate700,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Arrivals: ${crop.arrivalVolumeQuintals} qtl",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KisanGreenDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Recommendation Snippet
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Advice",
                    tint = KisanSaffron,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = crop.getRecommendation(currentLanguage),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CropDetailContent(
    crop: CropItem,
    currentLanguage: AppLanguage,
    selectedPriceUnit: com.example.data.model.PriceUnit = com.example.data.model.PriceUnit.QUINTAL,
    onClose: () -> Unit,
    onPlayAudio: (String) -> Unit
) {
    val ratePerKg = crop.currentPrice / 100.0
    val ratePerTon = crop.currentPrice * 10.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = crop.emoji, fontSize = 32.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = crop.getName(currentLanguage),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${crop.mandiName} • ${crop.district}, Maharashtra",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Multi-Unit Price Matrix Card
        Card(
            colors = CardDefaults.cardColors(containerColor = KisanGreenContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = LanguageManager.getString("modal_price", currentLanguage),
                            fontSize = 12.sp,
                            color = KisanGreenPrimary
                        )
                        Text(
                            text = "₹${crop.currentPrice} / Qtl",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = KisanGreenPrimary
                        )
                        Text(
                            text = "Min: ₹${crop.minPrice} | Max: ₹${crop.maxPrice}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = LanguageManager.getString("arrival_volume", currentLanguage),
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "${crop.arrivalVolumeQuintals} qtl",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Trend: +${crop.trendPercent}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrendGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = KisanGreenContainerBorder)
                Spacer(modifier = Modifier.height(8.dp))

                // Breakdown for Minimal (Kg) and Bulk (Ton)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("⚖️ Minimal Rate", fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Bold)
                            Text("₹${String.format(java.util.Locale.US, "%.2f", ratePerKg)} / kg", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KisanGreenDark)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("🚛 Bulk Wholesale", fontSize = 10.sp, color = Slate500, fontWeight = FontWeight.Bold)
                            Text("₹${String.format(java.util.Locale.US, "%,.0f", ratePerTon)} / Ton", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KisanGreenDark)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Graphical Price History Chart (14-Days Trend)
        Text(
            text = LanguageManager.getString("price_trend_title", currentLanguage),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        PriceHistoryChart(pricePoints = crop.priceHistory)

        Spacer(modifier = Modifier.height(16.dp))

        // Sale Window Recommendation Box
        Card(
            colors = CardDefaults.cardColors(containerColor = KisanSaffronContainer),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Recommendation",
                            tint = KisanSaffron,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageManager.getString("sale_window_rec", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KisanSaffron
                        )
                    }

                    // Audio Read aloud button
                    IconButton(
                        onClick = {
                            val speech = "${crop.getName(currentLanguage)}. ${crop.getRecommendation(currentLanguage)}"
                            onPlayAudio(speech)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak recommendation",
                            tint = KisanSaffron
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = crop.getRecommendation(currentLanguage),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
