package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SocialPlatform
import com.example.ui.chat.FullScreenChatView
import com.example.ui.chathead.ChatHeadExpandedWindow
import com.example.ui.chathead.ChatHeadOverlay
import com.example.ui.dialogs.AccountLoginDialog
import com.example.ui.dialogs.SimulateIncomingDialog
import com.example.ui.dialogs.StartupQuickConnectDialog
import com.example.ui.theme.MessengerBlue
import com.example.ui.theme.UnreadRed
import com.example.ui.viewmodel.SocialHubViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedDashboardScreen(
    viewModel: SocialHubViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // If full screen chat is active, show full chat
    if (uiState.activeChatId != null) {
        FullScreenChatView(
            conversationId = uiState.activeChatId!!,
            viewModel = viewModel,
            onBackClick = { viewModel.closeFullScreenChat() },
            onPopChatHead = { convId -> viewModel.openChatHeadBubble(convId) }
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF0084FF), Color(0xFFE1306C))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BubbleChart,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "UniChat",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { viewModel.openStartupQuickConnect() }) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Primary Platforms Quick Connect",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { viewModel.toggleThemeMode() }) {
                                Icon(
                                    imageVector = when (uiState.themeMode) {
                                        com.example.data.model.ThemeMode.DARK -> Icons.Default.LightMode
                                        com.example.data.model.ThemeMode.LIGHT -> Icons.Default.DarkMode
                                        com.example.data.model.ThemeMode.SYSTEM -> Icons.Default.DarkMode
                                    },
                                    contentDescription = "Toggle Theme Mode",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(onClick = { viewModel.openSimulateDialog() }) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = "Simulate Incoming Message",
                                    tint = MessengerBlue
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Platform Filter Bar (Visible in Inbox & Notifications tabs)
                    if (uiState.currentTab == 0 || uiState.currentTab == 1) {
                        TopPlatformBar(
                            accounts = uiState.accounts,
                            selectedPlatformFilter = uiState.selectedPlatformFilter,
                            onSelectPlatform = { viewModel.setPlatformFilter(it) }
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Tab 0: Inbox (Messages)
                    NavigationBarItem(
                        selected = uiState.currentTab == 0,
                        onClick = { viewModel.setCurrentTab(0) },
                        icon = {
                            BadgedBox(badge = {
                                if (uiState.totalUnreadMessages > 0) {
                                    Badge(containerColor = UnreadRed) {
                                        Text("${uiState.totalUnreadMessages}")
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = if (uiState.currentTab == 0) Icons.Filled.Forum else Icons.Outlined.Forum,
                                    contentDescription = "Inbox"
                                )
                            }
                        },
                        label = { Text("Inbox", fontWeight = if (uiState.currentTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )

                    // Tab 1: Notifications
                    NavigationBarItem(
                        selected = uiState.currentTab == 1,
                        onClick = { viewModel.setCurrentTab(1) },
                        icon = {
                            BadgedBox(badge = {
                                if (uiState.totalUnreadNotifications > 0) {
                                    Badge(containerColor = UnreadRed) {
                                        Text("${uiState.totalUnreadNotifications}")
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = if (uiState.currentTab == 1) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        },
                        label = { Text("Alerts", fontWeight = if (uiState.currentTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )

                    // Tab 2: Accounts
                    NavigationBarItem(
                        selected = uiState.currentTab == 2,
                        onClick = { viewModel.setCurrentTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentTab == 2) Icons.Filled.Hub else Icons.Outlined.Hub,
                                contentDescription = "Accounts"
                            )
                        },
                        label = { Text("Accounts", fontWeight = if (uiState.currentTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )

                    // Tab 3: Chat Heads Settings & Playground
                    NavigationBarItem(
                        selected = uiState.currentTab == 3,
                        onClick = { viewModel.setCurrentTab(3) },
                        icon = {
                            BadgedBox(badge = {
                                if (uiState.activeChatHeads.isNotEmpty()) {
                                    Badge(containerColor = MessengerBlue) {
                                        Text("${uiState.activeChatHeads.size}")
                                    }
                                }
                            }) {
                                Icon(
                                    imageVector = if (uiState.currentTab == 3) Icons.Filled.BubbleChart else Icons.Default.BubbleChart,
                                    contentDescription = "Chat Heads"
                                )
                            }
                        },
                        label = { Text("Chat Heads", fontWeight = if (uiState.currentTab == 3) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentTab) {
                    0 -> InboxTab(
                        conversations = uiState.filteredConversations,
                        searchQuery = uiState.searchQuery,
                        selectedCategory = uiState.selectedInboxCategory,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategorySelect = { viewModel.setInboxCategory(it) },
                        onConversationClick = { viewModel.openFullScreenChat(it) },
                        onPopChatHead = { viewModel.openChatHeadBubble(it) },
                        onTogglePin = { viewModel.togglePinConversation(it) },
                        onMarkRead = { viewModel.markConversationAsRead(it) },
                        onSimulateClick = { viewModel.openSimulateDialog() }
                    )

                    1 -> NotificationsTab(
                        notifications = uiState.filteredNotifications,
                        selectedFilter = uiState.selectedNotificationFilter,
                        onFilterSelect = { viewModel.setNotificationFilter(it) },
                        onNotificationClick = { notif ->
                            viewModel.markNotificationRead(notif.id)
                            notif.targetConversationId?.let { viewModel.openFullScreenChat(it) }
                        },
                        onOpenChatHead = { viewModel.openChatHeadBubble(it) },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() },
                        onClearAll = { viewModel.clearAllNotifications() }
                    )

                    2 -> AccountsTab(
                        accounts = uiState.accounts,
                        themeMode = uiState.themeMode,
                        isSystemOverlayActive = uiState.isSystemOverlayActive,
                        onToggleSystemOverlay = { viewModel.toggleSystemOverlay(context) },
                        onOpenQuickSetup = { viewModel.openStartupQuickConnect() },
                        onThemeModeChange = { viewModel.setThemeMode(it) },
                        onToggleConnect = { accId, status -> viewModel.toggleAccountConnected(accId, status) },
                        onToggleChatHeads = { accId, status -> viewModel.toggleAccountChatHeads(accId, status) },
                        onOpenLoginDialog = { platform -> viewModel.openLoginDialog(platform) }
                    )

                    3 -> ChatHeadsPlaygroundTab(
                        activeChatHeads = uiState.activeChatHeads,
                        conversations = uiState.conversations,
                        onSpawnChatHead = { viewModel.openChatHeadBubble(it) },
                        onDismissChatHead = { viewModel.dismissChatHead(it) },
                        onDismissAll = { viewModel.dismissAllChatHeads() },
                        onQuickSimulate = { platform, sender, msg ->
                            viewModel.triggerSimulatedMessage(platform, sender, msg)
                        },
                        onOpenCustomSimulate = { viewModel.openSimulateDialog() }
                    )
                }
            }
        }

        // Floating Messenger Chat Heads Overlay Layer (Renders on top of everything inside app)
        ChatHeadOverlay(
            activeChatHeads = uiState.activeChatHeads,
            latestToastBubble = uiState.latestToastBubble,
            onChatHeadClick = { viewModel.toggleChatHeadState(it) },
            onChatHeadDismissed = { viewModel.dismissChatHead(it) }
        )

        // Expanded Floating Chat Head Window (Messenger Modal Bubble)
        if (uiState.expandedChatHeadId != null) {
            ChatHeadExpandedWindow(
                expandedConversationId = uiState.expandedChatHeadId!!,
                activeChatHeads = uiState.activeChatHeads,
                viewModel = viewModel,
                onSelectChatHead = { viewModel.openChatHeadBubble(it) },
                onOpenFullScreen = { convId ->
                    viewModel.closeExpandedChatHead()
                    viewModel.openFullScreenChat(convId)
                },
                onMinimize = { viewModel.closeExpandedChatHead() },
                onClose = { convId -> viewModel.dismissChatHead(convId) }
            )
        }

        // Startup Quick Connect Dialog Modal (Facebook, Discord, Twitter, Threads, Instagram)
        if (uiState.showStartupQuickConnect) {
            StartupQuickConnectDialog(
                accounts = uiState.accounts,
                onDismiss = { viewModel.closeStartupQuickConnect() },
                onOpenLoginDialog = { platform ->
                    viewModel.closeStartupQuickConnect()
                    viewModel.openLoginDialog(platform)
                },
                onQuickConnectAll = {
                    SocialPlatform.primaryPlatforms.forEach { plat ->
                        viewModel.completeLogin(plat, "cody_${plat.id}", "Cody Wrin")
                    }
                    viewModel.closeStartupQuickConnect()
                },
                isSystemOverlayActive = uiState.isSystemOverlayActive,
                onToggleSystemOverlay = { viewModel.toggleSystemOverlay(context) }
            )
        }

        // Login Dialog Modal
        if (uiState.loginDialogPlatform != null) {
            AccountLoginDialog(
                platform = uiState.loginDialogPlatform!!,
                onDismiss = { viewModel.closeLoginDialog() },
                onConnect = { username, display ->
                    viewModel.completeLogin(uiState.loginDialogPlatform!!, username, display)
                }
            )
        }

        // Simulate Message Dialog Modal
        if (uiState.showSimulateDialog) {
            SimulateIncomingDialog(
                onDismiss = { viewModel.closeSimulateDialog() },
                onTrigger = { platform, sender, text ->
                    viewModel.triggerSimulatedMessage(platform, sender, text)
                }
            )
        }
    }
}
