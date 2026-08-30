package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.data.model.AppLanguage
import com.example.data.model.PriceUnit
import com.example.data.model.QuantityUnit
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun UnitConversionBar(
    currentLanguage: AppLanguage,
    selectedPriceUnit: PriceUnit,
    onUnitSelect: (PriceUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    var showConverterDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Price Unit Selector Group (Compact responsive layout)
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Unit:",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700,
                    modifier = Modifier.padding(end = 1.dp)
                )

                PriceUnit.values().forEach { unit ->
                    val isSelected = unit == selectedPriceUnit
                    val icon = when (unit) {
                        PriceUnit.KG -> "⚖️"
                        PriceUnit.QUINTAL -> "🌾"
                        PriceUnit.TON -> "🚛"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (isSelected) KisanGreenPrimary else Slate100)
                            .border(
                                1.dp,
                                if (isSelected) KisanGreenDark else Slate200,
                                RoundedCornerShape(5.dp)
                            )
                            .clickable { onUnitSelect(unit) }
                            .padding(horizontal = 5.dp, vertical = 2.5.dp)
                            .testTag("unit_pill_${unit.code}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$icon ${unit.getLabel(currentLanguage)}",
                                color = if (isSelected) Color.White else Slate700,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick Calculator Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(KisanGoldContainer)
                    .border(1.dp, Color(0xFFFDE047), RoundedCornerShape(5.dp))
                    .clickable { showConverterDialog = true }
                    .padding(horizontal = 5.dp, vertical = 2.5.dp)
                    .testTag("open_converter_calc_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Calculator",
                        tint = Color(0xFF854D0E),
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "कन्व्हर्टर",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF854D0E)
                    )
                }
            }
        }
    }

    if (showConverterDialog) {
        UnitConverterDialog(
            currentLanguage = currentLanguage,
            onDismiss = { showConverterDialog = false }
        )
    }
}

@Composable
fun UnitConverterDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit
) {
    var inputQuantityText by remember { mutableStateOf("2500") }
    var selectedInputUnit by remember { mutableStateOf(QuantityUnit.KG) }
    var samplePricePerQtlText by remember { mutableStateOf("2850") }

    val rawQuantity = inputQuantityText.toDoubleOrNull() ?: 0.0
    val samplePricePerQtl = samplePricePerQtlText.toDoubleOrNull() ?: 2850.0

    // Convert input to standard Quintals first
    val quantityInQuintals = rawQuantity * selectedInputUnit.toQuintalsMultiplier
    val quantityInKg = quantityInQuintals * 100.0
    val quantityInTons = quantityInQuintals / 10.0

    // Calculated values
    val totalValueInr = quantityInQuintals * samplePricePerQtl
    val ratePerKg = samplePricePerQtl / 100.0
    val ratePerTon = samplePricePerQtl * 10.0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(KisanGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Convert",
                                tint = KisanGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LanguageManager.getString("unit_calc_title", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            color = KisanGreenDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter Quantity to Convert (शेतमाल वजन टाका):",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputQuantityText,
                        onValueChange = { inputQuantityText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("converter_quantity_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Unit Selector Dropdown Pills
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        QuantityUnit.values().forEach { u ->
                            val isSel = u == selectedInputUnit
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) KisanGreenPrimary else Slate100)
                                    .clickable { selectedInputUnit = u }
                                    .padding(horizontal = 6.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = u.code.uppercase(),
                                    color = if (isSel) Color.White else Slate700,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instant Conversion Matrix
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = KisanBgLight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📐 समतुल्य वजन / Equivalent Conversion:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("⚖️ किलो (Kilograms):", fontSize = 12.sp, color = Slate700)
                            Text(
                                "${String.format(Locale.US, "%,.1f", quantityInKg)} Kg",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenDark
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🌾 क्विंटल (Quintals):", fontSize = 12.sp, color = Slate700)
                            Text(
                                "${String.format(Locale.US, "%,.2f", quantityInQuintals)} Qtl",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenDark
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🚛 मेट्रिक टन (Metric Tons):", fontSize = 12.sp, color = Slate700)
                            Text(
                                "${String.format(Locale.US, "%,.3f", quantityInTons)} Tons",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rate & Valuation Matrix
                OutlinedTextField(
                    value = samplePricePerQtlText,
                    onValueChange = { samplePricePerQtlText = it },
                    label = { Text("Mandi Price Reference (₹/Qtl)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = KisanGreenContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("₹/Kg Minimal Rate:", fontSize = 11.sp, color = Slate700)
                            Text("₹${String.format(Locale.US, "%.2f", ratePerKg)} / kg", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = KisanGreenDark)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("₹/Ton Bulk Rate:", fontSize = 11.sp, color = Slate700)
                            Text("₹${String.format(Locale.US, "%,.0f", ratePerTon)} / Ton", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = KisanGreenDark)
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp), color = KisanGreenContainerBorder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("एकूण किंमत / Total Value:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KisanGreenDark)
                            Text(
                                "₹${String.format(Locale.US, "%,.0f", totalValueInr)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = KisanGreenDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Done / बंद करा", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
