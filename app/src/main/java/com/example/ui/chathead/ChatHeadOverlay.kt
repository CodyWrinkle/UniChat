package com.example.ui.chathead

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ConversationEntity
import com.example.data.model.SocialPlatform
import com.example.ui.components.SocialAvatar
import com.example.ui.theme.UnreadRed
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Facebook Messenger-style Stacked Chat Heads Overlay.
 * All active conversation bubbles are grouped into a single draggable stack on screen.
 * Tapping opens the multi-chat switcher in the expanded window.
 */
@Composable
fun ChatHeadOverlay(
    activeChatHeads: List<ConversationEntity>,
    latestToastBubble: Pair<String, String>?,
    onChatHeadClick: (String) -> Unit,
    onChatHeadDismissed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (activeChatHeads.isEmpty()) return

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val headSizeDp = 64.dp
        val headSizePx = with(density) { headSizeDp.toPx() }

        var isDragging by remember { mutableStateOf(false) }
        var isOverDismissZone by remember { mutableStateOf(false) }

        val dismissZoneX = screenWidthPx / 2f
        val dismissZoneY = screenHeightPx - with(density) { 100.dp.toPx() }
        val dismissRadiusPx = with(density) { 65.dp.toPx() }

        // Render the single stacked chat head
        StackedChatHeadGroup(
            activeChatHeads = activeChatHeads,
            screenWidthPx = screenWidthPx,
            screenHeightPx = screenHeightPx,
            headSizePx = headSizePx,
            dismissZoneX = dismissZoneX,
            dismissZoneY = dismissZoneY,
            dismissRadiusPx = dismissRadiusPx,
            latestToastBubble = latestToastBubble,
            onDragStateChange = { dragging, overDismiss ->
                isDragging = dragging
                isOverDismissZone = overDismiss
            },
            onClick = { topConvId -> onChatHeadClick(topConvId) },
            onDismissAll = {
                // Dismiss the stack
                activeChatHeads.forEach { onChatHeadDismissed(it.id) }
            }
        )

        // Bottom Dismiss Target Zone (appears when dragging the stack)
        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn() + scaleIn(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp)
        ) {
            val targetBg = if (isOverDismissZone) Color(0xFFEF4444) else Color(0xCC1E293B)
            val targetScale = if (isOverDismissZone) 1.25f else 1.0f

            Box(
                modifier = Modifier
                    .size((68 * targetScale).dp)
                    .shadow(14.dp, CircleShape)
                    .clip(CircleShape)
                    .background(targetBg)
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Chat Stack",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Drop to Close",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StackedChatHeadGroup(
    activeChatHeads: List<ConversationEntity>,
    screenWidthPx: Float,
    screenHeightPx: Float,
    headSizePx: Float,
    dismissZoneX: Float,
    dismissZoneY: Float,
    dismissRadiusPx: Float,
    latestToastBubble: Pair<String, String>?,
    onDragStateChange: (Boolean, Boolean) -> Unit,
    onClick: (String) -> Unit,
    onDismissAll: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val topConversation = activeChatHeads.first()
    val secondConversation = activeChatHeads.getOrNull(1)
    val thirdConversation = activeChatHeads.getOrNull(2)

    val topPlatform = SocialPlatform.fromId(topConversation.platformId)
    val totalUnread = activeChatHeads.sumOf { it.unreadCount }
    val stackCount = activeChatHeads.size

    // Initial position: stacked along right edge
    val initialX = screenWidthPx - headSizePx - 20f
    val initialY = screenHeightPx * 0.32f

    val offsetX = remember { Animatable(initialX) }
    val offsetY = remember { Animatable(initialY) }

    var isDragging by remember { mutableStateOf(false) }

    // Check if toast message is for any of the active chat heads in this stack
    val isToastShowing = latestToastBubble != null && activeChatHeads.any { it.participantName == latestToastBubble.first }
    val toastSender = latestToastBubble?.first
    val toastMessage = latestToastBubble?.second

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .pointerInput(activeChatHeads.size) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onDragStateChange(true, false)
                    },
                    onDragEnd = {
                        isDragging = false
                        // Check if released over dismiss zone
                        val distToDismiss = sqrt(
                            (offsetX.value + headSizePx / 2 - dismissZoneX) * (offsetX.value + headSizePx / 2 - dismissZoneX) +
                                    (offsetY.value + headSizePx / 2 - dismissZoneY) * (offsetY.value + headSizePx / 2 - dismissZoneY)
                        )
                        if (distToDismiss < dismissRadiusPx) {
                            onDragStateChange(false, false)
                            onDismissAll()
                        } else {
                            onDragStateChange(false, false)
                            // Snap to nearest edge (left or right)
                            val snapX = if (offsetX.value + headSizePx / 2 < screenWidthPx / 2f) {
                                16f
                            } else {
                                screenWidthPx - headSizePx - 16f
                            }
                            val clampedY = offsetY.value.coerceIn(80f, screenHeightPx - headSizePx - 120f)
                            coroutineScope.launch {
                                offsetX.animateTo(
                                    snapX,
                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                                )
                            }
                            coroutineScope.launch {
                                offsetY.animateTo(clampedY, spring())
                            }
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        onDragStateChange(false, false)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)

                            val distToDismiss = sqrt(
                                (offsetX.value + headSizePx / 2 - dismissZoneX) * (offsetX.value + headSizePx / 2 - dismissZoneX) +
                                        (offsetY.value + headSizePx / 2 - dismissZoneY) * (offsetY.value + headSizePx / 2 - dismissZoneY)
                            )
                            onDragStateChange(true, distToDismiss < dismissRadiusPx)
                        }
                    }
                )
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val isOnRightSide = offsetX.value >= screenWidthPx / 2f

            // Speech Bubble Preview Toast if incoming message & docked to right
            if (isToastShowing && toastMessage != null && isOnRightSide) {
                Surface(
                    shape = RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .widthIn(max = 210.dp)
                        .clickable { onClick(topConversation.id) }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = toastSender ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = topPlatform.primaryColor
                            )
                            if (stackCount > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• Stack (${stackCount})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        Text(
                            text = toastMessage,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // --- THE 3D STACKED CHAT HEAD CLUSTER ---
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clickable { onClick(topConversation.id) },
                contentAlignment = Alignment.Center
            ) {
                // Layer 3: Back-most bubble peek (if 3 or more chats active)
                if (thirdConversation != null) {
                    val p3 = SocialPlatform.fromId(thirdConversation.platformId)
                    Box(
                        modifier = Modifier
                            .offset(x = (-8).dp, y = (-8).dp)
                            .size(52.dp)
                            .shadow(3.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF334155))
                            .border(1.5.dp, p3.primaryColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        SocialAvatar(
                            emoji = thirdConversation.participantAvatarEmoji,
                            bgColor = thirdConversation.avatarBgColor,
                            platform = p3,
                            size = 48.dp,
                            showOnline = false,
                            showPlatformBadge = false
                        )
                    }
                }

                // Layer 2: Second bubble peek (if 2 or more chats active)
                if (secondConversation != null) {
                    val p2 = SocialPlatform.fromId(secondConversation.platformId)
                    Box(
                        modifier = Modifier
                            .offset(x = (-4).dp, y = (-4).dp)
                            .size(56.dp)
                            .shadow(5.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(2.dp, p2.primaryColor.copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        SocialAvatar(
                            emoji = secondConversation.participantAvatarEmoji,
                            bgColor = secondConversation.avatarBgColor,
                            platform = p2,
                            size = 52.dp,
                            showOnline = false,
                            showPlatformBadge = false
                        )
                    }
                }

                // Layer 1 (FRONT): Main Top Chat Head Face
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.5.dp, topPlatform.primaryColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    SocialAvatar(
                        emoji = topConversation.participantAvatarEmoji,
                        bgColor = topConversation.avatarBgColor,
                        platform = topPlatform,
                        size = 58.dp,
                        showOnline = topConversation.isOnline,
                        showPlatformBadge = true
                    )
                }

                // Stack Count Badge (Top-Right: Shows "+2" or total count if multi-chat stack)
                if (stackCount > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-2).dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color(0xFF8B5CF6)
                                    )
                                )
                            )
                            .border(1.5.dp, Color.White, CircleShape)
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(9.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$stackCount",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                    }
                }

                // Total Unread Count Badge (Top-Left)
                if (totalUnread > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-4).dp, y = (-2).dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(UnreadRed)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (totalUnread > 9) "9+" else "$totalUnread",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Speech Bubble Preview Toast if docked to left
            if (isToastShowing && toastMessage != null && !isOnRightSide) {
                Surface(
                    shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .widthIn(max = 210.dp)
                        .clickable { onClick(topConversation.id) }
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = toastSender ?: "",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = topPlatform.primaryColor
                            )
                            if (stackCount > 1) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• Stack (${stackCount})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                        Text(
                            text = toastMessage,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
