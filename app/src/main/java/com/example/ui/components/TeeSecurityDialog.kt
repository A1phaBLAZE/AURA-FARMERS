package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.LanguageManager
import com.example.data.model.AppLanguage
import com.example.data.model.DlpAuditEvent
import com.example.data.model.DlpPolicySettings
import com.example.data.model.EnclaveEncryptedPayload
import com.example.data.model.TeeEnclaveStatus
import com.example.ui.theme.*

@Composable
fun TeeSecurityDialog(
    currentLanguage: AppLanguage,
    enclaveStatus: TeeEnclaveStatus,
    dlpPolicy: DlpPolicySettings,
    auditLogs: List<DlpAuditEvent>,
    onDismiss: () -> Unit,
    onToggleScreenCapture: (Boolean) -> Unit,
    onToggleClipboardPurge: (Boolean) -> Unit,
    onTogglePiiMasking: (Boolean) -> Unit,
    onToggleEscrowSigning: (Boolean) -> Unit,
    onTestEncrypt: (String) -> EnclaveEncryptedPayload,
    onTestDecrypt: (EnclaveEncryptedPayload) -> String,
    isPlayingAudio: Boolean = false,
    onPlayAudio: (String) -> Unit = {},
    onStopAudio: () -> Unit = {}
) {
    var testPlaintext by remember { mutableStateOf("Aadhaar: 4892-7840-1920 • Bank A/C: 60492810482") }
    var currentEncryptedPayload by remember { mutableStateOf<EnclaveEncryptedPayload?>(null) }
    var decryptedResult by remember { mutableStateOf<String?>(null) }
    var activeTab by remember { mutableStateOf(0) } // 0: Farmer View (Simple), 1: Expert Diagnostics (Vault & Logs)

    val audioSummary = LanguageManager.getString("farmer_audio_security_summary", currentLanguage)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
                .fillMaxHeight(0.90f)
                .padding(vertical = 8.dp)
                .testTag("tee_security_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(KisanGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Security Shield",
                                tint = KisanGreenDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = LanguageManager.getString("tee_shield_title", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Slate900
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = LanguageManager.getString("tee_status_active", currentLanguage),
                                    fontSize = 11.sp,
                                    color = KisanGreenDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_tee_dialog_top_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Farmer Audio Explainer & Reassurance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KisanGreenContainer.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(KisanGreenPrimary.copy(alpha = 0.3f)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = LanguageManager.getString("tee_shield_subtitle", currentLanguage),
                                fontSize = 11.5.sp,
                                color = Slate800,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (isPlayingAudio) {
                                    onStopAudio()
                                } else {
                                    onPlayAudio(audioSummary)
                                }
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isPlayingAudio) KisanSaffron else KisanGreenPrimary)
                                .testTag("tee_audio_btn")
                        ) {
                            Icon(
                                imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = "Listen to explanation",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Simple Tab Selection (Farmer View vs Expert Diagnostics)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate100)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf(
                        "🛡️ " + if (currentLanguage == AppLanguage.MR) "शेतकरी सुरक्षा (सोपे)"
                        else if (currentLanguage == AppLanguage.HI) "किसान सुरक्षा (सरल)"
                        else if (currentLanguage == AppLanguage.GU) "ખેડૂત સુરક્ષા (સરળ)"
                        else "Farmer Shield",

                        "🧪 " + if (currentLanguage == AppLanguage.MR) "तज्ज्ञ लॅब चाचणी"
                        else if (currentLanguage == AppLanguage.HI) "विशेषज्ञ लैब परीक्षण"
                        else if (currentLanguage == AppLanguage.GU) "નિષ્ણાત લૅબ ટેસ્ટ"
                        else "Expert Diagnostics"
                    )

                    tabs.forEachIndexed { idx, tabTitle ->
                        val isSelected = activeTab == idx
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { activeTab = idx }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tabTitle,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) KisanGreenDark else Slate600
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (activeTab == 0) {
                        // TAB 0: SIMPLE FARMER VIEW
                        // 3 Visual Pillar Cards
                        FarmerSecurityPillarCard(
                            icon = Icons.Default.AccountBalance,
                            title = LanguageManager.getString("farmer_shield_card_bank", currentLanguage),
                            badge = if (currentLanguage == AppLanguage.MR) "१००% सुरक्षित"
                            else if (currentLanguage == AppLanguage.HI) "100% सुरक्षित"
                            else "100% Protected",
                            description = LanguageManager.getString("farmer_shield_desc_bank", currentLanguage),
                            badgeColor = Color(0xFF16A34A)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FarmerSecurityPillarCard(
                            icon = Icons.Default.Payments,
                            title = LanguageManager.getString("farmer_shield_card_escrow", currentLanguage),
                            badge = if (currentLanguage == AppLanguage.MR) "सरकारी एस्क्रो"
                            else if (currentLanguage == AppLanguage.HI) "सरकारी एस्क्रो"
                            else "RBI Escrow",
                            description = LanguageManager.getString("farmer_shield_desc_escrow", currentLanguage),
                            badgeColor = Color(0xFF0284C7)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FarmerSecurityPillarCard(
                            icon = Icons.Default.Security,
                            title = LanguageManager.getString("farmer_shield_card_leak", currentLanguage),
                            badge = if (currentLanguage == AppLanguage.MR) "चोरी प्रतिबंध"
                            else if (currentLanguage == AppLanguage.HI) "चोरी रोकथाम"
                            else "Zero Leakage",
                            description = LanguageManager.getString("farmer_shield_desc_leak", currentLanguage),
                            badgeColor = Color(0xFF9333EA)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Easy Switches Section
                        Text(
                            text = if (currentLanguage == AppLanguage.MR) "सुरक्षा नियंत्रणे (Security Switches)"
                            else if (currentLanguage == AppLanguage.HI) "सुरक्षा नियंत्रण (Security Switches)"
                            else if (currentLanguage == AppLanguage.GU) "સુરક્ષા નિયંત્રણો"
                            else "Security Protection Controls",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        DlpPolicyToggleItem(
                            title = LanguageManager.getString("dlp_pii_masking", currentLanguage),
                            desc = LanguageManager.getString("dlp_pii_masking_desc", currentLanguage),
                            isChecked = dlpPolicy.isPiiMaskingEnabled,
                            onCheckedChange = onTogglePiiMasking,
                            tag = "toggle_pii_masking"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        DlpPolicyToggleItem(
                            title = LanguageManager.getString("dlp_clipboard_guard", currentLanguage),
                            desc = LanguageManager.getString("dlp_clipboard_guard_desc", currentLanguage),
                            isChecked = dlpPolicy.isSecureClipboardActive,
                            onCheckedChange = onToggleClipboardPurge,
                            tag = "toggle_clipboard_guard"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        DlpPolicyToggleItem(
                            title = LanguageManager.getString("tee_escrow_signing", currentLanguage),
                            desc = LanguageManager.getString("tee_escrow_signing_desc", currentLanguage),
                            isChecked = dlpPolicy.isEnclaveEscrowSigningEnforced,
                            onCheckedChange = onToggleEscrowSigning,
                            tag = "toggle_escrow_signing"
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        DlpPolicyToggleItem(
                            title = LanguageManager.getString("dlp_screen_guard", currentLanguage),
                            desc = LanguageManager.getString("dlp_screen_guard_desc", currentLanguage),
                            isChecked = dlpPolicy.isScreenCaptureBlocked,
                            onCheckedChange = onToggleScreenCapture,
                            tag = "toggle_screen_guard"
                        )
                    } else {
                        // TAB 1: EXPERT LAB & AUDIT LOGS
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate900)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Hardware Enclave Diagnostics (ARM® TrustZone™)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Root of Trust: ${enclaveStatus.rootOfTrust}",
                                    fontSize = 9.5.sp,
                                    color = Slate400
                                )
                                Text(
                                    text = "Attestation: ${enclaveStatus.hardwareAttestationHash.take(32)}...",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate400
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = LanguageManager.getString("tee_vault_tester", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = testPlaintext,
                            onValueChange = { testPlaintext = it },
                            label = { Text("Sensitive PII / Account Record", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tee_vault_input"),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.5.sp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val payload = onTestEncrypt(testPlaintext)
                                currentEncryptedPayload = payload
                                decryptedResult = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tee_encrypt_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Encrypt in Hardware TEE (AES-256-GCM)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }

                        if (currentEncryptedPayload != null) {
                            val p = currentEncryptedPayload!!
                            Spacer(modifier = Modifier.height(6.dp))

                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate100),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("🔐 TEE Enclave Ciphertext:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KisanGreenDark)
                                    Text("Cipher: ${p.cipherTextBase64.take(40)}...", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Slate800)
                                    Text("IV: ${p.ivBase64} • Tag: ${p.authTagBase64}", fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, color = Slate600)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = {
                                    decryptedResult = onTestDecrypt(p)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tee_decrypt_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Decrypt in TEE (Verify Integrity)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }
                        }

                        if (decryptedResult != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = KisanGreenContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = KisanGreenDark, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Decrypted: $decryptedResult",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KisanGreenDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Audit Trail List
                        Text(
                            text = LanguageManager.getString("tee_audit_trail", currentLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Slate800
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        auditLogs.take(8).forEach { log ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Slate100),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .border(1.dp, Slate200, RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (log.severity == "POLICY_ENFORCED") Color(0xFFDCFCE7)
                                                else if (log.severity == "WARNING") Color(0xFFFEF3C7)
                                                else KisanGreenContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (log.severity == "POLICY_ENFORCED") Icons.Default.Shield
                                            else if (log.severity == "WARNING") Icons.Default.Warning
                                            else Icons.Default.Check,
                                            contentDescription = null,
                                            tint = if (log.severity == "POLICY_ENFORCED") Color(0xFF15803D)
                                            else if (log.severity == "WARNING") Color(0xFFB45309)
                                            else KisanGreenDark,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(7.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = log.eventType,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp,
                                                color = Slate800
                                            )
                                            Text(
                                                text = log.timestamp,
                                                fontSize = 8.5.sp,
                                                color = Slate400
                                            )
                                        }
                                        Text(
                                            text = log.description,
                                            fontSize = 9.sp,
                                            color = Slate600
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("close_tee_dialog_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = KisanGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.MR) "समजले (सुरक्षित आहे)"
                        else if (currentLanguage == AppLanguage.HI) "समझ गए (सुरक्षित है)"
                        else if (currentLanguage == AppLanguage.GU) "સમજાયું (સુરક્ષિત છે)"
                        else "Understood & Protected",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FarmerSecurityPillarCard(
    icon: ImageVector,
    title: String,
    badge: String,
    description: String,
    badgeColor: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Slate900
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 10.5.sp,
                    color = Slate600,
                    lineHeight = 14.5.sp
                )
            }
        }
    }
}

@Composable
fun DlpPolicyToggleItem(
    title: String,
    desc: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Slate100),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate200, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = Slate900
                )
                Text(
                    text = desc,
                    fontSize = 10.sp,
                    color = Slate500,
                    lineHeight = 13.5.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(tag)
            )
        }
    }
}
