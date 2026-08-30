package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.model.*
import com.example.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun DeliveryTrackerCard(
    order: D2COrder,
    currentLanguage: AppLanguage,
    onAdvanceStatus: (D2COrder) -> Unit,
    onReleaseEscrow: (D2COrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("delivery_tracker_card_${order.orderId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Order Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = order.emoji, fontSize = 28.sp)
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${order.cropName} (${order.quantityKg.toInt()} kg)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(order.status.colorHex).copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(order.status.colorHex).copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = order.status.getTitle(currentLanguage),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(order.status.colorHex)
                                )
                            }
                        }
                        Text(
                            text = "Order #${order.orderId.takeLast(6)} • ${order.farmerName} (${order.farmVillage})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp).testTag("toggle_expand_order_${order.orderId}")
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand/Collapse"
                    )
                }
            }

            // ETA & Driver Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "ETA: ${order.estimatedDeliveryTime} (${order.distanceKm} km away)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = "OTP: ${order.otpForDelivery}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0284C7)
                    )
                }
            }

            // 4-Step Visual Stepper
            DeliveryStatusStepper(currentStatus = order.status)

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Driver info and vehicle details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ElectricCar, contentDescription = null, tint = KisanGreenPrimary, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    text = order.deliveryPartnerName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${order.vehicleNumber} • ${order.deliveryPartnerContact}",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "₹${order.totalAmountInr.roundToInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = KisanGreenPrimary
                        )
                    }

                    // Escrow Status & Release Action
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (order.isPaymentReleasedToFarmer) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (order.isPaymentReleasedToFarmer) Color(0xFF86EFAC) else Color(0xFFFDE68A)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    if (order.isPaymentReleasedToFarmer) Icons.Default.CheckCircle else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (order.isPaymentReleasedToFarmer) Color(0xFF15803D) else Color(0xFFB45309),
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = if (order.isPaymentReleasedToFarmer) "Farmer Payout Released" else "Payment Locked in Escrow",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (order.isPaymentReleasedToFarmer) Color(0xFF166534) else Color(0xFF92400E)
                                    )
                                    Text(
                                        text = if (order.isPaymentReleasedToFarmer)
                                            "₹${order.farmerPayoutInr.roundToInt()} credited directly to ${order.farmerName}"
                                        else
                                            "Funds held safe until delivery & quality verification",
                                        fontSize = 10.sp,
                                        color = if (order.isPaymentReleasedToFarmer) Color(0xFF15803D) else Color(0xFF78350F)
                                    )
                                }
                            }

                            if (!order.isPaymentReleasedToFarmer && order.status == DeliveryStatus.DELIVERED) {
                                Button(
                                    onClick = { onReleaseEscrow(order) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("release_escrow_btn_${order.orderId}")
                                ) {
                                    Text("Release Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Interactive Status Simulation Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (order.status != DeliveryStatus.DELIVERED) {
                            OutlinedButton(
                                onClick = { onAdvanceStatus(order) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("advance_delivery_status_btn_${order.orderId}"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KisanGreenPrimary)
                            ) {
                                Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                val nextStepLabel = when (order.status) {
                                    DeliveryStatus.BOOKED -> "Simulate: Mark Picked Up"
                                    DeliveryStatus.PICKED_UP -> "Simulate: Mark In Transit"
                                    DeliveryStatus.IN_TRANSIT -> "Simulate: Mark Delivered"
                                    else -> "Update Status"
                                }
                                Text(nextStepLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliveryStatusStepper(currentStatus: DeliveryStatus) {
    val steps = listOf(
        DeliveryStatus.BOOKED to "Booked",
        DeliveryStatus.PICKED_UP to "Picked Up",
        DeliveryStatus.IN_TRANSIT to "In Transit",
        DeliveryStatus.DELIVERED to "Delivered"
    )

    val currentIdx = currentStatus.stepIndex

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, (stepStatus, label) ->
            val isDone = currentIdx >= idx
            val isCurrent = currentIdx == idx
            val color = if (isDone) Color(stepStatus.colorHex) else Color(0xFFCBD5E1)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 26.dp else 22.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            if (isCurrent) 2.dp else 0.dp,
                            if (isCurrent) Color.White else Color.Transparent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isDone) Color(0xFF0F172A) else Color(0xFF94A3B8)
                )
            }

            if (idx < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.5.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (currentIdx > idx) Color(stepStatus.colorHex) else Color(0xFFE2E8F0),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun DemandForecastChartCard(
    forecast: CommodityDemandForecast,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("demand_forecast_chart_card_${forecast.commodity}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = forecast.emoji, fontSize = 28.sp)
                    Column {
                        Text(
                            text = forecast.commodity,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${forecast.district}, ${forecast.state} • ${forecast.totalMonthlyDemandTonnes} T/Month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(forecast.demandTrend.colorHex).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(forecast.demandTrend.colorHex).copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = forecast.demandTrend.iconEmoji, fontSize = 11.sp)
                        Text(
                            text = forecast.demandTrend.getLabel(currentLanguage),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(forecast.demandTrend.colorHex)
                        )
                    }
                }
            }

            // Consumer (D2C) vs Bulk (B2B) Split Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🛒 Consumer Retail (D2C): ${forecast.consumerHouseholdDemandPercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = "🏢 Institutional B2B: ${forecast.bulkInstitutionalDemandPercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7C3AED)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(forecast.consumerHouseholdDemandPercent.toFloat())
                            .fillMaxHeight()
                            .background(Color(0xFF0284C7))
                    )
                    Box(
                        modifier = Modifier
                            .weight(forecast.bulkInstitutionalDemandPercent.toFloat())
                            .fillMaxHeight()
                            .background(Color(0xFF7C3AED))
                    )
                }
            }

            // 4-Week Trajectory Visualizer
            Text(
                text = "4-Week Projected Demand vs AGMARKNET Arrivals (Tonnes):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxTonnes = forecast.weeklyForecasts.maxOfOrNull { it.projectedDemandTonnes } ?: 100.0

                forecast.weeklyForecasts.forEach { week ->
                    val demandHeightRatio = (week.projectedDemandTonnes / maxTonnes).toFloat().coerceIn(0.15f, 1f)
                    val arrivalHeightRatio = (week.expectedAgmarknetArrivalTonnes / maxTonnes).toFloat().coerceIn(0.15f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Projected Demand bar (Blue)
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .fillMaxHeight(demandHeightRatio)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(Color(0xFF0284C7))
                            )
                            // Mandi arrival bar (Green/Yellow)
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .fillMaxHeight(arrivalHeightRatio)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(Color(0xFF16A34A))
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = week.weekLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        Text(text = "${week.projectedDemandTonnes.toInt()}T", fontSize = 8.5.sp, color = Color(0xFF64748B))
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF0284C7), CircleShape))
                    Text("Projected Demand", fontSize = 10.sp, color = Color(0xFF475569))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF16A34A), CircleShape))
                    Text("AGMARKNET Mandi Arrivals", fontSize = 10.sp, color = Color(0xFF475569))
                }
            }

            // AI Actionable Advisory Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(15.dp))
                        Text(
                            text = "AI Sourcing & Retail Advisory",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E40AF)
                        )
                    }
                    Text(
                        text = forecast.getSourcingSummary(currentLanguage),
                        fontSize = 11.sp,
                        color = Color(0xFF1E3A8A),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
