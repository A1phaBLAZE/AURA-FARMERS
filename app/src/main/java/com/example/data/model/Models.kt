package com.example.data.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val regionTag: String = "India"
) {
    EN("en", "English", "English", "Global / All India"),
    HI("hi", "Hindi", "हिन्दी", "North & Central India"),
    MR("mr", "Marathi", "मराठी", "Maharashtra"),
    GU("gu", "Gujarati", "ગુજરાતી", "Gujarat"),
    PA("pa", "Punjabi", "ਪੰਜਾਬੀ", "Punjab"),
    BN("bn", "Bengali", "বাংলা", "West Bengal & Tripura"),
    TE("te", "Telugu", "తెలుగు", "Andhra Pradesh & Telangana"),
    TA("ta", "Tamil", "தமிழ்", "Tamil Nadu"),
    KN("kn", "Kannada", "ಕನ್ನಡ", "Karnataka"),
    ML("ml", "Malayalam", "മലയാളം", "Kerala"),
    OR("or", "Odia", "ଓଡ଼ିଆ", "Odisha"),
    AS("as", "Assamese", "অসমীয়া", "Assam & North East"),
    UR("ur", "Urdu", "اردو", "National / J&K"),
    SA("sa", "Sanskrit", "संस्कृतम्", "Classical India"),
    BHO("bho", "Bhojpuri", "भोजपुरी", "Bihar & Eastern UP"),
    MAI("mai", "Maithili", "मैथिली", "Bihar / Mithila"),
    NE("ne", "Nepali", "नेपाली", "Sikkim & North India"),
    SD("sd", "Sindhi", "सिंधी", "Western India"),
    DOI("doi", "Dogri", "डोगरी", "Jammu & Kashmir"),
    KOK("kok", "Konkani", "कोंकणी", "Goa & Konkan"),
    KS("ks", "Kashmiri", "कश्मीरी", "Jammu & Kashmir"),
    SAT("sat", "Santali", "ᱥᱟᱱᱛᱟᱲᱤ", "Jharkhand & Odisha"),
    ES("es", "Spanish", "Español", "International / Americas"),
    FR("fr", "French", "Français", "International / Europe"),
    AR("ar", "Arabic", "العربية", "Middle East / Gulf Trade")
}

enum class PriceUnit(
    val code: String,
    val shortLabelEn: String,
    val shortLabelMr: String,
    val shortLabelHi: String,
    val shortLabelGu: String,
    val multiplierFromQuintal: Double,
    val kgEquivalent: Double,
    val toQuintalPriceMultiplier: Double
) {
    KG("kg", "₹/Kg", "₹/किलो", "₹/किलो", "₹/કિલો", 0.01, 1.0, 100.0),
    QUINTAL("qtl", "₹/Qtl", "₹/क्विंटल", "₹/क्विंटल", "₹/ક્વિન્ટલ", 1.0, 100.0, 1.0),
    TON("ton", "₹/Ton", "₹/टन", "₹/टन", "₹/ટન", 10.0, 1000.0, 0.1);

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> shortLabelEn
        AppLanguage.MR -> shortLabelMr
        AppLanguage.HI -> shortLabelHi
        AppLanguage.GU -> shortLabelGu
        else -> shortLabelHi
    }

    fun formatPrice(pricePerQuintal: Int): String {
        return when (this) {
            KG -> {
                val perKg = pricePerQuintal / 100.0
                if (perKg % 1.0 == 0.0) "₹${perKg.toInt()}" else "₹${String.format(java.util.Locale.US, "%.1f", perKg)}"
            }
            QUINTAL -> "₹${pricePerQuintal}"
            TON -> "₹${pricePerQuintal * 10}"
        }
    }
}

enum class QuantityUnit(
    val code: String,
    val labelEn: String,
    val labelMr: String,
    val labelHi: String,
    val labelGu: String,
    val toQuintalsMultiplier: Double
) {
    KG("kg", "Kg (Minimal)", "किलो (किरकोळ)", "किलो (खुदरा)", "કિલો (છૂટક)", 0.01),
    QUINTAL("qtl", "Quintal (100 kg)", "क्विंटल (१०० किलो)", "क्विंटल (100 किलो)", "ક્વિન્ટલ (100 કિલો)", 1.0),
    TON("ton", "Metric Ton (1,000 kg)", "टन / MT (१००० किलो)", "टन / MT (1000 किलो)", "ટન / MT (1000 કિલો)", 10.0);

    fun getLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> labelEn
        AppLanguage.MR -> labelMr
        AppLanguage.HI -> labelHi
        AppLanguage.GU -> labelGu
        else -> labelHi
    }
}

enum class UserRole(
    val titleEn: String,
    val titleMr: String,
    val titleHi: String,
    val titleGu: String,
    val emoji: String
) {
    FARMER("Farmer / Producer", "शेतकरी / उत्पादक", "किसान / उत्पादक", "ખેડૂત / ઉત્પાદક", "🌾"),
    CONSUMER("Consumer / Direct Buyer", "ग्राहक / थेट ग्राहक", "उपभोक्ता / प्रत्यक्ष खरीदार", "ગ્રાહક / સીધા ખરીદદાર", "🛒"),
    BUYER("Institutional Buyer / FPO", "संस्थात्मक खरेदीदार / FPO", "संस्थागत खरीदार / FPO", "સંસ્થાકીય ખરીદદાર / FPO", "🏢"),
    TRADER("Mandi Trader / Commission Agent", "मंडी व्यापारी / आडत्या", "मंडी व्यापारी / आढ़ती", "માર્કેટ વેપારી / આડતિયા", "🏪");

    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> titleEn
        AppLanguage.MR -> titleMr
        AppLanguage.HI -> titleHi
        AppLanguage.GU -> titleGu
        else -> titleHi
    }
}

data class UserProfile(
    val userId: String,
    val fullName: String,
    val mobileNumber: String,
    val role: UserRole,
    val location: String,
    val kisanIdOrCompany: String,
    val isVerified: Boolean = true,
    val profileBadge: String = "Kisan Vani Verified"
)

enum class CropCategory(val displayName: String, val icon: String = "🌾") {
    ALL("All Categories", "🌾"),
    VEGETABLES("Vegetables", "🥬"),
    GRAINS_PULSES("Grains & Pulses", "🌾"),
    CASH_CROPS("Cash Crops", "🌱"),
    FRUITS("Fruits", "🍎"),
    SPICES("Spices", "🌶️");

    fun getLabel(lang: AppLanguage): String = when (this) {
        ALL -> when (lang) {
            AppLanguage.MR -> "सर्व वर्गवारी"
            AppLanguage.HI -> "सभी श्रेणियां"
            AppLanguage.GU -> "બધી શ્રેણીઓ"
            else -> "All Categories"
        }
        VEGETABLES -> when (lang) {
            AppLanguage.MR -> "भाजीपाला"
            AppLanguage.HI -> "सब्जियां"
            AppLanguage.GU -> "શાકભાજી"
            else -> "Vegetables"
        }
        GRAINS_PULSES -> when (lang) {
            AppLanguage.MR -> "धान्य व कडधान्ये"
            AppLanguage.HI -> "अनाज व दालें"
            AppLanguage.GU -> "અનાજ અને કઠોળ"
            else -> "Grains & Pulses"
        }
        CASH_CROPS -> when (lang) {
            AppLanguage.MR -> "नगदी पिके"
            AppLanguage.HI -> "नकदी फसलें"
            AppLanguage.GU -> "રોકડિયા પાક"
            else -> "Cash Crops"
        }
        FRUITS -> when (lang) {
            AppLanguage.MR -> "फळे"
            AppLanguage.HI -> "फल"
            AppLanguage.GU -> "ફળો"
            else -> "Fruits"
        }
        SPICES -> when (lang) {
            AppLanguage.MR -> "मसाले"
            AppLanguage.HI -> "मसाले"
            AppLanguage.GU -> "મસાલા"
            else -> "Spices"
        }
    }
}

data class PricePoint(
    val dayLabel: String,
    val price: Int,
    val volumeTonnes: Int
)

data class CropItem(
    val id: String,
    val nameEn: String,
    val nameMr: String,
    val nameHi: String,
    val nameGu: String,
    val category: CropCategory,
    val currentPrice: Int,            // INR per Quintal
    val minPrice: Int,
    val maxPrice: Int,
    val modalPrice: Int,
    val mandiName: String,
    val district: String,
    val state: String = "All India",
    val arrivalVolumeQuintals: Int,
    val trendPercent: Double,        // e.g. +4.8% or -2.1%
    val recommendationEn: String,
    val recommendationMr: String,
    val recommendationHi: String,
    val recommendationGu: String,
    val priceHistory: List<PricePoint>,
    val emoji: String
) {
    fun getName(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> nameEn
        AppLanguage.MR -> nameMr
        AppLanguage.HI -> nameHi
        AppLanguage.GU -> nameGu
        else -> nameEn
    }

    fun getRecommendation(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> recommendationEn
        AppLanguage.MR -> recommendationMr
        AppLanguage.HI -> recommendationHi
        AppLanguage.GU -> recommendationGu
        else -> recommendationEn
    }
}

enum class LotStatus(val displayName: String) {
    ACTIVE("Active"),
    OFFERS_RECEIVED("Offers Received"),
    ESCROW_LOCKED("Escrow Locked"),
    PAYMENT_COMPLETED("Payment Completed"),
    DISPUTED("Disputed")
}

data class FarmerLot(
    val id: String,
    val cropName: String,
    val variety: String,
    val qualityGrade: String,         // e.g., "Grade A+ (Export)", "Grade A (Standard)", "Grade B"
    val quantityQuintals: Double,
    val expectedPricePerQuintal: Int,
    val locationDistrict: String,
    val locationTaluka: String,
    val storageType: String,          // e.g., "Farm Shed", "Cold Storage", "APMC Warehouse"
    val harvestDate: String,
    val status: LotStatus = LotStatus.ACTIVE,
    val offersCount: Int = 0,
    val dateCreated: String
) {
    fun getFormattedQuantity(): String {
        return when {
            quantityQuintals < 1.0 -> "${(quantityQuintals * 100).toInt()} Kg"
            quantityQuintals >= 10.0 -> {
                val tons = quantityQuintals / 10.0
                if (tons % 1.0 == 0.0) "${tons.toInt()} Tons" else "${String.format(java.util.Locale.US, "%.1f", tons)} Tons"
            }
            else -> {
                if (quantityQuintals % 1.0 == 0.0) "${quantityQuintals.toInt()} Qtl" else "$quantityQuintals Qtl"
            }
        }
    }

    fun getDetailedQuantityBadge(): String {
        return when {
            quantityQuintals < 1.0 -> "⚖️ Minimal Batch: ${(quantityQuintals * 100).toInt()} Kg"
            quantityQuintals >= 10.0 -> {
                val tons = quantityQuintals / 10.0
                "🚛 Bulk Lot: ${if (tons % 1.0 == 0.0) "${tons.toInt()}" else String.format(java.util.Locale.US, "%.1f", tons)} Tons (${quantityQuintals.toInt()} Qtl)"
            }
            else -> "🌾 Mandi Lot: ${if (quantityQuintals % 1.0 == 0.0) "${quantityQuintals.toInt()}" else "$quantityQuintals"} Qtl (${(quantityQuintals * 100).toInt()} Kg)"
        }
    }

    fun getTotalEstimatedValue(): Double = quantityQuintals * expectedPricePerQuintal
}

enum class OfferStatus(val displayName: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    COUNTERED("Countered")
}

data class BuyerOffer(
    val id: String,
    val lotId: String,
    val buyerName: String,
    val buyerCompany: String,
    val buyerRating: Double,
    val isVerified: Boolean = true,
    val offeredPricePerQuintal: Int,
    val pickupDate: String,
    val paymentTerms: String,         // e.g., "100% Escrow via Razorpay", "Immediate on Gate Pass"
    val status: OfferStatus = OfferStatus.PENDING,
    val counteredPrice: Int? = null
)

enum class PaymentStatus(val displayName: String) {
    ESCROW_LOCKED("Escrow Locked"),
    RELEASED_TO_FARMER("Released to Farmer"),
    REFUNDED("Refunded"),
    DISPUTE_FILED("Dispute Filed")
}

data class RazorpayPayment(
    val id: String,
    val lotId: String,
    val cropName: String,
    val amountInr: Double,
    val razorpayOrderId: String,
    val paymentId: String,
    val utrNumber: String,
    val buyerName: String,
    val farmerKisanCard: String,
    val status: PaymentStatus,
    val timestamp: String,
    val disputeReason: String? = null
)

data class FarmerKisanCard(
    val kisanCardNumber: String,      // e.g., "MH-MSINS-784920"
    val farmerName: String,
    val fatherOrSpouseName: String,
    val mobileNumber: String,
    val state: String = "All India",
    val district: String,
    val taluka: String,
    val village: String,
    val landSizeAcres: Double,
    val primaryCrops: List<String>,
    val verifiedGovtBadge: Boolean = true,
    val aadhaarMasked: String = "XXXX-XXXX-4892",
    val issueDate: String = "15 Jan 2024",
    val bankAccountMasked: String = "State Bank of India (•••9041)"
)

data class PesticideAdvisory(
    val id: String,
    val cropName: String,
    val pestOrDiseaseEn: String,
    val pestOrDiseaseMr: String,
    val pestOrDiseaseHi: String,
    val pestOrDiseaseGu: String,
    val chemicalRemedy: String,
    val bioOrganicRemedy: String,
    val dosagePerAcre: String,
    val applicationAdviceEn: String,
    val applicationAdviceMr: String,
    val applicationAdviceHi: String,
    val applicationAdviceGu: String,
    val safetyPeriodDays: Int,        // Pre-Harvest Interval (PHI)
    val severityLevel: String         // Low, Medium, High
) {
    fun getPestName(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> pestOrDiseaseEn
        AppLanguage.MR -> pestOrDiseaseMr
        AppLanguage.HI -> pestOrDiseaseHi
        AppLanguage.GU -> pestOrDiseaseGu
        else -> pestOrDiseaseHi
    }

    fun getAdvice(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> applicationAdviceEn
        AppLanguage.MR -> applicationAdviceMr
        AppLanguage.HI -> applicationAdviceHi
        AppLanguage.GU -> applicationAdviceGu
        else -> applicationAdviceHi
    }
}

data class GovtHelpline(
    val id: String,
    val titleEn: String,
    val titleMr: String,
    val titleHi: String,
    val titleGu: String,
    val phoneNumber: String,
    val department: String,
    val descriptionEn: String,
    val descriptionMr: String,
    val descriptionHi: String,
    val descriptionGu: String,
    val hours: String
) {
    fun getTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> titleEn
        AppLanguage.MR -> titleMr
        AppLanguage.HI -> titleHi
        AppLanguage.GU -> titleGu
        else -> titleHi
    }

    fun getDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> descriptionEn
        AppLanguage.MR -> descriptionMr
        AppLanguage.HI -> descriptionHi
        AppLanguage.GU -> descriptionGu
        else -> descriptionHi
    }
}

data class ForecastDay(
    val dayName: String,
    val tempMax: Int,
    val tempMin: Int,
    val rainProbPercent: Int,
    val iconEmoji: String,
    val condition: String
)

data class WeatherAdvisory(
    val district: String,
    val tempCelsius: Int,
    val conditionEn: String,
    val conditionMr: String,
    val conditionHi: String,
    val conditionGu: String,
    val rainChancePercent: Int,
    val humidityPercent: Int,
    val windKmh: Int,
    val audioBulletinEn: String,
    val audioBulletinMr: String,
    val audioBulletinHi: String,
    val audioBulletinGu: String,
    val forecast5Days: List<ForecastDay>,
    val dataSource: String = "Open-Meteo API",
    val coordinates: String = "20.00° N, 73.78° E",
    val lastUpdated: String = "Showing demo/offline sample data",
    val isLiveSuccess: Boolean = false
) {
    fun getCondition(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> conditionEn
        AppLanguage.MR -> conditionMr
        AppLanguage.HI -> conditionHi
        AppLanguage.GU -> conditionGu
        else -> conditionEn
    }

    fun getAudioBulletin(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> audioBulletinEn
        AppLanguage.MR -> audioBulletinMr
        AppLanguage.HI -> audioBulletinHi
        AppLanguage.GU -> audioBulletinGu
        else -> audioBulletinHi
    }
}

enum class HarnessToolType {
    ARBITRAGE_ANALYZER,
    DISEASE_DIAGNOSTIC,
    LOT_GRADING,
    SUBSIDY_CALCULATOR,
    SMART_CONTRACT_MAKER
}

data class AgentExecutionTrace(
    val id: String,
    val stepNumber: Int,
    val stepName: String,
    val description: String,
    val status: String = "Completed", // "Running", "Completed", "Verified"
    val durationMs: Long = 120L
)

data class AgentActionCard(
    val toolType: HarnessToolType,
    val title: String,
    val badge: String,
    val summary: String,
    val metrics: List<Pair<String, String>>,
    val primaryActionLabel: String,
    val actionPayload: String
)

data class PathologyDiagnosticResult(
    val crop: String,
    val pestOrDisease: String,
    val severity: String,
    val chemicalRecommendation: String,
    val activeIngredient: String,
    val dosagePerAcre: String,
    val sprayTechnique: String,
    val phiDays: Int,
    val organicAlternative: String,
    val estimatedTreatmentCostPerAcre: Int,
    val cibrcApproved: Boolean = true
)

data class ArbitrageResult(
    val crop: String,
    val sourceMandi: String,
    val sourcePrice: Int,
    val destMandi: String,
    val destPrice: Int,
    val distanceKm: Int,
    val transportCostPerQuintal: Int,
    val mandiCessPerQuintal: Int,
    val netArbitrageProfitPerQuintal: Int,
    val recommendedDecision: String,
    val optimalSellingWindow: String
)

data class MoistureGradingResult(
    val crop: String,
    val moisturePercent: Double,
    val foreignMatterPercent: Double,
    val damagedPercent: Double,
    val faqGrade: String, // "Grade A+ (Export Premium)", "Grade A (Standard FAQ)", "Grade B (Commercial)", "Grade C (Moisture High)"
    val basePricePerQuintal: Int,
    val priceAdjustmentPerQuintal: Int, // +200 or -350
    val netRecommendedPrice: Int,
    val dryingRecommendation: String
)

data class SubsidyResult(
    val schemeName: String,
    val department: String,
    val landAcres: Double,
    val farmerCategory: String,
    val totalProjectCost: Int,
    val govtSubsidyAmount: Int,
    val farmerBeneficiaryShare: Int,
    val subsidyPercentage: Int,
    val portalName: String,
    val requiredDocuments: List<String>,
    val eligibilityStatus: String
)

data class ChatMessage(
    val id: String,
    val isFromUser: Boolean,
    val messageText: String,
    val timestamp: String,
    val audioPlayable: Boolean = true,
    val isOfflineFallback: Boolean = false,
    val executionTraces: List<AgentExecutionTrace> = emptyList(),
    val actionCard: AgentActionCard? = null,
    val latencyMs: Long = 0L
)

data class TeeEnclaveStatus(
    val isHardwareEnclaveActive: Boolean = true,
    val enclaveArchitecture: String = "ARM® TrustZone™ / Android StrongBox Keymaster",
    val securityLevel: String = "Hardware Isolated Enclave (EAL5+ Assurance)",
    val masterKeyAlias: String = "kisan_vani_tee_master_key_v2",
    val keyAlgorithm: String = "AES-256-GCM / 256-bit Hardware Key",
    val signatureAlgorithm: String = "RSA-2048 / SHA-256 Hardware Enclave Signature",
    val hardwareAttestationHash: String = "SHA256:7F8A:9B1C:3D4E:5F6A:7B8C:9D0E:1F2A:3B4C:5D6E:7F8A:9B0C",
    val rootOfTrust: String = "National Agristack TEE Root CA • Govt of India",
    val lastTamperCheck: String = "Just now",
    val isTamperProof: Boolean = true
)

data class EnclaveEncryptedPayload(
    val payloadId: String,
    val ivBase64: String,
    val cipherTextBase64: String,
    val authTagBase64: String,
    val teeSignatureBase64: String,
    val timestamp: String,
    val dataCategory: String
)

data class DlpPolicySettings(
    val isScreenCaptureBlocked: Boolean = false,
    val isSecureClipboardActive: Boolean = true,
    val isPiiMaskingEnabled: Boolean = true,
    val isEnclaveEscrowSigningEnforced: Boolean = true,
    val isZeroMemoryPurgeActive: Boolean = true
)

data class DlpAuditEvent(
    val id: String,
    val timestamp: String,
    val eventType: String,
    val description: String,
    val severity: String = "SECURE_ACTION",
    val isHardwareVerified: Boolean = true
)

// ==========================================
// GOVT MSP PROCUREMENT DATA MODELS
// ==========================================

enum class GovtTokenStatus(val label: String, val badgeColorHex: Long) {
    BOOKED("Slot Booked", 0xFF0284C7),
    PENDING_VERIFICATION("Verification Pending", 0xFFD97706),
    COMPLETED("Procurement Completed", 0xFF16A34A),
    CANCELLED("Booking Cancelled", 0xFFDC2626)
}

data class FarmerLandProfile(
    val farmerId: String,
    val farmerName: String,
    val maskedAadhaar: String,
    val state: String,
    val district: String,
    val subDistrictOrTehsil: String,
    val village: String,
    val landRecordType: String, // e.g. "Khatauni (Khasra/Gata)", "RoR / Khatian (BanglarBhumi)"
    val khatauniOrKhatianNo: String,
    val gataOrDagNo: String,
    val totalLandAreaAcres: Double,
    val cultivatedAreaAcres: Double,
    val irrigationType: String,
    val ownershipStatus: String, // "Owner-Cultivator", "Registered Tenant / Raiyat"
    val isLandVerified: Boolean = true,
    val verificationSource: String,
    val bankName: String,
    val maskedAccount: String,
    val maskedIfsc: String,
    val dbtLinked: Boolean = true,
    val verificationTimestamp: String = "2026-03-15 • Biometric Verified"
)

data class GovtMspCrop(
    val id: String,
    val cropNameEn: String,
    val cropNameHi: String,
    val cropNameMr: String,
    val cropNameGu: String,
    val season: String,
    val mspPricePerQuintal: Int,
    val procurementAgency: String,
    val maxYieldNormQuintalPerAcre: Double,
    val maxMoistureAllowedPercent: Double,
    val emoji: String,
    val isActiveProcurement: Boolean = true
) {
    fun getName(lang: AppLanguage): String = when (lang) {
        AppLanguage.EN -> cropNameEn
        AppLanguage.HI -> cropNameHi
        AppLanguage.MR -> cropNameMr
        AppLanguage.GU -> cropNameGu
        else -> cropNameEn
    }
}

data class GovtCropRegistration(
    val id: String,
    val farmerId: String,
    val farmerName: String,
    val state: String,
    val cropName: String,
    val season: String,
    val cultivatedAreaAcres: Double,
    val expectedProductionQuintals: Double,
    val approvedEligibleQuantityQuintals: Double,
    val mspRatePerQuintal: Int,
    val estimatedTotalMspPayout: Double,
    val registrationDate: String,
    val status: String = "Verified & Approved",
    val verificationRemarks: String = "Land area verified via State Bhulekh Registry"
)

data class ProcurementCenter(
    val id: String,
    val state: String,
    val district: String,
    val name: String,
    val address: String,
    val distanceKm: Double,
    val operatingAgency: String,
    val inchargeContact: String,
    val dailyCapacityQuintals: Int,
    val availableSlots: List<String> = listOf(
        "09:00 AM - 12:00 PM (Morning Slot)",
        "01:00 PM - 04:00 PM (Afternoon Slot)",
        "04:00 PM - 06:00 PM (Evening Slot)"
    )
)

data class GovtTokenBooking(
    val tokenNumber: String,
    val farmerId: String,
    val farmerName: String,
    val state: String,
    val cropName: String,
    val season: String,
    val estimatedQuantityQuintals: Double,
    val mspRate: Int,
    val centreId: String,
    val centreName: String,
    val centreAddress: String,
    val bookingDate: String,
    val timeSlot: String,
    val status: GovtTokenStatus = GovtTokenStatus.BOOKED,
    val qrPayload: String,
    val createdAt: String
)

data class GovtProcurementReceipt(
    val receiptNumber: String,
    val tokenNumber: String,
    val farmerName: String,
    val farmerId: String,
    val procurementCentre: String,
    val cropName: String,
    val deliveredDate: String,
    val deliveredQuantityQuintals: Double,
    val moistureMeasuredPercent: Double,
    val acceptedQuantityQuintals: Double,
    val rejectedQuantityQuintals: Double,
    val mspRatePerQuintal: Int,
    val grossPaymentInr: Double,
    val handlingDeductionInr: Double = 0.0,
    val netPayableInr: Double,
    val paymentStatus: String,
    val dbtReferenceNumber: String,
    val creditedBankName: String,
    val maskedAccountNumber: String,
    val expectedOrCreditedDate: String
)

data class GovtProcurementStateInfo(
    val stateName: String,
    val portalName: String,
    val portalUrl: String,
    val primaryCrops: List<String>,
    val landRecordTerminology: List<Pair<String, String>>,
    val procurementGuidelines: List<String>,
    val tollFreeHelpline: String,
    val activeSeason: String,
    val sampleFarmerProfile: FarmerLandProfile
)

enum class NotificationType {
    SUCCESS,
    ERROR,
    INFO
}

data class AppNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val type: NotificationType = NotificationType.INFO,
    val durationMs: Long = 3500L
)

