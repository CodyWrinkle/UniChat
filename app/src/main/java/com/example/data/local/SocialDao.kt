package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialDao {
    // Accounts
    @Query("SELECT * FROM social_accounts")
    fun getAllAccounts(): Flow<List<SocialAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: SocialAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<SocialAccountEntity>)

    @Update
    suspend fun updateAccount(account: SocialAccountEntity)

    @Query("UPDATE social_accounts SET isConnected = :connected WHERE id = :accountId")
    suspend fun setAccountConnected(accountId: String, connected: Boolean)

    @Query("UPDATE social_accounts SET chatHeadsEnabled = :enabled WHERE id = :accountId")
    suspend fun setChatHeadsEnabled(accountId: String, enabled: Boolean)

    // Conversations
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversationById(conversationId: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE isChatHeadActive = 1 ORDER BY lastMessageTimestamp DESC")
    fun getActiveChatHeads(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ConversationEntity>)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET isChatHeadActive = :active WHERE id = :conversationId")
    suspend fun setChatHeadActive(conversationId: String, active: Boolean)

    @Query("UPDATE conversations SET isChatHeadActive = 0")
    suspend fun dismissAllChatHeads()

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun markConversationAsRead(conversationId: String)

    @Query("UPDATE conversations SET isPinned = NOT isPinned WHERE id = :conversationId")
    suspend fun togglePinConversation(conversationId: String)

    @Query("UPDATE conversations SET chatHeadNormalizedX = :x, chatHeadNormalizedY = :y WHERE id = :conversationId")
    suspend fun updateChatHeadPosition(conversationId: String, x: Float, y: Float)

    // Messages
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE conversationId = :conversationId")
    suspend fun markMessagesAsRead(conversationId: String)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()
}
