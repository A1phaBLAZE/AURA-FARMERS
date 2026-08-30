package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LanguageManager
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onLoginFarmer: (fullName: String, mobile: String, district: String, kisanId: String) -> Unit,
    onLoginConsumer: (fullName: String, mobile: String, address: String, district: String) -> Unit,
    onLoginBuyer: (company: String, representative: String, mobile: String) -> Unit,
    onLoginTrader: (traderName: String, mandiName: String, licenseNo: String) -> Unit,
    onQuickDemoLogin: (UserRole) -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.FARMER) }

    // Farmer Form Fields
    var farmerName by remember { mutableStateOf("Ramesh Baburao Patil (रमेश पाटील)") }
    var farmerMobile by remember { mutableStateOf("9823456789") }
    var farmerDistrict by remember { mutableStateOf("Nashik (नाशिक)") }
    var kisanId by remember { mutableStateOf("MH-MSINS-784920") }
    var otpText by remember { mutableStateOf("4029") }
    var otpSent by remember { mutableStateOf(true) }

    // Consumer Form Fields
    var consumerName by remember { mutableStateOf("Rohit Sharma (रोहित शर्मा)") }
    var consumerMobile by remember { mutableStateOf("9823044921") }
    var consumerAddress by remember { mutableStateOf("Flat 402, Green Meadows Tower, Gangapur Road") }
    var consumerDistrict by remember { mutableStateOf("Nashik") }

    // Buyer Form Fields
    val buyerCompanies = listOf(
        "ITC Agri-Business Division (Pan-India)",
        "Reliance Retail Fresh Sourcing",
        "BigBasket Farm Direct B2B",
        "Sahyadri Farms Post-Harvest Care Ltd",
        "Adani Agri Logistics Ltd",
        "Punjab Agro Juices Ltd"
    )
    var selectedBuyerCompany by remember { mutableStateOf(buyerCompanies.first()) }
    var buyerRepName by remember { mutableStateOf("Vilas Shinde / Rajesh Sharma") }
    var buyerMobile by remember { mutableStateOf("9422001122") }
    var buyerExpanded by remember { mutableStateOf(false) }

    // Trader Form Fields
    val mandiList = listOf(
        "Khanna Grain APMC (Ludhiana, Punjab)",
        "Unjha Spice APMC (Mehsana, Gujarat)",
        "Lasalgaon Onion APMC (Nashik, Maharashtra)",
        "Indore Soya APMC (Madhya Pradesh)",
        "Guntur Chilli APMC (Andhra Pradesh)",
        "Agra Potato APMC (Uttar Pradesh)",
        "Gulabbagh Maize APMC (Purnea, Bihar)"
    )
    var selectedMandi by remember { mutableStateOf(mandiList.first()) }
    var traderName by remember { mutableStateOf("Balaji Agro Commission Agents") }
    var traderLicense by remember { mutableStateOf("APMC-LIC-NAS-4091") }
    var traderExpanded by remember { mutableStateOf(false) }

    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { selectedLang ->
                onLanguageChange(selectedLang)
                showLanguageDialog = false
            },
            onDismissRequest = { showLanguageDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KisanBgLight),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ultra-Compact Header: Logo + Title + 25-Language Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(KisanGreenPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌾",
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = LanguageManager.getString("app_title", currentLanguage),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = KisanGreenDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(KisanGreenContainer)
                                    .padding(horizontal = 3.dp, vertical = 0.5.dp)
                            ) {
                                Text(
                                    text = "MSInS",
                                    color = KisanGreenPrimary,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "APMC & MSP Direct Portal",
                            fontSize = 8.5.sp,
                            color = Slate500,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 25-Language Selection Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(KisanGreenContainer)
                        .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(6.dp))
                        .clickable { showLanguageDialog = true }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("login_lang_picker_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${currentLanguage.nativeName} (25)",
                            color = KisanGreenPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Role Selection Tabs (Farmer, Buyer, Trader)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Slate100)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    UserRole.values().forEach { role ->
                        val isSelected = role == selectedRole
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) KisanGreenPrimary else Color.Transparent)
                                .clickable { selectedRole = role }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                                .testTag("login_tab_${role.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = role.emoji,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = when (role) {
                                        UserRole.FARMER -> LanguageManager.getString("role_farmer", currentLanguage)
                                        UserRole.CONSUMER -> LanguageManager.getString("role_consumer", currentLanguage)
                                        UserRole.BUYER -> LanguageManager.getString("role_buyer", currentLanguage)
                                        UserRole.TRADER -> LanguageManager.getString("role_trader", currentLanguage)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Slate700,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Form Area based on selected Role
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    when (selectedRole) {
                        UserRole.FARMER -> {
                            Text(
                                text = "🌾 " + LanguageManager.getString("login_as_farmer", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = KisanGreenDark
                            )
                            Text(
                                text = "Access live mandi rates in ₹/Kg, ₹/Qtl, ₹/Ton and list harvest lots",
                                fontSize = 11.sp,
                                color = Slate500
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = farmerName,
                                onValueChange = { farmerName = it },
                                label = { Text("Farmer Name / शेतकरी नाव") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Name", tint = KisanGreenPrimary)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_farmer_name_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = farmerMobile,
                                onValueChange = { farmerMobile = it },
                                label = { Text(LanguageManager.getString("enter_mobile", currentLanguage)) },
                                leadingIcon = {
                                    Text(
                                        text = "+91 ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KisanGreenPrimary,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_farmer_mobile_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = kisanId,
                                    onValueChange = { kisanId = it },
                                    label = { Text("Kisan ID / आधार") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = farmerDistrict,
                                    onValueChange = { farmerDistrict = it },
                                    label = { Text("District / जिल्हा") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // OTP Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = otpText,
                                    onValueChange = { otpText = it },
                                    label = { Text(LanguageManager.getString("enter_otp", currentLanguage)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("login_otp_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Button(
                                    onClick = { otpSent = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = KisanGreenContainer),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text("OTP Sent ✓", color = KisanGreenDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onLoginFarmer(
                                        farmerName.ifBlank { "Ramesh Patil" },
                                        farmerMobile.ifBlank { "9823456789" },
                                        farmerDistrict.ifBlank { "Nashik" },
                                        kisanId.ifBlank { "MH-MSINS-784920" }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_login_btn")
                            ) {
                                Icon(Icons.Default.Login, contentDescription = "Login", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = LanguageManager.getString("verify_and_login", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        UserRole.CONSUMER -> {
                            Text(
                                text = "🛒 " + LanguageManager.getString("login_as_consumer", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = KisanGreenDark
                            )
                            Text(
                                text = "Buy fresh vegetables, fruits & grains directly from farmers at transparent farm-gate rates",
                                fontSize = 11.sp,
                                color = Slate500
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = consumerName,
                                onValueChange = { consumerName = it },
                                label = { Text("Full Name / नाव") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "Name", tint = KisanGreenPrimary)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_consumer_name_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = consumerMobile,
                                onValueChange = { consumerMobile = it },
                                label = { Text(LanguageManager.getString("enter_mobile", currentLanguage)) },
                                leadingIcon = {
                                    Text(
                                        text = "+91 ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KisanGreenPrimary,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_consumer_mobile_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = consumerAddress,
                                onValueChange = { consumerAddress = it },
                                label = { Text("Delivery Address / Society / Landmark") },
                                leadingIcon = {
                                    Icon(Icons.Default.Home, contentDescription = "Address", tint = KisanGreenPrimary)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_consumer_address_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = consumerDistrict,
                                onValueChange = { consumerDistrict = it },
                                label = { Text("District / City") },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = "District", tint = KisanGreenPrimary)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_consumer_district_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onLoginConsumer(
                                        consumerName.ifBlank { "Rohit Sharma" },
                                        consumerMobile.ifBlank { "9823044921" },
                                        consumerAddress.ifBlank { "Flat 402, Green Meadows Tower, Gangapur Road" },
                                        consumerDistrict.ifBlank { "Nashik" }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_consumer_login_btn")
                            ) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = "Login", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Enter Fresh D2C Marketplace",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        UserRole.BUYER -> {
                            Text(
                                text = "🏢 " + LanguageManager.getString("login_as_buyer", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = KisanGreenDark
                            )
                            Text(
                                text = "Procure minimal and bulk harvest lots with 100% Escrow protection",
                                fontSize = 11.sp,
                                color = Slate500
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            ExposedDropdownMenuBox(
                                expanded = buyerExpanded,
                                onExpandedChange = { buyerExpanded = !buyerExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedBuyerCompany,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Institutional Buyer Organization") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = buyerExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = buyerExpanded,
                                    onDismissRequest = { buyerExpanded = false }
                                ) {
                                    buyerCompanies.forEach { comp ->
                                        DropdownMenuItem(
                                            text = { Text(comp, fontSize = 12.sp) },
                                            onClick = {
                                                selectedBuyerCompany = comp
                                                buyerExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = buyerRepName,
                                onValueChange = { buyerRepName = it },
                                label = { Text("Procurement Officer / Manager") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = buyerMobile,
                                onValueChange = { buyerMobile = it },
                                label = { Text(LanguageManager.getString("enter_mobile", currentLanguage)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onLoginBuyer(selectedBuyerCompany, buyerRepName, buyerMobile)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_buyer_login_btn")
                            ) {
                                Icon(Icons.Default.Business, contentDescription = "Login", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Enter as Institutional Buyer",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        UserRole.TRADER -> {
                            Text(
                                text = "🏪 " + LanguageManager.getString("login_as_trader", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = KisanGreenDark
                            )
                            Text(
                                text = "APMC Licensed Trader & Commission Agent Terminal",
                                fontSize = 11.sp,
                                color = Slate500
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            ExposedDropdownMenuBox(
                                expanded = traderExpanded,
                                onExpandedChange = { traderExpanded = !traderExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedMandi,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Registered APMC Mandi Yard") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = traderExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = traderExpanded,
                                    onDismissRequest = { traderExpanded = false }
                                ) {
                                    mandiList.forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m, fontSize = 12.sp) },
                                            onClick = {
                                                selectedMandi = m
                                                traderExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = traderName,
                                onValueChange = { traderName = it },
                                label = { Text("Firm / Trader Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = traderLicense,
                                onValueChange = { traderLicense = it },
                                label = { Text("APMC License Number") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    onLoginTrader(traderName, selectedMandi, traderLicense)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_trader_login_btn")
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = "Login", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Enter as Mandi Trader",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick 1-Tap Demo Access Box
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
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Quick",
                            tint = KisanGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = LanguageManager.getString("quick_demo_login", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = KisanGreenDark
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onQuickDemoLogin(UserRole.FARMER) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("demo_login_farmer_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(KisanGreenPrimary)),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("🌾 Farmer (शेतकरी)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = KisanGreenDark)
                            }

                            OutlinedButton(
                                onClick = { onQuickDemoLogin(UserRole.CONSUMER) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("demo_login_consumer_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0284C7))),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("🛒 Consumer (ग्राहक)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onQuickDemoLogin(UserRole.BUYER) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("demo_login_buyer_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(KisanSaffron)),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("🏢 Buyer (खरेदीदार)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = KisanSaffron)
                            }

                            OutlinedButton(
                                onClick = { onQuickDemoLogin(UserRole.TRADER) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("demo_login_trader_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(GovtAshokaBlue)),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text("🏪 Trader (व्यापारी)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = GovtAshokaBlue)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Govt Trust Guarantee
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Secured",
                    tint = TrendGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = LanguageManager.getString("govt_trust_seal", currentLanguage),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
