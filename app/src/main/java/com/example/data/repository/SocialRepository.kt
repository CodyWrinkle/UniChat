package com.example.data.repository

import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.SocialAccountEntity
import com.example.data.local.SocialDao
import com.example.data.model.SocialPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class SocialRepository(private val dao: SocialDao) {

    val accounts: Flow<List<SocialAccountEntity>> = dao.getAllAccounts()
    val conversations: Flow<List<ConversationEntity>> = dao.getAllConversations()
    val activeChatHeads: Flow<List<ConversationEntity>> = dao.getActiveChatHeads()
    val notifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    fun getConversationMessages(conversationId: String): Flow<List<ChatMessageEntity>> =
        dao.getMessagesForConversation(conversationId)

    fun getConversation(conversationId: String): Flow<ConversationEntity?> =
        dao.getConversationById(conversationId)

    suspend fun initializeDefaultDataIfEmpty() {
        val existingAccounts = dao.getAllAccounts().firstOrNull()
        if (!existingAccounts.isNullOrEmpty()) return

        val initialAccounts = listOf(
            SocialAccountEntity(
                id = "acc_facebook",
                platformId = SocialPlatform.FACEBOOK.id,
                username = "cody.wrin.fb",
                displayName = "Cody Wrin",
                avatarEmoji = "👤",
                avatarBgColor = 0xFF1877F2,
                isConnected = true,
                chatHeadsEnabled = true,
                unreadMessagesCount = 3,
                unreadNotificationsCount = 2,
                syncFrequency = "Real-time"
            ),
            SocialAccountEntity(
                id = "acc_discord",
                platformId = SocialPlatform.DISCORD.id,
                username = "cody_w#4092",
                displayName = "Cody [Dev]",
                avatarEmoji = "🎮",
                avatarBgColor = 0xFF5865F2,
                isConnected = true,
                chatHeadsEnabled = true,
                unreadMessagesCount = 5,
                unreadNotificationsCount = 3,
                syncFrequency = "Real-time"
            ),
            SocialAccountEntity(
                id = "acc_x",
                platformId = SocialPlatform.X_TWITTER.id,
                username = "cody_builds",
                displayName = "Cody ⚡",
                avatarEmoji = "🚀",
                avatarBgColor = 0xFF0F1419,
                isConnected = true,
                chatHeadsEnabled = true,
                unreadMessagesCount = 1,
                unreadNotificationsCount = 5,
                syncFrequency = "Real-time"
            ),
            SocialAccountEntity(
                id = "acc_threads",
                platformId = SocialPlatform.THREADS.id,
                username = "cody_threads",
                displayName = "Cody Wrin",
                avatarEmoji = "🧵",
                avatarBgColor = 0xFF101010,
                isConnected = true,
                chatHeadsEnabled = true,
                unreadMessagesCount = 2,
                unreadNotificationsCount = 1,
                syncFrequency = "Real-time"
            ),
            SocialAccountEntity(
                id = "acc_instagram",
                platformId = SocialPlatform.INSTAGRAM.id,
                username = "cody_creative",
                displayName = "Cody Wrin",
                avatarEmoji = "🎨",
                avatarBgColor = 0xFFE1306C,
                isConnected = true,
                chatHeadsEnabled = true,
                unreadMessagesCount = 2,
                unreadNotificationsCount = 4,
                syncFrequency = "Real-time"
            ),
            SocialAccountEntity(
                id = "acc_whatsapp",
                platformId = SocialPlatform.WHATSAPP.id,
                username = "+1 (555) 382-9011",
                displayName = "Cody Wrin (Mobile)",
                avatarEmoji = "📱",
                avatarBgColor = 0xFF25D366,
                isConnected = true,
                chatHeadsEnabled = true,
                unreadMessagesCount = 4,
                unreadNotificationsCount = 1,
                syncFrequency = "Real-time"
            ),
            SocialAccountEntity(
                id = "acc_telegram",
                platformId = SocialPlatform.TELEGRAM.id,
                username = "codydev",
                displayName = "Cody Wrin",
                avatarEmoji = "⚡",
                avatarBgColor = 0xFF2AABEE,
                isConnected = true,
                chatHeadsEnabled = true,
                unreadMessagesCount = 2,
                unreadNotificationsCount = 0,
                syncFrequency = "Real-time"
            ),
            SocialAccountEntity(
                id = "acc_slack",
                platformId = SocialPlatform.SLACK.id,
                username = "cody.eng@slack.com",
                displayName = "Cody (Engineering)",
                avatarEmoji = "💼",
                avatarBgColor = 0xFF4A154B,
                isConnected = false,
                chatHeadsEnabled = false,
                unreadMessagesCount = 0,
                unreadNotificationsCount = 0,
                syncFrequency = "Real-time"
            )
        )
        dao.insertAccounts(initialAccounts)

        val now = System.currentTimeMillis()
        val minute = 60 * 1000L
        val hour = 60 * minute

        val initialConversations = listOf(
            ConversationEntity(
                id = "conv_1",
                platformId = SocialPlatform.FACEBOOK.id,
                accountId = "acc_facebook",
                participantName = "Sarah Jenkins",
                participantHandle = "sarah.jenkins.fb",
                participantAvatarEmoji = "👩‍🎨",
                avatarBgColor = 0xFF1877F2,
                lastMessageText = "Are we still meeting at 4 PM for the design review? ☕",
                lastMessageTimestamp = now - 2 * minute,
                unreadCount = 1,
                isPinned = true,
                isChatHeadActive = true,
                chatHeadNormalizedX = 0.92f,
                chatHeadNormalizedY = 0.22f,
                isOnline = true
            ),
            ConversationEntity(
                id = "conv_4",
                platformId = SocialPlatform.DISCORD.id,
                accountId = "acc_discord",
                participantName = "Alex Chen [Dev Guild]",
                participantHandle = "alexchen#1104",
                participantAvatarEmoji = "👾",
                avatarBgColor = 0xFF5865F2,
                lastMessageText = "Did you check the new Gemini API endpoints yet? 🎮",
                lastMessageTimestamp = now - 5 * minute,
                unreadCount = 2,
                isPinned = true,
                isChatHeadActive = true,
                chatHeadNormalizedX = 0.92f,
                chatHeadNormalizedY = 0.36f,
                isOnline = true
            ),
            ConversationEntity(
                id = "conv_5",
                platformId = SocialPlatform.X_TWITTER.id,
                accountId = "acc_x",
                participantName = "TechCrunch Direct",
                participantHandle = "@TechCrunch",
                participantAvatarEmoji = "🌐",
                avatarBgColor = 0xFF0F1419,
                lastMessageText = "Thanks for the tip! We just featured your project link. 𝕏",
                lastMessageTimestamp = now - 15 * minute,
                unreadCount = 1,
                isPinned = false,
                isChatHeadActive = true,
                chatHeadNormalizedX = 0.92f,
                chatHeadNormalizedY = 0.50f,
                isOnline = true
            ),
            ConversationEntity(
                id = "conv_threads",
                platformId = SocialPlatform.THREADS.id,
                accountId = "acc_threads",
                participantName = "Maya Lin",
                participantHandle = "@mayalin.design",
                participantAvatarEmoji = "✨",
                avatarBgColor = 0xFF101010,
                lastMessageText = "Your latest thread on Android Compose reached 10k views! 🧵",
                lastMessageTimestamp = now - 22 * minute,
                unreadCount = 1,
                isPinned = true,
                isChatHeadActive = false,
                chatHeadNormalizedX = 0.92f,
                chatHeadNormalizedY = 0.62f,
                isOnline = true
            ),
            ConversationEntity(
                id = "conv_2",
                platformId = SocialPlatform.INSTAGRAM.id,
                accountId = "acc_instagram",
                participantName = "Elena Rostova",
                participantHandle = "@elena.design",
                participantAvatarEmoji = "📸",
                avatarBgColor = 0xFFE1306C,
                lastMessageText = "Loved the new mockup! Left feedback on your story 🔥",
                lastMessageTimestamp = now - 35 * minute,
                unreadCount = 2,
                isPinned = false,
                isChatHeadActive = false,
                chatHeadNormalizedX = 0.92f,
                chatHeadNormalizedY = 0.74f,
                isOnline = true
            ),
            ConversationEntity(
                id = "conv_3",
                platformId = SocialPlatform.WHATSAPP.id,
                accountId = "acc_whatsapp",
                participantName = "Product Team Sync",
                participantHandle = "+1 555-TEAM",
                participantAvatarEmoji = "👥",
                avatarBgColor = 0xFF128C7E,
                lastMessageText = "David: Build #240 deployed to staging successfully. 🚀",
                lastMessageTimestamp = now - 1 * hour,
                unreadCount = 3,
                isPinned = false,
                isChatHeadActive = false,
                chatHeadNormalizedX = 0.92f,
                chatHeadNormalizedY = 0.85f,
                isOnline = true
            )
        )
        dao.insertConversations(initialConversations)

        // Seed messages for conv_1 (Sarah Jenkins - Facebook)
        val conv1Messages = listOf(
            ChatMessageEntity(
                id = "msg_1_1",
                conversationId = "conv_1",
                senderName = "Sarah Jenkins",
                platformId = SocialPlatform.FACEBOOK.id,
                text = "Hey Cody! How is the unified social app coming along?",
                timestamp = now - 35 * minute,
                isFromMe = false,
                isRead = true
            ),
            ChatMessageEntity(
                id = "msg_1_2",
                conversationId = "conv_1",
                senderName = "Cody Wrin",
                platformId = SocialPlatform.FACEBOOK.id,
                text = "Going fantastic! Just finished building the floating chat heads overlay 🚀",
                timestamp = now - 28 * minute,
                isFromMe = true,
                isRead = true
            ),
            ChatMessageEntity(
                id = "msg_1_3",
                conversationId = "conv_1",
                senderName = "Sarah Jenkins",
                platformId = SocialPlatform.FACEBOOK.id,
                text = "That is so cool! Dragging them around feels just like native Messenger.",
                timestamp = now - 15 * minute,
                isFromMe = false,
                isRead = true,
                reactionEmoji = "❤️"
            ),
            ChatMessageEntity(
                id = "msg_1_4",
                conversationId = "conv_1",
                senderName = "Sarah Jenkins",
                platformId = SocialPlatform.FACEBOOK.id,
                text = "Are we still meeting at 4 PM for the design review? ☕",
                timestamp = now - 2 * minute,
                isFromMe = false,
                isRead = false
            )
        )
        dao.insertMessages(conv1Messages)

        // Seed messages for conv_threads (Maya Lin - Threads)
        val convThreadsMessages = listOf(
            ChatMessageEntity(
                id = "msg_t_1",
                conversationId = "conv_threads",
                senderName = "Maya Lin",
                platformId = SocialPlatform.THREADS.id,
                text = "Hey! Saw your thread about multi-platform chat bubbles.",
                timestamp = now - 40 * minute,
                isFromMe = false,
                isRead = true
            ),
            ChatMessageEntity(
                id = "msg_t_2",
                conversationId = "conv_threads",
                senderName = "Maya Lin",
                platformId = SocialPlatform.THREADS.id,
                text = "Your latest thread on Android Compose reached 10k views! 🧵",
                timestamp = now - 22 * minute,
                isFromMe = false,
                isRead = false,
                reactionEmoji = "🔥"
            )
        )
        dao.insertMessages(convThreadsMessages)

        // Seed messages for conv_2 (Elena - Instagram)
        val conv2Messages = listOf(
            ChatMessageEntity(
                id = "msg_2_1",
                conversationId = "conv_2",
                senderName = "Cody Wrin",
                platformId = SocialPlatform.INSTAGRAM.id,
                text = "Hey Elena, just published the new UI case study.",
                timestamp = now - 30 * minute,
                isFromMe = true,
                isRead = true
            ),
            ChatMessageEntity(
                id = "msg_2_2",
                conversationId = "conv_2",
                senderName = "Elena Rostova",
                platformId = SocialPlatform.INSTAGRAM.id,
                text = "Loved the new mockup! Left feedback on your story 🔥",
                timestamp = now - 8 * minute,
                isFromMe = false,
                isRead = false,
                reactionEmoji = "🔥"
            )
        )
        dao.insertMessages(conv2Messages)

        // Seed messages for conv_4 (Alex Chen - Discord)
        val conv4Messages = listOf(
            ChatMessageEntity(
                id = "msg_4_1",
                conversationId = "conv_4",
                senderName = "Alex Chen",
                platformId = SocialPlatform.DISCORD.id,
                text = "Did you check the new Gemini API endpoints yet?",
                timestamp = now - 45 * minute,
                isFromMe = false,
                isRead = false
            )
        )
        dao.insertMessages(conv4Messages)

        // Seed Notifications
        val initialNotifications = listOf(
            NotificationEntity(
                id = "notif_1",
                platformId = SocialPlatform.MESSENGER.id,
                accountId = "acc_messenger",
                senderName = "Sarah Jenkins",
                senderAvatarEmoji = "👩‍🎨",
                avatarBgColor = 0xFFFF6B6B,
                title = "New Message from Sarah",
                body = "Are we still meeting at 4 PM for the design review? ☕",
                timestamp = now - 2 * minute,
                isRead = false,
                category = "DM",
                targetConversationId = "conv_1"
            ),
            NotificationEntity(
                id = "notif_2",
                platformId = SocialPlatform.INSTAGRAM.id,
                accountId = "acc_instagram",
                senderName = "Elena Rostova",
                senderAvatarEmoji = "✨",
                avatarBgColor = 0xFFE1306C,
                title = "Elena reacted to your Story",
                body = "🔥 Elena reacted with Fire to your recent post",
                timestamp = now - 8 * minute,
                isRead = false,
                category = "REACTION",
                targetConversationId = "conv_2"
            ),
            NotificationEntity(
                id = "notif_3",
                platformId = SocialPlatform.X_TWITTER.id,
                accountId = "acc_x",
                senderName = "TechCrunch",
                senderAvatarEmoji = "🌐",
                avatarBgColor = 0xFF0F1419,
                title = "Mentioned in a Post",
                body = "@TechCrunch mentioned you: 'Check out the new OmniChat unified social client by @cody_builds'",
                timestamp = now - 22 * minute,
                isRead = false,
                category = "MENTION",
                targetConversationId = "conv_5"
            ),
            NotificationEntity(
                id = "notif_4",
                platformId = SocialPlatform.DISCORD.id,
                accountId = "acc_discord",
                senderName = "Alex Chen",
                senderAvatarEmoji = "👾",
                avatarBgColor = 0xFF5865F2,
                title = "Direct Message Ping",
                body = "Alex Chen: 'Did you check the new Gemini API endpoints yet?'",
                timestamp = now - 45 * minute,
                isRead = false,
                category = "DM",
                targetConversationId = "conv_4"
            ),
            NotificationEntity(
                id = "notif_5",
                platformId = SocialPlatform.WHATSAPP.id,
                accountId = "acc_whatsapp",
                senderName = "Product Team",
                senderAvatarEmoji = "👥",
                avatarBgColor = 0xFF128C7E,
                title = "3 new messages in Product Team Sync",
                body = "David: Build #240 deployed to staging successfully.",
                timestamp = now - 25 * minute,
                isRead = true,
                category = "DM",
                targetConversationId = "conv_3"
            )
        )
        dao.insertNotifications(initialNotifications)
    }

    suspend fun sendMessage(conversationId: String, text: String, attachmentType: String? = null) {
        val now = System.currentTimeMillis()
        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderName = "Cody Wrin",
            platformId = SocialPlatform.MESSENGER.id,
            text = text,
            timestamp = now,
            isFromMe = true,
            isRead = true,
            attachmentType = attachmentType
        )
        dao.insertMessage(message)

        val conv = dao.getConversationById(conversationId).firstOrNull() ?: return
        val updatedConv = conv.copy(
            lastMessageText = text,
            lastMessageTimestamp = now
        )
        dao.updateConversation(updatedConv)

        // Trigger realistic smart incoming reply after 1.5 seconds
        simulatePartnerReply(conversationId, conv.participantName, conv.platformId)
    }

    private fun simulatePartnerReply(conversationId: String, partnerName: String, platformId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1600)
            val replies = listOf(
                "Got it! Thanks for the update 👍",
                "Sounds perfect! Let's touch base shortly.",
                "Awesome, will check it right now!",
                "Great! Sending you the details in a moment ✨",
                "Appreciate the quick reply!"
            )
            val replyText = replies.random()
            val now = System.currentTimeMillis()

            val incomingMessage = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderName = partnerName,
                platformId = platformId,
                text = replyText,
                timestamp = now,
                isFromMe = false,
                isRead = false
            )
            dao.insertMessage(incomingMessage)

            val conv = dao.getConversationById(conversationId).firstOrNull() ?: return@launch
            dao.updateConversation(
                conv.copy(
                    lastMessageText = replyText,
                    lastMessageTimestamp = now,
                    unreadCount = conv.unreadCount + 1,
                    isChatHeadActive = true // Bring up chat head!
                )
            )

            // Add notification
            val notif = NotificationEntity(
                id = UUID.randomUUID().toString(),
                platformId = platformId,
                accountId = conv.accountId,
                senderName = partnerName,
                senderAvatarEmoji = conv.participantAvatarEmoji,
                avatarBgColor = conv.avatarBgColor,
                title = "New message from $partnerName",
                body = replyText,
                timestamp = now,
                isRead = false,
                category = "DM",
                targetConversationId = conversationId
            )
            dao.insertNotification(notif)
        }
    }

    suspend fun triggerSimulatedIncomingMessage(
        platform: SocialPlatform,
        senderName: String,
        messageText: String
    ) {
        val now = System.currentTimeMillis()
        val convs = dao.getAllConversations().firstOrNull() ?: emptyList()
        var targetConv = convs.firstOrNull { it.participantName.equals(senderName, ignoreCase = true) }

        if (targetConv == null) {
            val emojis = listOf("👩‍💻", "👨‍🎨", "🕶️", "🚀", "🦊", "🌟")
            val colors = listOf(0xFF6366F1, 0xFFEC4899, 0xFF10B981, 0xFFF59E0B, 0xFF3B82F6)
            targetConv = ConversationEntity(
                id = UUID.randomUUID().toString(),
                platformId = platform.id,
                accountId = "acc_${platform.id}",
                participantName = senderName,
                participantHandle = senderName.lowercase().replace(" ", "."),
                participantAvatarEmoji = emojis.random(),
                avatarBgColor = colors.random(),
                lastMessageText = messageText,
                lastMessageTimestamp = now,
                unreadCount = 1,
                isPinned = false,
                isChatHeadActive = true, // Pops up as Chat Head!
                chatHeadNormalizedX = 0.92f,
                chatHeadNormalizedY = 0.35f,
                isOnline = true
            )
            dao.insertConversation(targetConv)
        } else {
            targetConv = targetConv.copy(
                lastMessageText = messageText,
                lastMessageTimestamp = now,
                unreadCount = targetConv.unreadCount + 1,
                isChatHeadActive = true // Pops up as Chat Head!
            )
            dao.updateConversation(targetConv)
        }

        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = targetConv.id,
            senderName = senderName,
            platformId = platform.id,
            text = messageText,
            timestamp = now,
            isFromMe = false,
            isRead = false
        )
        dao.insertMessage(message)

        val notif = NotificationEntity(
            id = UUID.randomUUID().toString(),
            platformId = platform.id,
            accountId = targetConv.accountId,
            senderName = senderName,
            senderAvatarEmoji = targetConv.participantAvatarEmoji,
            avatarBgColor = targetConv.avatarBgColor,
            title = "New ${platform.displayName} message from $senderName",
            body = messageText,
            timestamp = now,
            isRead = false,
            category = "DM",
            targetConversationId = targetConv.id
        )
        dao.insertNotification(notif)
    }

    suspend fun setChatHeadActive(conversationId: String, active: Boolean) =
        dao.setChatHeadActive(conversationId, active)

    suspend fun dismissAllChatHeads() = dao.dismissAllChatHeads()

    suspend fun updateChatHeadPosition(conversationId: String, x: Float, y: Float) =
        dao.updateChatHeadPosition(conversationId, x, y)

    suspend fun markConversationRead(conversationId: String) {
        dao.markConversationAsRead(conversationId)
        dao.markMessagesAsRead(conversationId)
    }

    suspend fun togglePinConversation(conversationId: String) =
        dao.togglePinConversation(conversationId)

    suspend fun setAccountConnected(accountId: String, connected: Boolean) =
        dao.setAccountConnected(accountId, connected)

    suspend fun setAccountChatHeadsEnabled(accountId: String, enabled: Boolean) =
        dao.setChatHeadsEnabled(accountId, enabled)

    suspend fun addAccount(account: SocialAccountEntity) {
        val existingAccounts = dao.getAllAccounts().firstOrNull() ?: emptyList()
        val matching = existingAccounts.firstOrNull { it.platformId.equals(account.platformId, ignoreCase = true) }
        val finalAccountId: String
        if (matching != null) {
            finalAccountId = matching.id
            dao.updateAccount(
                matching.copy(
                    username = account.username,
                    displayName = account.displayName,
                    isConnected = true,
                    chatHeadsEnabled = true
                )
            )
        } else {
            finalAccountId = account.id
            dao.insertAccount(account)
        }

        // Also add a starter conversation for this platform if none exists
        val existingConvs = dao.getAllConversations().firstOrNull() ?: emptyList()
        val hasConv = existingConvs.any { it.platformId.equals(account.platformId, ignoreCase = true) }
        if (!hasConv) {
            val platform = SocialPlatform.fromId(account.platformId)
            val now = System.currentTimeMillis()
            val convId = "conv_${account.platformId}_${System.currentTimeMillis()}"
            val partnerName = when (platform) {
                SocialPlatform.DISCORD -> "Alex Chen (Discord Admin)"
                SocialPlatform.SLACK -> "Maya Lin (Design Lead)"
                SocialPlatform.REDDIT -> "u/CodeNinja"
                SocialPlatform.TIKTOK -> "TikTok Studio Support"
                SocialPlatform.LINKEDIN -> "Sarah Connor (Tech Recruiter)"
                else -> "${platform.displayName} Hub"
            }
            val welcomeText = "Welcome to OmniChat! Your ${platform.displayName} account is now linked with real-time Messenger chat heads active."

            val newConv = ConversationEntity(
                id = convId,
                platformId = platform.id,
                accountId = finalAccountId,
                participantName = partnerName,
                participantHandle = "${platform.handlePrefix}${partnerName.lowercase().replace(" ", "_")}",
                participantAvatarEmoji = platform.iconSymbol,
                avatarBgColor = platform.primaryColor.value.toLong(),
                lastMessageText = welcomeText,
                lastMessageTimestamp = now,
                unreadCount = 1,
                isPinned = false,
                isChatHeadActive = true,
                chatHeadNormalizedX = 0.92f,
                chatHeadNormalizedY = 0.35f,
                isOnline = true
            )
            dao.insertConversation(newConv)

            val msg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                senderName = partnerName,
                platformId = platform.id,
                text = welcomeText,
                timestamp = now,
                isFromMe = false,
                isRead = false
            )
            dao.insertMessage(msg)

            val notif = NotificationEntity(
                id = UUID.randomUUID().toString(),
                platformId = platform.id,
                accountId = finalAccountId,
                senderName = partnerName,
                senderAvatarEmoji = platform.iconSymbol,
                avatarBgColor = platform.primaryColor.value.toLong(),
                title = "Account Linked: ${platform.displayName}",
                body = "Successfully linked @${account.username}. Real-time chat heads are active.",
                timestamp = now,
                isRead = false,
                category = "SYSTEM",
                targetConversationId = convId
            )
            dao.insertNotification(notif)
        }
    }

    suspend fun markNotificationRead(id: String) = dao.markNotificationAsRead(id)

    suspend fun markAllNotificationsRead() = dao.markAllNotificationsAsRead()

    suspend fun deleteNotification(id: String) = dao.deleteNotification(id)

    suspend fun clearAllNotifications() = dao.clearAllNotifications()
}
