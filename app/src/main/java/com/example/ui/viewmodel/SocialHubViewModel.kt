package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ConversationEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.SocialAccountEntity
import com.example.data.local.SocialDatabase
import com.example.data.model.SocialPlatform
import com.example.data.model.ThemeMode
import com.example.data.repository.SocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SocialHubUiState(
    val accounts: List<SocialAccountEntity> = emptyList(),
    val conversations: List<ConversationEntity> = emptyList(),
    val filteredConversations: List<ConversationEntity> = emptyList(),
    val activeChatHeads: List<ConversationEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val filteredNotifications: List<NotificationEntity> = emptyList(),
    val selectedPlatformFilter: String? = null, // null for All
    val selectedInboxCategory: String = "ALL", // "ALL", "UNREAD", "PINNED", "DIRECT"
    val selectedNotificationFilter: String = "ALL", // "ALL", "DM", "MENTION", "REACTION"
    val currentTab: Int = 0, // 0=Inbox, 1=Notifications, 2=Accounts, 3=ChatHeads
    val searchQuery: String = "",
    val expandedChatHeadId: String? = null, // Floating bubble popup
    val activeChatId: String? = null, // Full screen chat
    val loginDialogPlatform: SocialPlatform? = null,
    val showSimulateDialog: Boolean = false,
    val totalUnreadMessages: Int = 0,
    val totalUnreadNotifications: Int = 0,
    val latestToastBubble: Pair<String, String>? = null, // (participantName, text)
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val showStartupQuickConnect: Boolean = false,
    val isSystemOverlayActive: Boolean = false
)

class SocialHubViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SocialRepository

    private val _selectedPlatformFilter = MutableStateFlow<String?>(null)
    private val _selectedInboxCategory = MutableStateFlow("ALL")
    private val _selectedNotificationFilter = MutableStateFlow("ALL")
    private val _currentTab = MutableStateFlow(0)
    private val _searchQuery = MutableStateFlow("")
    private val _expandedChatHeadId = MutableStateFlow<String?>(null)
    private val _activeChatId = MutableStateFlow<String?>(null)
    private val _loginDialogPlatform = MutableStateFlow<SocialPlatform?>(null)
    private val _showSimulateDialog = MutableStateFlow(false)
    private val _latestToastBubble = MutableStateFlow<Pair<String, String>?>(null)
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val _showStartupQuickConnect = MutableStateFlow(false)
    private val _isSystemOverlayActive = MutableStateFlow(false)

    val uiState: StateFlow<SocialHubUiState>

    init {
        val database = SocialDatabase.getDatabase(application)
        repository = SocialRepository(database.socialDao())

        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }

        uiState = combine(
            repository.accounts,
            repository.conversations,
            repository.activeChatHeads,
            repository.notifications,
            _selectedPlatformFilter,
            _selectedInboxCategory,
            _selectedNotificationFilter,
            _currentTab,
            _searchQuery,
            _expandedChatHeadId,
            _activeChatId,
            _loginDialogPlatform,
            _showSimulateDialog,
            _latestToastBubble,
            _themeMode,
            _showStartupQuickConnect,
            _isSystemOverlayActive
        ) { args: Array<Any?> ->
            @Suppress("UNCHECKED_CAST")
            val accounts = args[0] as List<SocialAccountEntity>
            @Suppress("UNCHECKED_CAST")
            val conversations = args[1] as List<ConversationEntity>
            @Suppress("UNCHECKED_CAST")
            val activeChatHeads = args[2] as List<ConversationEntity>
            @Suppress("UNCHECKED_CAST")
            val notifications = args[3] as List<NotificationEntity>
            val platformFilter = args[4] as String?
            val inboxCategory = args[5] as String
            val notifFilter = args[6] as String
            val tab = args[7] as Int
            val search = args[8] as String
            val expandedChatHead = args[9] as String?
            val activeChat = args[10] as String?
            val loginPlatform = args[11] as SocialPlatform?
            val showSim = args[12] as Boolean
            @Suppress("UNCHECKED_CAST")
            val toastBubble = args[13] as Pair<String, String>?
            val theme = args[14] as ThemeMode
            val showQuick = args[15] as Boolean
            val systemOverlay = args[16] as Boolean

            // Filter conversations
            val filteredConvs = conversations.filter { conv ->
                val matchesPlatform = platformFilter == null || conv.platformId.equals(platformFilter, ignoreCase = true)
                val matchesSearch = search.isBlank() ||
                        conv.participantName.contains(search, ignoreCase = true) ||
                        conv.lastMessageText.contains(search, ignoreCase = true) ||
                        conv.participantHandle.contains(search, ignoreCase = true)
                val matchesCategory = when (inboxCategory) {
                    "UNREAD" -> conv.unreadCount > 0
                    "PINNED" -> conv.isPinned
                    "DIRECT" -> !conv.participantName.contains("Team", ignoreCase = true) && !conv.participantName.contains("Lounge", ignoreCase = true)
                    else -> true
                }
                matchesPlatform && matchesSearch && matchesCategory
            }

            // Filter notifications
            val filteredNotifs = notifications.filter { notif ->
                val matchesPlatform = platformFilter == null || notif.platformId.equals(platformFilter, ignoreCase = true)
                val matchesFilter = when (notifFilter) {
                    "DM" -> notif.category == "DM"
                    "MENTION" -> notif.category == "MENTION"
                    "REACTION" -> notif.category == "REACTION"
                    else -> true
                }
                matchesPlatform && matchesFilter
            }

            val totalUnreadMsgs = conversations.sumOf { it.unreadCount }
            val totalUnreadNotifs = notifications.count { !it.isRead }

            SocialHubUiState(
                accounts = accounts,
                conversations = conversations,
                filteredConversations = filteredConvs,
                activeChatHeads = activeChatHeads,
                notifications = notifications,
                filteredNotifications = filteredNotifs,
                selectedPlatformFilter = platformFilter,
                selectedInboxCategory = inboxCategory,
                selectedNotificationFilter = notifFilter,
                currentTab = tab,
                searchQuery = search,
                expandedChatHeadId = expandedChatHead,
                activeChatId = activeChat,
                loginDialogPlatform = loginPlatform,
                showSimulateDialog = showSim,
                totalUnreadMessages = totalUnreadMsgs,
                totalUnreadNotifications = totalUnreadNotifs,
                latestToastBubble = toastBubble,
                themeMode = theme,
                showStartupQuickConnect = showQuick,
                isSystemOverlayActive = systemOverlay
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SocialHubUiState()
        )
    }

    fun setPlatformFilter(platformId: String?) {
        _selectedPlatformFilter.value = platformId
    }

    fun setInboxCategory(category: String) {
        _selectedInboxCategory.value = category
    }

    fun setNotificationFilter(filter: String) {
        _selectedNotificationFilter.value = filter
    }

    fun setCurrentTab(tab: Int) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openChatHeadBubble(conversationId: String) {
        viewModelScope.launch {
            repository.setChatHeadActive(conversationId, true)
            _expandedChatHeadId.value = conversationId
            repository.markConversationRead(conversationId)
        }
    }

    fun toggleChatHeadState(conversationId: String) {
        if (_expandedChatHeadId.value == conversationId) {
            _expandedChatHeadId.value = null
        } else {
            _expandedChatHeadId.value = conversationId
            viewModelScope.launch {
                repository.markConversationRead(conversationId)
            }
        }
    }

    fun closeExpandedChatHead() {
        _expandedChatHeadId.value = null
    }

    fun dismissChatHead(conversationId: String) {
        viewModelScope.launch {
            if (_expandedChatHeadId.value == conversationId) {
                _expandedChatHeadId.value = null
            }
            repository.setChatHeadActive(conversationId, false)
        }
    }

    fun dismissAllChatHeads() {
        viewModelScope.launch {
            _expandedChatHeadId.value = null
            repository.dismissAllChatHeads()
        }
    }

    fun updateChatHeadPosition(conversationId: String, x: Float, y: Float) {
        viewModelScope.launch {
            repository.updateChatHeadPosition(conversationId, x, y)
        }
    }

    fun openFullScreenChat(conversationId: String) {
        _activeChatId.value = conversationId
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
        }
    }

    fun closeFullScreenChat() {
        _activeChatId.value = null
    }

    fun sendMessage(conversationId: String, text: String, attachmentType: String? = null) {
        if (text.isBlank() && attachmentType == null) return
        viewModelScope.launch {
            repository.sendMessage(conversationId, text.ifBlank { "Sent an attachment" }, attachmentType)
        }
    }

    fun getConversationMessages(conversationId: String) =
        repository.getConversationMessages(conversationId)

    fun togglePinConversation(conversationId: String) {
        viewModelScope.launch {
            repository.togglePinConversation(conversationId)
        }
    }

    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
        }
    }

    fun toggleAccountConnected(accountId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.setAccountConnected(accountId, !currentStatus)
        }
    }

    fun toggleAccountChatHeads(accountId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.setAccountChatHeadsEnabled(accountId, !currentStatus)
        }
    }

    fun openLoginDialog(platform: SocialPlatform) {
        _loginDialogPlatform.value = platform
    }

    fun closeLoginDialog() {
        _loginDialogPlatform.value = null
    }

    fun completeLogin(platform: SocialPlatform, username: String, displayName: String) {
        viewModelScope.launch {
            val emojis = listOf("🌟", "🚀", "✨", "🔥", "💫", "🎯")
            val newAccount = SocialAccountEntity(
                id = "acc_${platform.id}_${System.currentTimeMillis()}",
                platformId = platform.id,
                username = username,
                displayName = displayName.ifBlank { username },
                avatarEmoji = emojis.random(),
                avatarBgColor = platform.primaryColor.value.toLong(),
                isConnected = true,
                chatHeadsEnabled = true,
                unreadMessagesCount = 0,
                unreadNotificationsCount = 0,
                syncFrequency = "Real-time"
            )
            repository.addAccount(newAccount)
            _loginDialogPlatform.value = null
        }
    }

    fun openSimulateDialog() {
        _showSimulateDialog.value = true
    }

    fun closeSimulateDialog() {
        _showSimulateDialog.value = false
    }

    fun triggerSimulatedMessage(platform: SocialPlatform, senderName: String, text: String) {
        viewModelScope.launch {
            _showSimulateDialog.value = false
            repository.triggerSimulatedIncomingMessage(platform, senderName, text)
            _latestToastBubble.value = Pair(senderName, text)
            // Auto-clear preview toast after 4 seconds
            kotlinx.coroutines.delay(4000)
            if (_latestToastBubble.value?.first == senderName) {
                _latestToastBubble.value = null
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun toggleThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
    }

    fun openStartupQuickConnect() {
        _showStartupQuickConnect.value = true
    }

    fun closeStartupQuickConnect() {
        _showStartupQuickConnect.value = false
    }

    fun toggleSystemOverlay(context: android.content.Context) {
        val next = !_isSystemOverlayActive.value
        _isSystemOverlayActive.value = next
        if (next) {
            com.example.service.FloatingChatHeadService.start(context)
        } else {
            com.example.service.FloatingChatHeadService.stop(context)
        }
    }

    fun startSystemOverlay(context: android.content.Context) {
        _isSystemOverlayActive.value = true
        com.example.service.FloatingChatHeadService.start(context)
    }

    fun stopSystemOverlay(context: android.content.Context) {
        _isSystemOverlayActive.value = false
        com.example.service.FloatingChatHeadService.stop(context)
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }
}
