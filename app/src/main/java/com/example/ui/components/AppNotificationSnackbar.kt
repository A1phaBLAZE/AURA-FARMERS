package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType

@Composable
fun AppNotificationBanner(
    notification: AppNotification?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = fadeIn(animationSpec = tween(220)) + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(250)
        ),
        exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(220)
        ),
        modifier = modifier
    ) {
        if (notification != null) {
            val (bgColor, borderColor, iconColor, iconVector, badgeTitle) = when (notification.type) {
                NotificationType.SUCCESS -> NotificationVisuals(
                    backgroundColor = Color(0xFFF0FDF4),
                    borderColor = Color(0xFF86EFAC),
                    iconColor = Color(0xFF16A34A),
                    icon = Icons.Default.CheckCircle,
                    title = "SUCCESS"
                )
                NotificationType.ERROR -> NotificationVisuals(
                    backgroundColor = Color(0xFFFEF2F2),
                    borderColor = Color(0xFFFCA5A5),
                    iconColor = Color(0xFFDC2626),
                    icon = Icons.Default.ErrorOutline,
                    title = "ERROR"
                )
                NotificationType.INFO -> NotificationVisuals(
                    backgroundColor = Color(0xFFEFF6FF),
                    borderColor = Color(0xFF93C5FD),
                    iconColor = Color(0xFF2563EB),
                    icon = Icons.Default.Info,
                    title = "NOTICE"
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                    .testTag("app_notification_banner_${notification.type.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = badgeTitle,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = badgeTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = iconColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = notification.message,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1F2937),
                            lineHeight = 17.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("app_notification_dismiss_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class NotificationVisuals(
    val backgroundColor: Color,
    val borderColor: Color,
    val iconColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String
)
