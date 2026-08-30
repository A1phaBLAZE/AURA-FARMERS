package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLotDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (
        cropName: String,
        variety: String,
        grade: String,
        quantity: Double,
        price: Int,
        district: String,
        taluka: String,
        storage: String
    ) -> Unit
) {
    val cropOptions = listOf(
        "Onion (Red)",
        "Soybean (Yellow)",
        "Tomato (Hybrid)",
        "Garlic (Desi)",
        "Pomegranate (Bhagwa)",
        "Cotton (BT)",
        "Turmeric (Salem)",
        "Grapes (Thomson)",
        "Chilli (Teja)"
    )
    var selectedCrop by remember { mutableStateOf(cropOptions.first()) }
    var cropExpanded by remember { mutableStateOf(false) }

    var variety by remember { mutableStateOf("") }
    var qualityGrade by remember { mutableStateOf("Grade A+ (Export / Standard)") }

    // Unit Support: Minimal (Kg), Standard (Quintal), Bulk (Ton)
    var selectedQuantityUnit by remember { mutableStateOf(QuantityUnit.QUINTAL) }
    var selectedPriceUnit by remember { mutableStateOf(PriceUnit.QUINTAL) }

    var quantityText by remember { mutableStateOf("50") }
    var priceText by remember { mutableStateOf("2850") }
    var district by remember { mutableStateOf("Nashik") }
    var taluka by remember { mutableStateOf("Niphad") }
    var storageType by remember { mutableStateOf("Ventilated Farm Chawl / Warehouse") }

    val rawQuantity = quantityText.toDoubleOrNull() ?: 0.0
    val rawPrice = priceText.toDoubleOrNull() ?: 0.0

    // Converted to standard Quintals
    val canonicalQuintals = rawQuantity * selectedQuantityUnit.toQuintalsMultiplier
    val canonicalPricePerQuintal = rawPrice * selectedPriceUnit.toQuintalPriceMultiplier
    val totalEstimatedValue = canonicalQuintals * canonicalPricePerQuintal

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌾 " + LanguageManager.getString("create_lot_btn", currentLanguage).replace("+ ", ""),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KisanGreenPrimary
                    )
                }

                Text(
                    text = "Direct connection to verified buyers (Minimal & Bulk support)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Crop Dropdown
                ExposedDropdownMenuBox(
                    expanded = cropExpanded,
                    onExpandedChange = { cropExpanded = !cropExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCrop,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Crop / पीक निवडा") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cropExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = cropExpanded,
                        onDismissRequest = { cropExpanded = false }
                    ) {
                        cropOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedCrop = option
                                    cropExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Variety & Quality Grade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = variety,
                        onValueChange = { variety = it },
                        label = { Text("Variety (वाण)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = qualityGrade,
                        onValueChange = { qualityGrade = it },
                        label = { Text("Grade / प्रत") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // QUANTITY INPUT WITH UNIT SELECTION (Kg, Qtl, Ton)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate100)
                        .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "वजन एकक (Quantity Unit):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )

                        // Unit Selector Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            QuantityUnit.values().forEach { unit ->
                                val isSelected = unit == selectedQuantityUnit
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) KisanGreenPrimary else Color.White)
                                        .border(1.dp, if (isSelected) KisanGreenPrimary else Slate200, RoundedCornerShape(6.dp))
                                        .clickable { selectedQuantityUnit = unit }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                        .testTag("lot_qty_unit_${unit.code}")
                                ) {
                                    Text(
                                        text = unit.getLabel(currentLanguage),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Slate700
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Enter Quantity in ${selectedQuantityUnit.getLabel(currentLanguage)}") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lot_quantity_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // PRICE INPUT WITH PRICE UNIT SELECTION
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate100)
                        .border(1.dp, Slate200, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "दर एकक (Rate Unit):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )

                        // Price Unit Selector Pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PriceUnit.values().forEach { unit ->
                                val isSelected = unit == selectedPriceUnit
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) KisanGreenPrimary else Color.White)
                                        .border(1.dp, if (isSelected) KisanGreenPrimary else Slate200, RoundedCornerShape(6.dp))
                                        .clickable { selectedPriceUnit = unit }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                        .testTag("lot_price_unit_${unit.code}")
                                ) {
                                    Text(
                                        text = unit.getLabel(currentLanguage),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Slate700
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Expected Price (${selectedPriceUnit.getLabel(currentLanguage)})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lot_price_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time Conversion and Valuation Preview Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = KisanGreenContainer)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = KisanGreenDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Standardized Lot Size:", fontSize = 11.sp, color = KisanGreenDark, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                "${String.format(Locale.US, "%.1f", canonicalQuintals)} Qtl / ${String.format(Locale.US, "%.2f", canonicalQuintals / 10.0)} Tons",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = KisanGreenDark
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("एकूण अंदाजित रक्कम (Total Value):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text(
                                "₹${String.format(Locale.US, "%,.0f", totalEstimatedValue)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = KisanGreenDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Location & Storage
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = taluka,
                        onValueChange = { taluka = it },
                        label = { Text("Taluka") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = storageType,
                    onValueChange = { storageType = it },
                    label = { Text("Storage Facility (Farm Chawl / Silo / Cold)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSubmit(
                                selectedCrop,
                                variety.ifBlank { "Standard Local" },
                                qualityGrade,
                                canonicalQuintals,
                                canonicalPricePerQuintal.toInt(),
                                district.ifBlank { "Nashik" },
                                taluka.ifBlank { "Niphad" },
                                storageType
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("submit_lot_btn")
                    ) {
                        Text("Publish Harvest Lot", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
