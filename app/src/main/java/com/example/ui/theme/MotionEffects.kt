package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Adds a soft, tactile spring bounce animation when an item is pressed.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.94f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceClickScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Animated Shimmer Brush for classy metallic/golden/emerald shine effects.
 */
@Composable
fun rememberShimmerBrush(
    shimmerColors: List<Color> = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.35f),
        Color.White.copy(alpha = 0.0f)
    ),
    durationMillis: Int = 1600
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )
}

/**
 * Pulsing Live Indicator Dot for Mandi rates and TEE Security
 */
@Composable
fun PulsingLiveDot(
    color: Color = Color(0xFF22C55E),
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseLiveTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseLiveScale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseLiveAlpha"
    )

    Box(
        modifier = modifier.size(size * 2),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(size * scale)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha))
        )
        // Center solid core
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * Animated Audio Equalizer Waveform for Voice TTS and Farmer Audio summary.
 */
@Composable
fun AudioWaveformBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White,
    barCount: Int = 5
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioWaveTransition")

    val animValues = List(barCount) { index ->
        val duration = 400 + (index * 90)
        infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "waveBar$index"
        )
    }

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animValues.forEachIndexed { _, animState ->
            val heightFraction = if (isPlaying) animState.value else 0.3f
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(heightFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * Animated Weather Atmosphere: Drifting Clouds & Golden Rotating Sunrays Canvas
 */
@Composable
fun AnimatedWeatherAtmosphere(
    conditionType: String, // "rain", "sunny", "cloudy"
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "weatherCanvasTransition")

    // Rotation for sun
    val sunAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotate"
    )

    // Floating offset for clouds/rain
    val driftOffset by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloudDrift"
    )

    // Rain drop vertical animation
    val rainDropAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainDrop"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        if (conditionType.contains("sun", ignoreCase = true) || conditionType.contains("clear", ignoreCase = true)) {
            // Sun with rotating warm rays
            rotate(sunAngle, pivot = Offset(w * 0.85f, h * 0.25f)) {
                for (i in 0 until 8) {
                    val angleRad = Math.toRadians((i * 45).toDouble())
                    val startX = (w * 0.85f + 32 * kotlin.math.cos(angleRad)).toFloat()
                    val startY = (h * 0.25f + 32 * kotlin.math.sin(angleRad)).toFloat()
                    val endX = (w * 0.85f + 50 * kotlin.math.cos(angleRad)).toFloat()
                    val endY = (h * 0.25f + 50 * kotlin.math.sin(angleRad)).toFloat()

                    drawLine(
                        color = Color(0xFFFFD54F).copy(alpha = 0.45f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        if (conditionType.contains("rain", ignoreCase = true) || conditionType.contains("shower", ignoreCase = true)) {
            // Animated subtle raindrops
            val rainAlpha = 0.4f
            for (i in 0 until 12) {
                val startX = (w * 0.1f) + (i * (w * 0.8f / 12f))
                val baseY = (h * 0.35f) + (((i * 17) % 30))
                val currentY = baseY + (rainDropAnim * (h * 0.55f))

                drawLine(
                    color = Color(0xFF93C5FD).copy(alpha = rainAlpha),
                    start = Offset(startX + (rainDropAnim * 8f), currentY),
                    end = Offset(startX + 6f + (rainDropAnim * 8f), currentY + 12f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Animated Breathing Glow Box for Important Bids & Escrow Status
 */
@Composable
fun AnimatedBreathingCard(
    modifier: Modifier = Modifier,
    glowColor: Color = KisanGreenPrimary,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowTransition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = glowColor.copy(alpha = alphaAnim),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                )
            },
        content = content
    )
}
