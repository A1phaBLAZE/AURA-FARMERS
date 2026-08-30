package com.example.ui.components

import androidx.compose.animation.core.*
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
import com.example.data.LanguageManager
import com.example.data.model.AppLanguage
import com.example.ui.theme.*

@Composable
fun AppTopBar(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    currentUser: com.example.data.model.UserProfile? = null,
    onLogoutClick: () -> Unit = {},
    onTeeSecurityClick: () -> Unit = {},
    isOfflineSynced: Boolean,
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    isPlayingAudio: Boolean,
    onStopAudioClick: () -> Unit,
    onKisanCardClick: () -> Unit = {},
    isKisanCardSelected: Boolean = false
) {
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

    Surface(
        color = Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 0.5.dp, color = Slate200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(KisanGreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KV",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "KISAN VANI",
                        color = KisanGreenDark,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = "Direct Agri Network",
                        color = Slate500,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: Quick Action Controls + TOP-RIGHT KISAN CARD PROFILE CORNER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Audio Playing Indicator (if playing)
                if (isPlayingAudio) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(KisanSaffron)
                            .bounceClick { onStopAudioClick() }
                            .padding(horizontal = 5.dp, vertical = 3.dp)
                            .testTag("stop_audio_header_btn"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AudioWaveformBars(isPlaying = true, barColor = Color.White, barCount = 3)
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop audio",
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                // Compact Language Selector Button (Opens 25-Language Dialog)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(KisanGreenContainer)
                        .border(1.dp, KisanGreenContainerBorder, RoundedCornerShape(6.dp))
                        .bounceClick { showLanguageDialog = true }
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                        .testTag("lang_selector_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = currentLanguage.nativeName.take(3),
                            color = KisanGreenPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // TEE & DLP Security Shield Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFDCFCE7))
                        .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(6.dp))
                        .bounceClick { onTeeSecurityClick() }
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                        .testTag("tee_shield_topbar_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingLiveDot(color = Color(0xFF15803D), size = 4.dp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Shield",
                            tint = Color(0xFF15803D),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                // Sync Mandi Rates Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSyncing) KisanGoldContainer else Slate100)
                        .border(1.dp, Slate200, RoundedCornerShape(6.dp))
                        .bounceClick { onSyncClick() }
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                        .testTag("sync_rates_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(11.dp),
                            color = KisanGreenPrimary,
                            strokeWidth = 1.5.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync Mandi Rates",
                            tint = KisanGreenPrimary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                // ========================================================
                // TOP-RIGHT CORNER: DEDICATED KISAN CARD PROFILE BUTTON
                // ========================================================
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isKisanCardSelected) KisanGreenPrimary else Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isKisanCardSelected) 1.5.dp else 1.dp,
                        color = if (isKisanCardSelected) KisanGreenDark else Color(0xFF86EFAC)
                    ),
                    shadowElevation = if (isKisanCardSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .bounceClick { onKisanCardClick() }
                        .testTag("kisan_card_topbar_corner_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = "Kisan Card Profile",
                            tint = if (isKisanCardSelected) Color.White else KisanGreenPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "Kisan Card",
                                color = if (isKisanCardSelected) Color.White else KisanGreenDark,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 11.sp
                            )
                            if (currentUser != null) {
                                Text(
                                    text = currentUser.role.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (isKisanCardSelected) Color(0xFFDCFCE7) else Slate500,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 9.sp
                                )
                            }
                        }
                    }
                }

                // Logout Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate100)
                        .border(1.dp, Slate200, RoundedCornerShape(6.dp))
                        .bounceClick { onLogoutClick() }
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                        .testTag("logout_header_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Slate600,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}

