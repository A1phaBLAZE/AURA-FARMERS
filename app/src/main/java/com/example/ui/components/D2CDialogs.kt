package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun D2COrderCheckoutDialog(
    listing: D2CProduceListing,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirmOrder: (
        consumerName: String,
        consumerMobile: String,
        deliveryAddress: String,
        deliveryDistrict: String,
        deliveryPincode: String,
        quantityKg: Double,
        selectedSlot: String,
        paymentMethod: String
    ) -> Unit
) {
    var selectedKg by remember { mutableStateOf(listing.minOrderKg.coerceAtLeast(2.0)) }
    var consumerName by remember { mutableStateOf("Rohit Sharma") }
    var consumerMobile by remember { mutableStateOf("+91 98230 44921") }
    var deliveryAddress by remember { mutableStateOf("Flat 402, Green Meadows Tower, Gangapur Road") }
    var deliveryDistrict by remember { mutableStateOf("Nashik") }
    var deliveryPincode by remember { mutableStateOf("422013") }
    var selectedSlot by remember { mutableStateOf(listing.availablePickupSlots.firstOrNull() ?: "07:00 AM - 10:00 AM") }
    var selectedPaymentMode by remember { mutableStateOf("Pay on Delivery (Escrow Guarantee)") }

    val farmerAmount = selectedKg * listing.farmerBasePricePerKg
    val logisticsAmount = selectedKg * listing.logisticsFeePerKg
    val totalAmount = selectedKg * listing.totalPricePerKg
    val retailAmount = selectedKg * listing.typicalRetailPricePerKg
    val totalSavings = (retailAmount - totalAmount).coerceAtLeast(0.0)
    val savingsPct = if (retailAmount > 0) ((totalSavings / retailAmount) * 100.0).roundToInt() else 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(vertical = 16.dp)
                .testTag("d2c_order_checkout_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = listing.emoji, fontSize = 28.sp)
                        Column {
                            Text(
                                text = "Direct Farm Order",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${listing.cropName} (${listing.variety})",
                                style = MaterialTheme.typography.bodySmall,
                                color = KisanGreenPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_checkout_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                // Farmer & Freshness badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = KisanGreenPrimary.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KisanGreenPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Eco, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(22.dp))
                        Column {
                            Text(
                                text = "Direct from ${listing.farmerName} • ${listing.farmNameOrFpo}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "📍 ${listing.village}, ${listing.district} • ${listing.harvestFreshness}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Quantity Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Select Quantity (Kg):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listing.packSizesKg.forEach { size ->
                            val isSelected = selectedKg == size
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) KisanGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedKg = size }
                                    .testTag("qty_chip_${size.toInt()}kg")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${size.toInt()} Kg",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // TRANSPARENT PRICING BREAKDOWN CARD
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth().testTag("transparent_price_breakdown_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF0F172A), modifier = Modifier.size(16.dp))
                            Text(
                                text = "Transparent Price Breakdown",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        // Breakdown lines
                        PriceLineItem(
                            label = "Farmer Payout (100% Direct)",
                            rate = "₹${listing.farmerBasePricePerKg}/kg",
                            amount = "₹${farmerAmount.roundToInt()}",
                            highlightColor = KisanGreenPrimary
                        )
                        PriceLineItem(
                            label = "Agri-Logistics & Cold-Chain",
                            rate = "₹${listing.logisticsFeePerKg}/kg",
                            amount = "₹${logisticsAmount.roundToInt()}",
                            highlightColor = Color(0xFF64748B)
                        )

                        HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.8.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total D2C Price (${selectedKg.toInt()} kg)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "₹${totalAmount.roundToInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = KisanGreenPrimary
                            )
                        }

                        // Savings comparison banner
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
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
                                    Text(text = "🛒 Typical Supermarket: ₹${retailAmount.roundToInt()}", fontSize = 11.sp, color = Color(0xFF166534))
                                }
                                Text(
                                    text = "Save $savingsPct% (₹${totalSavings.roundToInt()})",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                }

                // Delivery Info Fields
                OutlinedTextField(
                    value = consumerName,
                    onValueChange = { consumerName = it },
                    label = { Text("Your Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_consumer_name")
                )

                OutlinedTextField(
                    value = consumerMobile,
                    onValueChange = { consumerMobile = it },
                    label = { Text("Mobile Number for OTP & Delivery") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_consumer_mobile")
                )

                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery Address / Society / Landmark") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("input_consumer_address")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = deliveryDistrict,
                        onValueChange = { deliveryDistrict = it },
                        label = { Text("District/City") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("input_consumer_district")
                    )
                    OutlinedTextField(
                        value = deliveryPincode,
                        onValueChange = { deliveryPincode = it },
                        label = { Text("Pincode") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.8f).testTag("input_consumer_pincode")
                    )
                }

                // Delivery Slot Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Preferred Morning/Afternoon Slot:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    listing.availablePickupSlots.forEach { slot ->
                        val isSlotSelected = selectedSlot == slot
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSlotSelected) KisanGreenPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSlotSelected) KisanGreenPrimary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSlot = slot }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RadioButton(
                                    selected = isSlotSelected,
                                    onClick = { selectedSlot = slot },
                                    colors = RadioButtonDefaults.colors(selectedColor = KisanGreenPrimary)
                                )
                                Text(
                                    text = slot,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSlotSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Payment Terms Guarantee (Escrow Security)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(20.dp))
                        Column {
                            Text(
                                text = "100% Escrow Protection",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E3A8A)
                            )
                            Text(
                                text = "Pay on delivery via Cash or UPI. Farmer payout is released only upon doorstep inspection.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).testTag("cancel_order_btn")
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onConfirmOrder(
                                consumerName,
                                consumerMobile,
                                deliveryAddress,
                                deliveryDistrict,
                                deliveryPincode,
                                selectedKg,
                                selectedSlot,
                                selectedPaymentMode
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                        modifier = Modifier.weight(1.5f).testTag("confirm_d2c_order_btn")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Place Order (₹${totalAmount.roundToInt()})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceLineItem(
    label: String,
    rate: String,
    amount: String,
    highlightColor: Color
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
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(highlightColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF334155)
            )
            Text(
                text = "($rate)",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
    }
}

@Composable
fun FarmerLogisticsConfigDialog(
    currentConfig: FarmerLogisticsConfig,
    onDismiss: () -> Unit,
    onSaveConfig: (FarmerLogisticsConfig) -> Unit
) {
    var deliveryRadius by remember { mutableStateOf(currentConfig.deliveryRadiusKm) }
    var maxDailyCapacity by remember { mutableStateOf(currentConfig.maxDailyCapacityKg.toString()) }
    var slotMorning by remember { mutableStateOf(true) }
    var slotAfternoon by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(vertical = 16.dp)
                .testTag("farmer_logistics_config_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        Text(
                            text = "Logistics & Pickup Slots",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider()

                Text(
                    text = "Configure farm dispatch radius and daily availability for consumer and FPO orders.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Delivery Radius Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Delivery Radius:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${deliveryRadius.toInt()} km",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = KisanGreenPrimary
                        )
                    }
                    Slider(
                        value = deliveryRadius.toFloat(),
                        onValueChange = { deliveryRadius = it.toDouble().roundToInt().toDouble() },
                        valueRange = 10f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = KisanGreenPrimary,
                            activeTrackColor = KisanGreenPrimary
                        ),
                        modifier = Modifier.testTag("logistics_radius_slider")
                    )
                }

                // Max Daily Capacity
                OutlinedTextField(
                    value = maxDailyCapacity,
                    onValueChange = { maxDailyCapacity = it },
                    label = { Text("Max Daily Fulfillment Capacity (Kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_max_capacity")
                )

                // Pickup Slots Checkboxes
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Available Pickup Windows:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { slotMorning = !slotMorning }
                    ) {
                        Checkbox(
                            checked = slotMorning,
                            onCheckedChange = { slotMorning = it },
                            colors = CheckboxDefaults.colors(checkedColor = KisanGreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("06:30 AM - 09:30 AM (Morning Fresh Run)", fontSize = 12.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { slotAfternoon = !slotAfternoon }
                    ) {
                        Checkbox(
                            checked = slotAfternoon,
                            onCheckedChange = { slotAfternoon = it },
                            colors = CheckboxDefaults.colors(checkedColor = KisanGreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("02:00 PM - 05:00 PM (Afternoon Express Run)", fontSize = 12.sp)
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val slots = mutableListOf<String>()
                            if (slotMorning) slots.add("06:30 AM - 09:30 AM (Morning Fresh Run)")
                            if (slotAfternoon) slots.add("02:00 PM - 05:00 PM (Afternoon Express Run)")
                            val cap = maxDailyCapacity.toDoubleOrNull() ?: currentConfig.maxDailyCapacityKg
                            onSaveConfig(
                                currentConfig.copy(
                                    deliveryRadiusKm = deliveryRadius,
                                    maxDailyCapacityKg = cap,
                                    availablePickupSlots = if (slots.isEmpty()) listOf("07:00 AM - 10:00 AM") else slots
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                        modifier = Modifier.weight(1.5f).testTag("save_logistics_config_btn")
                    ) {
                        Text("Save Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
