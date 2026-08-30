package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PricePoint
import com.example.ui.theme.KisanGreenPrimary
import com.example.ui.theme.KisanGreenPrimaryDark
import com.example.ui.theme.KisanSaffron
import kotlin.math.max
import kotlin.math.min

@Composable
fun PriceHistoryChart(
    pricePoints: List<PricePoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = KisanGreenPrimary,
    showVolumeBars: Boolean = true
) {
    if (pricePoints.isEmpty()) return

    val minPrice = pricePoints.minOfOrNull { it.price } ?: 0
    val maxPrice = pricePoints.maxOfOrNull { it.price } ?: 100
    val priceRange = max(100, maxPrice - minPrice)
    val maxVolume = pricePoints.maxOfOrNull { it.volumeTonnes } ?: 1

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Min: ₹$minPrice/qtl",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Peak: ₹$maxPrice/qtl",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = KisanSaffron
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val chartBottom = height - 24.dp.toPx()
                val chartTop = 16.dp.toPx()
                val usableHeight = chartBottom - chartTop

                val stepX = width / (pricePoints.size - 1).coerceAtLeast(1)

                // Draw Horizontal Grid lines
                for (i in 0..3) {
                    val y = chartTop + (usableHeight / 3) * i
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Points calculation
                val points = pricePoints.mapIndexed { index, point ->
                    val x = index * stepX
                    val normalized = (point.price - minPrice).toFloat() / priceRange.toFloat()
                    val y = chartBottom - (normalized * usableHeight)
                    Offset(x, y)
                }

                // Gradient Area Fill
                val fillPath = Path().apply {
                    moveTo(0f, chartBottom)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(width, chartBottom)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                        startY = chartTop,
                        endY = chartBottom
                    )
                )

                // Draw Main Trend Line
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        // Smooth cubic bezier
                        val controlX = (prev.x + curr.x) / 2f
                        cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw Points & Volume Bars
                points.forEachIndexed { index, pt ->
                    // Volume Bar at Bottom
                    if (showVolumeBars) {
                        val vol = pricePoints[index].volumeTonnes
                        val barHeight = (vol.toFloat() / maxVolume.toFloat()) * 18.dp.toPx()
                        drawRect(
                            color = lineColor.copy(alpha = 0.2f),
                            topLeft = Offset(pt.x - 4.dp.toPx(), chartBottom - barHeight),
                            size = androidx.compose.ui.geometry.Size(8.dp.toPx(), barHeight)
                        )
                    }

                    // Point circles
                    drawCircle(
                        color = Color.White,
                        radius = 4.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = lineColor,
                        radius = 3.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        // Date X-Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            pricePoints.forEachIndexed { index, pt ->
                if (index == 0 || index == pricePoints.size / 2 || index == pricePoints.size - 1) {
                    Text(
                        text = pt.dayLabel,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
