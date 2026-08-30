package com.example

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.LanguageManager
import com.example.data.model.*
import com.example.service.*
import com.example.ui.components.AppNotificationBanner
import com.example.ui.components.AppTopBar
import com.example.ui.components.TeeSecurityDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.KisanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KisanVaniApp(window = window)
                }
            }
        }
    }
}

@Composable
fun KisanVaniApp(
    window: Window? = null,
    viewModel: KisanViewModel = viewModel()
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedPriceUnit by viewModel.selectedPriceUnit.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCrop by viewModel.selectedCrop.collectAsState()
    val filteredCrops by viewModel.filteredCrops.collectAsState()
    val allLots by viewModel.allLots.collectAsState()
    val allOffers by viewModel.allOffers.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val farmerKisanCard by viewModel.farmerKisanCard.collectAsState()
    val pesticides by viewModel.pesticides.collectAsState()
    val helplines by viewModel.helplines.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val selectedWeatherState by viewModel.selectedWeatherState.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val isWeatherLoading by viewModel.isWeatherLoading.collectAsState()
    val weatherNetworkError by viewModel.weatherNetworkError.collectAsState()
    val marketMonitorState by viewModel.marketMonitorState.collectAsState()
    val agmarknetUiState by viewModel.agmarknetUiState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val isPlayingAudio by viewModel.isAudioPlaying.collectAsState()
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val isOfflineSynced by viewModel.isOfflineSynced.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val showCreateLotDialog by viewModel.showCreateLotDialog.collectAsState()
    val activePaymentModal by viewModel.activeRazorpayModal.collectAsState()

    // AI Harness States
    val harnessTab by viewModel.harnessTab.collectAsState()
    val pathologyResult by viewModel.pathologyResult.collectAsState()
    val arbitrageResult by viewModel.arbitrageResult.collectAsState()
    val moistureGradingResult by viewModel.moistureGradingResult.collectAsState()
    val subsidyResult by viewModel.subsidyResult.collectAsState()

    // TEE & DLP Security States
    val enclaveStatus by viewModel.enclaveStatus.collectAsState()
    val dlpPolicy by viewModel.dlpPolicy.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val showTeeSecurityDialog by viewModel.showTeeSecurityDialog.collectAsState()

    // Govt Procurement States
    val selectedProcurementState by viewModel.selectedProcurementState.collectAsState()
    val currentFarmerLandProfile by viewModel.currentFarmerLandProfile.collectAsState()
    val mspCrops by viewModel.mspCrops.collectAsState()
    val procurementCenters by viewModel.procurementCenters.collectAsState()
    val cropRegistrations by viewModel.cropRegistrations.collectAsState()
    val tokenBookings by viewModel.tokenBookings.collectAsState()
    val procurementReceipts by viewModel.procurementReceipts.collectAsState()
    val stateProcurementInfo by viewModel.stateProcurementInfo.collectAsState()
    val lastBookedToken by viewModel.lastBookedToken.collectAsState()
    val appNotification by viewModel.appNotification.collectAsState()

    // Apply FLAG_SECURE to prevent screenshot / screen recording leakage
    LaunchedEffect(dlpPolicy.isScreenCaptureBlocked, window) {
        viewModel.setScreenCaptureBlocked(dlpPolicy.isScreenCaptureBlocked, window)
    }

    // TEE & DLP Security Dialog
    if (showTeeSecurityDialog) {
        TeeSecurityDialog(
            currentLanguage = currentLanguage,
            enclaveStatus = enclaveStatus,
            dlpPolicy = dlpPolicy,
            auditLogs = auditLogs,
            onDismiss = { viewModel.toggleTeeSecurityDialog(false) },
            onToggleScreenCapture = { viewModel.setScreenCaptureBlocked(it, window) },
            onToggleClipboardPurge = { viewModel.setSecureClipboardActive(it) },
            onTogglePiiMasking = { viewModel.setPiiMaskingEnabled(it) },
            onToggleEscrowSigning = { viewModel.setEscrowSigningEnforced(it) },
            onTestEncrypt = { viewModel.testTeeEncrypt(it) },
            onTestDecrypt = { viewModel.testTeeDecrypt(it) },
            isPlayingAudio = isPlayingAudio,
            onPlayAudio = { viewModel.playAudio(it) },
            onStopAudio = { viewModel.stopAudio() }
        )
    }

    // SHOW LOGIN INTERFACE AT THE BEGINNING
    if (!isLoggedIn) {
        LoginScreen(
            currentLanguage = currentLanguage,
            onLanguageChange = { viewModel.changeLanguage(it) },
            onLoginFarmer = { name, mob, dist, kid ->
                viewModel.loginAsFarmer(name, mob, dist, kid)
            },
            onLoginConsumer = { name, mob, addr, dist ->
                viewModel.loginAsConsumer(name, mob, addr, dist)
            },
            onLoginBuyer = { comp, rep, mob ->
                viewModel.loginAsBuyer(comp, rep, mob)
            },
            onLoginTrader = { trd, mnd, lic ->
                viewModel.loginAsTrader(trd, mnd, lic)
            },
            onQuickDemoLogin = { role ->
                viewModel.quickDemoLogin(role)
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                AppTopBar(
                    currentLanguage = currentLanguage,
                    onLanguageChange = { viewModel.changeLanguage(it) },
                    currentUser = currentUser,
                    onLogoutClick = { viewModel.logout() },
                    onTeeSecurityClick = { viewModel.toggleTeeSecurityDialog(true) },
                    isOfflineSynced = isOfflineSynced,
                    isSyncing = isSyncing,
                    onSyncClick = { viewModel.syncMandiRates() },
                    isPlayingAudio = isPlayingAudio,
                    onStopAudioClick = { viewModel.stopAudio() },
                    onKisanCardClick = { viewModel.setTab(5) },
                    isKisanCardSelected = (selectedTab == 5)
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val navItems = if (currentUser?.role == UserRole.CONSUMER) {
                        listOf(
                            Triple(7, "tab_consumer_marketplace", Icons.Default.ShoppingBag),
                            Triple(0, "tab_mandi", Icons.Default.Store),
                            Triple(1, "tab_crop_trends", Icons.Default.TrendingUp),
                            Triple(4, "tab_weather", Icons.Default.WbSunny)
                        )
                    } else {
                        listOf(
                            Triple(0, "tab_mandi", Icons.Default.Store),
                            Triple(1, "tab_crop_trends", Icons.Default.TrendingUp),
                            Triple(2, "tab_lots", Icons.Default.Inventory2),
                            Triple(7, "tab_consumer_marketplace", Icons.Default.ShoppingBag),
                            Triple(3, "tab_govt_procurement", Icons.Default.AccountBalance),
                            Triple(4, "tab_weather", Icons.Default.WbSunny)
                        )
                    }

                    navItems.forEach { (index, labelKey, icon) ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.setTab(index) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = LanguageManager.getString(labelKey, currentLanguage),
                                    tint = if (isSelected) KisanGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = LanguageManager.getString(labelKey, currentLanguage),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) KisanGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_$index")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
                when (selectedTab) {
                    0 -> MandiScreen(
                        crops = filteredCrops,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.setCategory(it) },
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        currentLanguage = currentLanguage,
                        selectedPriceUnit = selectedPriceUnit,
                        onPriceUnitChange = { viewModel.setPriceUnit(it) },
                        selectedCrop = selectedCrop,
                        onCropSelect = { viewModel.selectCrop(it) },
                        marketMonitorState = marketMonitorState,
                        onSyncLivePrices = { viewModel.syncMandiRates() },
                        isPlayingAudio = isPlayingAudio,
                        onPlayAudio = { viewModel.playAudio(it) },
                        onStopAudio = { viewModel.stopAudio() },
                        agmarknetUiState = agmarknetUiState,
                        onFetchAgmarknetPrices = { viewModel.fetchAgmarknetMandiPrices(it) },
                        onAgmarknetStateChange = { viewModel.setAgmarknetState(it) },
                        onAgmarknetDistrictChange = { viewModel.setAgmarknetDistrict(it) },
                        onAgmarknetMarketChange = { viewModel.setAgmarknetMarket(it) },
                        onAgmarknetCommodityChange = { viewModel.setAgmarknetCommodity(it) },
                        onAgmarknetVarietyChange = { viewModel.setAgmarknetVariety(it) },
                        onClearAgmarknetFilters = { viewModel.clearAgmarknetFilters() },
                        onSpeakAgmarknetRecord = { viewModel.speakAgmarknetRecord(it) }
                    )

                    1 -> CropTrendsScreen(
                        viewModel = viewModel
                    )

                    2 -> LotsScreen(
                        lots = allLots,
                        offers = allOffers,
                        payments = allPayments,
                        currentLanguage = currentLanguage,
                        showCreateLotDialog = showCreateLotDialog,
                        onToggleCreateLotDialog = { viewModel.toggleCreateLotDialog(it) },
                        onCreateLot = { crop, variety, grade, qty, prc, dist, tal, st ->
                            viewModel.createLot(crop, variety, grade, qty, prc, dist, tal, st)
                        },
                        onAcceptOffer = { viewModel.acceptOffer(it) },
                        onRejectOffer = { viewModel.rejectOffer(it) },
                        onCounterOffer = { offer, prc -> viewModel.counterOffer(offer, prc) },
                        activePaymentModal = activePaymentModal,
                        onShowPaymentModal = { viewModel.showPaymentModal(it) },
                        onReleaseFunds = { viewModel.releaseEscrowPayment(it) }
                    )

                    3 -> GovtProcurementScreen(
                        currentLanguage = currentLanguage,
                        selectedState = selectedProcurementState,
                        onSelectState = { viewModel.selectProcurementState(it) },
                        farmerProfile = currentFarmerLandProfile,
                        mspCrops = mspCrops,
                        procurementCenters = procurementCenters,
                        cropRegistrations = cropRegistrations,
                        tokenBookings = tokenBookings,
                        procurementReceipts = procurementReceipts,
                        stateInfo = stateProcurementInfo,
                        lastBookedToken = lastBookedToken,
                        onRegisterCrop = { crop, season, acres, qty ->
                            viewModel.registerCropForProcurement(crop, season, acres, qty)
                        },
                        onBookToken = { crop, qty, centreId, date, slot ->
                            viewModel.bookGovtToken(crop, qty, centreId, date, slot)
                        },
                        onCancelToken = { viewModel.cancelGovtToken(it) },
                        isPlayingAudio = isPlayingAudio,
                        onPlayAudio = { viewModel.playAudio(it) },
                        onStopAudio = { viewModel.stopAudio() }
                    )

                    4 -> WeatherScreen(
                        weather = weather,
                        currentLanguage = currentLanguage,
                        selectedState = selectedWeatherState,
                        onStateSelect = { state, autoSpeak -> viewModel.selectWeatherStateAndFilter(state, autoSpeak) },
                        availableDistricts = viewModel.liveWeatherService.getDistrictsForState(selectedWeatherState),
                        selectedDistrict = selectedDistrict,
                        onDistrictSelect = { dist, autoSpeak -> viewModel.selectDistrictAndFetchWeather(dist, autoSpeak) },
                        isLoading = isWeatherLoading,
                        networkError = weatherNetworkError,
                        onRefreshWeather = { autoSpeak -> viewModel.refreshLiveWeather(autoSpeak) },
                        isPlayingAudio = isPlayingAudio,
                        onPlayAudio = { viewModel.playAudio(it) },
                        onStopAudio = { viewModel.stopAudio() }
                    )

                    5 -> KisanCardScreen(
                        kisanCard = farmerKisanCard,
                        pesticides = pesticides,
                        helplines = helplines,
                        currentLanguage = currentLanguage,
                        onOpenTeeSecurity = { viewModel.toggleTeeSecurityDialog(true) },
                        onCopySecure = { text, label -> viewModel.secureCopyToClipboard(text, label) }
                    )

                    6 -> AiAssistantScreen(
                        messages = chatMessages,
                        isThinking = isAiThinking,
                        isNetworkAvailable = isNetworkAvailable,
                        onSendMessage = { viewModel.sendAiPrompt(it) },
                        currentLanguage = currentLanguage,
                        isPlayingAudio = isPlayingAudio,
                        onPlayAudio = { viewModel.playAudio(it) },
                        onStopAudio = { viewModel.stopAudio() },
                        onClearChat = { viewModel.clearChat() }
                    )

                    7 -> ConsumerMarketplaceScreen(
                        viewModel = viewModel
                    )

                    else -> NotFoundScreen(
                        currentLanguage = currentLanguage,
                        onNavigateToTab = { viewModel.setTab(it) },
                        onSearchCrops = { 
                            viewModel.setSearchQuery(it)
                            viewModel.setTab(0)
                        }
                    )
                }

                // Floating AI Assistant Button on Bottom-Left (Above Bottom Navigation Bar)
                if (selectedTab != 6) {
                    FloatingActionButton(
                        onClick = { viewModel.setTab(6) },
                        containerColor = KisanGreenPrimary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, bottom = 16.dp)
                            .testTag("floating_ai_assistant_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI Assistant",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = LanguageManager.getString("floating_ai_assistant_btn", currentLanguage),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Global Top Floating Feedback & Error/Success Notifications
                AppNotificationBanner(
                    notification = appNotification,
                    onDismiss = { viewModel.dismissNotification() },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

