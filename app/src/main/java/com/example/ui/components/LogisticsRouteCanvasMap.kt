package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LogisticsMultiStopRoute
import com.example.data.model.RouteStop
import com.example.data.model.RouteStopType
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun LogisticsRouteCanvasMap(
    route: LogisticsMultiStopRoute,
    modifier: Modifier = Modifier,
    selectedStopIndex: Int? = null,
    onSelectStop: (Int) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Dark modern GIS slate
        modifier = modifier
            .fillMaxWidth()
            .testTag("logistics_route_canvas_map")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Map header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "AI Optimized Route Map & GIS Path",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF22C55E), RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = "Live GPS Active",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Map canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    drawRouteMap(route.stops, size.width, size.height)
                }

                // Legend overlay at bottom
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A).copy(alpha = 0.88f), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MapLegendItem(color = Color(0xFFEAB308), label = "Hub / Depot")
                    MapLegendItem(color = Color(0xFF22C55E), label = "🌾 Farm Pickup")
                    MapLegendItem(color = Color(0xFF38BDF8), label = "🏠 Consumer Drop")
                    Text(
                        text = "${route.totalDistanceKm} km • ${route.estimatedDurationMinutes} mins",
                        color = Color(0xFFF1F5F9),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Route Metrics Summary Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RouteMetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Agriculture,
                    label = "Farm Pickups",
                    value = "${route.totalPickups} Stops",
                    accentColor = Color(0xFF22C55E)
                )
                RouteMetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.HomeWork,
                    label = "Consumer Drops",
                    value = "${route.totalDeliveries} Drops",
                    accentColor = Color(0xFF38BDF8)
                )
                RouteMetricTile(
                    modifier = Modifier.weight(1.1f),
                    icon = Icons.Default.Eco,
                    label = "CO₂ Saved",
                    value = "${route.co2SavedKg} kg",
                    accentColor = Color(0xFF4ADE80)
                )
            }
        }
    }
}

@Composable
private fun MapLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Text(text = label, color = Color(0xFFCBD5E1), fontSize = 10.sp)
    }
}

@Composable
private fun RouteMetricTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(13.dp))
                Text(text = label, color = Color(0xFF94A3B8), fontSize = 9.5.sp)
            }
            Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun DrawScope.drawRouteMap(stops: List<RouteStop>, canvasWidth: Float, canvasHeight: Float) {
    // Background GIS grid lines
    val gridColor = Color(0xFF334155).copy(alpha = 0.4f)
    for (x in 0..4) {
        val posX = (canvasWidth / 4) * x
        drawLine(gridColor, Offset(posX, 0f), Offset(posX, canvasHeight), strokeWidth = 1f)
    }
    for (y in 0..3) {
        val posY = (canvasHeight / 3) * y
        drawLine(gridColor, Offset(0f, posY), Offset(canvasWidth, posY), strokeWidth = 1f)
    }

    if (stops.isEmpty()) {
        // Draw empty hub point
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
        drawCircle(Color(0xFFEAB308), radius = 10f, center = center)
        return
    }

    // Determine coordinate bounds for responsive projection
    var minLat = stops.minOf { it.latitude }
    var maxLat = stops.maxOf { it.latitude }
    var minLon = stops.minOf { it.longitude }
    var maxLon = stops.maxOf { it.longitude }

    if (maxLat - minLat < 0.001) {
        minLat -= 0.05
        maxLat += 0.05
    }
    if (maxLon - minLon < 0.001) {
        minLon -= 0.05
        maxLon += 0.05
    }

    val paddingX = canvasWidth * 0.12f
    val paddingY = canvasHeight * 0.15f
    val plotWidth = canvasWidth - (paddingX * 2f)
    val plotHeight = canvasHeight - (paddingY * 2f)

    fun projectPoint(lat: Double, lon: Double): Offset {
        val normX = ((lon - minLon) / (maxLon - minLon)).toFloat().coerceIn(0f, 1f)
        val normY = (1f - ((lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0f, 1f)
        return Offset(paddingX + (normX * plotWidth), paddingY + (normY * plotHeight))
    }

    val projectedOffsets = stops.map { projectPoint(it.latitude, it.longitude) }

    // 1. Draw connecting route line
    val routePath = Path()
    projectedOffsets.forEachIndexed { idx, offset ->
        if (idx == 0) routePath.moveTo(offset.x, offset.y)
        else routePath.lineTo(offset.x, offset.y)
    }

    // Glow underlay
    drawPath(
        path = routePath,
        color = Color(0xFF38BDF8).copy(alpha = 0.35f),
        style = Stroke(width = 8f, pathEffect = PathEffect.cornerPathEffect(16f))
    )

    // Solid route line
    drawPath(
        path = routePath,
        color = Color(0xFF38BDF8),
        style = Stroke(width = 3.5f, pathEffect = PathEffect.cornerPathEffect(16f))
    )

    // 2. Draw Stop Waypoints & Markers
    projectedOffsets.forEachIndexed { index, offset ->
        val stop = stops[index]
        val pinColor = when (stop.stopType) {
            RouteStopType.PICKUP -> Color(0xFF22C55E) // Green for farmer
            RouteStopType.DELIVERY -> Color(0xFF38BDF8) // Blue for consumer
        }

        // Outer halo
        drawCircle(
            color = pinColor.copy(alpha = 0.25f),
            radius = 16f,
            center = offset
        )

        // Core marker
        drawCircle(
            color = pinColor,
            radius = 9f,
            center = offset
        )

        drawCircle(
            color = Color(0xFF0F172A),
            radius = 4f,
            center = offset
        )

        // Sequence number text
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        drawContext.canvas.nativeCanvas.drawText(
            "${stop.stopSequence}",
            offset.x,
            offset.y - 14f,
            paint
        )
    }
}
