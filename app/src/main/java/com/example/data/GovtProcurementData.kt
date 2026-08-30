package com.example.data

import com.example.data.model.*

object GovtProcurementData {

    val upFarmerProfile = FarmerLandProfile(
        farmerId = "UP-KID-784920",
        farmerName = "Rameshwar Prasad Singh",
        maskedAadhaar = "•••• •••• 8842",
        state = "Uttar Pradesh",
        district = "Hardoi",
        subDistrictOrTehsil = "Sandila Tehsil",
        village = "Kachhauna",
        landRecordType = "Khatauni (Khasra/Gata)",
        khatauniOrKhatianNo = "Khatauni: 00412",
        gataOrDagNo = "Gata No: 418/1, 418/2, 420",
        totalLandAreaAcres = 4.5,
        cultivatedAreaAcres = 4.0,
        irrigationType = "Borewell + Canal Command",
        ownershipStatus = "Owner-Cultivator (Bhumidhar with Transferable Rights)",
        isLandVerified = true,
        verificationSource = "UP Bhulekh Realtime Portal (fcs.up.gov.in)",
        bankName = "State Bank of India (SBI)",
        maskedAccount = "•••• •••• 4821",
        maskedIfsc = "SBIN0001294",
        dbtLinked = true,
        verificationTimestamp = "15 Mar 2026 • Aadhaar Biometric Authenticated"
    )

    val wbFarmerProfile = FarmerLandProfile(
        farmerId = "WB-KID-449102",
        farmerName = "Anupam Mukherjee",
        maskedAadhaar = "•••• •••• 4190",
        state = "West Bengal",
        district = "Purba Bardhaman",
        subDistrictOrTehsil = "Memari-I Block",
        village = "Bagila Gram",
        landRecordType = "RoR / Khatian (BanglarBhumi)",
        khatauniOrKhatianNo = "Khatian: 1842/J-7",
        gataOrDagNo = "Dag / Plot No: 812, 814, 819",
        totalLandAreaAcres = 3.5,
        cultivatedAreaAcres = 3.2,
        irrigationType = "Deep Tubewell (DVC Command)",
        ownershipStatus = "Registered Raiyat (Krishak Bandhu ID: KB-WB-99214)",
        isLandVerified = true,
        verificationSource = "BanglarBhumi Land Portal (procurement.wbfood.in)",
        bankName = "Punjab National Bank (PNB)",
        maskedAccount = "•••• •••• 9012",
        maskedIfsc = "PUNB0029300",
        dbtLinked = true,
        verificationTimestamp = "12 Feb 2026 • Krishak Bandhu e-KYC Verified"
    )

    val mspCropsList: List<GovtMspCrop> = listOf(
        GovtMspCrop(
            id = "msp_wheat",
            cropNameEn = "Wheat",
            cropNameHi = "गेहूं",
            cropNameMr = "गहू",
            cropNameGu = "ઘઉં",
            season = "Rabi 2025-26",
            mspPricePerQuintal = 2425,
            procurementAgency = "FCI / UP Food & Civil Supplies",
            maxYieldNormQuintalPerAcre = 18.0,
            maxMoistureAllowedPercent = 12.0,
            emoji = "🌾"
        ),
        GovtMspCrop(
            id = "msp_paddy_common",
            cropNameEn = "Paddy (Common)",
            cropNameHi = "धान (सामान्य)",
            cropNameMr = "भात / धान (साधारण)",
            cropNameGu = "ડાંગર (સામાન્ય)",
            season = "Kharif 2025-26",
            mspPricePerQuintal = 2300,
            procurementAgency = "WBSCSC / Food & Supplies Dept",
            maxYieldNormQuintalPerAcre = 20.0,
            maxMoistureAllowedPercent = 17.0,
            emoji = "🍚"
        ),
        GovtMspCrop(
            id = "msp_paddy_grade_a",
            cropNameEn = "Paddy (Grade A)",
            cropNameHi = "धान (ग्रेड-ए)",
            cropNameMr = "भात / धान (ग्रेड-अ)",
            cropNameGu = "ડાંગર (ગ્રેડ-એ)",
            season = "Kharif 2025-26",
            mspPricePerQuintal = 2320,
            procurementAgency = "FCI / State Agencies",
            maxYieldNormQuintalPerAcre = 20.0,
            maxMoistureAllowedPercent = 17.0,
            emoji = "🌾"
        ),
        GovtMspCrop(
            id = "msp_mustard",
            cropNameEn = "Mustard / Rapeseed",
            cropNameHi = "सरसों / राई",
            cropNameMr = "मोहरी",
            cropNameGu = "રાયડો / સરસવ",
            season = "Rabi 2025-26",
            mspPricePerQuintal = 5650,
            procurementAgency = "NAFED / State Cooperative",
            maxYieldNormQuintalPerAcre = 8.5,
            maxMoistureAllowedPercent = 8.0,
            emoji = "🌼"
        ),
        GovtMspCrop(
            id = "msp_maize",
            cropNameEn = "Maize (Corn)",
            cropNameHi = "मक्का",
            cropNameMr = "मका",
            cropNameGu = "મકાઈ",
            season = "Kharif 2025-26",
            mspPricePerQuintal = 2090,
            procurementAgency = "FCI / State Cooperative",
            maxYieldNormQuintalPerAcre = 16.0,
            maxMoistureAllowedPercent = 14.0,
            emoji = "🌽"
        ),
        GovtMspCrop(
            id = "msp_chana",
            cropNameEn = "Gram (Chana)",
            cropNameHi = "चना",
            cropNameMr = "हरभरा",
            cropNameGu = "ચણા",
            season = "Rabi 2025-26",
            mspPricePerQuintal = 5440,
            procurementAgency = "NAFED / SFAC",
            maxYieldNormQuintalPerAcre = 9.0,
            maxMoistureAllowedPercent = 10.0,
            emoji = "🧆"
        )
    )

    val upProcurementCenters: List<ProcurementCenter> = listOf(
        ProcurementCenter(
            id = "PPC-UP-01",
            state = "Uttar Pradesh",
            district = "Hardoi",
            name = "Kisan Seva Kendra PPC - Sandila Mandi",
            address = "Gate No. 2, Sandila APMC Yard, Hardoi, UP - 241204",
            distanceKm = 4.2,
            operatingAgency = "UP State Food & Essential Commodities Corp (UPSFEC)",
            inchargeContact = "Centre Incharge: +91 94500 28192",
            dailyCapacityQuintals = 600
        ),
        ProcurementCenter(
            id = "PPC-UP-02",
            state = "Uttar Pradesh",
            district = "Hardoi",
            name = "Kachhauna PACS Procurement Centre",
            address = "Primary Ag Credit Samiti, Station Road, Kachhauna, UP - 241126",
            distanceKm = 7.8,
            operatingAgency = "UP Cooperative Federation (PCU)",
            inchargeContact = "PPC Secretary: +91 94511 82910",
            dailyCapacityQuintals = 450
        ),
        ProcurementCenter(
            id = "PPC-UP-03",
            state = "Uttar Pradesh",
            district = "Hardoi",
            name = "Hardoi Central Mandi PPC Complex",
            address = "Mandi Parishad Main Campus, Sitapur Road, Hardoi, UP - 241001",
            distanceKm = 14.5,
            operatingAgency = "Food Corporation of India (FCI)",
            inchargeContact = "Control Room: 1800-1800-150",
            dailyCapacityQuintals = 1200
        )
    )

    val wbProcurementCenters: List<ProcurementCenter> = listOf(
        ProcurementCenter(
            id = "PPC-WB-01",
            state = "West Bengal",
            district = "Purba Bardhaman",
            name = "Memari Krishak Samiti Central Paddy Centre (CPC)",
            address = "Memari Station Road, Purba Bardhaman, WB - 713146",
            distanceKm = 3.8,
            operatingAgency = "WB State Cooperative Marketing Federation (BENFED)",
            inchargeContact = "Officer In-charge: +91 98321 04921",
            dailyCapacityQuintals = 550
        ),
        ProcurementCenter(
            id = "PPC-WB-02",
            state = "West Bengal",
            district = "Purba Bardhaman",
            name = "Bagila PACS Paddy Purchase Camp",
            address = "Bagila Gram Panchayat Bhavan, Purba Bardhaman, WB - 713157",
            distanceKm = 6.2,
            operatingAgency = "WB State Essential Commodities Supply Corp (WBSCSC)",
            inchargeContact = "PPC Manager: +91 98322 71092",
            dailyCapacityQuintals = 400
        ),
        ProcurementCenter(
            id = "PPC-WB-03",
            state = "West Bengal",
            district = "Purba Bardhaman",
            name = "Burdwan Central Regulated Market Yard",
            address = "G.T. Road, Alisha More, Burdwan, WB - 713103",
            distanceKm = 18.0,
            operatingAgency = "Food & Supplies Dept, Govt of West Bengal",
            inchargeContact = "Toll-free Helpdesk: 1800-345-5505",
            dailyCapacityQuintals = 1500
        )
    )

    val initialUpCropRegistration = GovtCropRegistration(
        id = "REG-UP-2026-004128",
        farmerId = "UP-KID-784920",
        farmerName = "Rameshwar Prasad Singh",
        state = "Uttar Pradesh",
        cropName = "Wheat",
        season = "Rabi 2025-26",
        cultivatedAreaAcres = 4.0,
        expectedProductionQuintals = 72.0,
        approvedEligibleQuantityQuintals = 72.0,
        mspRatePerQuintal = 2425,
        estimatedTotalMspPayout = 174600.0,
        registrationDate = "18 Mar 2026",
        status = "Verified & Approved",
        verificationRemarks = "Area 4.0 Acres verified matching Gata No 418/1 in UP Bhulekh"
    )

    val initialWbCropRegistration = GovtCropRegistration(
        id = "REG-WB-2026-009183",
        farmerId = "WB-KID-449102",
        farmerName = "Anupam Mukherjee",
        state = "West Bengal",
        cropName = "Paddy (Common)",
        season = "Kharif 2025-26",
        cultivatedAreaAcres = 3.2,
        expectedProductionQuintals = 64.0,
        approvedEligibleQuantityQuintals = 64.0,
        mspRatePerQuintal = 2300,
        estimatedTotalMspPayout = 147200.0,
        registrationDate = "15 Jan 2026",
        status = "Verified & Approved",
        verificationRemarks = "Area 3.2 Acres verified matching Khatian 1842/J-7 via BanglarBhumi"
    )

    val initialUpTokenBooking = GovtTokenBooking(
        tokenNumber = "UP-WHT-2026-88421",
        farmerId = "UP-KID-784920",
        farmerName = "Rameshwar Prasad Singh",
        state = "Uttar Pradesh",
        cropName = "Wheat",
        season = "Rabi 2025-26",
        estimatedQuantityQuintals = 70.0,
        mspRate = 2425,
        centreId = "PPC-UP-01",
        centreName = "Kisan Seva Kendra PPC - Sandila Mandi",
        centreAddress = "Gate No. 2, Sandila APMC Yard, Hardoi, UP - 241204",
        bookingDate = "29 Mar 2026",
        timeSlot = "09:00 AM - 12:00 PM (Morning Slot)",
        status = GovtTokenStatus.BOOKED,
        qrPayload = "KISAN_GOVT_TOKEN:UP-WHT-2026-88421|FARMER:UP-KID-784920|CROP:WHEAT|QTY:70QTL|MSP:2425|PPC:SANDILA|DATE:2026-03-29|AUTH:UP-FCS-EAL5-VALID",
        createdAt = "20 Mar 2026"
    )

    val initialUpProcurementReceipt = GovtProcurementReceipt(
        receiptNumber = "RCP-FCSUP-2026-99382",
        tokenNumber = "UP-WHT-2026-88421",
        farmerName = "Rameshwar Prasad Singh",
        farmerId = "UP-KID-784920",
        procurementCentre = "Kisan Seva Kendra PPC - Sandila Mandi",
        cropName = "Wheat (FAQ Grade)",
        deliveredDate = "25 Mar 2026 • 10:45 AM",
        deliveredQuantityQuintals = 70.0,
        moistureMeasuredPercent = 11.2,
        acceptedQuantityQuintals = 70.0,
        rejectedQuantityQuintals = 0.0,
        mspRatePerQuintal = 2425,
        grossPaymentInr = 169750.0,
        handlingDeductionInr = 0.0,
        netPayableInr = 169750.0,
        paymentStatus = "DBT Credited via PFMS (Ref: DBT-2026-991823)",
        dbtReferenceNumber = "PFMS-UPFCS-2026-99182348",
        creditedBankName = "State Bank of India",
        maskedAccountNumber = "•••• •••• 4821",
        expectedOrCreditedDate = "26 Mar 2026 (Credited)"
    )

    val upStateInfo = GovtProcurementStateInfo(
        stateName = "Uttar Pradesh",
        portalName = "e-Kharid UP / FCS Portal (fcs.up.gov.in)",
        portalUrl = "https://fcs.up.gov.in",
        primaryCrops = listOf("Wheat (गेहूं)", "Paddy (धान)", "Mustard (सरसों)", "Maize (मक्का)"),
        landRecordTerminology = listOf(
            "Khatauni (खतौनी)" to "Primary land ownership title document registered in UP Revenue Board.",
            "Gata / Khasra No. (गाटा / खसरा संख्या)" to "Specific survey plot number of agricultural land in village.",
            "UP Bhulekh (यूपी भूलेख)" to "Digital land database used by food procurement centres to verify sowing area.",
            "Bhumidhar (भूमिधर)" to "Legal landholder status eligible for direct MSP procurement into bank account."
        ),
        procurementGuidelines = listOf(
            "Registration must be completed on e-Kharid portal with Aadhaar authentication.",
            "Token generation opens 7 days before scheduled mandi arrival.",
            "Maximum moisture standard for Wheat is 12% and foreign matter must be less than 0.75%.",
            "Payment credited directly to NPCI-mapped bank account within 48 to 72 hours via DBT."
        ),
        tollFreeHelpline = "1800-1800-150 / 1967 (UP Food & Civil Supplies Toll-Free)",
        activeSeason = "Rabi Procurement Season 2025-26",
        sampleFarmerProfile = upFarmerProfile
    )

    val wbStateInfo = GovtProcurementStateInfo(
        stateName = "West Bengal",
        portalName = "Online Paddy Procurement / Food & Supplies (procurement.wbfood.in)",
        portalUrl = "https://procurement.wbfood.in",
        primaryCrops = listOf("Paddy Common (ধান সাধারণ)", "Paddy Grade A (ধান গ্রেড-এ)", "Mustard (সর্ষে)", "Jute (পাট)"),
        landRecordTerminology = listOf(
            "BanglarBhumi (বাংলারভূমি)" to "West Bengal land records portal linking Khatian and Dag numbers.",
            "Khatian Number (খতিয়ান নম্বর)" to "Unique ledger number representing farmer's landholding in Mouza.",
            "Dag / Plot Number (দাগ নম্বর)" to "Cadastral survey plot boundary number for cultivated field.",
            "Krishak Bandhu (কৃষক বন্ধু)" to "Farmer scheme ID enabling single-click eligibility and DBT disbursement."
        ),
        procurementGuidelines = listOf(
            "Farmers can self-schedule their paddy delivery slots online or via nearest Bangla Sahayata Kendra (BSK).",
            "Maximum paddy purchase limit is 45 Quintals per farmer under normal decentralized procurement.",
            "Moisture limit for Paddy is 17% (FAQ specification).",
            "Payment directly transferred to farmer's bank account through IFMS / DBT within 3 working days."
        ),
        tollFreeHelpline = "1800-345-5505 / 1967 (WB Khadya Sathi Helpdesk)",
        activeSeason = "KMS 2025-26 (Kharif Marketing Season)",
        sampleFarmerProfile = wbFarmerProfile
    )

    val procurementFaqs = listOf(
        Pair(
            "How does the government know my land details?",
            "State procurement portals are directly integrated with digital land registries (UP Bhulekh for Uttar Pradesh, BanglarBhumi for West Bengal). When you enter your Farmer ID or Aadhaar, your Khatauni/Khatian and Gata numbers are automatically fetched and verified."
        ),
        Pair(
            "Can tenant / leased (Bataidar) farmers sell at MSP?",
            "Yes. Tenant and sharecropper farmers can register by submitting a signed agreement/declaration form (Samjhauta Patra in UP or Krishak Bandhu tenant certification in WB) verified by the local Gram Pradhan or Agriculture Officer."
        ),
        Pair(
            "How is the eligible procurement quantity calculated?",
            "Eligible quantity is calculated using the formula: (Cultivated Land Area in Acres) × (Government District Average Yield Norm). For example, 4.0 Acres of Wheat × 18 Quintals/Acre = 72 Quintals maximum eligible quota."
        ),
        Pair(
            "What happens if I miss my token booking date?",
            "If you cannot bring your harvest on the booked date due to rain or transport issues, your token remains valid for re-scheduling for up to 3 days. You can also re-book a fresh slot from the Token Booking screen without losing your registration."
        ),
        Pair(
            "When and how will the MSP payment arrive in my bank account?",
            "Once weighing is completed at the procurement centre, an electronic e-weighment receipt (J-Form / W-Form) is generated. Government releases the full MSP amount directly into your Aadhaar-linked bank account (DBT/PFMS) within 24 to 72 hours with zero deductions."
        ),
        Pair(
            "What quality parameters (FAQ standards) are checked at the centre?",
            "Procurement officers inspect 3 main parameters: 1) Moisture level (Must be under 12% for Wheat, 17% for Paddy), 2) Foreign organic matter / chaff (<0.75%), and 3) Damaged / discolored grains (<2%). Sun-drying your harvest for 1-2 days before transport ensures 100% acceptance."
        )
    )

    val requiredDocuments = listOf(
        Triple("Aadhaar Card", "Mandatory for Biometric / OTP e-KYC and DBT link verification", true),
        Triple("Land Ownership Document (Khatauni / Khatian / RoR)", "Updated computerized land record copy from Bhulekh / BanglarBhumi", true),
        Triple("Bank Account Passbook / Cancelled Cheque", "NPCI-mapped active bank account for direct MSP disbursement", true),
        Triple("Crop Sowing Certificate / Girdawari", "Proof of cultivated crop area issued by Revenue / Patwari / Krishak Bandhu", true),
        Triple("Tenant Agreement / Self-Declaration", "Required only for leased / tenant (Bataidar) farmers", false)
    )
}
