package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.KisanViewModel
import java.util.Locale
import kotlin.math.roundToInt

enum class MarketplaceTab(val labelEn: String, val labelHi: String, val labelMr: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PRODUCE("Fresh Farm Produce", "ताजा खेत उपज", "ताजा शेतमाल", Icons.Default.Agriculture),
    ORDERS("My Orders & Tracker", "मेरे ऑर्डर व ट्रैकर", "माझे ऑर्डर्स व ट्रॅकर", Icons.Default.LocalShipping),
    LOGISTICS("FPO Logistics & Route", "लॉजिस्टिक्स व रूट", "लॉजिस्टिक्स व मार्ग", Icons.Default.AltRoute),
    DEMAND("AI Demand Forecast", "मांग पूर्वानुमान", "मागणी अंदाज", Icons.Default.TrendingUp);

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> labelEn
        AppLanguage.MR -> labelMr
        AppLanguage.HI -> labelHi
        else -> labelEn
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumerMarketplaceScreen(
    viewModel: KisanViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val d2cListings by viewModel.d2cListings.collectAsState()
    val d2cOrders by viewModel.d2cOrders.collectAsState()
    val multiStopRoute by viewModel.activeMultiStopRoute.collectAsState()
    val farmerLogisticsConfig by viewModel.farmerLogisticsConfig.collectAsState()
    val demandForecast by viewModel.currentDemandForecast.collectAsState()

    var selectedTab by remember { mutableStateOf(MarketplaceTab.PRODUCE) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CropCategory?>(null) }
    var maxDistanceKmFilter by remember { mutableStateOf<Double?>(null) }

    var listingToOrder by remember { mutableStateOf<D2CProduceListing?>(null) }
    var showLogisticsConfigDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = LanguageManager.getString("tab_consumer_marketplace", currentLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = KisanGreenPrimary.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, KisanGreenPrimary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "D2C Direct Farm",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KisanGreenPrimary
                                )
                            }
                        }
                        Text(
                            text = "Zero Middlemen • Verified FPO & Farmer Produce",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Farmer Logistics Config shortcut if farmer/buyer
                    if (userProfile?.role == UserRole.FARMER || userProfile?.role == UserRole.BUYER) {
                        IconButton(
                            onClick = { showLogisticsConfigDialog = true },
                            modifier = Modifier.testTag("open_logistics_config_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = "Logistics Settings", tint = KisanGreenPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("consumer_marketplace_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sub-navigation Tabs
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = KisanGreenPrimary,
                modifier = Modifier.fillMaxWidth().testTag("marketplace_subtabs")
            ) {
                MarketplaceTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        text = {
                            Text(
                                text = tab.getLabel(currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                    )
                }
            }

            when (selectedTab) {
                MarketplaceTab.PRODUCE -> {
                    ProduceBrowseContent(
                        listings = d2cListings,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        selectedCategory = selectedCategory,
                        onSelectCategory = { selectedCategory = it },
                        maxDistanceKm = maxDistanceKmFilter,
                        onSelectDistanceFilter = { maxDistanceKmFilter = it },
                        currentLanguage = currentLanguage,
                        onOrderClick = { listing -> listingToOrder = listing }
                    )
                }
                MarketplaceTab.ORDERS -> {
                    OrdersTrackingContent(
                        orders = d2cOrders,
                        currentLanguage = currentLanguage,
                        onAdvanceStatus = { order ->
                            viewModel.advanceOrderStatus(order.orderId)
                            snackbarMessage = "Order #${order.orderId.takeLast(5)} updated to next delivery status!"
                        },
                        onReleaseEscrow = { order ->
                            viewModel.releaseEscrowForOrder(order.orderId)
                            snackbarMessage = "₹${order.farmerPayoutInr.roundToInt()} released to farmer ${order.farmerName}!"
                        }
                    )
                }
                MarketplaceTab.LOGISTICS -> {
                    LogisticsDispatcherContent(
                        route = multiStopRoute,
                        orders = d2cOrders,
                        farmerConfig = farmerLogisticsConfig,
                        currentLanguage = currentLanguage,
                        onOptimizeRoute = {
                            viewModel.recalculateOptimizedRoute()
                            snackbarMessage = "AI multi-stop route optimized across all farmer pickups & consumer deliveries!"
                        },
                        onOpenConfig = { showLogisticsConfigDialog = true }
                    )
                }
                MarketplaceTab.DEMAND -> {
                    DemandForecastContent(
                        demandForecast = demandForecast,
                        availableCommodities = viewModel.availableForecastCommodities,
                        selectedCommodity = viewModel.selectedDemandCommodity,
                        onSelectCommodity = { comm -> viewModel.updateSelectedDemandCommodity(comm) },
                        selectedDistrict = viewModel.selectedDemandDistrict,
                        onSelectDistrict = { dist -> viewModel.updateSelectedDemandDistrict(dist) },
                        currentLanguage = currentLanguage
                    )
                }
            }
        }
    }

    // Checkout Dialog
    listingToOrder?.let { listing ->
        D2COrderCheckoutDialog(
            listing = listing,
            currentLanguage = currentLanguage,
            onDismiss = { listingToOrder = null },
            onConfirmOrder = { name, mobile, address, district, pincode, qty, slot, paymentMode ->
                viewModel.placeD2COrder(
                    listing = listing,
                    consumerName = name,
                    consumerMobile = mobile,
                    deliveryAddress = address,
                    deliveryDistrict = district,
                    deliveryPincode = pincode,
                    quantityKg = qty,
                    selectedSlot = slot,
                    paymentMethod = paymentMode
                )
                listingToOrder = null
                selectedTab = MarketplaceTab.ORDERS
                snackbarMessage = "Order booked successfully! Tracking active."
            }
        )
    }

    // Farmer Logistics Config Dialog
    if (showLogisticsConfigDialog) {
        FarmerLogisticsConfigDialog(
            currentConfig = farmerLogisticsConfig,
            onDismiss = { showLogisticsConfigDialog = false },
            onSaveConfig = { newConfig ->
                viewModel.saveFarmerLogisticsConfig(newConfig)
                showLogisticsConfigDialog = false
                snackbarMessage = "Logistics pickup slots and ${newConfig.deliveryRadiusKm.toInt()}km radius updated!"
            }
        )
    }
}

@Composable
private fun ProduceBrowseContent(
    listings: List<D2CProduceListing>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: CropCategory?,
    onSelectCategory: (CropCategory?) -> Unit,
    maxDistanceKm: Double?,
    onSelectDistanceFilter: (Double?) -> Unit,
    currentLanguage: AppLanguage,
    onOrderClick: (D2CProduceListing) -> Unit
) {
    val filteredListings = remember(listings, searchQuery, selectedCategory, maxDistanceKm) {
        listings.filter { item ->
            val matchSearch = searchQuery.isBlank() ||
                    item.cropName.contains(searchQuery, ignoreCase = true) ||
                    item.variety.contains(searchQuery, ignoreCase = true) ||
                    item.farmerName.contains(searchQuery, ignoreCase = true) ||
                    item.farmNameOrFpo.contains(searchQuery, ignoreCase = true)

            val matchCategory = selectedCategory == null || item.category == selectedCategory
            val matchDistance = maxDistanceKm == null || item.deliveryRadiusKm <= maxDistanceKm

            matchSearch && matchCategory && matchDistance
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Search & Filter Header
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search fresh tomatoes, onions, dal, fruits...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KisanGreenPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("d2c_search_input")
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("All Categories") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KisanGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(CropCategory.values()) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { onSelectCategory(if (selectedCategory == cat) null else cat) },
                        label = { Text("${cat.icon} ${cat.getLabel(currentLanguage)}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KisanGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Distance & Proximity Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Proximity:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                DistanceFilterChip(label = "All Radius", isSelected = maxDistanceKm == null, onClick = { onSelectDistanceFilter(null) })
                DistanceFilterChip(label = "< 15 km", isSelected = maxDistanceKm == 15.0, onClick = { onSelectDistanceFilter(if (maxDistanceKm == 15.0) null else 15.0) })
                DistanceFilterChip(label = "< 35 km", isSelected = maxDistanceKm == 35.0, onClick = { onSelectDistanceFilter(if (maxDistanceKm == 35.0) null else 35.0) })
                DistanceFilterChip(label = "< 50 km", isSelected = maxDistanceKm == 50.0, onClick = { onSelectDistanceFilter(if (maxDistanceKm == 50.0) null else 50.0) })
            }
        }

        // Direct-to-Consumer Produce Cards
        if (filteredListings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🌾", fontSize = 40.sp)
                        Text(
                            text = "No produce matching your filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Try clearing the search query or expanding the proximity radius.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredListings, key = { it.id }) { listing ->
                D2CProduceCard(
                    listing = listing,
                    currentLanguage = currentLanguage,
                    onOrderClick = { onOrderClick(listing) }
                )
            }
        }
    }
}

@Composable
private fun DistanceFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) KisanGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) KisanGreenPrimary else Color.Transparent
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) KisanGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun D2CProduceCard(
    listing: D2CProduceListing,
    currentLanguage: AppLanguage,
    onOrderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("d2c_produce_card_${listing.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Emoji + Title + Freshness & Organic Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(KisanGreenPrimary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = listing.emoji, fontSize = 28.sp)
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = listing.cropName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (listing.isOrganicCertified) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "🌱 Organic",
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${listing.variety} • ${listing.harvestFreshness}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Farmer Rating & Orders count
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(15.dp))
                        Text(
                            text = "${listing.farmerRating}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${listing.totalOrdersFulfilled} sold",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Farmer & Village Info Strip
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(14.dp))
                        Text(
                            text = "${listing.farmerName} (${listing.farmNameOrFpo})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                    Text(
                        text = "📍 ${listing.village}, ${listing.district}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // TRANSPARENT PRICING PILL
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "₹${listing.totalPricePerKg.roundToInt()}/kg",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = KisanGreenPrimary
                            )
                            Text(
                                text = "₹${listing.typicalRetailPricePerKg.roundToInt()}/kg Retail",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Text(
                                text = "Save ${listing.savingsPercent.roundToInt()}% (₹${listing.savingsPerKg.roundToInt()}/kg)",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    // Formula breakdown text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌾 Farmer gets ₹${listing.farmerBasePricePerKg.roundToInt()} (${listing.farmerSharePercent.roundToInt()}%) + 🚚 Logistics ₹${listing.logisticsFeePerKg.roundToInt()}",
                            fontSize = 10.5.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "Stock: ${listing.availableStockKg.toInt()} kg",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            // Pack size hints and Order Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Packs:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    listing.packSizesKg.take(3).forEach { size ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${size.toInt()}kg",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Button(
                    onClick = onOrderClick,
                    colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("buy_fresh_btn_${listing.id}")
                ) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Buy Direct", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun OrdersTrackingContent(
    orders: List<D2COrder>,
    currentLanguage: AppLanguage,
    onAdvanceStatus: (D2COrder) -> Unit,
    onReleaseEscrow: (D2COrder) -> Unit
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "📦", fontSize = 48.sp)
                Text(
                    text = "No Active D2C Orders Yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Browse farm fresh produce and place your first direct farm order to see live delivery tracking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            items(orders, key = { it.orderId }) { order ->
                DeliveryTrackerCard(
                    order = order,
                    currentLanguage = currentLanguage,
                    onAdvanceStatus = onAdvanceStatus,
                    onReleaseEscrow = onReleaseEscrow
                )
            }
        }
    }
}

@Composable
private fun LogisticsDispatcherContent(
    route: LogisticsMultiStopRoute,
    orders: List<D2COrder>,
    farmerConfig: FarmerLogisticsConfig,
    currentLanguage: AppLanguage,
    onOptimizeRoute: () -> Unit,
    onOpenConfig: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Dispatcher Header Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = KisanGreenPrimary)
                            Column {
                                Text(
                                    text = "FPO Green Logistics Run",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Vehicle: ${route.vehicleId} • Driver: ${route.driverName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenConfig,
                            modifier = Modifier.testTag("open_logistics_config_btn_in_tab")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = KisanGreenPrimary)
                        }
                    }

                    // Optimization Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOptimizeRoute,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.weight(1f).testTag("recompute_route_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Optimize Multi-Stop Route", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Interactive Canvas Map Preview
        item {
            LogisticsRouteCanvasMap(route = route)
        }

        // Stop Itinerary Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ordered Stop Itinerary (${route.stops.size} Stops):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Payload: ${route.totalCargoKg.toInt()} kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = KisanGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Stop Items
        items(route.stops, key = { it.stopSequence }) { stop ->
            RouteStopCard(stop = stop)
        }
    }
}

@Composable
private fun RouteStopCard(stop: RouteStop) {
    val isPickup = stop.stopType == RouteStopType.PICKUP
    val accentColor = if (isPickup) Color(0xFF16A34A) else Color(0xFF0284C7)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sequence Number Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${stop.stopSequence}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = stop.stopType.badge,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                    }
                    Text(
                        text = stop.partyName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "📍 ${stop.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "📦 ${stop.cropItem} • Cumulative Dist: ${stop.cumulativeDistanceKm} km",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stop.etaTime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Cargo: ${stop.currentVehiclePayloadKg.toInt()}kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DemandForecastContent(
    demandForecast: CommodityDemandForecast,
    availableCommodities: List<String>,
    selectedCommodity: String,
    onSelectCommodity: (String) -> Unit,
    selectedDistrict: String,
    onSelectDistrict: (String) -> Unit,
    currentLanguage: AppLanguage
) {
    val districts = listOf("Nashik", "Pune", "Mumbai", "Nagpur", "Aurangabad", "Ahmednagar", "Kolhapur", "Solapur")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
    ) {
        // Commodity Horizontal Scroll
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Select Commodity for Volume Forecasting:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCommodities) { comm ->
                        val isSelected = selectedCommodity.startsWith(comm.take(5), ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCommodity(comm) },
                            label = { Text(comm) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // District Filter
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Consuming District / Region:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(districts) { dist ->
                        val isSelected = selectedDistrict.equals(dist, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectDistrict(dist) },
                            label = { Text("📍 $dist") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KisanGreenPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Demand Forecast Visual Card
        item {
            DemandForecastChartCard(
                forecast = demandForecast,
                currentLanguage = currentLanguage
            )
        }

        // Sourcing & AGMARKNET Correlation Details
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = "AGMARKNET Mandi Supply Correlation",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = demandForecast.agmarknetArrivalCorrelation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Confidence Score: ${demandForecast.confidenceScorePercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = KisanGreenPrimary
                        )
                        Text(
                            text = "Elasticity: ${demandForecast.priceElasticityIndex}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}
