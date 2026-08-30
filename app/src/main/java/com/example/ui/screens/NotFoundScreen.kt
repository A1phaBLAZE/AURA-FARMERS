package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.*

@Composable
fun NotFoundScreen(
    currentLanguage: AppLanguage,
    onNavigateToTab: (Int) -> Unit,
    onSearchCrops: (String) -> Unit = {},
    customPathOrId: String = "Requested Page / Record",
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val (titleText, descText, hintText) = when (currentLanguage) {
        AppLanguage.MR -> Triple(
            "४०४ • पृष्ठ अथवा शेती नोंद सापडली नाही",
            "तुम्ही शोधत असलेली मंडी नोंद, लॉट अथवा पान उपलब्ध नाही किंवा ते कालबाह्य झाले आहे.",
            "खालील पर्यायांमधून मुख्य बाजारभाव अथवा इतर विभागांकडे जा:"
        )
        AppLanguage.HI -> Triple(
            "404 • पृष्ठ या कृषि रिकॉर्ड नहीं मिला",
            "आप जिस मंडी भाव, लॉट या पृष्ठ की तलाश कर रहे हैं वह उपलब्ध नहीं है या समाप्त हो चुका है।",
            "कृपया मुख्य मंडी डैशबोर्ड पर लौटें या नीचे दिए गए विकल्पों का चयन करें:"
        )
        AppLanguage.GU -> Triple(
            "404 • પેજ અથવા રેકોર્ડ મળ્યો નથી",
            "તમે જે માર્કેટ યાર્ડ ભાવ, લોટ અથવા પેજ શોધી રહ્યા છો તે ઉપલબ્ધ નથી અથવા સમાપ્ત થઈ ગયું છે.",
            "કૃપા કરીને નીચેના વિકલ્પોમાંથી પસંદ કરીને આગળ વધો:"
        )
        AppLanguage.EN -> Triple(
            "404 • Page or Mandi Record Not Found",
            "The market record, trade lot, or page you requested is unavailable, has expired, or the link may be broken.",
            "You can return to the primary Mandi dashboard or explore the quick links below:"
        )
        else -> Triple(
            "404 • पृष्ठ या रिकॉर्ड नहीं मिला",
            "आप जिस मंडी भाव, लॉट या पृष्ठ की तलाश कर रहे हैं वह उपलब्ध नहीं है।",
            "कृपया मुख्य मंडी डैशबोर्ड पर लौटें:"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .testTag("not_found_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Large 404 Badge with agricultural leaf motif
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEF2F2))
                .border(2.dp, Color(0xFFFCA5A5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = "404 Not Found",
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 404 Pill
        Surface(
            color = Color(0xFFFEE2E2),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "ERROR 404 • HTTP_NOT_FOUND",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF991B1B),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = titleText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = descText,
            fontSize = 13.sp,
            color = Slate600,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Target: $customPathOrId",
            fontSize = 11.sp,
            color = Slate400,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        // In-page Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search crops, mandis or lots...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate500, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        onSearchCrops(searchQuery)
                        onNavigateToTab(0)
                    }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Submit", tint = KisanGreenPrimary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_404_search")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = hintText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate700,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Action Navigation Cards
        NotFoundNavOption(
            title = if (currentLanguage == AppLanguage.MR) "बाजार भाव (Mandi Rates)" else "Mandi Rates Dashboard",
            subtitle = "Live APMC prices, price tickers & AGMARKNET daily arrivals",
            icon = Icons.Default.Store,
            accentColor = KisanGreenPrimary,
            onClick = { onNavigateToTab(0) },
            testTag = "btn_404_go_mandi"
        )

        Spacer(modifier = Modifier.height(8.dp))

        NotFoundNavOption(
            title = if (currentLanguage == AppLanguage.MR) "सरकारी खरेदी (Govt MSP)" else "Govt MSP Procurement",
            subtitle = "Land record verification, token slot booking & DBT status",
            icon = Icons.Default.AccountBalance,
            accentColor = Color(0xFF1E40AF),
            onClick = { onNavigateToTab(2) },
            testTag = "btn_404_go_procurement"
        )

        Spacer(modifier = Modifier.height(8.dp))

        NotFoundNavOption(
            title = if (currentLanguage == AppLanguage.MR) "हवामान व सल्ला (Weather Advisory)" else "Live Weather & Advice",
            subtitle = "District forecasts, rain probability & crop protection",
            icon = Icons.Default.WbSunny,
            accentColor = Color(0xFFD97706),
            onClick = { onNavigateToTab(3) },
            testTag = "btn_404_go_weather"
        )

        Spacer(modifier = Modifier.height(8.dp))

        NotFoundNavOption(
            title = if (currentLanguage == AppLanguage.MR) "कृषी AI सहाय्यक (Krishi Voice AI)" else "Krishi AI Voice Assistant",
            subtitle = "Ask agricultural queries in your regional voice",
            icon = Icons.Default.SmartToy,
            accentColor = Color(0xFF7C3AED),
            onClick = { onNavigateToTab(5) },
            testTag = "btn_404_go_ai"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Return Home Button
        Button(
            onClick = { onNavigateToTab(0) },
            colors = ButtonDefaults.buttonColors(containerColor = KisanGreenDark),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("btn_404_return_home")
        ) {
            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (currentLanguage == AppLanguage.MR) "मुख्य पानावर परत जा" else "Back to Main Home",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun NotFoundNavOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Slate500,
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
