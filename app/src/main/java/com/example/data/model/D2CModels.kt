package com.example.data.model

import java.util.UUID

// ==========================================
// DIRECT-TO-CONSUMER (D2C) PRODUCE LISTINGS
// ==========================================

data class D2CProduceListing(
    val id: String = UUID.randomUUID().toString(),
    val farmerId: String,
    val farmerName: String,
    val farmNameOrFpo: String,
    val cropName: String,
    val variety: String,
    val category: CropCategory,
    val emoji: String,
    val harvestFreshness: String,        // e.g. "Harvested Today • 06:00 AM", "Fresh Morning Pluck"
    val availableStockKg: Double,
    val minOrderKg: Double = 1.0,
    val packSizesKg: List<Double> = listOf(1.0, 2.0, 5.0, 10.0, 25.0),
    val farmerBasePricePerKg: Double,    // Farm gate payout to farmer (e.g. ₹32.0)
    val logisticsFeePerKg: Double,       // Cold chain & hyper-local transit (e.g. ₹4.5)
    val typicalRetailPricePerKg: Double, // Supermarket/retail baseline (e.g. ₹55.0)
    val village: String,
    val district: String,
    val state: String = "Maharashtra",
    val latitude: Double,
    val longitude: Double,
    val farmerRating: Double = 4.8,
    val totalOrdersFulfilled: Int = 142,
    val isOrganicCertified: Boolean = true,
    val isFpoVerified: Boolean = true,
    val deliveryRadiusKm: Double = 35.0,
    val availablePickupSlots: List<String> = listOf(
        "07:00 AM - 10:00 AM (Early Harvest Slot)",
        "02:00 PM - 05:00 PM (Afternoon Dispatch Slot)"
    ),
    val imageDescription: String = "Farm fresh harvest batch"
) {
    val totalPricePerKg: Double get() = farmerBasePricePerKg + logisticsFeePerKg
    val savingsPerKg: Double get() = (typicalRetailPricePerKg - totalPricePerKg).coerceAtLeast(0.0)
    val savingsPercent: Double get() = if (typicalRetailPricePerKg > 0) {
        ((savingsPerKg / typicalRetailPricePerKg) * 100.0).coerceIn(0.0, 90.0)
    } else 0.0
    val farmerSharePercent: Double get() = if (totalPricePerKg > 0) {
        ((farmerBasePricePerKg / totalPricePerKg) * 100.0).coerceIn(50.0, 95.0)
    } else 85.0
}

// ==========================================
// D2C CONSUMER ORDERS & TRACKING
// ==========================================

enum class DeliveryStatus(
    val titleEn: String,
    val titleHi: String,
    val titleMr: String,
    val stepIndex: Int,
    val colorHex: Long
) {
    BOOKED("Order Booked", "ऑर्डर बुक हो गया", "ऑर्डर नोंदवला", 0, 0xFF0284C7),
    PICKED_UP("Picked Up from Farm", "खेत से पिकअप संपन्न", "शेतातून माल उचलला", 1, 0xFF7C3AED),
    IN_TRANSIT("In Transit to You", "डिलीवरी रास्ते में है", "वाटेत आहे (प्रवासात)", 2, 0xFFD97706),
    DELIVERED("Delivered Successfully", "सफलतापूर्वक डिलीवर", "यशस्वीरित्या पोहोचवला", 3, 0xFF16A34A),
    CANCELLED("Cancelled", "रद्द किया गया", "रद्द केले", -1, 0xFFDC2626);

    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> titleEn
        AppLanguage.MR -> titleMr
        AppLanguage.HI -> titleHi
        else -> titleEn
    }
}

data class DeliveryTrackingStep(
    val status: DeliveryStatus,
    val title: String,
    val description: String,
    val timestamp: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val location: String
)

data class D2COrder(
    val orderId: String,
    val consumerId: String,
    val consumerName: String,
    val consumerMobile: String,
    val deliveryAddress: String,
    val deliveryDistrict: String,
    val deliveryPincode: String,
    val deliveryLatitude: Double,
    val deliveryLongitude: Double,
    val listingId: String,
    val cropName: String,
    val variety: String,
    val emoji: String,
    val farmerId: String,
    val farmerName: String,
    val farmNameOrFpo: String,
    val farmVillage: String,
    val farmerDistrict: String,
    val farmerLatitude: Double,
    val farmerLongitude: Double,
    val quantityKg: Double,
    val farmerPricePerKg: Double,
    val logisticsFeePerKg: Double,
    val totalAmountInr: Double,
    val farmerPayoutInr: Double,
    val logisticsAmountInr: Double,
    val typicalRetailAmountInr: Double,
    val totalSavingsInr: Double,
    val orderDate: String,
    val estimatedDeliveryTime: String,
    val selectedSlot: String,
    val distanceKm: Double,
    val status: DeliveryStatus = DeliveryStatus.BOOKED,
    val paymentMethod: String = "Pay on Delivery (Escrow Guarantee)",
    val isEscrowLocked: Boolean = true,
    val isPaymentReleasedToFarmer: Boolean = false,
    val deliveryPartnerName: String = "Kisan Vani Hyperlocal Green Fleet",
    val deliveryPartnerContact: String = "+91 98234 51290",
    val vehicleNumber: String = "MH-15-EV-4092 (Electric Mini-Van)",
    val trackingSteps: List<DeliveryTrackingStep> = emptyList(),
    val otpForDelivery: String = "4892"
)

// ==========================================
// FARMER / FPO LOGISTICS CONFIGURATION
// ==========================================

data class FarmerLogisticsConfig(
    val farmerId: String,
    val farmerName: String,
    val fpoName: String,
    val farmLatitude: Double = 19.9975,
    val farmLongitude: Double = 73.7898,
    val district: String = "Nashik",
    val deliveryRadiusKm: Double = 35.0,
    val maxDailyCapacityKg: Double = 500.0,
    val isSelfDeliveryEnabled: Boolean = true,
    val isFpoAggregatedLogistics: Boolean = true,
    val availablePickupSlots: List<String> = listOf(
        "06:30 AM - 09:30 AM (Morning Fresh Run)",
        "02:00 PM - 05:00 PM (Afternoon Express Run)"
    )
)

// ==========================================
// MULTI-STOP ROUTE OPTIMIZATION MODELS
// ==========================================

enum class RouteStopType(val label: String, val badge: String) {
    PICKUP("Farm Pickup", "🌾 PICKUP"),
    DELIVERY("Consumer Drop-off", "🏠 DELIVERY")
}

data class RouteStop(
    val stopSequence: Int,
    val stopType: RouteStopType,
    val partyName: String,
    val contactNumber: String,
    val address: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val cropItem: String,
    val quantityKg: Double,
    val etaTime: String,
    val cumulativeDistanceKm: Double,
    val currentVehiclePayloadKg: Double,
    val isCompleted: Boolean = false,
    val orderId: String? = null
)

data class LogisticsMultiStopRoute(
    val routeId: String = "ROUTE-${UUID.randomUUID().toString().take(6).uppercase()}",
    val routeDate: String,
    val vehicleId: String = "MH-15-EV-4092",
    val driverName: String = "Sunil Shinde (Green Fleet Partner)",
    val driverMobile: String = "+91 98220 18493",
    val totalPickups: Int,
    val totalDeliveries: Int,
    val totalCargoKg: Double,
    val totalDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val fuelOrPowerCostInr: Double,
    val co2SavedKg: Double,
    val stops: List<RouteStop>,
    val routingAlgorithm: String = "AI 2-Opt Vehicle Routing Optimization (VRP)"
)

// ==========================================
// AI DEMAND FORECASTING MODELS
// ==========================================

enum class DemandTrend(
    val labelEn: String,
    val labelHi: String,
    val labelMr: String,
    val iconEmoji: String,
    val colorHex: Long
) {
    SURGING_HIGH("Surging Demand (High Deficit)", "मांग में भारी उछाल", "वाढती मागणी (तुटवडा)", "🔥", 0xFFDC2626),
    STEADY_GROWTH("Steady Growth (+10-20%)", "निरंतर वृद्धि", "स्थिर वाढ", "📈", 0xFF16A34A),
    BALANCED("Balanced Supply & Demand", "संतुलित मांग-आपूर्ति", "संतुलित पुरवठा", "⚖️", 0xFF0284C7),
    SEASONAL_DIP("Seasonal Transition Dip", "मौसमी गिरावट", "हंगामी घट", "📉", 0xFFD97706);

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> labelEn
        AppLanguage.MR -> labelMr
        AppLanguage.HI -> labelHi
        else -> labelEn
    }
}

data class DemandVolumeByWeek(
    val weekNumber: Int,
    val weekLabel: String,                 // e.g. "Week 1 (1-7 Sep)"
    val projectedDemandTonnes: Double,     // Total consumption estimate
    val projectedD2cConsumerOrders: Int,   // Retail household orders
    val projectedB2bBulkLots: Int,         // Institutional lot demand
    val expectedAgmarknetArrivalTonnes: Double, // Mandi supply arrivals
    val deficitOrSurplusTonnes: Double,    // Net gap
    val demandDeficitPercent: Double
)

data class CommodityDemandForecast(
    val commodity: String,
    val category: CropCategory,
    val emoji: String,
    val district: String,
    val state: String = "Maharashtra",
    val totalMonthlyDemandTonnes: Double,
    val consumerHouseholdDemandPercent: Int = 45, // % D2C
    val bulkInstitutionalDemandPercent: Int = 55, // % B2B
    val weeklyForecasts: List<DemandVolumeByWeek>,
    val demandTrend: DemandTrend,
    val confidenceScorePercent: Int = 94,
    val priceElasticityIndex: Double = 0.78,
    val recommendedFarmerRetailStockKg: Double,
    val sourcingStrategySummaryEn: String,
    val sourcingStrategySummaryHi: String,
    val sourcingStrategySummaryMr: String,
    val agmarknetArrivalCorrelation: String
) {
    fun getSourcingSummary(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> sourcingStrategySummaryEn
        AppLanguage.MR -> sourcingStrategySummaryMr
        AppLanguage.HI -> sourcingStrategySummaryHi
        else -> sourcingStrategySummaryEn
    }
}
