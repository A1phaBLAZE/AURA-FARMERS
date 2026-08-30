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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.LanguageManager
import com.example.data.model.*
import com.example.ui.components.CreateLotDialog
import com.example.ui.components.PaymentModal
import com.example.ui.theme.*
import java.util.Locale

enum class LotsSectionFilter {
    ALL,
    MY_LOTS,
    BUYER_OFFERS,
    ESCROW_PAYMENTS
}

@Composable
fun LotsScreen(
    lots: List<FarmerLot>,
    offers: List<BuyerOffer>,
    payments: List<RazorpayPayment>,
    currentLanguage: AppLanguage,
    showCreateLotDialog: Boolean,
    onToggleCreateLotDialog: (Boolean) -> Unit,
    onCreateLot: (
        cropName: String,
        variety: String,
        grade: String,
        quantity: Double,
        price: Int,
        district: String,
        taluka: String,
        storage: String
    ) -> Unit,
    onAcceptOffer: (BuyerOffer) -> Unit,
    onRejectOffer: (BuyerOffer) -> Unit,
    onCounterOffer: (BuyerOffer, Int) -> Unit,
    activePaymentModal: RazorpayPayment?,
    onShowPaymentModal: (RazorpayPayment?) -> Unit,
    onReleaseFunds: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(LotsSectionFilter.ALL) }
    var offerToCounter by remember { mutableStateOf<BuyerOffer?>(null) }

    val pendingOffersCount = offers.count { it.status == OfferStatus.PENDING }
    val totalEscrowLocked = payments
        .filter { it.status == PaymentStatus.ESCROW_LOCKED }
        .sumOf { it.amountInr }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Trust & Escrow Guarantee Hero Card
            item {
                EscrowGuaranteeHeroCard(
                    lotsCount = lots.size,
                    pendingOffersCount = pendingOffersCount,
                    totalEscrowLocked = totalEscrowLocked,
                    currentLanguage = currentLanguage
                )
            }

            // 2. Interactive Segment Filter Chips
            item {
                LotsFilterChipRow(
                    selectedFilter = selectedFilter,
                    onFilterSelect = { selectedFilter = it },
                    lotsCount = lots.size,
                    offersCount = offers.size,
                    paymentsCount = payments.size,
                    currentLanguage = currentLanguage
                )
            }

            // 3. Dynamic Section Content based on Filter
            when (selectedFilter) {
                LotsSectionFilter.ALL -> {
                    // SECTION A: Pending Buyer Bids & Offers (if any)
                    item {
                        SectionHeader(
                            title = LanguageManager.getString("incoming_buyer_offers", currentLanguage),
                            badgeText = "${offers.size} Offers",
                            badgeColor = if (pendingOffersCount > 0) KisanSaffron else Slate500,
                            actionText = if (offers.size > 2) "View All" else null,
                            onActionClick = { selectedFilter = LotsSectionFilter.BUYER_OFFERS }
                        )
                    }

                    if (offers.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.Handshake,
                                title = "No pending buyer offers yet",
                                subtitle = "Institutional buyers will make direct bids on your listed harvest."
                            )
                        }
                    } else {
                        items(offers, key = { it.id }) { offer ->
                            BuyerOfferCard(
                                offer = offer,
                                currentLanguage = currentLanguage,
                                onAccept = { onAcceptOffer(offer) },
                                onReject = { onRejectOffer(offer) },
                                onInitiateCounter = { offerToCounter = offer }
                            )
                        }
                    }

                    // SECTION B: Active Harvest Lots Listed
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader(
                            title = LanguageManager.getString("active_lots", currentLanguage),
                            badgeText = "${lots.size} Listed",
                            badgeColor = KisanGreenPrimary,
                            actionText = if (lots.size > 2) "View All" else null,
                            onActionClick = { selectedFilter = LotsSectionFilter.MY_LOTS }
                        )
                    }

                    if (lots.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.Agriculture,
                                title = "No lots listed yet",
                                subtitle = "Tap the '+ List Harvest Lot' button to register your produce for direct sale.",
                                actionButtonText = "+ List Harvest Lot",
                                onActionClick = { onToggleCreateLotDialog(true) }
                            )
                        }
                    } else {
                        items(lots, key = { it.id }) { lot ->
                            FarmerLotCard(
                                lot = lot,
                                currentLanguage = currentLanguage,
                                onFilterOffersForLot = {
                                    selectedFilter = LotsSectionFilter.BUYER_OFFERS
                                }
                            )
                        }
                    }

                    // SECTION C: Razorpay Escrow & Settlement History
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        SectionHeader(
                            title = LanguageManager.getString("razorpay_title", currentLanguage),
                            badgeText = "${payments.size} Records",
                            badgeColor = KisanGreenDark,
                            actionText = if (payments.size > 2) "View All" else null,
                            onActionClick = { selectedFilter = LotsSectionFilter.ESCROW_PAYMENTS }
                        )
                    }

                    if (payments.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.AccountBalance,
                                title = "No payment records found",
                                subtitle = "Settlement records appear here once buyers deposit funds to Escrow."
                            )
                        }
                    } else {
                        items(payments, key = { it.id }) { payment ->
                            PaymentItemCard(
                                payment = payment,
                                onClick = { onShowPaymentModal(payment) }
                            )
                        }
                    }
                }

                LotsSectionFilter.MY_LOTS -> {
                    item {
                        SectionHeader(
                            title = LanguageManager.getString("active_lots", currentLanguage),
                            badgeText = "${lots.size} Active Lots",
                            badgeColor = KisanGreenPrimary
                        )
                    }

                    if (lots.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.Agriculture,
                                title = "No harvest lots listed yet",
                                subtitle = "Sell directly to verified institutional buyers, FPOs, and traders with zero mandi brokerage.",
                                actionButtonText = "+ Create Your First Lot",
                                onActionClick = { onToggleCreateLotDialog(true) }
                            )
                        }
                    } else {
                        items(lots, key = { it.id }) { lot ->
                            FarmerLotCard(
                                lot = lot,
                                currentLanguage = currentLanguage,
                                onFilterOffersForLot = {
                                    selectedFilter = LotsSectionFilter.BUYER_OFFERS
                                }
                            )
                        }
                    }
                }

                LotsSectionFilter.BUYER_OFFERS -> {
                    item {
                        SectionHeader(
                            title = LanguageManager.getString("incoming_buyer_offers", currentLanguage),
                            badgeText = "${offers.size} Received",
                            badgeColor = if (pendingOffersCount > 0) KisanSaffron else Slate500
                        )
                    }

                    if (offers.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.LocalOffer,
                                title = "No buyer offers at the moment",
                                subtitle = "Buyers and FPOs review newly listed lots continuously. Keep notifications enabled for instant bids."
                            )
                        }
                    } else {
                        items(offers, key = { it.id }) { offer ->
                            BuyerOfferCard(
                                offer = offer,
                                currentLanguage = currentLanguage,
                                onAccept = { onAcceptOffer(offer) },
                                onReject = { onRejectOffer(offer) },
                                onInitiateCounter = { offerToCounter = offer }
                            )
                        }
                    }
                }

                LotsSectionFilter.ESCROW_PAYMENTS -> {
                    item {
                        SectionHeader(
                            title = LanguageManager.getString("razorpay_title", currentLanguage),
                            badgeText = "₹${String.format(Locale.US, "%,.0f", totalEscrowLocked)} Protected",
                            badgeColor = KisanGreenDark
                        )
                    }

                    if (payments.isEmpty()) {
                        item {
                            EmptyStateCard(
                                icon = Icons.Default.Security,
                                title = "No escrow payments yet",
                                subtitle = "All transactions are secured with 100% upfront buyer escrow before harvest collection."
                            )
                        }
                    } else {
                        items(payments, key = { it.id }) { payment ->
                            PaymentItemCard(
                                payment = payment,
                                onClick = { onShowPaymentModal(payment) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button to List Harvest Lot
        ExtendedFloatingActionButton(
            onClick = { onToggleCreateLotDialog(true) },
            icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add Lot") },
            text = {
                Text(
                    text = LanguageManager.getString("create_lot_btn", currentLanguage).replace("+ ", ""),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            },
            containerColor = KisanGreenPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("floating_create_lot_btn")
        )
    }

    // Counter Offer Dialog
    if (offerToCounter != null) {
        val targetOffer = offerToCounter!!
        var counterPriceInput by remember(targetOffer.id) {
            mutableStateOf((targetOffer.counteredPrice ?: (targetOffer.offeredPricePerQuintal + 150)).toString())
        }

        Dialog(onDismissRequest = { offerToCounter = null }) {
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
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(KisanSaffronContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🤝", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = LanguageManager.getString("counter_offer", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Buyer: ${targetOffer.buyerName} (${targetOffer.buyerCompany})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Original Offer: ₹${targetOffer.offeredPricePerQuintal}/Quintal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = counterPriceInput,
                        onValueChange = { counterPriceInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Your Expected Rate (₹/Quintal)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("counter_price_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        supportingText = {
                            val parsed = counterPriceInput.toDoubleOrNull() ?: 0.0
                            Text(
                                text = "= ₹${String.format(Locale.US, "%.1f", parsed / 100.0)}/Kg",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { offerToCounter = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel / रद्द")
                        }

                        Button(
                            onClick = {
                                val price = counterPriceInput.toIntOrNull() ?: (targetOffer.offeredPricePerQuintal + 150)
                                onCounterOffer(targetOffer, price)
                                offerToCounter = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_counter_offer_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = KisanSaffron),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Send Bid", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showCreateLotDialog) {
        CreateLotDialog(
            currentLanguage = currentLanguage,
            onDismiss = { onToggleCreateLotDialog(false) },
            onSubmit = onCreateLot
        )
    }

    if (activePaymentModal != null) {
        PaymentModal(
            payment = activePaymentModal,
            currentLanguage = currentLanguage,
            onDismiss = { onShowPaymentModal(null) },
            onReleaseFunds = onReleaseFunds
        )
    }
}

// -------------------------------------------------------------
// 1. Escrow Guarantee & KPI Summary Hero Card
// -------------------------------------------------------------
@Composable
private fun EscrowGuaranteeHeroCard(
    lotsCount: Int,
    pendingOffersCount: Int,
    totalEscrowLocked: Double,
    currentLanguage: AppLanguage
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KisanGreenContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header with Trust Shield
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(KisanGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Escrow Shield",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageManager.getString("payment_guarantee", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = KisanGreenDark
                    )
                    Text(
                        text = "100% upfront buyer funds locked in Razorpay Escrow before farm dispatch",
                        fontSize = 9.5.sp,
                        color = Slate700,
                        lineHeight = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3-Metric KPI Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(1.dp, KisanGreenContainerBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric 1: Lots Listed
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Lots",
                        fontSize = 10.sp,
                        color = Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$lotsCount",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = KisanGreenDark
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(Slate200)
                )

                // Metric 2: Active Bids
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Buyer Bids",
                        fontSize = 10.sp,
                        color = Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$pendingOffersCount",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (pendingOffersCount > 0) KisanSaffron else Slate800
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(26.dp)
                        .background(Slate200)
                )

                // Metric 3: Escrow Protected
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "Escrow Locked",
                        fontSize = 10.sp,
                        color = Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%,.0f", totalEscrowLocked)}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Black,
                        color = KisanGreenPrimary
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Segmented Filter Chips Row
// -------------------------------------------------------------
@Composable
private fun LotsFilterChipRow(
    selectedFilter: LotsSectionFilter,
    onFilterSelect: (LotsSectionFilter) -> Unit,
    lotsCount: Int,
    offersCount: Int,
    paymentsCount: Int,
    currentLanguage: AppLanguage
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == LotsSectionFilter.ALL,
            onClick = { onFilterSelect(LotsSectionFilter.ALL) },
            label = {
                Text(
                    text = "All Overview",
                    fontSize = 12.sp,
                    fontWeight = if (selectedFilter == LotsSectionFilter.ALL) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = KisanGreenDark,
                selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("lots_tab_all")
        )

        FilterChip(
            selected = selectedFilter == LotsSectionFilter.MY_LOTS,
            onClick = { onFilterSelect(LotsSectionFilter.MY_LOTS) },
            label = {
                Text(
                    text = "My Lots ($lotsCount)",
                    fontSize = 12.sp,
                    fontWeight = if (selectedFilter == LotsSectionFilter.MY_LOTS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = KisanGreenDark,
                selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("lots_tab_my_lots")
        )

        FilterChip(
            selected = selectedFilter == LotsSectionFilter.BUYER_OFFERS,
            onClick = { onFilterSelect(LotsSectionFilter.BUYER_OFFERS) },
            label = {
                Text(
                    text = "Buyer Offers ($offersCount)",
                    fontSize = 12.sp,
                    fontWeight = if (selectedFilter == LotsSectionFilter.BUYER_OFFERS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = KisanGreenDark,
                selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("lots_tab_buyer_offers")
        )

        FilterChip(
            selected = selectedFilter == LotsSectionFilter.ESCROW_PAYMENTS,
            onClick = { onFilterSelect(LotsSectionFilter.ESCROW_PAYMENTS) },
            label = {
                Text(
                    text = "Escrow & Bank ($paymentsCount)",
                    fontSize = 12.sp,
                    fontWeight = if (selectedFilter == LotsSectionFilter.ESCROW_PAYMENTS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = KisanGreenDark,
                selectedLabelColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("lots_tab_escrow_payments")
        )
    }
}

// -------------------------------------------------------------
// 3. Section Header with Badge & Action
// -------------------------------------------------------------
@Composable
private fun SectionHeader(
    title: String,
    badgeText: String? = null,
    badgeColor: Color = KisanGreenPrimary,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
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
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Slate900
            )
            if (badgeText != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (actionText != null && onActionClick != null) {
            Text(
                text = "$actionText ➔",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = KisanGreenPrimary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

// -------------------------------------------------------------
// 4. Polished, Symmetrical FarmerLotCard
// -------------------------------------------------------------
@Composable
fun FarmerLotCard(
    lot: FarmerLot,
    currentLanguage: AppLanguage,
    onFilterOffersForLot: (() -> Unit)? = null
) {
    val ratePerKg = lot.expectedPricePerQuintal / 100.0
    val cropEmoji = getCropEmoji(lot.cropName)
    val hasOffers = lot.offersCount > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
            .testTag("lot_card_${lot.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Crop Avatar + Name & Variety + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Crop Avatar Box
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(KisanGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = cropEmoji, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = lot.cropName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Slate900
                            )
                        }
                        Text(
                            text = "${lot.variety} • ${lot.qualityGrade}",
                            fontSize = 11.5.sp,
                            color = Slate600
                        )
                    }
                }

                // Status Badge
                val statusContainerColor = when (lot.status) {
                    LotStatus.ACTIVE -> KisanGreenContainer
                    LotStatus.OFFERS_RECEIVED -> KisanSaffronContainer
                    LotStatus.ESCROW_LOCKED -> Color(0xFFFEF3C7)
                    LotStatus.PAYMENT_COMPLETED -> TrendGreen.copy(alpha = 0.15f)
                    LotStatus.DISPUTED -> Color(0xFFFEE2E2)
                }
                val statusTextColor = when (lot.status) {
                    LotStatus.ACTIVE -> KisanGreenDark
                    LotStatus.OFFERS_RECEIVED -> Color(0xFFB45309)
                    LotStatus.ESCROW_LOCKED -> Color(0xFF92400E)
                    LotStatus.PAYMENT_COMPLETED -> TrendGreen
                    LotStatus.DISPUTED -> Color(0xFFDC2626)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusContainerColor)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = lot.status.displayName,
                        color = statusTextColor,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Symmetrical 3-Column Metrics Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate50)
                    .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Quantity
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quantity / वजन",
                        fontSize = 10.sp,
                        color = Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = lot.getFormattedQuantity(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = if (lot.quantityQuintals >= 10.0) "${lot.quantityQuintals.toInt()} Qtl" else "${(lot.quantityQuintals * 100).toInt()} Kg",
                        fontSize = 9.5.sp,
                        color = Slate500
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Slate200)
                )

                // Column 2: Expected Rate
                Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                    Text(
                        text = "Expected Rate",
                        fontSize = 10.sp,
                        color = Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "₹${lot.expectedPricePerQuintal}/qtl",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KisanGreenPrimary
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%.1f", ratePerKg)}/kg",
                        fontSize = 9.5.sp,
                        color = Slate500
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Slate200)
                )

                // Column 3: Total Est. Value
                Column(modifier = Modifier.weight(1.1f), horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Est. Total Value",
                        fontSize = 10.sp,
                        color = Slate500,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "₹${String.format(Locale.US, "%,.0f", lot.getTotalEstimatedValue())}",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        color = KisanGreenDark
                    )
                    Text(
                        text = "Net Value",
                        fontSize = 9.5.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location, Storage, and Offers Notification Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Slate500,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${lot.locationTaluka}, ${lot.locationDistrict} • ${lot.storageType}",
                        fontSize = 10.5.sp,
                        color = Slate600
                    )
                }

                Text(
                    text = "ID: ${lot.id}",
                    fontSize = 10.sp,
                    color = Slate400,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Offers Banner Button (if offers exist)
            if (hasOffers && onFilterOffersForLot != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(KisanSaffronContainer)
                        .clickable(onClick = onFilterOffersForLot)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📩", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${lot.offersCount} Verified Buyer Offer(s) Received",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E)
                        )
                    }
                    Text(
                        text = "View Bids ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KisanSaffron
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. Polished, Symmetrical BuyerOfferCard
// -------------------------------------------------------------
@Composable
fun BuyerOfferCard(
    offer: BuyerOffer,
    currentLanguage: AppLanguage,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onInitiateCounter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
            .testTag("offer_card_${offer.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top: Buyer info & Price Callout
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(KisanGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏢", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = offer.buyerName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Slate900
                            )
                            if (offer.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = KisanGreenPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Text(
                            text = "${offer.buyerCompany} • ★ ${offer.buyerRating}",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }
                }

                // Price Callout Box
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${offer.offeredPricePerQuintal}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = KisanGreenPrimary
                    )
                    Text(
                        text = "per Quintal",
                        fontSize = 9.5.sp,
                        color = Slate500
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Terms & Logistics Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate50)
                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pickup: ${offer.pickupDate}",
                        fontSize = 11.sp,
                        color = Slate800,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = KisanGreenPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = offer.paymentTerms,
                        fontSize = 10.5.sp,
                        color = KisanGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons based on Status
            when (offer.status) {
                OfferStatus.PENDING -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Main Accept Button
                        Button(
                            onClick = onAccept,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("accept_offer_btn_${offer.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = LanguageManager.getString("accept_offer", currentLanguage),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Secondary Actions Row (Counter & Decline)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onInitiateCounter,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KisanSaffron),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(KisanSaffron)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = "🤝 " + LanguageManager.getString("counter_offer", currentLanguage),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            OutlinedButton(
                                onClick = onReject,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFDC2626).copy(alpha = 0.5f))
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = LanguageManager.getString("reject_offer", currentLanguage),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                OfferStatus.COUNTERED -> {
                    val counterPrice = offer.counteredPrice ?: (offer.offeredPricePerQuintal + 150)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🔄 Counter Bid Submitted",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "Requested: ₹$counterPrice/qtl • Awaiting Buyer Response",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Button(
                                onClick = onInitiateCounter,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Edit Bid", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }

                OfferStatus.ACCEPTED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TrendGreen.copy(alpha = 0.15f))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ Offer Accepted • 100% Escrow Secured",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrendGreen
                        )
                    }
                }

                OfferStatus.REJECTED -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate100)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕ Offer Declined",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate600
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. Polished PaymentItemCard
// -------------------------------------------------------------
@Composable
fun PaymentItemCard(
    payment: RazorpayPayment,
    onClick: () -> Unit
) {
    val isReleased = payment.status == PaymentStatus.RELEASED_TO_FARMER

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("payment_card_${payment.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isReleased) TrendGreen.copy(alpha = 0.15f) else KisanSaffronContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isReleased) Icons.Default.CheckCircle else Icons.Default.LockClock,
                        contentDescription = null,
                        tint = if (isReleased) TrendGreen else KisanSaffron,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = payment.buyerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Slate900
                    )
                    Text(
                        text = "${payment.cropName} • ${payment.timestamp}",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                    Text(
                        text = "UTR: ${payment.utrNumber}",
                        fontSize = 9.5.sp,
                        color = Slate400
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format(Locale.US, "%,.0f", payment.amountInr)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (isReleased) TrendGreen else KisanGreenPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isReleased) TrendGreen.copy(alpha = 0.12f) else KisanSaffronContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isReleased) "Paid to Bank" else "In Escrow",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isReleased) TrendGreen else KisanSaffron
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. Empty State Card Component
// -------------------------------------------------------------
@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate800
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = Slate500,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 15.sp
            )

            if (actionButtonText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = actionButtonText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. Helper Functions
// -------------------------------------------------------------
private fun getCropEmoji(cropName: String): String {
    val lower = cropName.lowercase()
    return when {
        lower.contains("onion") -> "🧅"
        lower.contains("soybean") -> "🌱"
        lower.contains("tomato") -> "🍅"
        lower.contains("garlic") -> "🧄"
        lower.contains("pomegranate") -> "🍎"
        lower.contains("cotton") -> "☁️"
        lower.contains("turmeric") -> "🟡"
        lower.contains("grape") -> "🍇"
        lower.contains("chilli") || lower.contains("chili") -> "🌶️"
        lower.contains("wheat") -> "🌾"
        lower.contains("rice") || lower.contains("paddy") -> "🍚"
        lower.contains("maize") || lower.contains("corn") -> "🌽"
        lower.contains("banana") -> "🍌"
        lower.contains("ginger") -> "🫚"
        lower.contains("potato") -> "🥔"
        else -> "🌾"
    }
}
