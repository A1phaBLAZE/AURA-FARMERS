package com.example.ui.screens

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
import com.example.data.model.ForecastDay
import com.example.data.model.INDIAN_STATES
import com.example.data.model.WeatherAdvisory
import com.example.service.AgriDistrict
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    weather: WeatherAdvisory,
    currentLanguage: AppLanguage,
    selectedState: String = "Maharashtra",
    onStateSelect: (String, Boolean) -> Unit = { _, _ -> },
    availableDistricts: List<AgriDistrict> = emptyList(),
    selectedDistrict: AgriDistrict? = null,
    onDistrictSelect: (AgriDistrict, Boolean) -> Unit = { _, _ -> },
    isLoading: Boolean = false,
    networkError: String? = null,
    onRefreshWeather: (Boolean) -> Unit = {},
    isPlayingAudio: Boolean,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit
) {
    var autoSpeakOnChange by remember { mutableStateOf(false) }
    var stateDropdownExpanded by remember { mutableStateOf(false) }
    var districtDropdownExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
            // Live Internet Connection Status Bar
            item {
                val isLive = weather.isLiveSuccess && networkError == null
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLive) Color(0xFFF0FDF4) else Color(0xFFFEF3C7)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isLive) Color(0xFF86EFAC) else Color(0xFFFCD34D)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = KisanGreenPrimary
                                )
                            } else {
                                PulsingLiveDot(
                                    color = if (isLive) Color(0xFF16A34A) else Color(0xFFD97706),
                                    size = 7.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isLoading) "Fetching forecast from Open-Meteo API..."
                                    else if (isLive) "Live forecast from Open-Meteo • Updated ${weather.lastUpdated}"
                                    else "Showing demo/offline sample data",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLive) Color(0xFF166534) else Color(0xFF92400E)
                                )
                                Text(
                                    text = "Source: ${weather.dataSource} • Coordinates: ${weather.coordinates}",
                                    fontSize = 10.sp,
                                    color = Slate600
                                )
                            }
                        }

                        IconButton(
                            onClick = { onRefreshWeather(autoSpeakOnChange) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Forecast from Open-Meteo",
                                tint = KisanGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // State & District Location Selector (Cascading Filters)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
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
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = KisanGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Select Region & District / प्रदेश व जिल्हा",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            }

                            // Auto-say toggle switch
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { autoSpeakOnChange = !autoSpeakOnChange }
                                    .background(if (autoSpeakOnChange) KisanGreenContainer else Slate100)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (autoSpeakOnChange) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                    contentDescription = null,
                                    tint = if (autoSpeakOnChange) KisanGreenPrimary else Slate500,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Auto-Speak",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (autoSpeakOnChange) KisanGreenDark else Slate600
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Filter 1: State Horizontal Carousel & Dropdown
                        Text(
                            text = "State / राज्य: $selectedState",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            INDIAN_STATES.forEach { state ->
                                val isStateSelected = (selectedState.equals(state, ignoreCase = true))
                                FilterChip(
                                    selected = isStateSelected,
                                    onClick = { onStateSelect(state, autoSpeakOnChange) },
                                    label = {
                                        Text(
                                            text = state,
                                            fontSize = 11.sp,
                                            fontWeight = if (isStateSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KisanGreenDark,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("weather_state_chip_$state")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter 2: District Selection within Selected State
                        Text(
                            text = "Districts in $selectedState (${availableDistricts.size}):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Horizontal Chip Carousel of agricultural districts in the state
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableDistricts.forEach { dist ->
                                val isSelected = (selectedDistrict?.nameEn == dist.nameEn || weather.district == dist.nameEn)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onDistrictSelect(dist, autoSpeakOnChange) },
                                    label = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dist.getName(currentLanguage),
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = dist.primaryCrops.split(",").firstOrNull() ?: "",
                                                fontSize = 9.sp,
                                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else Slate500
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KisanGreenPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .bounceClick { onDistrictSelect(dist, autoSpeakOnChange) }
                                        .testTag("district_chip_${dist.nameEn}")
                                )
                            }
                        }
                    }
                }
            }

            // Main Live Weather Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick {},
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KisanGreenPrimary)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Atmospheric Visual Layer
                        AnimatedWeatherAtmosphere(
                            conditionType = weather.conditionEn,
                            modifier = Modifier.matchParentSize()
                        )

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
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = KisanGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = selectedDistrict?.getName(currentLanguage) ?: weather.district,
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (weather.isLiveSuccess) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            PulsingLiveDot(color = KisanGold, size = 5.dp)
                                        }
                                    }
                                    Text(
                                        text = "Forecast Source: ${weather.dataSource} • ${weather.coordinates}",
                                        color = Color(0xFFD0E8D0),
                                        fontSize = 10.5.sp
                                    )
                                }

                                Text(
                                    text = if (weather.conditionEn.contains("Rain", true) || weather.conditionEn.contains("Drizzle", true)) "🌧️"
                                    else if (weather.conditionEn.contains("Thunder", true)) "⛈️"
                                    else if (weather.conditionEn.contains("Cloud", true) || weather.conditionEn.contains("Overcast", true)) "⛅"
                                    else "☀️",
                                    fontSize = 38.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Text(
                                        text = "${weather.tempCelsius}",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "°C",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KisanGold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = weather.getCondition(currentLanguage),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2
                                    )
                                    Text(
                                        text = "High / Low: ${weather.forecast5Days.firstOrNull()?.tempMax ?: (weather.tempCelsius + 3)}° / ${weather.forecast5Days.firstOrNull()?.tempMin ?: (weather.tempCelsius - 4)}°",
                                        fontSize = 11.sp,
                                        color = Color(0xFFE2F0E2)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Stats Grid (Rain, Humidity, Wind)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                WeatherStatItem(
                                    icon = Icons.Default.WaterDrop,
                                    label = "Today's Peak Rain Prob.",
                                    value = "${weather.rainChancePercent}%"
                                )
                                WeatherStatItem(
                                    icon = Icons.Default.Cloud,
                                    label = "Relative Humidity",
                                    value = "${weather.humidityPercent}%"
                                )
                                WeatherStatItem(
                                    icon = Icons.Default.Air,
                                    label = "Wind Velocity",
                                    value = "${weather.windKmh} km/h"
                                )
                            }
                        }
                    }
                }
            }

            // PROMINENT "SAY SELECTED PLACE WEATHER" AUDIO BUTTON
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .bounceClick {
                            if (isPlayingAudio) {
                                onStopAudio()
                            } else {
                                onPlayAudio(weather.getAudioBulletin(currentLanguage))
                            }
                        }
                        .testTag("play_weather_audio_btn"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPlayingAudio) KisanSaffron else KisanGold
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Play voice audio advisory",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isPlayingAudio) "⏹️ " + LanguageManager.getString("stop_audio", currentLanguage)
                                        else "🗣️ " + when (currentLanguage) {
                                            AppLanguage.MR -> "${selectedDistrict?.nameMr ?: weather.district} चे हवामान ऐका"
                                            AppLanguage.HI -> "${selectedDistrict?.nameHi ?: weather.district} का मौसम सुनें"
                                            AppLanguage.GU -> "${selectedDistrict?.nameGu ?: weather.district} નું હવામાન સાંભળો"
                                            AppLanguage.EN -> "Say Weather for ${selectedDistrict?.nameEn ?: weather.district}"
                                            else -> "${selectedDistrict?.nameHi ?: weather.district} का मौसम सुनें"
                                        },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )
                                    if (isPlayingAudio) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        AudioWaveformBars(isPlaying = true, barColor = Color.Black, barCount = 5)
                                    }
                                }
                                Text(
                                    text = "Spoken bulletin in ${currentLanguage.nativeName} based on Open-Meteo forecast",
                                    fontSize = 11.sp,
                                    color = Color(0xFF222222)
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Real-time Spoken Text Preview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = KisanGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Agro-Meteorology Advisory / कृषी हवामान सल्ला",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = KisanGreenPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = weather.getAudioBulletin(currentLanguage),
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 5-Day Farming Forecast Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageManager.getString("forecast_5_days", currentLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "5-Day Trend",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }

            // 5-Day Cards
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(weather.forecast5Days) { day ->
                        ForecastDayCard(day = day)
                    }
                }
            }

            // Field Spray & Irrigation Precision Advisory
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = KisanGreenContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "💡 Farmer Safety & Spray Decision Index",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KisanGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val spraySafety = if (weather.rainChancePercent > 50) {
                            "⚠️ Spray Caution: Rain chance is ${weather.rainChancePercent}%. Chemical foliar sprays may wash off. Postpone application."
                        } else if (weather.windKmh > 20) {
                            "💨 Drift Warning: Wind velocity is ${weather.windKmh} km/h. Use drift-reduction nozzles and spray during dawn or evening."
                        } else {
                            "✅ Optimal Conditions: Temperature (${weather.tempCelsius}°C) and wind (${weather.windKmh} km/h) are ideal for pesticide/fertilizer spraying."
                        }
                        Text(
                            text = spraySafety,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Maintain furrow drainage channels in onion and soybean plots.\n• Ensure proper storage aeration for harvested crops.",
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp,
                            color = Slate600
                        )
                    }
                }
            }
        }
    }

@Composable
fun WeatherStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = label, color = Color(0xFFD0E8D0), fontSize = 10.sp)
    }
}

@Composable
fun ForecastDayCard(day: ForecastDay) {
    Card(
        modifier = Modifier
            .width(105.dp)
            .testTag("forecast_day_${day.dayName}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = day.dayName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = day.iconEmoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${day.tempMax}° / ${day.tempMin}°", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = "${day.rainProbPercent}%", fontSize = 10.sp, color = Color(0xFF1976D2))
            }
        }
    }
}
