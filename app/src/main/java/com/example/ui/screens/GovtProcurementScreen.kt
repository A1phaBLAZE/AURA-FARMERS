package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.GovtProcurementData
import com.example.data.LanguageManager
import com.example.util.AppActionHelper
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale

enum class GovtProcurementSubTab(val title: String, val icon: String) {
    LAND_ELIGIBILITY("My Land", "🌾"),
    REGISTER_CROP("Register Crop", "📝"),
    TOKEN_BOOKING("Token / Slot", "🎟️"),
    SELL_TRACKER("Sell & Receipt", "⚖️"),
    STATE_INFO("State Portals", "🏛️"),
    HELP_DOCS("Help & FAQs", "❓")
}

@Composable
fun GovtProcurementScreen(
    currentLanguage: AppLanguage,
    selectedState: String,
    onSelectState: (String) -> Unit,
    farmerProfile: FarmerLandProfile,
    mspCrops: List<GovtMspCrop>,
    procurementCenters: List<ProcurementCenter>,
    cropRegistrations: List<GovtCropRegistration>,
    tokenBookings: List<GovtTokenBooking>,
    procurementReceipts: List<GovtProcurementReceipt>,
    stateInfo: GovtProcurementStateInfo,
    lastBookedToken: GovtTokenBooking?,
    onRegisterCrop: (cropName: String, season: String, cultivatedAcres: Double, expectedProduction: Double) -> GovtCropRegistration,
    onBookToken: (cropName: String, quantity: Double, centreId: String, date: String, slot: String) -> GovtTokenBooking,
    onCancelToken: (tokenNumber: String) -> Unit,
    isPlayingAudio: Boolean,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit
) {
    var activeSubTab by remember { mutableStateOf(GovtProcurementSubTab.LAND_ELIGIBILITY) }
    val context = LocalContext.current

    // Dialog state for booking token
    var showBookSlotDialog by remember { mutableStateOf(false) }
    var showCancelConfirmDialog by remember { mutableStateOf<GovtTokenBooking?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val isUp = selectedState.contains("Uttar Pradesh", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Govt Trust Header & State Switcher
            GovtProcurementTopHeader(
                selectedState = selectedState,
                onSelectState = onSelectState,
                farmerProfile = farmerProfile,
                currentLanguage = currentLanguage,
                isPlayingAudio = isPlayingAudio,
                onSpeakGuide = {
                    onPlayAudio(
                        if (isUp) {
                            "उत्तर प्रदेश ई-खरीद पोर्टल: किसान रामेश्वर सिंह की खतौनी 00412 एवं गाटा 418/1 सत्यापित है। गेहूं एमएसपी ₹2425 प्रति क्विंटल पर सरकारी क्रय केंद्र पर उपलब्ध है।"
                        } else {
                            "West Bengal Online Paddy Procurement Portal: Farmer Anupam Mukherjee Khatian 1842 verified. Paddy Common MSP is ₹2300 per quintal with direct DBT payment."
                        }
                    )
                },
                onStopAudio = onStopAudio
            )

            // 2. Horizontal Sub-Tab Bar
            GovtSubTabBar(
                activeTab = activeSubTab,
                onTabSelect = { activeSubTab = it }
            )

            // 3. Sub-Tab Body Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (activeSubTab) {
                    GovtProcurementSubTab.LAND_ELIGIBILITY -> {
                        LandEligibilitySection(
                            farmerProfile = farmerProfile,
                            mspCrops = mspCrops,
                            currentLanguage = currentLanguage,
                            onNavigateToRegister = { activeSubTab = GovtProcurementSubTab.REGISTER_CROP }
                        )
                    }

                    GovtProcurementSubTab.REGISTER_CROP -> {
                        RegisterCropSection(
                            farmerProfile = farmerProfile,
                            mspCrops = mspCrops,
                            registrations = cropRegistrations.filter { it.state.contains(if (isUp) "Uttar Pradesh" else "West Bengal", ignoreCase = true) },
                            currentLanguage = currentLanguage,
                            onSubmitRegistration = { crop, season, acres, qty ->
                                onRegisterCrop(crop, season, acres, qty)
                                snackbarMessage = "Crop registered successfully for MSP procurement!"
                            },
                            onNavigateToToken = { activeSubTab = GovtProcurementSubTab.TOKEN_BOOKING }
                        )
                    }

                    GovtProcurementSubTab.TOKEN_BOOKING -> {
                        TokenBookingSection(
                            farmerProfile = farmerProfile,
                            mspCrops = mspCrops,
                            procurementCenters = procurementCenters,
                            tokenBookings = tokenBookings.filter { it.state.contains(if (isUp) "Uttar Pradesh" else "West Bengal", ignoreCase = true) },
                            currentLanguage = currentLanguage,
                            onOpenBookDialog = { showBookSlotDialog = true },
                            onCancelBooking = { showCancelConfirmDialog = it },
                            onShareToken = { token ->
                                val shareText = "🏛️ GOVT MSP TOKEN: ${token.tokenNumber}\nFarmer: ${token.farmerName} (${token.farmerId})\nCrop: ${token.cropName} (${token.estimatedQuantityQuintals} Qtl)\nCentre: ${token.centreName}\nDate & Time: ${token.bookingDate}, ${token.timeSlot}\nVerified via State e-Procurement Portal."
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Govt Token"))
                            },
                            onDownloadToken = { token ->
                                snackbarMessage = "Token #${token.tokenNumber} saved to device storage (PDF / E-Pass)."
                            }
                        )
                    }

                    GovtProcurementSubTab.SELL_TRACKER -> {
                        SellCropTrackerSection(
                            farmerProfile = farmerProfile,
                            receipts = procurementReceipts,
                            currentLanguage = currentLanguage,
                            onPlayVoiceSummary = {
                                onPlayAudio(
                                    "Government Procurement Step Tracker: Land verification completed. Crop registration active. Token booked. Weighing complete. Full payment credited via PFMS Direct Benefit Transfer to State Bank of India account."
                                )
                            }
                        )
                    }

                    GovtProcurementSubTab.STATE_INFO -> {
                        StateProcurementInfoSection(
                            stateInfo = stateInfo,
                            selectedState = selectedState,
                            onSelectState = onSelectState,
                            currentLanguage = currentLanguage
                        )
                    }

                    GovtProcurementSubTab.HELP_DOCS -> {
                        HelpAndDocumentsSection(
                            currentLanguage = currentLanguage,
                            onSpeakFaq = { q, a ->
                                onPlayAudio("$q. $a")
                            }
                        )
                    }
                }
            }
        }

        // Global Snackbar for feedback
        if (snackbarMessage != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { snackbarMessage = null }) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = KisanGreenDark,
                contentColor = Color.White
            ) {
                Text(text = snackbarMessage!!, fontSize = 13.sp)
            }
        }
    }

    // Book Slot Dialog
    if (showBookSlotDialog) {
        BookSlotModalDialog(
            farmerProfile = farmerProfile,
            mspCrops = mspCrops,
            procurementCenters = procurementCenters,
            onDismiss = { showBookSlotDialog = false },
            onConfirm = { crop, qty, centreId, date, slot ->
                onBookToken(crop, qty, centreId, date, slot)
                showBookSlotDialog = false
                snackbarMessage = "Slot booked successfully! Token generated."
            }
        )
    }

    // Cancel Token Dialog
    if (showCancelConfirmDialog != null) {
        val targetToken = showCancelConfirmDialog!!
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = TrendRed) },
            title = { Text("Cancel Token Booking?") },
            text = {
                Text(
                    text = "Are you sure you want to cancel Token #${targetToken.tokenNumber} for ${targetToken.cropName} at ${targetToken.centreName} on ${targetToken.bookingDate}? You can re-book another slot anytime.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelToken(targetToken.tokenNumber)
                        showCancelConfirmDialog = null
                        snackbarMessage = "Token booking cancelled."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TrendRed)
                ) {
                    Text("Yes, Cancel", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelConfirmDialog = null }) {
                    Text("Keep Booking")
                }
            }
        )
    }
}

// -------------------------------------------------------------------------------------
// 1. Top Header & State Switcher
// -------------------------------------------------------------------------------------
@Composable
private fun GovtProcurementTopHeader(
    selectedState: String,
    onSelectState: (String) -> Unit,
    farmerProfile: FarmerLandProfile,
    currentLanguage: AppLanguage,
    isPlayingAudio: Boolean,
    onSpeakGuide: () -> Unit,
    onStopAudio: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            // Row 1: Official Emblem / Icon + Title + Voice Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(KisanGreenDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Govt Procurement Emblem",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Govt MSP Procurement",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = KisanGreenDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(KisanSaffron.copy(alpha = 0.15f))
                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "DBT",
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KisanSaffron
                                )
                            }
                        }
                    }
                }

                // Audio Button
                IconButton(
                    onClick = { if (isPlayingAudio) onStopAudio() else onSpeakGuide() },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isPlayingAudio) TrendGreen.copy(alpha = 0.2f) else Slate100)
                        .testTag("govt_audio_guide_btn")
                ) {
                    Icon(
                        imageVector = if (isPlayingAudio) Icons.Default.VolumeUp else Icons.Outlined.VolumeUp,
                        contentDescription = "Listen guide",
                        tint = if (isPlayingAudio) TrendGreen else KisanGreenDark,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Row 2: State Selector Chips (UP vs West Bengal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "State:",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )

                FilterChip(
                    selected = selectedState.contains("Uttar Pradesh", ignoreCase = true),
                    onClick = { onSelectState("Uttar Pradesh") },
                    label = { Text("🏛️ UP (e-Kharid)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KisanGreenPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("state_chip_up")
                )

                FilterChip(
                    selected = selectedState.contains("West Bengal", ignoreCase = true),
                    onClick = { onSelectState("West Bengal") },
                    label = { Text("🏛️ WB (e-Paddy)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KisanGreenPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("state_chip_wb")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Row 3: Active Profile Strip & Disclaimer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate50)
                    .border(0.5.dp, Slate200, RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${farmerProfile.farmerName} (${farmerProfile.farmerId})",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate800
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = TrendGreen,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "e-KYC Verified",
                        fontSize = 8.5.sp,
                        color = TrendGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Demo disclaimer line
            Text(
                text = "ℹ️ Demo information—verify current rules on the official state procurement portal (fcs.up.gov.in / procurement.wbfood.in).",
                fontSize = 9.5.sp,
                color = Slate500,
                lineHeight = 12.sp
            )
        }
    }
}

// -------------------------------------------------------------------------------------
// 2. Sub-Tab Bar
// -------------------------------------------------------------------------------------
@Composable
private fun GovtSubTabBar(
    activeTab: GovtProcurementSubTab,
    onTabSelect: (GovtProcurementSubTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GovtProcurementSubTab.values().forEach { tab ->
            val isSelected = activeTab == tab
            Card(
                onClick = { onTabSelect(tab) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) KisanGreenDark else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("govt_subtab_${tab.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = tab.icon, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = tab.title,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Slate700
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SECTION 1: My Land & Eligibility
// -------------------------------------------------------------------------------------
@Composable
private fun LandEligibilitySection(
    farmerProfile: FarmerLandProfile,
    mspCrops: List<GovtMspCrop>,
    currentLanguage: AppLanguage,
    onNavigateToRegister: () -> Unit
) {
    var selectedCropForEstimate by remember { mutableStateOf(mspCrops.first()) }

    val estimatedQty = farmerProfile.cultivatedAreaAcres * selectedCropForEstimate.maxYieldNormQuintalPerAcre
    val estimatedMspValue = estimatedQty * selectedCropForEstimate.mspPricePerQuintal

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Verified Land Record Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(16.dp))
                    .testTag("land_record_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Header with Land Verified Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(KisanGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🗺️", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Verified Agricultural Land",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = farmerProfile.landRecordType,
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }

                        // Verified Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TrendGreen.copy(alpha = 0.12f))
                                .border(1.dp, TrendGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified",
                                    tint = TrendGreen,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Land Verified",
                                    color = TrendGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2x2 Grid of Land Details
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Slate50)
                            .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem(
                                label = "State & District",
                                value = "${farmerProfile.state}, ${farmerProfile.district}",
                                modifier = Modifier.weight(1f)
                            )
                            DetailItem(
                                label = "Tehsil / Block & Village",
                                value = "${farmerProfile.subDistrictOrTehsil}, ${farmerProfile.village}",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Divider(color = Slate200, thickness = 0.8.dp)

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem(
                                label = "Title / Registry No.",
                                value = farmerProfile.khatauniOrKhatianNo,
                                isBold = true,
                                modifier = Modifier.weight(1f)
                            )
                            DetailItem(
                                label = "Plot / Survey No.",
                                value = farmerProfile.gataOrDagNo,
                                isBold = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Divider(color = Slate200, thickness = 0.8.dp)

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem(
                                label = "Total Land Area",
                                value = "${farmerProfile.totalLandAreaAcres} Acres",
                                modifier = Modifier.weight(1f)
                            )
                            DetailItem(
                                label = "Cultivated Area",
                                value = "${farmerProfile.cultivatedAreaAcres} Acres",
                                isHighlight = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Divider(color = Slate200, thickness = 0.8.dp)

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem(
                                label = "Ownership Status",
                                value = farmerProfile.ownershipStatus,
                                modifier = Modifier.weight(1f)
                            )
                            DetailItem(
                                label = "Irrigation Source",
                                value = farmerProfile.irrigationType,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Linked Bank Account Strip (Masked for privacy)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(KisanGreenContainer.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = KisanGreenDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DBT Bank: ${farmerProfile.bankName} (${farmerProfile.maskedAccount})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KisanGreenDark
                            )
                        }

                        Text(
                            text = "Aadhaar Linked",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrendGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Source: ${farmerProfile.verificationSource} • ${farmerProfile.verificationTimestamp}",
                        fontSize = 9.5.sp,
                        color = Slate500
                    )
                }
            }
        }

        // 2. Interactive Crop Eligibility Estimator
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                    .testTag("eligibility_estimator_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📊", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Estimated MSP Eligibility",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Slate900
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(KisanSaffronContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Yield Norms",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Select a crop to estimate maximum quota based on your ${farmerProfile.cultivatedAreaAcres} acres of verified land:",
                        fontSize = 11.5.sp,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Crop Selection Horizontal Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(mspCrops) { crop ->
                            val isSelected = selectedCropForEstimate.id == crop.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCropForEstimate = crop },
                                label = {
                                    Text(
                                        text = "${crop.emoji} ${crop.cropNameEn}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KisanGreenPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3-Metric Calculation Display
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(KisanGreenContainer.copy(alpha = 0.5f))
                            .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Official MSP",
                                fontSize = 10.sp,
                                color = Slate600,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "₹${selectedCropForEstimate.mspPricePerQuintal}/Qtl",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = KisanGreenDark
                            )
                            Text(
                                text = "${selectedCropForEstimate.season}",
                                fontSize = 9.sp,
                                color = Slate500
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Slate300)
                        )

                        Column(
                            modifier = Modifier.weight(1.1f).padding(horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Eligible Quota",
                                fontSize = 10.sp,
                                color = Slate600,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", estimatedQty)} Qtl",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Black,
                                color = KisanGreenPrimary
                            )
                            Text(
                                text = "${selectedCropForEstimate.maxYieldNormQuintalPerAcre} Qtl/Acre norm",
                                fontSize = 9.sp,
                                color = Slate500
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Slate300)
                        )

                        Column(
                            modifier = Modifier.weight(1.2f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Est. Total Payout",
                                fontSize = 10.sp,
                                color = Slate600,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.0f", estimatedMspValue)}",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF15803D)
                            )
                            Text(
                                text = "Direct to Bank",
                                fontSize = 9.sp,
                                color = Slate500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Explanation callout
                    Text(
                        text = "💡 \"Your eligible quantity is estimated from registered land, crop area, and government procurement rules.\"",
                        fontSize = 11.sp,
                        color = Slate700,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("register_this_crop_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Register ${selectedCropForEstimate.cropNameEn} for Procurement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SECTION 2: Register Crop for Procurement
// -------------------------------------------------------------------------------------
@Composable
private fun RegisterCropSection(
    farmerProfile: FarmerLandProfile,
    mspCrops: List<GovtMspCrop>,
    registrations: List<GovtCropRegistration>,
    currentLanguage: AppLanguage,
    onSubmitRegistration: (crop: String, season: String, acres: Double, qty: Double) -> Unit,
    onNavigateToToken: () -> Unit
) {
    var selectedCrop by remember { mutableStateOf(mspCrops.first()) }
    var selectedSeason by remember { mutableStateOf("Rabi 2025-26") }
    var cultivatedAreaText by remember { mutableStateOf(farmerProfile.cultivatedAreaAcres.toString()) }
    var expectedProductionText by remember { mutableStateOf("70") }

    val parsedAcres = cultivatedAreaText.toDoubleOrNull() ?: farmerProfile.cultivatedAreaAcres
    val maxAllowedQty = parsedAcres * selectedCrop.maxYieldNormQuintalPerAcre
    val parsedExpectedQty = expectedProductionText.toDoubleOrNull() ?: maxAllowedQty
    val eligibleApprovedQty = minOf(parsedExpectedQty, maxAllowedQty)
    val estimatedPayout = eligibleApprovedQty * selectedCrop.mspPricePerQuintal

    var isSubmitting by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Registration Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                    .testTag("register_crop_form_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📝", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "New Crop Sowing Registration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Slate900
                            )
                        }

                        Text(
                            text = farmerProfile.state,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KisanGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Select Crop
                    Text(text = "Select Crop to Sell *", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(mspCrops) { crop ->
                            val isSelected = selectedCrop.id == crop.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCrop = crop
                                    selectedSeason = crop.season
                                    expectedProductionText = (parsedAcres * crop.maxYieldNormQuintalPerAcre).toInt().toString()
                                },
                                label = { Text("${crop.emoji} ${crop.cropNameEn}", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KisanGreenPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Select Season
                    Text(text = "Procurement Season *", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Rabi 2025-26", "Kharif 2025-26").forEach { season ->
                            val isSelected = selectedSeason == season
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSeason = season },
                                label = { Text(season, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KisanGreenDark,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Cultivated Area (Acres) & Expected Production (Quintals)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = cultivatedAreaText,
                            onValueChange = { cultivatedAreaText = it },
                            label = { Text("Cultivated Area (Acres)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cultivated_area_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            supportingText = {
                                Text("Max: ${farmerProfile.totalLandAreaAcres} Acres", fontSize = 10.sp)
                            }
                        )

                        OutlinedTextField(
                            value = expectedProductionText,
                            onValueChange = { expectedProductionText = it },
                            label = { Text("Expected Yield (Qtl)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("expected_yield_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            supportingText = {
                                Text("~${String.format(Locale.US, "%.1f", maxAllowedQty)} Qtl norm", fontSize = 10.sp)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // MSP Rate & Estimated Value Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = KisanGreenContainer)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Official ${selectedCrop.cropNameEn} MSP:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = KisanGreenDark
                                )
                                Text(
                                    text = "₹${selectedCrop.mspPricePerQuintal} / Quintal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = KisanGreenDark
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Approved Eligible Quantity:",
                                    fontSize = 11.5.sp,
                                    color = Slate700
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", eligibleApprovedQty)} Quintals",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KisanGreenPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Estimated MSP Payout:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "₹${String.format(Locale.US, "%,.0f", estimatedPayout)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Government Warning Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "Warning",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Warning: Final eligible quantity is confirmed after physical inspection and moisture verification (<${selectedCrop.maxMoistureAllowedPercent}%) at the procurement centre.",
                            fontSize = 10.5.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            isSubmitting = true
                            onSubmitRegistration(selectedCrop.cropNameEn, selectedSeason, parsedAcres, parsedExpectedQty)
                            isSubmitting = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("submit_crop_registration_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isSubmitting
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Submit Crop Registration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Active Registrations List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Crop Registrations (${registrations.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Slate900
                )

                Text(
                    text = "Book Slot ➔",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = KisanGreenPrimary,
                    modifier = Modifier.clickable(onClick = onNavigateToToken)
                )
            }
        }

        items(registrations) { reg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(14.dp))
                    .testTag("reg_card_${reg.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🌾", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${reg.cropName} (${reg.season})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Slate900
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TrendGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = reg.status,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrendGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate50)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Area Registered", fontSize = 9.5.sp, color = Slate500)
                            Text(text = "${reg.cultivatedAreaAcres} Acres", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        }
                        Column {
                            Text(text = "Approved Quota", fontSize = 9.5.sp, color = Slate500)
                            Text(text = "${reg.approvedEligibleQuantityQuintals} Qtl", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KisanGreenPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Est. Payout", fontSize = 9.5.sp, color = Slate500)
                            Text(text = "₹${String.format(Locale.US, "%,.0f", reg.estimatedTotalMspPayout)}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = KisanGreenDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Reg No: ${reg.id}", fontSize = 10.sp, color = Slate500)
                        Text(text = reg.registrationDate, fontSize = 10.sp, color = Slate500)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SECTION 3: Token / Slot Booking
// -------------------------------------------------------------------------------------
@Composable
private fun TokenBookingSection(
    farmerProfile: FarmerLandProfile,
    mspCrops: List<GovtMspCrop>,
    procurementCenters: List<ProcurementCenter>,
    tokenBookings: List<GovtTokenBooking>,
    currentLanguage: AppLanguage,
    onOpenBookDialog: () -> Unit,
    onCancelBooking: (GovtTokenBooking) -> Unit,
    onShareToken: (GovtTokenBooking) -> Unit,
    onDownloadToken: (GovtTokenBooking) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Booking CTA
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KisanGreenDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Government Token / Slot Booking",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Select nearby purchase center & arrive with verified token.",
                                fontSize = 11.sp,
                                color = Slate200
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎟️", fontSize = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenBookDialog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("book_new_slot_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = KisanSaffron),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Book New Delivery Slot",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Active Tokens List Header
        item {
            Text(
                text = "My Digital Procurement Tokens (${tokenBookings.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Slate900
            )
        }

        if (tokenBookings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎟️", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "No active tokens booked yet", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        Text(text = "Tap '+ Book New Delivery Slot' above to reserve your date.", fontSize = 11.sp, color = Slate600)
                    }
                }
            }
        } else {
            items(tokenBookings) { token ->
                DigitalTokenCard(
                    token = token,
                    onShare = { onShareToken(token) },
                    onDownload = { onDownloadToken(token) },
                    onCancel = { onCancelBooking(token) }
                )
            }
        }
    }
}

// Digital Token Card Component with QR Code Canvas
@Composable
private fun DigitalTokenCard(
    token: GovtTokenBooking,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate300, RoundedCornerShape(16.dp))
            .testTag("digital_token_card_${token.tokenNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Bar: Token ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GOVT PROCUREMENT TOKEN",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "#${token.tokenNumber}",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = KisanGreenDark
                    )
                }

                val statusColor = Color(token.status.badgeColorHex)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = token.status.label,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dashed Divider
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            ) {
                drawLine(
                    color = Slate300,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Section: Farmer details & QR representation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Farmer Name & ID", fontSize = 9.5.sp, color = Slate500)
                    Text(text = token.farmerName, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(text = "ID: ${token.farmerId}", fontSize = 10.sp, color = Slate600)

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "Crop & Estimated Qty", fontSize = 9.5.sp, color = Slate500)
                    Text(
                        text = "${token.cropName} • ${token.estimatedQuantityQuintals} Qtl",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = KisanGreenPrimary
                    )
                    Text(text = "Official Rate: ₹${token.mspRate}/Qtl", fontSize = 10.sp, color = Slate600)
                }

                // Visual Stylized QR Code Box
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate100)
                        .border(1.dp, Slate300, RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 3f
                        // Simulated QR matrix blocks
                        drawRect(Color.Black, Offset(0f, 0f), Size(20f, 20f))
                        drawRect(Color.Black, Offset(size.width - 20f, 0f), Size(20f, 20f))
                        drawRect(Color.Black, Offset(0f, size.height - 20f), Size(20f, 20f))
                        drawRect(Color.Black, Offset(size.width / 2 - 6f, size.height / 2 - 6f), Size(12f, 12f))

                        drawRect(KisanGreenDark, Offset(26f, 10f), Size(16f, 6f))
                        drawRect(KisanGreenDark, Offset(10f, 26f), Size(6f, 16f))
                        drawRect(KisanGreenDark, Offset(size.width - 24f, 28f), Size(14f, 10f))
                        drawRect(KisanGreenDark, Offset(28f, size.height - 22f), Size(18f, 8f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Centre, Date, and Time Slot Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate50)
                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = KisanGreenDark, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = token.centreName,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                Text(
                    text = token.centreAddress,
                    fontSize = 10.sp,
                    color = Slate600,
                    modifier = Modifier.padding(start = 17.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 17.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📅 Date: ${token.bookingDate}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate800
                    )
                    Text(
                        text = "⏰ ${token.timeSlot}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KisanGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Download, Share, Cancel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp)
                }

                if (token.status == GovtTokenStatus.BOOKED) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TrendRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TrendRed.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Cancel", fontSize = 11.sp, color = TrendRed)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SECTION 4: Sell Crop to Government (6-Step Tracker & Official Receipt)
// -------------------------------------------------------------------------------------
@Composable
private fun SellCropTrackerSection(
    farmerProfile: FarmerLandProfile,
    receipts: List<GovtProcurementReceipt>,
    currentLanguage: AppLanguage,
    onPlayVoiceSummary: () -> Unit
) {
    val steps = listOf(
        Pair("1. Land & Farmer Verification", "Bhulekh / BanglarBhumi land record authenticated with Aadhaar e-KYC."),
        Pair("2. Crop Sowing Registration", "Selected crop area verified against government yield norms."),
        Pair("3. Digital Token Slot Booking", "Procurement centre, arrival date, and time slot reserved."),
        Pair("4. Crop Delivery & Quality Check", "Moisture testing (<12% for Wheat, <17% for Paddy) and FAQ grading."),
        Pair("5. Weighing & E-Receipt Issuance", "Electronic weighbridge weighment & official J-Form / W-Form generated."),
        Pair("6. Direct DBT Payment Credited", "100% MSP funds transferred directly to bank account via PFMS in 24-72 hrs.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 6-Step Visual Lifecycle Tracker
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                    .testTag("lifecycle_tracker_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚖️", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Govt Procurement Step Tracker",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Slate900
                            )
                        }

                        IconButton(
                            onClick = onPlayVoiceSummary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Outlined.VolumeUp, contentDescription = "Listen", tint = KisanGreenDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    steps.forEachIndexed { index, (title, desc) ->
                        val isCompleted = index <= 5 // All active demo steps completed for realistic inspection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isCompleted) TrendGreen else Slate300),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                if (index < steps.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(26.dp)
                                            .background(TrendGreen)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.padding(bottom = if (index < steps.size - 1) 10.dp else 0.dp)) {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = desc,
                                    fontSize = 10.5.sp,
                                    color = Slate600,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Official Procurement Settlement Receipts Header
        item {
            Text(
                text = "Procurement Receipts & DBT Settlements",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Slate900
            )
        }

        items(receipts) { receipt ->
            OfficialReceiptCard(receipt = receipt)
        }
    }
}

// Official Procurement Receipt Card (J-Form / W-Form)
@Composable
private fun OfficialReceiptCard(receipt: GovtProcurementReceipt) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(16.dp))
            .testTag("procurement_receipt_${receipt.receiptNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Receipt Top Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OFFICIAL MSP PROCUREMENT RECEIPT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = KisanGreenDark,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = receipt.receiptNumber,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TrendGreen.copy(alpha = 0.15f))
                        .border(1.dp, TrendGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "PAID VIA DBT",
                        color = TrendGreen,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Table
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate50)
                    .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReceiptRow(label = "Farmer Name & ID", value = "${receipt.farmerName} (${receipt.farmerId})")
                ReceiptRow(label = "Procurement Centre", value = receipt.procurementCentre)
                ReceiptRow(label = "Delivered Crop & Date", value = "${receipt.cropName} • ${receipt.deliveredDate}")
                Divider(color = Slate200)

                ReceiptRow(label = "Gross Delivered Qty", value = "${receipt.deliveredQuantityQuintals} Qtl")
                ReceiptRow(label = "Moisture Measured", value = "${receipt.moistureMeasuredPercent}% (FAQ Standard Passed)")
                ReceiptRow(label = "Accepted Qty", value = "${receipt.acceptedQuantityQuintals} Qtl", isBold = true)
                ReceiptRow(label = "Rejected / Deduction", value = "${receipt.rejectedQuantityQuintals} Qtl")
                Divider(color = Slate200)

                ReceiptRow(label = "Official MSP Rate", value = "₹${receipt.mspRatePerQuintal} / Quintal")
                ReceiptRow(label = "Mandi / Handling Deductions", value = "₹0.00 (Govt Covered)")
                ReceiptRow(
                    label = "Total Net Payout",
                    value = "₹${String.format(Locale.US, "%,.0f", receipt.netPayableInr)}",
                    isHighlight = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct DBT Bank Transfer Confirmation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(KisanGreenContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bank: ${receipt.creditedBankName} (${receipt.maskedAccountNumber})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KisanGreenDark
                    )
                    Text(
                        text = receipt.paymentStatus,
                        fontSize = 9.5.sp,
                        color = Slate700
                    )
                }

                Text(
                    text = "Ref: ${receipt.dbtReferenceNumber}",
                    fontSize = 9.sp,
                    color = Slate600,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Slate600
        )
        Text(
            text = value,
            fontSize = if (isHighlight) 13.sp else 11.5.sp,
            fontWeight = if (isHighlight || isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) TrendGreen else Slate900
        )
    }
}

// -------------------------------------------------------------------------------------
// SECTION 5: State-Specific Procurement Information
// -------------------------------------------------------------------------------------
@Composable
private fun StateProcurementInfoSection(
    stateInfo: GovtProcurementStateInfo,
    selectedState: String,
    onSelectState: (String) -> Unit,
    currentLanguage: AppLanguage
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // State Selector Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "State Procurement Guidelines & Portal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select state to view authentic land terminology, procurement target rules, and official portals:",
                        fontSize = 11.sp,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedState.contains("Uttar Pradesh"),
                            onClick = { onSelectState("Uttar Pradesh") },
                            label = { Text("Uttar Pradesh (e-Kharid)", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KisanGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        FilterChip(
                            selected = selectedState.contains("West Bengal"),
                            onClick = { onSelectState("West Bengal") },
                            label = { Text("West Bengal (e-Paddy)", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KisanGreenPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Official Portal Overview Card
        item {
            val context = LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KisanGreenContainer)
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
                        Text(
                            text = "🏛️ ${stateInfo.stateName}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = KisanGreenDark
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stateInfo.activeSeason,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Clickable Official Portal Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                            .clickable { AppActionHelper.openWebUrl(context, stateInfo.portalUrl) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("official_portal_link"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Official Portal",
                                fontSize = 10.sp,
                                color = Slate500,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stateInfo.portalName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenDark
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Portal",
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Clickable Toll-Free Helpline Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                            .clickable { AppActionHelper.openDialer(context, stateInfo.tollFreeHelpline) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("toll_free_helpline_call"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Toll-Free Helpline (Tappable)",
                                fontSize = 10.sp,
                                color = Slate500,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stateInfo.tollFreeHelpline,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Helpline",
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Primary MSP Crops: ${stateInfo.primaryCrops.joinToString(", ")}",
                        fontSize = 11.sp,
                        color = Slate700
                    )
                }
            }
        }

        // Land Record Terminology Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📖", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "State Land Record Terminology Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    stateInfo.landRecordTerminology.forEach { (term, explanation) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "• $term",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = KisanGreenDark
                            )
                            Text(
                                text = explanation,
                                fontSize = 10.5.sp,
                                color = Slate600,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Procurement Rules Checklist
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📋", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Key Procurement Guidelines",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Slate900
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    stateInfo.procurementGuidelines.forEachIndexed { idx, rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "${idx + 1}.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KisanGreenPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = rule, fontSize = 11.sp, color = Slate700, lineHeight = 14.sp)
                        }
                    }
                }
            }
        }

        // Demo Disclaimer Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🛡️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notice: This section provides realistic demo guidance. Always confirm final dates and quotas on the official state portals (${stateInfo.portalUrl}).",
                        fontSize = 10.sp,
                        color = Slate600,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// SECTION 6: Help & Documents (Document Checklist & FAQs)
// -------------------------------------------------------------------------------------
@Composable
private fun HelpAndDocumentsSection(
    currentLanguage: AppLanguage,
    onSpeakFaq: (String, String) -> Unit
) {
    val documents = GovtProcurementData.requiredDocuments
    val faqs = GovtProcurementData.procurementFaqs

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Document Checklist Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                    .testTag("document_checklist_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📑", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mandatory Documents Checklist",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(KisanGreenContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "5 Items",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    documents.forEach { (docTitle, docDesc, isVerified) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isVerified) TrendGreen.copy(alpha = 0.15f) else Slate100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isVerified) Icons.Default.Check else Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if (isVerified) TrendGreen else Slate500,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = docTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = docDesc,
                                    fontSize = 10.sp,
                                    color = Slate600
                                )
                            }

                            if (isVerified) {
                                Text(
                                    text = "Verified",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrendGreen
                                )
                            }
                        }
                        Divider(color = Slate100, thickness = 0.8.dp)
                    }
                }
            }
        }

        // FAQs Section Header
        item {
            Text(
                text = "Frequently Asked Questions (FAQ)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Slate900
            )
        }

        items(faqs) { (question, answer) ->
            var isExpanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .testTag("faq_card_${question.take(15)}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "❓", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = question,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Slate900
                            )
                        }

                        IconButton(
                            onClick = { onSpeakFaq(question, answer) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Outlined.VolumeUp, contentDescription = "Listen", tint = KisanGreenDark, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = answer,
                            fontSize = 11.5.sp,
                            color = Slate700,
                            lineHeight = 15.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate50)
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        // Direct Helpline Support Card (Clickable Phone Numbers)
        item {
            val context = LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = KisanGreenContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📞", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Direct Farmer Helplines (Tap to Call)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = KisanGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val helplines = listOf(
                        Triple("National Kisan Call Centre (Toll Free)", "1800-180-1551", "All India Kisan Assistance"),
                        Triple("UP Food & Civil Supplies Support", "1800-1800-150", "Uttar Pradesh e-Kharid Desk"),
                        Triple("WB Food & Supplies Helpline", "1967", "West Bengal e-Paddy Desk")
                    )

                    helplines.forEach { (name, phone, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.85f))
                                .clickable { AppActionHelper.openDialer(context, phone) }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("helpline_call_$phone"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = desc,
                                    fontSize = 10.sp,
                                    color = Slate600
                                )
                                Text(
                                    text = "📞 $phone",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = KisanGreenDark
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = KisanGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// Helper Dialog: Book Slot Modal Dialog
// -------------------------------------------------------------------------------------
@Composable
private fun BookSlotModalDialog(
    farmerProfile: FarmerLandProfile,
    mspCrops: List<GovtMspCrop>,
    procurementCenters: List<ProcurementCenter>,
    onDismiss: () -> Unit,
    onConfirm: (crop: String, qty: Double, centreId: String, date: String, slot: String) -> Unit
) {
    var selectedCrop by remember { mutableStateOf(mspCrops.first()) }
    var quantityInput by remember { mutableStateOf("70") }
    var selectedCentre by remember { mutableStateOf(procurementCenters.first()) }
    var selectedDate by remember { mutableStateOf("29 Mar 2026") }
    var selectedSlot by remember { mutableStateOf("09:00 AM - 12:00 PM (Morning Slot)") }

    val dateOptions = listOf("28 Mar 2026", "29 Mar 2026", "30 Mar 2026", "31 Mar 2026", "02 Apr 2026")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎟️ Book Procurement Slot",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Slate900
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Farmer: ${farmerProfile.farmerName} (${farmerProfile.farmerId})",
                    fontSize = 11.sp,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Crop Picker
                Text(text = "Select Crop:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(mspCrops) { crop ->
                        FilterChip(
                            selected = selectedCrop.id == crop.id,
                            onClick = { selectedCrop = crop },
                            label = { Text("${crop.emoji} ${crop.cropNameEn}", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity Input
                OutlinedTextField(
                    value = quantityInput,
                    onValueChange = { quantityInput = it },
                    label = { Text("Quantity to bring (Quintals)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slot_booking_quantity_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    supportingText = {
                        Text("Rate: ₹${selectedCrop.mspPricePerQuintal}/Qtl", fontSize = 10.sp)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Select Centre
                Text(text = "Select Nearby Procurement Centre:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    procurementCenters.forEach { centre ->
                        val isSelected = selectedCentre.id == centre.id
                        Card(
                            onClick = { selectedCentre = centre },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) KisanGreenContainer else Slate50
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) KisanGreenPrimary else Slate200
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = centre.name,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) KisanGreenDark else Slate900
                                )
                                Text(
                                    text = "${centre.distanceKm} km away • ${centre.address}",
                                    fontSize = 10.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Select Date
                Text(text = "Select Date:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(dateOptions) { d ->
                        FilterChip(
                            selected = selectedDate == d,
                            onClick = { selectedDate = d },
                            label = { Text(d, fontSize = 10.5.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Select Time Slot
                Text(text = "Select Time Slot:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
                selectedCentre.availableSlots.forEach { slot ->
                    val isSelected = selectedSlot == slot
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSlot = slot },
                        label = { Text(slot, fontSize = 10.5.sp) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val parsedQty = quantityInput.toDoubleOrNull() ?: 70.0
                        onConfirm(selectedCrop.cropNameEn, parsedQty, selectedCentre.id, selectedDate, selectedSlot)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("confirm_slot_booking_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Generate & Confirm Token", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------
// Helper Component: Detail Item
// -------------------------------------------------------------------------------------
@Composable
private fun DetailItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = Slate500
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = if (isHighlight || isBold) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) KisanGreenPrimary else Slate800
        )
    }
}
