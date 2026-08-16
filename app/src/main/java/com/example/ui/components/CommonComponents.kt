package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SocialPlatform
import com.example.ui.theme.OnlineGreen

@Composable
fun SocialAvatar(
    emoji: String,
    bgColor: Long,
    platform: SocialPlatform,
    size: Dp = 48.dp,
    showOnline: Boolean = true,
    showPlatformBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Main Avatar Circle
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(bgColor).copy(alpha = 0.85f),
                            Color(bgColor)
                        )
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = (size.value * 0.45f).sp
            )
        }

        // Platform Badge mini icon on bottom-right
        if (showPlatformBadge) {
            val badgeSize = (size.value * 0.4f).dp
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(platform.primaryColor, platform.secondaryColor)
                        )
                    )
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = platform.iconSymbol,
                    fontSize = (badgeSize.value * 0.55f).sp,
                    color = Color.White
                )
            }
        }

        // Online Indicator dot on top-right
        if (showOnline) {
            val dotSize = (size.value * 0.24f).dp
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(OnlineGreen)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@Composable
fun PlatformBadge(
    platform: SocialPlatform,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = platform.primaryColor.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, platform.primaryColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = platform.iconSymbol,
                fontSize = if (compact) 10.sp else 12.sp
            )
            if (!compact) {
                Text(
                    text = " " + platform.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = platform.primaryColor,
                        fontWeight = FontWeight.Bold
                    ),
                    fontSize = 11.sp
                )
            }
        }
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "Yesterday"
        else -> "${days}d ago"
    }
}
