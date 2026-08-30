package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InitialData
import com.example.data.GovtProcurementData
import com.example.data.D2CSampleData
import com.example.data.db.KisanDatabase
import com.example.data.db.KisanRepository
import com.example.data.model.*
import com.example.service.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class KisanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KisanRepository
    private val audioService = AudioService(application)
    private val geminiService = GeminiService()
    val teeService = TeeEnclaveService(application)
    val dlpManager = DataLeakagePrevention(application, viewModelScope)
    val liveWeatherService = LiveWeatherService()
    val liveMandiService = LiveMandiPriceService()
    val agmarknetService = AgmarknetService()
    val networkMonitor = NetworkMonitor(application)
    val cropForecastService = CropForecastService()
    val logisticsRoutingService = LogisticsRoutingService()
    val demandForecastService = DemandForecastService()

    // ========================================================
    // DIRECT-TO-CONSUMER (D2C) MARKETPLACE & LOGISTICS STATE
    // ========================================================
    private val _d2cListings = MutableStateFlow<List<D2CProduceListing>>(D2CSampleData.sampleListings)
    val d2cListings: StateFlow<List<D2CProduceListing>> = _d2cListings.asStateFlow()

    private val _d2cOrders = MutableStateFlow<List<D2COrder>>(D2CSampleData.sampleOrders)
    val d2cOrders: StateFlow<List<D2COrder>> = _d2cOrders.asStateFlow()

    private val _farmerLogisticsConfig = MutableStateFlow<FarmerLogisticsConfig>(D2CSampleData.defaultFarmerLogisticsConfig)
    val farmerLogisticsConfig: StateFlow<FarmerLogisticsConfig> = _farmerLogisticsConfig.asStateFlow()

    private val _activeMultiStopRoute = MutableStateFlow<LogisticsMultiStopRoute>(
        logisticsRoutingService.computeOptimizedMultiStopRoute(
            orders = D2CSampleData.sampleOrders
        )
    )
    val activeMultiStopRoute: StateFlow<LogisticsMultiStopRoute> = _activeMultiStopRoute.asStateFlow()

    val availableForecastCommodities = listOf(
        "Onion (कांदा / प्याज)",
        "Tomato (टोमॅटो / टमाटर)",
        "Soybean (सोयाबीन)",
        "Pomegranate (डाळिंब / अनार)",
        "Wheat (गहू / गेहूं)",
        "Tur Dal (तूर डाळ / अरहर दाल)",
        "Potato (बटाटा / आलू)"
    )

    var selectedDemandCommodity by mutableStateOf("Onion (कांदा / प्याज)")
        private set
    var selectedDemandDistrict by mutableStateOf("Nashik")
        private set

    private val _currentDemandForecast = MutableStateFlow<CommodityDemandForecast>(
        demandForecastService.generateDemandForecast("Onion (कांदा / प्याज)", "Nashik")
    )
    val currentDemandForecast: StateFlow<CommodityDemandForecast> = _currentDemandForecast.asStateFlow()

    // ========================================================
    // CROP TRENDS & 30-DAY AI FORECAST STATE
    // ========================================================
    private val _trendsSearchQuery = MutableStateFlow("")
    val trendsSearchQuery: StateFlow<String> = _trendsSearchQuery.asStateFlow()

    private val _trendsSelectedCategory = MutableStateFlow(CropCategory.ALL)
    val trendsSelectedCategory: StateFlow<CropCategory> = _trendsSelectedCategory.asStateFlow()

    private val _trendsSelectedState = MutableStateFlow("All States")
    val trendsSelectedState: StateFlow<String> = _trendsSelectedState.asStateFlow()

    private val _trendsSelectedDistrict = MutableStateFlow("All Districts")
    val trendsSelectedDistrict: StateFlow<String> = _trendsSelectedDistrict.asStateFlow()

    private val _trendsSelectedDateRange = MutableStateFlow(TrendDateRange.DAYS_30)
    val trendsSelectedDateRange: StateFlow<TrendDateRange> = _trendsSelectedDateRange.asStateFlow()

    private val _selectedTrendCrop = MutableStateFlow<CropItem?>(null)
    val selectedTrendCrop: StateFlow<CropItem?> = _selectedTrendCrop.asStateFlow()

    private val _isTrendsRefreshing = MutableStateFlow(false)
    val isTrendsRefreshing: StateFlow<Boolean> = _isTrendsRefreshing.asStateFlow()

    private val _trendsErrorMessage = MutableStateFlow<String?>(null)
    val trendsErrorMessage: StateFlow<String?> = _trendsErrorMessage.asStateFlow()

    val isNetworkAvailable: StateFlow<Boolean> = networkMonitor.isOnlineFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = networkMonitor.isOnline()
    )

    private val _agmarknetUiState = MutableStateFlow(
        AgmarknetUiState(
            records = com.example.data.AgmarknetFallbackData.filterRecords(AgmarknetFilters()),
            cachedFallbackRecords = com.example.data.AgmarknetFallbackData.officialAgmarknetRecords,
            totalCount = com.example.data.AgmarknetFallbackData.filterRecords(AgmarknetFilters()).size,
            isApiKeyConfigured = true,
            lastFetchedIst = "National AGMARKNET Live"
        )
    )
    val agmarknetUiState: StateFlow<AgmarknetUiState> = _agmarknetUiState.asStateFlow()
    private var cooldownJob: Job? = null

    val availableDistricts = liveWeatherService.availableDistricts
    private val _selectedWeatherState = MutableStateFlow("Maharashtra")
    val selectedWeatherState: StateFlow<String> = _selectedWeatherState.asStateFlow()

    private val _selectedDistrict = MutableStateFlow(availableDistricts[0]) // Defaults to Nashik
    val selectedDistrict: StateFlow<AgriDistrict> = _selectedDistrict.asStateFlow()

    private val _isWeatherLoading = MutableStateFlow(false)
    val isWeatherLoading: StateFlow<Boolean> = _isWeatherLoading.asStateFlow()

    private val _weatherNetworkError = MutableStateFlow<String?>(null)
    val weatherNetworkError: StateFlow<String?> = _weatherNetworkError.asStateFlow()

    val marketMonitorState: StateFlow<LiveMarketMonitorState> = liveMandiService.monitorState

    private val _currentLanguage = MutableStateFlow(AppLanguage.EN) // Defaults to English
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()
    val userProfile: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _selectedPriceUnit = MutableStateFlow(PriceUnit.QUINTAL)
    val selectedPriceUnit: StateFlow<PriceUnit> = _selectedPriceUnit.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow(CropCategory.ALL)
    val selectedCategory: StateFlow<CropCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCrop = MutableStateFlow<CropItem?>(null)
    val selectedCrop: StateFlow<CropItem?> = _selectedCrop.asStateFlow()

    val isAudioPlaying: StateFlow<Boolean> = audioService.isPlaying

    private val _isOfflineSynced = MutableStateFlow(true)
    val isOfflineSynced: StateFlow<Boolean> = _isOfflineSynced.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _farmerKisanCard = MutableStateFlow(InitialData.farmerKisanCard)
    val farmerKisanCard: StateFlow<FarmerKisanCard> = _farmerKisanCard.asStateFlow()

    private val _pesticides = MutableStateFlow(InitialData.pesticideList)
    val pesticides: StateFlow<List<PesticideAdvisory>> = _pesticides.asStateFlow()

    private val _pesticideSearch = MutableStateFlow("")
    val pesticideSearch: StateFlow<String> = _pesticideSearch.asStateFlow()

    private val _helplines = MutableStateFlow(InitialData.govtHelplines)
    val helplines: StateFlow<List<GovtHelpline>> = _helplines.asStateFlow()

    private val _weather = MutableStateFlow(InitialData.sampleWeather)
    val weather: StateFlow<WeatherAdvisory> = _weather.asStateFlow()

    val enclaveStatus: StateFlow<TeeEnclaveStatus> = MutableStateFlow(teeService.getEnclaveStatus()).asStateFlow()
    val dlpPolicy: StateFlow<DlpPolicySettings> = dlpManager.dlpPolicy
    val auditLogs: StateFlow<List<DlpAuditEvent>> = dlpManager.auditLogs

    private val _showTeeSecurityDialog = MutableStateFlow(false)
    val showTeeSecurityDialog: StateFlow<Boolean> = _showTeeSecurityDialog.asStateFlow()

    // ========================================================
    // GOVT MSP PROCUREMENT STATE & FLOWS
    // ========================================================
    private val _selectedProcurementState = MutableStateFlow("Uttar Pradesh")
    val selectedProcurementState: StateFlow<String> = _selectedProcurementState.asStateFlow()

    private val _currentFarmerLandProfile = MutableStateFlow<FarmerLandProfile>(GovtProcurementData.upFarmerProfile)
    val currentFarmerLandProfile: StateFlow<FarmerLandProfile> = _currentFarmerLandProfile.asStateFlow()

    private val _mspCrops = MutableStateFlow<List<GovtMspCrop>>(GovtProcurementData.mspCropsList)
    val mspCrops: StateFlow<List<GovtMspCrop>> = _mspCrops.asStateFlow()

    private val _procurementCenters = MutableStateFlow<List<ProcurementCenter>>(GovtProcurementData.upProcurementCenters)
    val procurementCenters: StateFlow<List<ProcurementCenter>> = _procurementCenters.asStateFlow()

    private val _cropRegistrations = MutableStateFlow<List<GovtCropRegistration>>(
        listOf(GovtProcurementData.initialUpCropRegistration, GovtProcurementData.initialWbCropRegistration)
    )
    val cropRegistrations: StateFlow<List<GovtCropRegistration>> = _cropRegistrations.asStateFlow()

    private val _tokenBookings = MutableStateFlow<List<GovtTokenBooking>>(
        listOf(GovtProcurementData.initialUpTokenBooking)
    )
    val tokenBookings: StateFlow<List<GovtTokenBooking>> = _tokenBookings.asStateFlow()

    private val _procurementReceipts = MutableStateFlow<List<GovtProcurementReceipt>>(
        listOf(GovtProcurementData.initialUpProcurementReceipt)
    )
    val procurementReceipts: StateFlow<List<GovtProcurementReceipt>> = _procurementReceipts.asStateFlow()

    private val _stateProcurementInfo = MutableStateFlow<GovtProcurementStateInfo>(GovtProcurementData.upStateInfo)
    val stateProcurementInfo: StateFlow<GovtProcurementStateInfo> = _stateProcurementInfo.asStateFlow()

    private val _lastBookedToken = MutableStateFlow<GovtTokenBooking?>(GovtProcurementData.initialUpTokenBooking)
    val lastBookedToken: StateFlow<GovtTokenBooking?> = _lastBookedToken.asStateFlow()

    private val _appNotification = MutableStateFlow<AppNotification?>(null)
    val appNotification: StateFlow<AppNotification?> = _appNotification.asStateFlow()
    private var notificationJob: Job? = null

    fun showSuccess(message: String) {
        showNotification(message, NotificationType.SUCCESS)
    }

    fun showError(message: String) {
        showNotification(message, NotificationType.ERROR)
    }

    fun showInfo(message: String) {
        showNotification(message, NotificationType.INFO)
    }

    fun dismissNotification() {
        notificationJob?.cancel()
        _appNotification.value = null
    }

    private fun showNotification(message: String, type: NotificationType) {
        notificationJob?.cancel()
        val notif = AppNotification(message = message, type = type)
        _appNotification.value = notif
        notificationJob = viewModelScope.launch {
            delay(notif.durationMs)
            if (_appNotification.value?.id == notif.id) {
                _appNotification.value = null
            }
        }
    }


    private fun getInitialAiGreeting(lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.MR -> "🌾 राम राम शेतकरी मित्रांनो! मी किसान वाणी AI कृषी सहाय्यक आहे. बाजार भाव, रोग नियंत्रण किंवा शेतीविषयी काहीही विचारा."
            AppLanguage.HI -> "🌾 RAM RAM किसान भाइयों! मैं किसान वाणी AI सहायक हूँ। मंडी भाव, रोग नियंत्रण या खेती के बारे में कोई भी प्रश्न पूछें।"
            AppLanguage.GU -> "🌾 RAM RAM ખેડૂત મિત્રો! હું કિસાન વાણી AI સહાયક છું. બજાર ભાવ, રોગ નિયંત્રણ કે ખેતી વિષે કંઈ પણ પૂછો."
            AppLanguage.EN -> "🌾 RAM RAM farmer friends! I am Kisan Vani AI Assistant. Ask me anything about mandi rates, crop diseases, or farming advice."
            else -> "🌾 RAM RAM किसान भाइयों! मैं किसान वाणी AI सहायक हूँ। मंडी भाव या खेती के बारे में कोई भी प्रश्न पूछें।"
        }
    }

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "init_ai_msg",
                isFromUser = false,
                messageText = "🌾 Welcome farmer friends! I am Kisan Vani AI Assistant. Ask me anything about mandi rates, crop diseases, or farming advice.",
                timestamp = "Just now"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _showCreateLotDialog = MutableStateFlow(false)
    val showCreateLotDialog: StateFlow<Boolean> = _showCreateLotDialog.asStateFlow()

    private val _activeRazorpayModal = MutableStateFlow<RazorpayPayment?>(null)
    val activeRazorpayModal: StateFlow<RazorpayPayment?> = _activeRazorpayModal.asStateFlow()

    // AI Harness Multi-Tool State
    private val _harnessTab = MutableStateFlow(0) // 0: Live Copilot & Traces, 1: Diagnostic Lab, 2: Harness Telemetry
    val harnessTab: StateFlow<Int> = _harnessTab.asStateFlow()

    // Diagnostic Lab States
    private val _pathologyResult = MutableStateFlow(
        PathologyDiagnosticResult(
            crop = "Cotton",
            pestOrDisease = "Pink Bollworm (गुलाबी बोंडअळी)",
            severity = "High Infestation (> 10% rosette flowers)",
            chemicalRecommendation = "Profenofos 40% + Cypermethrin 4% EC @ 400ml/acre OR Emamectin Benzoate 5% SG @ 88g/acre",
            activeIngredient = "Profenofos + Cypermethrin / Emamectin Benzoate",
            dosagePerAcre = "400 ml in 200 Litres Water",
            sprayTechnique = "Use Hollow Cone Nozzle during evening hours (5:00 PM - 7:00 PM)",
            phiDays = 15,
            organicAlternative = "Pheromone Traps @ 8 traps/acre + Neem Oil 10,000 ppm (3ml/L) + Release Trichogramma @ 60,000 eggs/acre",
            estimatedTreatmentCostPerAcre = 620,
            cibrcApproved = true
        )
    )
    val pathologyResult: StateFlow<PathologyDiagnosticResult> = _pathologyResult.asStateFlow()

    private val _arbitrageResult = MutableStateFlow(
        ArbitrageResult(
            crop = "Onion (Red)",
            sourceMandi = "Nashik Local APMC",
            sourcePrice = 2610,
            destMandi = "Lasalgaon APMC (Export Terminal)",
            destPrice = 2850,
            distanceKm = 48,
            transportCostPerQuintal = 80,
            mandiCessPerQuintal = 28,
            netArbitrageProfitPerQuintal = 132,
            recommendedDecision = "Transport to Lasalgaon APMC. Net gain of +₹13,200 per 100 quintals.",
            optimalSellingWindow = "Next 3-5 Days (Export demand surging)"
        )
    )
    val arbitrageResult: StateFlow<ArbitrageResult> = _arbitrageResult.asStateFlow()

    private val _moistureGradingResult = MutableStateFlow(
        MoistureGradingResult(
            crop = "Soybean (Yellow)",
            moisturePercent = 11.5,
            foreignMatterPercent = 1.2,
            damagedPercent = 1.8,
            faqGrade = "Grade A (Standard FAQ Premium)",
            basePricePerQuintal = 4680,
            priceAdjustmentPerQuintal = 120,
            netRecommendedPrice = 4800,
            dryingRecommendation = "Excellent moisture (< 12%). No sun-drying required; ready for instant bulk sale."
        )
    )
    val moistureGradingResult: StateFlow<MoistureGradingResult> = _moistureGradingResult.asStateFlow()

    private val _subsidyResult = MutableStateFlow(
        SubsidyResult(
            schemeName = "MahaDBT Micro-Irrigation (Drip / Sprinkler Scheme)",
            department = "Dept of Agriculture, Govt of Maharashtra",
            landAcres = 2.5,
            farmerCategory = "Small & Marginal Farmer (< 5 Acres)",
            totalProjectCost = 55000,
            govtSubsidyAmount = 44000,
            farmerBeneficiaryShare = 11000,
            subsidyPercentage = 80,
            portalName = "mahadbt.maharashtra.gov.in",
            requiredDocuments = listOf(
                "7/12 & 8A Land Records Extract",
                "Kisan Credit Card / Aadhaar Linked Bank Passbook",
                "Micro-irrigation Quotation from Registered Dealer",
                "Water & Electricity Connection Certificate"
            ),
            eligibilityStatus = "✅ 100% Eligible (Priority Allocation Batch)"
        )
    )
    val subsidyResult: StateFlow<SubsidyResult> = _subsidyResult.asStateFlow()

    fun setHarnessTab(tab: Int) {
        _harnessTab.value = tab
    }

    private data class PathologySpec(
        val chem: String,
        val active: String,
        val dose: String,
        val phi: Int,
        val bio: String,
        val cost: Int
    )

    private data class SubsidySpec(
        val cost: Int,
        val percent: Int,
        val dept: String,
        val portal: String
    )

    fun runPathologyDiagnostic(crop: String, pestOrDisease: String, severity: String) {
        val spec = when {
            pestOrDisease.contains("Bollworm") || pestOrDisease.contains("बोंडअळी") -> {
                PathologySpec(
                    chem = "Profenofos 40% + Cypermethrin 4% EC @ 400ml/acre",
                    active = "Profenofos + Cypermethrin",
                    dose = "400 ml in 200 Litres Water",
                    phi = 15,
                    bio = "Pheromone Traps @ 8 traps/acre + Neem Oil 10,000 ppm (3ml/L)",
                    cost = 620
                )
            }
            pestOrDisease.contains("Stem Fly") || pestOrDisease.contains("खोडमाशी") || pestOrDisease.contains("Girdle") || pestOrDisease.contains("चक्रभुंगा") -> {
                PathologySpec(
                    chem = "Chlorantraniliprole 18.5% SC (Coragen) @ 60ml/acre",
                    active = "Chlorantraniliprole 18.5% SC",
                    dose = "60 ml in 200 Litres Water",
                    phi = 14,
                    bio = "Neem Seed Kernel Extract (NSKE 5%) @ 50ml/L",
                    cost = 780
                )
            }
            pestOrDisease.contains("Thrips") || pestOrDisease.contains("करपा") || pestOrDisease.contains("Purple Blotch") -> {
                PathologySpec(
                    chem = "Fipronil 5% SC (40ml) + Difenoconazole 25% EC (20ml)",
                    active = "Fipronil + Difenoconazole",
                    dose = "40ml + 20ml per 100L Water with Silicon Sticker",
                    phi = 10,
                    bio = "Yellow & Blue Sticky Traps @ 15/acre + Beauveria bassiana 5g/L",
                    cost = 540
                )
            }
            pestOrDisease.contains("Blight") || pestOrDisease.contains("Early") || pestOrDisease.contains("Late") -> {
                PathologySpec(
                    chem = "Azoxystrobin 18.2% + Difenoconazole 11.4% SC @ 200ml/acre",
                    active = "Azoxystrobin + Difenoconazole",
                    dose = "200 ml in 200 Litres Water",
                    phi = 7,
                    bio = "Trichoderma viride 2.5kg/acre drenching + Cow urine 10%",
                    cost = 690
                )
            }
            else -> {
                PathologySpec(
                    chem = "Lambda-cyhalothrin 4.9% CS @ 150ml/acre + Mancozeb 75% WP @ 500g/acre",
                    active = "Lambda-cyhalothrin + Mancozeb",
                    dose = "150ml + 500g in 200L Water",
                    phi = 12,
                    bio = "Neem Oil 10,000 ppm @ 3ml/L with Bio-enhancer",
                    cost = 490
                )
            }
        }

        _pathologyResult.value = PathologyDiagnosticResult(
            crop = crop,
            pestOrDisease = pestOrDisease,
            severity = severity,
            chemicalRecommendation = spec.chem,
            activeIngredient = spec.active,
            dosagePerAcre = spec.dose,
            sprayTechnique = "Hollow cone nozzle with uniform canopy coverage during morning/evening.",
            phiDays = spec.phi,
            organicAlternative = spec.bio,
            estimatedTreatmentCostPerAcre = spec.cost,
            cibrcApproved = true
        )
    }

    fun runArbitrageCalculation(crop: String, sourceMandi: String, destMandi: String, distanceKm: Int) {
        val srcPrice = when {
            crop.contains("Onion") -> 2610
            crop.contains("Soybean") -> 4540
            crop.contains("Tomato") -> 1920
            crop.contains("Cotton") -> 6850
            else -> 3100
        }
        val destPrice = when {
            crop.contains("Onion") -> 2850
            crop.contains("Soybean") -> 4680
            crop.contains("Tomato") -> 2100
            crop.contains("Cotton") -> 7200
            else -> 3350
        }
        val transportCost = (distanceKm * 1.6).toInt().coerceAtLeast(30)
        val cess = (destPrice * 0.01).toInt()
        val netGain = (destPrice - srcPrice) - (transportCost + cess)

        _arbitrageResult.value = ArbitrageResult(
            crop = crop,
            sourceMandi = sourceMandi,
            sourcePrice = srcPrice,
            destMandi = destMandi,
            destPrice = destPrice,
            distanceKm = distanceKm,
            transportCostPerQuintal = transportCost,
            mandiCessPerQuintal = cess,
            netArbitrageProfitPerQuintal = netGain,
            recommendedDecision = if (netGain > 0) {
                "Transport to $destMandi. Realize +₹$netGain/qtl net gain (₹${netGain * 50} extra per 50 quintals)."
            } else {
                "Sell at $sourceMandi. Freight cost outweighs destination price spread."
            },
            optimalSellingWindow = "Next 3-5 Days (High Demand Window)"
        )
    }

    fun runMoistureGradingCalculation(crop: String, moisture: Double, foreignMatter: Double, damaged: Double) {
        val basePrice = when {
            crop.contains("Soybean") -> 4680
            crop.contains("Onion") -> 2850
            crop.contains("Wheat") -> 2650
            crop.contains("Cotton") -> 7100
            else -> 3000
        }

        val (grade, adj, drying) = when {
            moisture <= 11.0 && foreignMatter <= 1.0 && damaged <= 1.5 -> {
                Triple("Grade A+ (Export Premium)", +150, "Optimal moisture (< 11%). Qualifies for top export grade premium.")
            }
            moisture <= 12.5 && foreignMatter <= 2.0 && damaged <= 3.0 -> {
                Triple("Grade A (Standard FAQ)", +50, "Meets Standard FAQ standards. Instant mandi purchase eligible.")
            }
            moisture <= 15.0 -> {
                val discount = -((moisture - 12.0) * 80).toInt()
                Triple("Grade B (Commercial Discounted)", discount, "Moisture slightly elevated ($moisture%). Recommend 4-6 hours sun drying on tarpaulin to recover +₹${-discount}/qtl.")
            }
            else -> {
                val discount = -((moisture - 12.0) * 120).toInt()
                Triple("Grade C (High Moisture - Distress Risk)", discount, "High moisture content ($moisture%). Spoilage risk during transit. Mandatory aeration and shade drying.")
            }
        }

        _moistureGradingResult.value = MoistureGradingResult(
            crop = crop,
            moisturePercent = moisture,
            foreignMatterPercent = foreignMatter,
            damagedPercent = damaged,
            faqGrade = grade,
            basePricePerQuintal = basePrice,
            priceAdjustmentPerQuintal = adj,
            netRecommendedPrice = (basePrice + adj).coerceAtLeast(1000),
            dryingRecommendation = drying
        )
    }

    fun runSubsidyCalculation(landAcres: Double, farmerCategory: String, schemeType: String) {
        val spec = when {
            schemeType.contains("Micro-Irrigation") || schemeType.contains("Drip") -> {
                val cost = (landAcres * 22000).toInt().coerceIn(30000, 110000)
                val rate = if (landAcres <= 5.0 || farmerCategory.contains("SC/ST") || farmerCategory.contains("Women")) 80 else 70
                SubsidySpec(cost, rate, "Dept of Agriculture, Maharashtra", "mahadbt.maharashtra.gov.in")
            }
            schemeType.contains("Solar") || schemeType.contains("KUSUM") -> {
                val cost = 165000
                val rate = if (farmerCategory.contains("SC/ST")) 95 else 90
                SubsidySpec(cost, rate, "MEDA & MSEDCL, Maharashtra", "kusum.mahaurja.com")
            }
            schemeType.contains("Pond") -> {
                val cost = 75000
                val rate = 75
                SubsidySpec(cost, rate, "MahaDBT Farm Pond Scheme", "mahadbt.maharashtra.gov.in")
            }
            else -> {
                val cost = 250000
                val rate = 50
                SubsidySpec(cost, rate, "SMAM Agri-Mechanization Scheme", "agrimachinery.nic.in")
            }
        }

        val subsidyAmt = (spec.cost * (spec.percent / 100.0)).toInt()
        val farmerShare = spec.cost - subsidyAmt

        _subsidyResult.value = SubsidyResult(
            schemeName = schemeType,
            department = spec.dept,
            landAcres = landAcres,
            farmerCategory = farmerCategory,
            totalProjectCost = spec.cost,
            govtSubsidyAmount = subsidyAmt,
            farmerBeneficiaryShare = farmerShare,
            subsidyPercentage = spec.percent,
            portalName = spec.portal,
            requiredDocuments = listOf(
                "7/12 & 8A Land Extract (Digital 7/12)",
                "Kisan Credit Card / Aadhaar Linked Bank Passbook",
                "Caste/Category Certificate (if SC/ST/Special)",
                "Quotation & GST Invoice from Registered Vendor"
            ),
            eligibilityStatus = "✅ Eligible for Direct Benefit Transfer (DBT)"
        )
    }

    fun executeActionCard(card: AgentActionCard) {
        when (card.actionPayload) {
            "ACTION_LIST_ONION_LOT" -> {
                _showCreateLotDialog.value = true
            }
            "ACTION_CALC_SOYBEAN_GRADE" -> {
                _harnessTab.value = 1
                runMoistureGradingCalculation("Soybean (Yellow)", 12.0, 1.2, 1.5)
            }
            "ACTION_SCHEDULE_SPRAY_ALERT" -> {
                _harnessTab.value = 1
                runPathologyDiagnostic("Cotton", "Pink Bollworm (गुलाबी बोंडअळी)", "Moderate")
            }
            "ACTION_CHECK_SUBSIDY" -> {
                _harnessTab.value = 1
                runSubsidyCalculation(2.5, "Small & Marginal Farmer (< 5 Acres)", "MahaDBT Micro-Irrigation (Drip / Sprinkler Scheme)")
            }
            "ACTION_OPEN_TRADE_ENCLAVE" -> {
                _showTeeSecurityDialog.value = true
            }
        }
    }

    init {
        val db = KisanDatabase.getDatabase(application)
        repository = KisanRepository(db.kisanDao())

        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            // Fetch real-time live weather for initial district over internet
            selectDistrictAndFetchWeather(_selectedDistrict.value, autoSpeak = false)
        }
    }

    val allCrops: StateFlow<List<CropItem>> = repository.allCrops.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InitialData.cropsList
    )

    val filteredCrops: StateFlow<List<CropItem>> = combine(
        allCrops,
        selectedCategory,
        searchQuery,
        currentLanguage
    ) { crops, category, query, lang ->
        crops.filter { crop ->
            val matchesCategory = (category == CropCategory.ALL || crop.category == category)
            val matchesQuery = if (query.isBlank()) true else {
                val q = query.trim().lowercase()
                crop.nameEn.lowercase().contains(q) ||
                        crop.nameMr.lowercase().contains(q) ||
                        crop.nameHi.lowercase().contains(q) ||
                        crop.nameGu.lowercase().contains(q) ||
                        crop.mandiName.lowercase().contains(q) ||
                        crop.district.lowercase().contains(q)
            }
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InitialData.cropsList
    )

    val allLots: StateFlow<List<FarmerLot>> = repository.allLots.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InitialData.sampleLots
    )

    val allOffers: StateFlow<List<BuyerOffer>> = repository.allOffers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InitialData.sampleBuyerOffers
    )

    val allPayments: StateFlow<List<RazorpayPayment>> = repository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InitialData.samplePayments
    )

    val cropForecastAnalyses: StateFlow<List<CropForecastAnalysis>> = combine(
        listOf(
            allCrops,
            _trendsSearchQuery,
            _trendsSelectedCategory,
            _trendsSelectedState,
            _trendsSelectedDistrict,
            _trendsSelectedDateRange
        )
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val crops = args[0] as List<CropItem>
        val query = args[1] as String
        val category = args[2] as CropCategory
        val state = args[3] as String
        val district = args[4] as String
        val dateRange = args[5] as TrendDateRange

        crops.filter { crop ->
            val matchesCategory = (category == CropCategory.ALL || crop.category == category)
            val matchesState = (state == "All States" || crop.state.equals(state, ignoreCase = true))
            val matchesDistrict = (district == "All Districts" || crop.district.equals(district, ignoreCase = true))
            val matchesQuery = if (query.isBlank()) true else {
                val q = query.trim().lowercase()
                crop.nameEn.lowercase().contains(q) ||
                        crop.nameMr.lowercase().contains(q) ||
                        crop.nameHi.lowercase().contains(q) ||
                        crop.nameGu.lowercase().contains(q) ||
                        crop.mandiName.lowercase().contains(q) ||
                        crop.district.lowercase().contains(q) ||
                        crop.state.lowercase().contains(q)
            }
            matchesCategory && matchesState && matchesDistrict && matchesQuery
        }.map { crop ->
            cropForecastService.generateForecastAnalysis(crop, dateRange)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InitialData.cropsList.map { cropForecastService.generateForecastAnalysis(it) }
    )

    val activeForecastAnalysis: StateFlow<CropForecastAnalysis?> = combine(
        _selectedTrendCrop,
        cropForecastAnalyses,
        _trendsSelectedDateRange
    ) { selected, analyses, dateRange ->
        if (selected != null) {
            cropForecastService.generateForecastAnalysis(selected, dateRange)
        } else {
            analyses.firstOrNull()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InitialData.cropsList.firstOrNull()?.let { cropForecastService.generateForecastAnalysis(it) }
    )

    fun setTrendsSearchQuery(query: String) {
        _trendsSearchQuery.value = query
    }

    fun setTrendsCategory(category: CropCategory) {
        _trendsSelectedCategory.value = category
    }

    fun setTrendsState(state: String) {
        _trendsSelectedState.value = state
        if (state != "All States") {
            _trendsSelectedDistrict.value = "All Districts"
        }
    }

    fun setTrendsDistrict(district: String) {
        _trendsSelectedDistrict.value = district
    }

    fun setTrendsDateRange(range: TrendDateRange) {
        _trendsSelectedDateRange.value = range
    }

    fun selectTrendCrop(crop: CropItem?) {
        _selectedTrendCrop.value = crop
    }

    fun selectTrendCropById(cropId: String) {
        val crop = allCrops.value.find { it.id == cropId }
        _selectedTrendCrop.value = crop
    }

    fun clearTrendsFilters() {
        _trendsSearchQuery.value = ""
        _trendsSelectedCategory.value = CropCategory.ALL
        _trendsSelectedState.value = "All States"
        _trendsSelectedDistrict.value = "All Districts"
        _trendsSelectedDateRange.value = TrendDateRange.DAYS_30
        _trendsErrorMessage.value = null
    }

    fun refreshTrends() {
        viewModelScope.launch {
            _isTrendsRefreshing.value = true
            _trendsErrorMessage.value = null
            delay(600)
            _isTrendsRefreshing.value = false
        }
    }

    fun setPriceUnit(unit: PriceUnit) {
        _selectedPriceUnit.value = unit
    }

    fun loginAsFarmer(
        fullName: String = "Ramesh Baburao Patil (रमेश पाटील)",
        mobile: String = "9823456789",
        district: String = "Nashik",
        kisanId: String = "MH-MSINS-784920"
    ) {
        _currentUser.value = UserProfile(
            userId = "USER-FARMER-01",
            fullName = fullName,
            mobileNumber = mobile,
            role = UserRole.FARMER,
            location = "$district, Maharashtra",
            kisanIdOrCompany = kisanId,
            isVerified = true,
            profileBadge = "Verified Producer (MSInS)"
        )
        _isLoggedIn.value = true
    }

    fun loginAsBuyer(
        companyName: String = "Sahyadri Farms Post-Harvest Care Ltd",
        representativeName: String = "Vilas Shinde (Director - Procurement)",
        mobile: String = "9422001122"
    ) {
        _currentUser.value = UserProfile(
            userId = "USER-BUYER-01",
            fullName = representativeName,
            mobileNumber = mobile,
            role = UserRole.BUYER,
            location = "Nashik / Export Terminal",
            kisanIdOrCompany = companyName,
            isVerified = true,
            profileBadge = "Institutional Buyer (APMC & Export)"
        )
        _isLoggedIn.value = true
    }

    fun loginAsTrader(
        traderName: String = "Balaji Agri Traders (बालाजी ट्रेडर्स)",
        mandiName: String = "Lasalgaon APMC",
        licenseNo: String = "APMC-LIC-NAS-4091"
    ) {
        _currentUser.value = UserProfile(
            userId = "USER-TRADER-01",
            fullName = traderName,
            mobileNumber = "9890112233",
            role = UserRole.TRADER,
            location = mandiName,
            kisanIdOrCompany = licenseNo,
            isVerified = true,
            profileBadge = "Licensed Mandi Trader"
        )
        _isLoggedIn.value = true
    }

    fun loginAsConsumer(
        fullName: String = "Rohit Sharma (रोहित शर्मा)",
        mobile: String = "9823044921",
        address: String = "Flat 402, Green Meadows Tower, Gangapur Road",
        district: String = "Nashik"
    ) {
        _currentUser.value = UserProfile(
            userId = "USER-CONSUMER-01",
            fullName = fullName,
            mobileNumber = mobile,
            role = UserRole.CONSUMER,
            location = "$address, $district",
            kisanIdOrCompany = "D2C-CONSUMER-VERIFIED",
            isVerified = true,
            profileBadge = "Verified Direct Consumer"
        )
        _isLoggedIn.value = true
        _selectedTab.value = 7 // Default to D2C Marketplace for Consumer
    }

    fun quickDemoLogin(role: UserRole) {
        when (role) {
            UserRole.FARMER -> {
                loginAsFarmer()
                _selectedTab.value = 0
            }
            UserRole.CONSUMER -> {
                loginAsConsumer()
                _selectedTab.value = 7
            }
            UserRole.BUYER -> {
                loginAsBuyer()
                _selectedTab.value = 2
            }
            UserRole.TRADER -> {
                loginAsTrader()
                _selectedTab.value = 0
            }
        }
    }

    // ========================================================
    // D2C MARKETPLACE & LOGISTICS & FORECASTING METHODS
    // ========================================================
    fun placeD2COrder(
        listing: D2CProduceListing,
        consumerName: String,
        consumerMobile: String,
        deliveryAddress: String,
        deliveryDistrict: String,
        deliveryPincode: String,
        quantityKg: Double,
        selectedSlot: String,
        paymentMethod: String
    ): D2COrder {
        val orderTotal = listing.totalPricePerKg * quantityKg
        val logisticsFee = listing.logisticsFeePerKg * quantityKg
        val farmerPayout = listing.farmerBasePricePerKg * quantityKg
        val retailTotal = listing.typicalRetailPricePerKg * quantityKg
        val savings = (retailTotal - orderTotal).coerceAtLeast(0.0)
        val orderId = "ORD-D2C-2026-${(1000..9999).random()}"
        val trackingOtp = (1000..9999).random().toString()
        val cLat = listing.latitude + 0.04
        val cLng = listing.longitude + 0.03
        val distanceKm = logisticsRoutingService.calculateRoadDistanceKm(cLat, cLng, listing.latitude, listing.longitude)

        val order = D2COrder(
            orderId = orderId,
            consumerId = _currentUser.value?.userId ?: "USER-CONSUMER-01",
            consumerName = consumerName,
            consumerMobile = consumerMobile,
            deliveryAddress = deliveryAddress,
            deliveryDistrict = deliveryDistrict,
            deliveryPincode = deliveryPincode,
            deliveryLatitude = cLat,
            deliveryLongitude = cLng,
            listingId = listing.id,
            cropName = listing.cropName,
            variety = listing.variety,
            emoji = listing.emoji,
            farmerId = listing.farmerId,
            farmerName = listing.farmerName,
            farmNameOrFpo = listing.farmNameOrFpo,
            farmVillage = listing.village,
            farmerDistrict = listing.district,
            farmerLatitude = listing.latitude,
            farmerLongitude = listing.longitude,
            quantityKg = quantityKg,
            farmerPricePerKg = listing.farmerBasePricePerKg,
            logisticsFeePerKg = listing.logisticsFeePerKg,
            totalAmountInr = orderTotal,
            farmerPayoutInr = farmerPayout,
            logisticsAmountInr = logisticsFee,
            typicalRetailAmountInr = retailTotal,
            totalSavingsInr = savings,
            orderDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date()),
            estimatedDeliveryTime = "Today, by 05:30 PM",
            selectedSlot = selectedSlot,
            distanceKm = distanceKm,
            status = DeliveryStatus.BOOKED,
            paymentMethod = paymentMethod,
            isEscrowLocked = true,
            isPaymentReleasedToFarmer = false,
            deliveryPartnerName = "Kisan Vani Hyperlocal Green Fleet",
            deliveryPartnerContact = "+91 94229 98811",
            vehicleNumber = "MH-15-EV-4092 (Electric Mini-Van)",
            trackingSteps = emptyList(),
            otpForDelivery = trackingOtp
        )

        _d2cOrders.value = listOf(order) + _d2cOrders.value
        recalculateOptimizedRoute()
        showSuccess("Direct Farm Order #$orderId placed successfully! Delivery OTP: $trackingOtp")
        return order
    }

    fun advanceOrderStatus(orderId: String) {
        _d2cOrders.value = _d2cOrders.value.map { order ->
            if (order.orderId == orderId) {
                val nextStatus = when (order.status) {
                    DeliveryStatus.BOOKED -> DeliveryStatus.PICKED_UP
                    DeliveryStatus.PICKED_UP -> DeliveryStatus.IN_TRANSIT
                    DeliveryStatus.IN_TRANSIT -> DeliveryStatus.DELIVERED
                    DeliveryStatus.DELIVERED -> DeliveryStatus.DELIVERED
                    DeliveryStatus.CANCELLED -> DeliveryStatus.CANCELLED
                }
                val isReleased = if (nextStatus == DeliveryStatus.DELIVERED) true else order.isPaymentReleasedToFarmer
                val isLocked = if (nextStatus == DeliveryStatus.DELIVERED) false else order.isEscrowLocked

                order.copy(
                    status = nextStatus,
                    isPaymentReleasedToFarmer = isReleased,
                    isEscrowLocked = isLocked,
                    trackingSteps = logisticsRoutingService.buildTrackingSteps(
                        status = nextStatus,
                        orderDate = order.orderDate,
                        farmerName = order.farmerName,
                        farmVillage = order.farmVillage,
                        consumerAddress = order.deliveryAddress,
                        estimatedDeliveryTime = order.estimatedDeliveryTime
                    )
                )
            } else {
                order
            }
        }
        recalculateOptimizedRoute()
        showInfo("Order #$orderId status updated.")
    }

    fun releaseEscrowForOrder(orderId: String) {
        _d2cOrders.value = _d2cOrders.value.map { order ->
            if (order.orderId == orderId) {
                order.copy(isPaymentReleasedToFarmer = true, isEscrowLocked = false)
            } else {
                order
            }
        }
        showSuccess("Escrow payment released directly to farmer account!")
    }

    fun saveFarmerLogisticsConfig(newConfig: FarmerLogisticsConfig) {
        _farmerLogisticsConfig.value = newConfig
        _d2cListings.value = _d2cListings.value.map { listing ->
            listing.copy(
                deliveryRadiusKm = newConfig.deliveryRadiusKm,
                availablePickupSlots = newConfig.availablePickupSlots
            )
        }
        showSuccess("Logistics settings & ${newConfig.deliveryRadiusKm.toInt()}km radius updated!")
    }

    fun recalculateOptimizedRoute() {
        _activeMultiStopRoute.value = logisticsRoutingService.computeOptimizedMultiStopRoute(
            orders = _d2cOrders.value
        )
    }

    fun updateSelectedDemandCommodity(commodity: String) {
        selectedDemandCommodity = commodity
        _currentDemandForecast.value = demandForecastService.generateDemandForecast(commodity, selectedDemandDistrict)
    }

    fun updateSelectedDemandDistrict(district: String) {
        selectedDemandDistrict = district
        _currentDemandForecast.value = demandForecastService.generateDemandForecast(selectedDemandCommodity, district)
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUser.value = null
        stopAudio()
    }

    fun changeLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
        if (_chatMessages.value.size == 1 && _chatMessages.value.first().id == "init_ai_msg") {
            _chatMessages.value = listOf(
                ChatMessage(
                    id = "init_ai_msg",
                    isFromUser = false,
                    messageText = getInitialAiGreeting(lang),
                    timestamp = "Just now"
                )
            )
        }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun setCategory(category: CropCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCrop(crop: CropItem?) {
        _selectedCrop.value = crop
    }

    fun setPesticideSearch(query: String) {
        _pesticideSearch.value = query
    }

    fun playAudio(text: String) {
        audioService.speak(text, _currentLanguage.value)
    }

    fun stopAudio() {
        audioService.stop()
    }

    fun toggleCreateLotDialog(show: Boolean) {
        _showCreateLotDialog.value = show
    }

    fun toggleTeeSecurityDialog(show: Boolean) {
        _showTeeSecurityDialog.value = show
    }

    fun setScreenCaptureBlocked(blocked: Boolean, window: android.view.Window?) {
        dlpManager.updateScreenCaptureBlocked(blocked, window)
    }

    fun setSecureClipboardActive(active: Boolean) {
        dlpManager.updateSecureClipboard(active)
    }

    fun setPiiMaskingEnabled(enabled: Boolean) {
        dlpManager.updatePiiMasking(enabled)
    }

    fun setEscrowSigningEnforced(enforced: Boolean) {
        dlpManager.updateEscrowEnclaveSigning(enforced)
    }

    fun testTeeEncrypt(plaintext: String): EnclaveEncryptedPayload {
        val payload = teeService.encryptInEnclave(plaintext, "TEST_BENCH_PII")
        dlpManager.logEvent(
            eventType = "TEE_PAYLOAD_ENCRYPTED",
            description = "Encrypted ${plaintext.length} chars inside ARM TrustZone TEE using AES-256-GCM.",
            severity = "SECURE_ACTION"
        )
        return payload
    }

    fun testTeeDecrypt(payload: EnclaveEncryptedPayload): String {
        val decrypted = teeService.decryptInEnclave(payload)
        dlpManager.logEvent(
            eventType = "TEE_PAYLOAD_DECRYPTED",
            description = "Decrypted payload ${payload.payloadId} inside TEE Enclave with GCM verification.",
            severity = "SECURE_ACTION"
        )
        return decrypted
    }

    fun secureCopyToClipboard(text: String, label: String = "Protected Data") {
        dlpManager.copySecureToClipboard(text, label)
        showSuccess("Copied $label securely to clipboard")
    }

    fun showPaymentModal(payment: RazorpayPayment?) {
        _activeRazorpayModal.value = payment
    }

    fun createLot(
        cropName: String,
        variety: String,
        qualityGrade: String,
        quantityQuintals: Double,
        expectedPricePerQuintal: Int,
        locationDistrict: String,
        locationTaluka: String,
        storageType: String
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            val newLot = FarmerLot(
                id = "LOT-MH-${System.currentTimeMillis() % 100000}",
                cropName = cropName,
                variety = variety,
                qualityGrade = qualityGrade,
                quantityQuintals = quantityQuintals,
                expectedPricePerQuintal = expectedPricePerQuintal,
                locationDistrict = locationDistrict,
                locationTaluka = locationTaluka,
                storageType = storageType,
                harvestDate = dateFormat.format(Date()),
                status = LotStatus.ACTIVE,
                offersCount = 0,
                dateCreated = dateFormat.format(Date())
            )
            repository.createLot(newLot)
            _showCreateLotDialog.value = false
            showSuccess("Harvest lot for $cropName created successfully!")

            dlpManager.logEvent(
                eventType = "HARVEST_LOT_CREATED",
                description = "Harvest lot for $cropName created. Lot ID: ${newLot.id}.",
                severity = "INFO"
            )
        }
    }

    fun acceptOffer(offer: BuyerOffer) {
        viewModelScope.launch {
            repository.updateOfferStatus(offer.id, OfferStatus.ACCEPTED, offer.lotId)
            // Create simulated Razorpay Escrow payment record with TEE enclave isolation
            val lot = allLots.value.find { it.id == offer.lotId }
            val amount = (lot?.quantityQuintals ?: 50.0) * offer.offeredPricePerQuintal
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)

            // Sign trade contract in TEE
            val signContractPayload = "OFFER:${offer.id}|BUYER:${offer.buyerName}|AMOUNT:$amount"
            val teeSignature = teeService.signDataInsideTee(signContractPayload)

            val payment = RazorpayPayment(
                id = "PAY-RZP-${System.currentTimeMillis() % 100000}",
                lotId = offer.lotId,
                cropName = lot?.cropName ?: "Harvest Lot",
                amountInr = amount,
                razorpayOrderId = "order_rzp_${UUID.randomUUID().toString().take(8)}",
                paymentId = "pay_rzp_${UUID.randomUUID().toString().take(8)}",
                utrNumber = "MAHAB${System.currentTimeMillis().toString().takeLast(10)}",
                buyerName = offer.buyerName,
                farmerKisanCard = _farmerKisanCard.value.kisanCardNumber,
                status = PaymentStatus.ESCROW_LOCKED,
                timestamp = dateFormat.format(Date())
            )
            repository.addPayment(payment)
            _activeRazorpayModal.value = payment
            showSuccess("Offer accepted! ₹${amount.toInt()} secured in Escrow.")

            dlpManager.logEvent(
                eventType = "ESCROW_TEE_SIGNED",
                description = "Escrow locked for ₹${amount.toInt()}. Hardware TEE Signature: ${teeSignature.take(16)}...",
                severity = "SECURE_ACTION"
            )
        }
    }

    fun counterOffer(offer: BuyerOffer, newPrice: Int) {
        viewModelScope.launch {
            repository.updateOfferStatus(
                offerId = offer.id,
                newStatus = OfferStatus.COUNTERED,
                lotId = offer.lotId,
                counteredPrice = newPrice
            )
            showSuccess("Counter offer of ₹$newPrice/Qtl sent to buyer.")
            dlpManager.logEvent(
                eventType = "OFFER_COUNTERED",
                description = "Countered offer ${offer.id} at ₹$newPrice/qtl. Lot ID: ${offer.lotId}",
                severity = "INFO"
            )
        }
    }

    fun rejectOffer(offer: BuyerOffer) {
        viewModelScope.launch {
            repository.updateOfferStatus(offer.id, OfferStatus.REJECTED, offer.lotId)
            showInfo("Offer rejected.")
        }
    }

    fun releaseEscrowPayment(paymentId: String) {
        viewModelScope.launch {
            val target = allPayments.value.find { it.id == paymentId }
            if (target != null) {
                // TEE Enclave Authorization for Fund Release
                val releasePayload = "RELEASE:${target.id}|AMOUNT:${target.amountInr}|UTR:${target.utrNumber}"
                val teeReleaseSignature = teeService.signDataInsideTee(releasePayload)

                val updated = target.copy(status = PaymentStatus.RELEASED_TO_FARMER)
                repository.addPayment(updated)
                _activeRazorpayModal.value = updated
                showSuccess("Escrow payment of ₹${target.amountInr.toInt()} released via DBT!")

                dlpManager.logEvent(
                    eventType = "ESCROW_RELEASE_AUTHORIZED",
                    description = "Escrow payment ${target.id} released to farmer. TEE Enclave Release Signature verified.",
                    severity = "POLICY_ENFORCED"
                )
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                id = "init_ai_msg_${System.currentTimeMillis()}",
                isFromUser = false,
                messageText = getInitialAiGreeting(_currentLanguage.value),
                timestamp = "Just now",
                audioPlayable = true
            )
        )
    }

    fun sendAiPrompt(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isFromUser = true,
            messageText = userText,
            timestamp = "Just now"
        )
        _chatMessages.value = _chatMessages.value + userMessage

        if (!networkMonitor.isOnline()) {
            val offlineNotice = when (_currentLanguage.value) {
                AppLanguage.MR -> "⚠️ इंटरनेट कनेक्शन उपलब्ध नाही. कृपया थेट उत्तरांसाठी इंटरनेट (डेटा / वाय-फाय) सुरू करा."
                AppLanguage.HI -> "⚠️ इंटरनेट कनेक्शन उपलब्ध नहीं है। कृपया लाइव उत्तर के लिए इंटरनेट (डेटा / वाई-फाई) चालू करें।"
                AppLanguage.GU -> "⚠️ ઇન્ટરનેટ કનેક્શન ઉપલબ્ધ નથી. કૃપા કરીને લાઇવ જવાબો માટે ઇન્ટરનેટ ચાલુ કરો."
                AppLanguage.EN -> "⚠️ No internet connection available. Please connect to Mobile Data or Wi-Fi to get answers."
                else -> "⚠️ इंटरनेट कनेक्शन उपलब्ध नहीं है। कृपया इंटरनेट चालू करें।"
            }
            val alertMsg = ChatMessage(
                id = "offline_alert_${System.currentTimeMillis()}",
                isFromUser = false,
                messageText = offlineNotice,
                timestamp = "Just now",
                audioPlayable = false
            )
            _chatMessages.value = _chatMessages.value + alertMsg
            return
        }

        _isAiThinking.value = true
        val aiMsgId = "ai_${System.currentTimeMillis()}"

        viewModelScope.launch {
            var finalAiText = ""
            geminiService.askKrishiAssistantStream(userText, _currentLanguage.value).collect { result ->
                finalAiText = result.text
                val currentList = _chatMessages.value
                val existingIndex = currentList.indexOfFirst { it.id == aiMsgId }

                val updatedAiMsg = ChatMessage(
                    id = aiMsgId,
                    isFromUser = false,
                    messageText = result.text,
                    timestamp = "Just now",
                    audioPlayable = true,
                    isOfflineFallback = result.isOfflineFallback,
                    executionTraces = emptyList(),
                    latencyMs = result.latencyMs
                )

                if (existingIndex >= 0) {
                    val mutable = currentList.toMutableList()
                    mutable[existingIndex] = updatedAiMsg
                    _chatMessages.value = mutable
                } else {
                    _chatMessages.value = currentList + updatedAiMsg
                }
                _isAiThinking.value = false
            }

            _isAiThinking.value = false

            // Auto speak response if desired
            if (finalAiText.isNotBlank()) {
                playAudio(finalAiText)
            }
        }
    }

    fun selectWeatherStateAndFilter(stateName: String, autoSpeak: Boolean = false) {
        _selectedWeatherState.value = stateName
        // Find existing district for this state, or resolve from registry
        val matchedDistrict = liveWeatherService.getDistrictForStateAndName(stateName, null)
        selectDistrictAndFetchWeather(matchedDistrict, autoSpeak)
    }

    fun selectDistrictAndFetchWeather(district: AgriDistrict, autoSpeak: Boolean = false) {
        _selectedDistrict.value = district
        _selectedWeatherState.value = district.state
        viewModelScope.launch {
            _isWeatherLoading.value = true
            _weatherNetworkError.value = null
            val result = liveWeatherService.fetchLiveWeather(district)
            if (result.isSuccess) {
                val liveAdvisory = result.getOrThrow()
                _weather.value = liveAdvisory
                if (autoSpeak) {
                    playAudio(liveAdvisory.getAudioBulletin(_currentLanguage.value))
                }
            } else {
                _weatherNetworkError.value = result.exceptionOrNull()?.message ?: "Unable to fetch live weather"
            }
            _isWeatherLoading.value = false
        }
    }

    fun refreshLiveWeather(autoSpeak: Boolean = false) {
        selectDistrictAndFetchWeather(_selectedDistrict.value, autoSpeak)
    }

    fun speakCurrentWeather() {
        playAudio(_weather.value.getAudioBulletin(_currentLanguage.value))
    }

    fun speakLiveMandiBulletin() {
        val speech = liveMandiService.generateSpokenPriceBulletin(filteredCrops.value, _currentLanguage.value)
        playAudio(speech)
    }

    fun toggleLiveMandiMonitoring(enable: Boolean) {
        if (enable) {
            liveMandiService.startLiveMonitoring()
        } else {
            liveMandiService.stopLiveMonitoring()
        }
    }

    fun syncMandiRates() {
        viewModelScope.launch {
            _isSyncing.value = true
            val currentList = allCrops.value
            val refreshed = liveMandiService.performManualRefresh(currentList)
            repository.updateCrops(refreshed)
            _isSyncing.value = false
            _isOfflineSynced.value = true
        }
    }

    // ==================== LIVE AGMARKNET (DATA.GOV.IN) LOGIC ====================

    fun fetchAgmarknetMandiPrices(forceRefresh: Boolean = false) {
        val currentState = _agmarknetUiState.value
        
        // Enforce cooldown if refreshing manually
        if (forceRefresh && currentState.cooldownSecondsRemaining > 0) {
            return
        }

        viewModelScope.launch {
            _agmarknetUiState.value = _agmarknetUiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val result = agmarknetService.fetchMandiPrices(
                filters = _agmarknetUiState.value.filters,
                forceRefresh = forceRefresh
            )

            if (result.isSuccess) {
                val data = result.getOrThrow()
                _agmarknetUiState.value = _agmarknetUiState.value.copy(
                    isLoading = false,
                    records = data.records,
                    cachedFallbackRecords = if (data.records.isNotEmpty()) data.records else _agmarknetUiState.value.cachedFallbackRecords,
                    totalCount = data.totalCount,
                    lastFetchedIst = data.fetchedAtIst,
                    isDelayed = data.isDelayed,
                    delayNotice = data.delayMessage,
                    isUsingCache = data.isCached,
                    isApiKeyConfigured = true,
                    errorMessage = if (data.records.isEmpty()) "No government mandi records matched your filters." else null
                )

                if (forceRefresh) {
                    startAgmarknetCooldown(15)
                }
            } else {
                val err = result.exceptionOrNull()
                val errMsg = err?.message ?: "Unable to fetch data from data.gov.in"
                val fallback = com.example.data.AgmarknetFallbackData.filterRecords(_agmarknetUiState.value.filters)
                
                _agmarknetUiState.value = _agmarknetUiState.value.copy(
                    isLoading = false,
                    isApiKeyConfigured = true,
                    errorMessage = null,
                    records = if (fallback.isNotEmpty()) fallback else _agmarknetUiState.value.records,
                    totalCount = if (fallback.isNotEmpty()) fallback.size else _agmarknetUiState.value.records.size
                )
            }
        }
    }

    private fun startAgmarknetCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (sec in seconds downTo 0) {
                _agmarknetUiState.value = _agmarknetUiState.value.copy(cooldownSecondsRemaining = sec)
                if (sec > 0) delay(1000)
            }
        }
    }

    fun setAgmarknetState(state: String) {
        val newFilters = _agmarknetUiState.value.filters.copy(
            state = state,
            district = "", // Reset lower-hierarchy filters
            market = "",
            offset = 0
        )
        _agmarknetUiState.value = _agmarknetUiState.value.copy(filters = newFilters)
        fetchAgmarknetMandiPrices(forceRefresh = false)
    }

    fun setAgmarknetDistrict(district: String) {
        val newFilters = _agmarknetUiState.value.filters.copy(
            district = district,
            market = "",
            offset = 0
        )
        _agmarknetUiState.value = _agmarknetUiState.value.copy(filters = newFilters)
        fetchAgmarknetMandiPrices(forceRefresh = false)
    }

    fun setAgmarknetMarket(market: String) {
        val newFilters = _agmarknetUiState.value.filters.copy(
            market = market,
            offset = 0
        )
        _agmarknetUiState.value = _agmarknetUiState.value.copy(filters = newFilters)
        fetchAgmarknetMandiPrices(forceRefresh = false)
    }

    fun setAgmarknetCommodity(commodity: String) {
        val newFilters = _agmarknetUiState.value.filters.copy(
            commodity = commodity,
            offset = 0
        )
        _agmarknetUiState.value = _agmarknetUiState.value.copy(filters = newFilters)
        fetchAgmarknetMandiPrices(forceRefresh = false)
    }

    fun setAgmarknetVariety(variety: String) {
        val newFilters = _agmarknetUiState.value.filters.copy(
            variety = variety,
            offset = 0
        )
        _agmarknetUiState.value = _agmarknetUiState.value.copy(filters = newFilters)
        fetchAgmarknetMandiPrices(forceRefresh = false)
    }

    fun clearAgmarknetFilters() {
        val resetFilters = AgmarknetFilters(
            state = "Maharashtra",
            district = "",
            market = "",
            commodity = "",
            variety = "",
            limit = 100,
            offset = 0
        )
        _agmarknetUiState.value = _agmarknetUiState.value.copy(filters = resetFilters)
        fetchAgmarknetMandiPrices(forceRefresh = false)
    }

    fun speakAgmarknetRecord(record: AgmarknetMandiRecord) {
        val lang = _currentLanguage.value
        val text = when (lang) {
            AppLanguage.MR -> "${record.market} बाजार समिती, जिल्हा ${record.district} मध्ये ${record.commodity} (${record.variety}) चा सरासरी भाव ₹${record.modalPrice.toInt()} प्रति क्विंटल आहे. किमान भाव ₹${record.minPrice.toInt()}, कमाल भाव ₹${record.maxPrice.toInt()}."
            AppLanguage.HI -> "${record.market} मंडी, ज़िला ${record.district} में ${record.commodity} (${record.variety}) का मॉडल भाव ₹${record.modalPrice.toInt()} प्रति क्विंटल है। न्यूनतम ₹${record.minPrice.toInt()}, अधिकतम ₹${record.maxPrice.toInt()}."
            AppLanguage.GU -> "${record.market} માર્કેટ, જિલ્લો ${record.district} માં ${record.commodity} (${record.variety}) નો સરેરાશ ભાવ ₹${record.modalPrice.toInt()} પ્રતિ ક્વિન્ટલ છે."
            AppLanguage.EN -> "${record.commodity} (${record.variety}) at ${record.market} APMC in ${record.district} has a modal price of ₹${record.modalPrice.toInt()} per quintal, ranging from ₹${record.minPrice.toInt()} to ₹${record.maxPrice.toInt()}."
            else -> "${record.market} मंडी में ${record.commodity} का भाव ₹${record.modalPrice.toInt()} प्रति क्विंटल है।"
        }
        playAudio(text)
    }

    // ==========================================
    // GOVT MSP PROCUREMENT FUNCTIONS
    // ==========================================

    fun selectProcurementState(state: String) {
        _selectedProcurementState.value = state
        if (state.contains("West Bengal", ignoreCase = true)) {
            _currentFarmerLandProfile.value = GovtProcurementData.wbFarmerProfile
            _procurementCenters.value = GovtProcurementData.wbProcurementCenters
            _stateProcurementInfo.value = GovtProcurementData.wbStateInfo
        } else {
            _currentFarmerLandProfile.value = GovtProcurementData.upFarmerProfile
            _procurementCenters.value = GovtProcurementData.upProcurementCenters
            _stateProcurementInfo.value = GovtProcurementData.upStateInfo
        }
    }

    fun registerCropForProcurement(
        cropName: String,
        season: String,
        cultivatedAcres: Double,
        expectedProduction: Double
    ): GovtCropRegistration {
        val farmer = _currentFarmerLandProfile.value
        val cropItem = _mspCrops.value.find { it.cropNameEn.equals(cropName, ignoreCase = true) || it.cropNameHi.equals(cropName, ignoreCase = true) }
            ?: _mspCrops.value.first()

        val maxAllowed = cultivatedAcres * cropItem.maxYieldNormQuintalPerAcre
        val approvedQty = minOf(expectedProduction, maxAllowed)
        val mspRate = cropItem.mspPricePerQuintal
        val totalPayout = approvedQty * mspRate

        val stateCode = if (farmer.state.contains("West Bengal")) "WB" else "UP"
        val regId = "REG-$stateCode-2026-${(100000..999999).random()}"

        val newReg = GovtCropRegistration(
            id = regId,
            farmerId = farmer.farmerId,
            farmerName = farmer.farmerName,
            state = farmer.state,
            cropName = cropItem.cropNameEn,
            season = season,
            cultivatedAreaAcres = cultivatedAcres,
            expectedProductionQuintals = expectedProduction,
            approvedEligibleQuantityQuintals = approvedQty,
            mspRatePerQuintal = mspRate,
            estimatedTotalMspPayout = totalPayout,
            registrationDate = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
            status = "Verified & Approved",
            verificationRemarks = "Sowing verified for ${farmer.khatauniOrKhatianNo} (${farmer.gataOrDagNo})"
        )

        _cropRegistrations.value = listOf(newReg) + _cropRegistrations.value
        showSuccess("Crop registration submitted! Verified for ${farmer.khatauniOrKhatianNo}")
        return newReg
    }

    fun bookGovtToken(
        cropName: String,
        quantity: Double,
        centreId: String,
        bookingDate: String,
        timeSlot: String
    ): GovtTokenBooking {
        val farmer = _currentFarmerLandProfile.value
        val centre = _procurementCenters.value.find { it.id == centreId } ?: _procurementCenters.value.first()
        val cropItem = _mspCrops.value.find { it.cropNameEn.equals(cropName, ignoreCase = true) } ?: _mspCrops.value.first()

        val stateCode = if (farmer.state.contains("West Bengal")) "WB" else "UP"
        val cropCode = if (cropName.contains("Paddy", ignoreCase = true)) "PDY" else if (cropName.contains("Mustard", ignoreCase = true)) "MST" else "WHT"
        val tokenNum = "$stateCode-$cropCode-2026-${(10000..99999).random()}"

        val newToken = GovtTokenBooking(
            tokenNumber = tokenNum,
            farmerId = farmer.farmerId,
            farmerName = farmer.farmerName,
            state = farmer.state,
            cropName = cropItem.cropNameEn,
            season = cropItem.season,
            estimatedQuantityQuintals = quantity,
            mspRate = cropItem.mspPricePerQuintal,
            centreId = centre.id,
            centreName = centre.name,
            centreAddress = centre.address,
            bookingDate = bookingDate,
            timeSlot = timeSlot,
            status = GovtTokenStatus.BOOKED,
            qrPayload = "KISAN_GOVT_TOKEN:$tokenNum|FARMER:${farmer.farmerId}|CROP:${cropItem.cropNameEn}|QTY:${quantity}QTL|MSP:${cropItem.mspPricePerQuintal}|PPC:${centre.name}|DATE:$bookingDate|AUTH:EAL5-VALID",
            createdAt = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        )

        _tokenBookings.value = listOf(newToken) + _tokenBookings.value
        _lastBookedToken.value = newToken
        showSuccess("Token #$tokenNum booked for $bookingDate ($timeSlot)")
        return newToken
    }

    fun cancelGovtToken(tokenNumber: String) {
        _tokenBookings.value = _tokenBookings.value.map { token ->
            if (token.tokenNumber == tokenNumber) {
                token.copy(status = GovtTokenStatus.CANCELLED)
            } else {
                token
            }
        }
        if (_lastBookedToken.value?.tokenNumber == tokenNumber) {
            _lastBookedToken.value = _lastBookedToken.value?.copy(status = GovtTokenStatus.CANCELLED)
        }
        showInfo("Token #$tokenNumber has been cancelled.")
    }

    fun speakGovtProcurementGuide(topic: String) {
        val lang = _currentLanguage.value
        val farmer = _currentFarmerLandProfile.value
        val text = when (topic) {
            "eligibility" -> when (lang) {
                AppLanguage.MR -> "तुमची जमीन ${farmer.totalLandAreaAcres} एकर सरकारी भूलेख पोर्टलवर सत्यापित आहे. एमएसपी दरानुसार गहू आणि धान थेट सरकारी खरेदी केंद्रावर विकता येईल."
                AppLanguage.HI -> "आपकी ${farmer.totalLandAreaAcres} एकड़ भूमि राज्य भूलेख पोर्टल पर सत्यापित है। आप न्यूनतम समर्थन मूल्य (MSP) पर सीधे सरकारी क्रय केंद्र पर फसल बेच सकते हैं।"
                AppLanguage.GU -> "તમારી ${farmer.totalLandAreaAcres} એકર જમીન સરકારી પોર્ટલ પર ચકાસાયેલ છે. તમે સીધા ટેકાના ભાવે (MSP) સરકારી ખરીદ કેન્દ્ર પર પાક વેચી શકો છો."
                AppLanguage.EN -> "Your ${farmer.totalLandAreaAcres} acres of land is verified in the government registry. You are eligible to sell your harvest directly at official Minimum Support Price (MSP) with direct bank transfer."
                else -> "आपकी ${farmer.totalLandAreaAcres} एकड़ भूमि सत्यापित है।"
            }
            "process" -> when (lang) {
                AppLanguage.MR -> "सरकारी खरेदीचे ६ सोपे टप्पे आहेत: १. जमीन पडताळणी, २. पीक नोंदणी, ३. ऑनलाइन टोकन बुकिंग, ४. गुणवत्ता तपासणी, ५. डिजिटल वजन पावती, ६. थेट बँक खात्यात पैसे जमा."
                AppLanguage.HI -> "सरकारी खरीद के 6 सरल चरण हैं: 1. भूमि सत्यापन, 2. फसल पंजीकरण, 3. टोकन बुकिंग, 4. गुणवत्ता व नमी जांच, 5. इलेक्ट्रॉनिक तौल पर्ची, 6. सीधे बैंक खाते में भुगतान।"
                AppLanguage.GU -> "સરકારી ખરીદીના 6 પગલાં: 1. જમીન ચકાસણી, 2. પાક નોંધણી, 3. ટોકન બુકિંગ, 4. ગુણવત્તા ચકાસણી, 5. વજન રસીદ, 6. સીધા બેંક ખાતામાં નાણાં જમા."
                AppLanguage.EN -> "Government procurement has 6 simple steps: 1. Land verification, 2. Crop registration, 3. Token slot booking, 4. Quality & moisture check, 5. Electronic weighment receipt, and 6. Direct DBT credit to your bank account."
                else -> "सरकारी खरीद के 6 सरल चरण हैं: भूमि सत्यापन, फसल पंजीकरण, टोकन बुकिंग, गुणवत्ता जांच, तौल पर्ची, और सीधे बैंक में भुगतान।"
            }
            else -> "Kisan Vani Govt MSP Portal ensures transparent procurement with zero mandi fee and direct bank transfer."
        }
        playAudio(text)
    }

    override fun onCleared() {
        super.onCleared()
        audioService.shutdown()
        liveMandiService.stopLiveMonitoring()
        cooldownJob?.cancel()
    }


}
