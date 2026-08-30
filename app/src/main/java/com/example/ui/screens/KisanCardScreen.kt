package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LanguageManager
import com.example.data.model.AppLanguage
import com.example.data.model.FarmerKisanCard
import com.example.data.model.GovtHelpline
import com.example.data.model.PesticideAdvisory
import com.example.ui.theme.*

@Composable
fun KisanCardScreen(
    kisanCard: FarmerKisanCard,
    pesticides: List<PesticideAdvisory>,
    helplines: List<GovtHelpline>,
    currentLanguage: AppLanguage,
    onOpenTeeSecurity: () -> Unit = {},
    onCopySecure: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var pesticideQuery by remember { mutableStateOf("") }
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    val filteredPesticides = remember(pesticideQuery, pesticides) {
        if (pesticideQuery.isBlank()) pesticides else {
            val q = pesticideQuery.lowercase()
            pesticides.filter {
                it.cropName.lowercase().contains(q) ||
                        it.pestOrDiseaseEn.lowercase().contains(q) ||
                        it.pestOrDiseaseMr.lowercase().contains(q) ||
                        it.pestOrDiseaseHi.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
            // SECTION 1: Digital Kisan Identity Card
            item {
                DigitalKisanCardWidget(
                    kisanCard = kisanCard,
                    currentLanguage = currentLanguage,
                    onCopyAadhaar = {
                        onCopySecure(kisanCard.aadhaarMasked, "Kisan Aadhaar")
                        copiedNotice = "Aadhaar copied securely (Auto-purges in 25s)"
                    },
                    onCopyBank = {
                        onCopySecure(kisanCard.bankAccountMasked, "Kisan Bank Account")
                        copiedNotice = "Bank A/C copied securely (Auto-purges in 25s)"
                    }
                )
            }

            // TEE & Data Leakage Prevention Shield Banner
            item {
                AnimatedBreathingCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = Color(0xFF22C55E)
                ) {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(onClick = onOpenTeeSecurity)
                            .testTag("tee_kisan_card_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
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
                                        .background(Color(0xFF15803D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = "TEE Shield",
                                        tint = Color(0xFF86EFAC),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PulsingLiveDot(color = Color(0xFF86EFAC), size = 4.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = LanguageManager.getString("tee_shield_title", currentLanguage),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = if (currentLanguage == AppLanguage.MR) "बँक खाते, आधार व शेतमालाचे पैसे १००% सुरक्षित आहेत"
                                        else if (currentLanguage == AppLanguage.HI) "बैंक खाता, आधार और फसल का भुगतान 100% सुरक्षित है"
                                        else if (currentLanguage == AppLanguage.GU) "બેંક ખાતું, આધાર અને પાકના નાણાં ૧૦૦% સુરક્ષિત છે"
                                        else "100% Protected: Bank, Aadhaar & Escrow Locked",
                                        fontSize = 10.sp,
                                        color = Color(0xFF86EFAC),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.MR) "पहा"
                                    else if (currentLanguage == AppLanguage.HI) "देखें"
                                    else if (currentLanguage == AppLanguage.GU) "જુઓ"
                                    else "VIEW",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }

                if (copiedNotice != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = KisanGreenContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KisanGreenDark, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = copiedNotice!!,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KisanGreenDark
                            )
                        }
                    }
                }
            }

            // SECTION 2: Government Agriculture Helplines (1-Tap Call)
            item {
                Text(
                    text = LanguageManager.getString("govt_helplines", currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(helplines, key = { it.id }) { helpline ->
                HelplineCard(
                    helpline = helpline,
                    currentLanguage = currentLanguage,
                    onCallClick = {
                        try {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${helpline.phoneNumber}"))
                            context.startActivity(dialIntent)
                        } catch (e: Exception) {
                            // Fallback
                        }
                    }
                )
            }

            // SECTION 3: Pesticide Guide & Pest Management
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = LanguageManager.getString("pesticide_guide", currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                OutlinedTextField(
                    value = pesticideQuery,
                    onValueChange = { pesticideQuery = it },
                    placeholder = { Text("Search crop or pest (e.g. Onion, Thrips, Blight)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = KisanGreenPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pesticide_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            items(filteredPesticides, key = { it.id }) { pest ->
                PesticideCard(pesticide = pest, currentLanguage = currentLanguage)
            }
        }
    }

@Composable
fun DigitalKisanCardWidget(
    kisanCard: FarmerKisanCard,
    currentLanguage: AppLanguage,
    onCopyAadhaar: () -> Unit = {},
    onCopyBank: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kisan_identity_card"),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(KisanGreenPrimary, Color(0xFF144520))
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                // Card Header: Govt & Verified Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🇮🇳", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "KISAN VANI BHARAT • KISAN PEHCHAN",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "National AgriStack Digital Farmer ID • Verified",
                                color = KisanGold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KisanSaffron)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = LanguageManager.getString("verified_farmer", currentLanguage),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Farmer Name & ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = kisanCard.farmerName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Card ID: ${kisanCard.kisanCardNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KisanGold
                        )
                    }

                    // QR Code Graphic Placeholder
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Kisan QR Code",
                            tint = Color.Black,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Details Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    KisanCardRow(label = "Village / Taluka", value = "${kisanCard.village}, ${kisanCard.taluka} (${kisanCard.district})")
                    KisanCardRow(label = "Landholding", value = "${kisanCard.landSizeAcres} Acres (Irrigated)")
                    KisanCardRow(label = "Primary Crops", value = kisanCard.primaryCrops.joinToString(", "))
                    KisanCardRow(label = "Aadhaar / Bank", value = "${kisanCard.aadhaarMasked} • ${kisanCard.bankAccountMasked}")
                }
            }
        }
    }
}

@Composable
private fun KisanCardRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFFD0E8D0), fontSize = 11.sp)
        Text(text = value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HelplineCard(
    helpline: GovtHelpline,
    currentLanguage: AppLanguage,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("helpline_card_${helpline.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = helpline.getTitle(currentLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = helpline.department,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = helpline.getDescription(currentLanguage),
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⏰ ${helpline.hours}",
                    fontSize = 10.sp,
                    color = KisanGreenPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onCallClick,
                colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("call_helpline_${helpline.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = helpline.phoneNumber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PesticideCard(
    pesticide: PesticideAdvisory,
    currentLanguage: AppLanguage
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pesticide_card_${pesticide.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KisanGreenContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = pesticide.cropName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = KisanGreenPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pesticide.getPestName(currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (pesticide.severityLevel == "High") Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${pesticide.severityLevel} Alert",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pesticide.severityLevel == "High") Color.Red else KisanSaffron
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chemical Remedy
            Text(
                text = "🧪 Chemical Remedy: ${pesticide.chemicalRemedy}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Bio-Organic Remedy
            Text(
                text = "🌱 Bio-Organic Remedy: ${pesticide.bioOrganicRemedy}",
                fontSize = 12.sp,
                color = KisanGreenPrimaryDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Dosage & Safety Period
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Dosage: ${pesticide.dosagePerAcre}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = "Safety Waiting Period: ${pesticide.safetyPeriodDays} Days", fontSize = 11.sp, color = KisanSaffron, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "💡 ${pesticide.getAdvice(currentLanguage)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
