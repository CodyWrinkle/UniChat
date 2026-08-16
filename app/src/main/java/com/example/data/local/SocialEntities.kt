package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "social_accounts")
data class SocialAccountEntity(
    @PrimaryKey val id: String,
    val platformId: String,
    val username: String,
    val displayName: String,
    val avatarEmoji: String,
    val avatarBgColor: Long,
    val isConnected: Boolean = true,
    val chatHeadsEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val unreadMessagesCount: Int = 0,
    val unreadNotificationsCount: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val syncFrequency: String = "Real-time"
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val platformId: String,
    val accountId: String,
    val participantName: String,
    val participantHandle: String,
    val participantAvatarEmoji: String,
    val avatarBgColor: Long,
    val lastMessageText: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isChatHeadActive: Boolean = false,
    val chatHeadNormalizedX: Float = 0.9f,
    val chatHeadNormalizedY: Float = 0.35f,
    val isOnline: Boolean = true,
    val typingStatus: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderName: String,
    val platformId: String,
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean,
    val isRead: Boolean = true,
    val attachmentType: String? = null, // "IMAGE", "VOICE", "LOCATION", null
    val attachmentData: String? = null,
    val reactionEmoji: String? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val platformId: String,
    val accountId: String,
    val senderName: String,
    val senderAvatarEmoji: String,
    val avatarBgColor: Long,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val category: String = "DM", // "DM", "MENTION", "REACTION", "REQUEST", "SYSTEM"
    val targetConversationId: String? = null
)
