package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.KisanGreenDark
import com.example.ui.theme.KisanGreenPrimary
import com.example.ui.theme.KisanGreenPrimaryDark
import com.example.ui.theme.KisanSaffron
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun CropTrendForecastChart(
    analysis: CropForecastAnalysis,
    selectedPriceUnit: PriceUnit,
    currentLanguage: AppLanguage,
    modifier: Modifier = Modifier,
    onPointSelected: ((TrendDataPoint?) -> Unit)? = null
) {
    val timeline = remember(analysis) { analysis.combinedTimeline }
    if (timeline.isEmpty()) return

    val historicalPoints = remember(analysis) { analysis.historicalPoints }
    val predictedPoints = remember(analysis) { analysis.predictedPoints }

    val allPrices = remember(timeline) { timeline.map { it.pricePerQuintal } }
    val minRawPrice = (allPrices.minOrNull() ?: 1000)
    val maxRawPrice = (allPrices.maxOrNull() ?: 2000)
    val paddingSpan = max(100, ((maxRawPrice - minRawPrice) * 0.15).toInt())
    val chartMin = max(0, minRawPrice - paddingSpan)
    val chartMax = maxRawPrice + paddingSpan
    val priceRange = max(100, chartMax - chartMin)

    var selectedIndex by remember(analysis) { mutableStateOf(historicalPoints.size - 1) }
    val activePoint = timeline.getOrNull(selectedIndex)

    LaunchedEffect(activePoint) {
        onPointSelected?.invoke(activePoint)
    }

    val officialLineColor = KisanGreenPrimary
    val aiLineColor = Color(0xFFD97706) // Distinct Amber-Gold
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("crop_trend_forecast_chart")
    ) {
        // Chart Header with Min, Peak & Current
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Historical & 30-Day AI Forecast",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any node on the line to inspect date & price",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(timeline) {
                        detectTapGestures { offset ->
                            val stepX = size.width / (timeline.size - 1).coerceAtLeast(1)
                            val tappedIndex = (offset.x / stepX).toInt().coerceIn(0, timeline.size - 1)
                            selectedIndex = tappedIndex
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val chartTop = 16.dp.toPx()
                val chartBottom = height - 28.dp.toPx()
                val usableHeight = chartBottom - chartTop
                val stepX = width / (timeline.size - 1).coerceAtLeast(1)

                // 1. Draw horizontal guide lines & price labels
                val gridSteps = 3
                for (i in 0..gridSteps) {
                    val y = chartTop + (usableHeight / gridSteps) * i
                    val priceVal = chartMax - ((priceRange.toFloat() / gridSteps) * i).toInt()
                    val convertedPrice = selectedPriceUnit.convertPrice(priceVal.toDouble())
                    val priceText = "₹${convertedPrice.toInt()}"

                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                // 2. Map coordinates for all timeline points
                val calculatedOffsets = timeline.mapIndexed { index, point ->
                    val x = index * stepX
                    val normalized = (point.pricePerQuintal - chartMin).toFloat() / priceRange.toFloat()
                    val y = chartBottom - (normalized * usableHeight)
                    Offset(x, y)
                }

                val officialCount = historicalPoints.size
                val officialOffsets = calculatedOffsets.take(officialCount)
                val predictionOffsets = if (officialOffsets.isNotEmpty()) {
                    listOf(officialOffsets.last()) + calculatedOffsets.drop(officialCount)
                } else calculatedOffsets

                // 3. Draw Vertical Transition Line dividing Official vs AI Prediction
                if (officialOffsets.isNotEmpty() && predictionOffsets.size > 1) {
                    val transitionX = officialOffsets.last().x
                    drawLine(
                        color = aiLineColor.copy(alpha = 0.6f),
                        start = Offset(transitionX, chartTop - 8.dp.toPx()),
                        end = Offset(transitionX, chartBottom + 16.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                }

                // 4. Fill Area Under Official Historical Curve (Green Gradient)
                if (officialOffsets.isNotEmpty()) {
                    val histFill = Path().apply {
                        moveTo(officialOffsets.first().x, chartBottom)
                        officialOffsets.forEach { lineTo(it.x, it.y) }
                        lineTo(officialOffsets.last().x, chartBottom)
                        close()
                    }
                    drawPath(
                        path = histFill,
                        brush = Brush.verticalGradient(
                            colors = listOf(officialLineColor.copy(alpha = 0.35f), Color.Transparent),
                            startY = chartTop,
                            endY = chartBottom
                        )
                    )
                }

                // 5. Fill Area Under Prediction Curve (Amber Gradient)
                if (predictionOffsets.size > 1) {
                    val predFill = Path().apply {
                        moveTo(predictionOffsets.first().x, chartBottom)
                        predictionOffsets.forEach { lineTo(it.x, it.y) }
                        lineTo(predictionOffsets.last().x, chartBottom)
                        close()
                    }
                    drawPath(
                        path = predFill,
                        brush = Brush.verticalGradient(
                            colors = listOf(aiLineColor.copy(alpha = 0.28f), Color.Transparent),
                            startY = chartTop,
                            endY = chartBottom
                        )
                    )
                }

                // 6. Draw Official Solid Curve
                if (officialOffsets.size > 1) {
                    val histLine = Path().apply {
                        moveTo(officialOffsets.first().x, officialOffsets.first().y)
                        for (i in 1 until officialOffsets.size) {
                            val prev = officialOffsets[i - 1]
                            val curr = officialOffsets[i]
                            val controlX = (prev.x + curr.x) / 2f
                            cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                        }
                    }
                    drawPath(
                        path = histLine,
                        color = officialLineColor,
                        style = Stroke(width = 3.2.dp.toPx())
                    )
                }

                // 7. Draw AI Prediction Dashed Curve
                if (predictionOffsets.size > 1) {
                    val predLine = Path().apply {
                        moveTo(predictionOffsets.first().x, predictionOffsets.first().y)
                        for (i in 1 until predictionOffsets.size) {
                            val prev = predictionOffsets[i - 1]
                            val curr = predictionOffsets[i]
                            val controlX = (prev.x + curr.x) / 2f
                            cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                        }
                    }
                    drawPath(
                        path = predLine,
                        color = aiLineColor,
                        style = Stroke(
                            width = 3.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )
                }

                // 8. Draw Node Points
                calculatedOffsets.forEachIndexed { index, pt ->
                    val isPred = timeline[index].isPrediction
                    val isSelected = index == selectedIndex
                    val nodeColor = if (isPred) aiLineColor else officialLineColor

                    // Glow or Ring if selected
                    if (isSelected) {
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.35f),
                            radius = 12.dp.toPx(),
                            center = pt
                        )
                        drawLine(
                            color = nodeColor.copy(alpha = 0.5f),
                            start = Offset(pt.x, chartTop),
                            end = Offset(pt.x, chartBottom),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                        )
                    }

                    // Outer white ring
                    drawCircle(
                        color = Color.White,
                        radius = (if (isSelected) 6.dp else 4.5.dp).toPx(),
                        center = pt
                    )

                    // Center dot
                    drawCircle(
                        color = nodeColor,
                        radius = (if (isSelected) 4.5.dp else 3.dp).toPx(),
                        center = pt
                    )
                }
            }
        }

        // X-Axis Date Range Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val firstDate = timeline.firstOrNull()?.dateLabel ?: ""
            val todayDate = historicalPoints.lastOrNull()?.dateLabel ?: "Today"
            val futureEnd = predictedPoints.lastOrNull()?.dateLabel ?: "+30D"

            Text(
                text = "Past: $firstDate",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "• $todayDate •",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = KisanGreenPrimaryDark
            )
            Text(
                text = "Forecast: $futureEnd",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD97706)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legend: Official vs AI Prediction
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Official Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(officialLineColor)
                )
                Text(
                    text = "Official historical price",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // AI Prediction Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(aiLineColor)
                )
                Text(
                    text = "30-day AI prediction",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFB45309)
                )
            }
        }

        // Active Inspection Node Card
        activePoint?.let { pt ->
            Spacer(modifier = Modifier.height(10.dp))
            val convertedPrice = selectedPriceUnit.convertPrice(pt.pricePerQuintal.toDouble())
            val deltaFromCurrent = pt.pricePerQuintal - analysis.currentOfficialPrice
            val isPrediction = pt.isPrediction

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPrediction) Color(0xFFFFFBEB) else Color(0xFFF0FDF4)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isPrediction) Color(0xFFFDE68A) else Color(0xFFBBF7D0),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isPrediction) Icons.Default.AutoAwesome else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isPrediction) Color(0xFFD97706) else KisanGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = pt.dateLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isPrediction) "✨ AI Forecasted Price (Estimate)" else "🏛️ Official APMC Recorded Rate",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = if (isPrediction) Color(0xFF92400E) else Color(0xFF166534)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${convertedPrice.toInt()} ${selectedPriceUnit.getSymbol(currentLanguage)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPrediction) Color(0xFFB45309) else KisanGreenDark
                        )
                        if (deltaFromCurrent != 0) {
                            Text(
                                text = if (deltaFromCurrent > 0) "+₹$deltaFromCurrent vs Today" else "-₹${abs(deltaFromCurrent)} vs Today",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (deltaFromCurrent > 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }
        }
    }
}
