package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.SocialDatabase
import com.example.data.model.SocialPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FloatingChatHeadService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingHeadView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    private var currentParticipantName = "Alex Chen"
    private var currentPlatformEmoji = "🎮"
    private var currentPlatformColor = 0xFF5865F2.toInt()
    private var currentUnreadCount = 3
    private var currentPlatformName = "Discord"
    private var currentStackCount = 1

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        if (canDrawOverlays(this)) {
            setupFloatingChatHead()
            observeActiveChatHeads()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val name = intent?.getStringExtra(EXTRA_PARTICIPANT_NAME)
        val emoji = intent?.getStringExtra(EXTRA_EMOJI)
        val color = intent?.getIntExtra(EXTRA_COLOR, 0) ?: 0
        val unread = intent?.getIntExtra(EXTRA_UNREAD, -1) ?: -1
        val platform = intent?.getStringExtra(EXTRA_PLATFORM)

        if (!name.isNullOrBlank()) currentParticipantName = name
        if (!emoji.isNullOrBlank()) currentPlatformEmoji = emoji
        if (color != 0) currentPlatformColor = color
        if (unread >= 0) currentUnreadCount = unread
        if (!platform.isNullOrBlank()) currentPlatformName = platform

        floatingHeadView?.invalidate()

        return START_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "omnichat_floating_heads_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "OmniChat Floating Heads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Active draggable floating social chat heads overlay outside the app"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("OmniChat Floating Heads Active")
            .setContentText("Draggable chat heads are active over other apps. Tap to return to Hub.")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingChatHead() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        val headSizePx = (64 * displayMetrics.density).toInt()

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        layoutParams = WindowManager.LayoutParams(
            headSizePx,
            headSizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = screenWidth - headSizePx - (16 * displayMetrics.density).toInt()
            y = (screenHeight * 0.35f).toInt()
        }

        // Custom drawn floating bubble view
        val bubbleView = object : View(this) {
            private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 3 * displayMetrics.density
                color = Color.WHITE
            }
            private val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FF2A55")
                style = Paint.Style.FILL
            }
            private val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 11 * displayMetrics.scaledDensity
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }
            private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 26 * displayMetrics.scaledDensity
                textAlign = Paint.Align.CENTER
            }
            private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#44000000")
                style = Paint.Style.FILL
            }

            private val stackBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#334155")
                style = Paint.Style.FILL
            }
            private val stackBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 2 * displayMetrics.density
                color = Color.parseColor("#64748B")
            }
            private val stackCountBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#6366F1")
                style = Paint.Style.FILL
            }
            private val stackCountTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 9 * displayMetrics.scaledDensity
                typeface = Typeface.DEFAULT_BOLD
                textAlign = Paint.Align.CENTER
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                val w = width.toFloat()
                val h = height.toFloat()
                val radius = (min(w, h) / 2f) - (6 * displayMetrics.density)
                val cx = w / 2f
                val cy = h / 2f

                // Drop shadow
                canvas.drawCircle(cx, cy + (2 * displayMetrics.density), radius, shadowPaint)

                // If multiple chats stacked, draw background stacked circle
                if (currentStackCount > 1) {
                    val stackOffset = 4 * displayMetrics.density
                    canvas.drawCircle(cx - stackOffset, cy - stackOffset, radius - 2, stackBgPaint)
                    canvas.drawCircle(cx - stackOffset, cy - stackOffset, radius - 2, stackBorderPaint)
                }

                // Background circle
                bgPaint.color = currentPlatformColor
                canvas.drawCircle(cx, cy, radius, bgPaint)
                canvas.drawCircle(cx, cy, radius, borderPaint)

                // Central Emoji / Avatar
                val textBaseline = cy - ((emojiPaint.descent() + emojiPaint.ascent()) / 2)
                canvas.drawText(currentPlatformEmoji, cx, textBaseline, emojiPaint)

                // Multi-Chat Stack Badge (Top-Right)
                if (currentStackCount > 1) {
                    val sBadgeRadius = 9 * displayMetrics.density
                    val sBadgeCx = cx + radius - (4 * displayMetrics.density)
                    val sBadgeCy = cy - radius + (4 * displayMetrics.density)
                    canvas.drawCircle(sBadgeCx, sBadgeCy, sBadgeRadius, stackCountBgPaint)
                    val sTextBaseline = sBadgeCy - ((stackCountTextPaint.descent() + stackCountTextPaint.ascent()) / 2)
                    canvas.drawText(currentStackCount.toString(), sBadgeCx, sTextBaseline, stackCountTextPaint)
                }

                // Unread Badge Counter (Top-Left)
                if (currentUnreadCount > 0) {
                    val badgeRadius = 9 * displayMetrics.density
                    val badgeCx = cx - radius + (4 * displayMetrics.density)
                    val badgeCy = cy - radius + (4 * displayMetrics.density)
                    canvas.drawCircle(badgeCx, badgeCy, badgeRadius, badgeBgPaint)
                    val countText = if (currentUnreadCount > 9) "9+" else currentUnreadCount.toString()
                    val badgeTextBaseline = badgeCy - ((badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2)
                    canvas.drawText(countText, badgeCx, badgeTextBaseline, badgeTextPaint)
                }
            }
        }

        // Draggable gesture listener
        bubbleView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var touchStartTime = 0L

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null || layoutParams == null) return false

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        touchStartTime = System.currentTimeMillis()
                        initialX = layoutParams!!.x
                        initialY = layoutParams!!.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()

                        layoutParams!!.x = initialX + dx
                        layoutParams!!.y = initialY + dy

                        // Keep inside screen bounds
                        val maxX = screenWidth - headSizePx
                        val maxY = screenHeight - headSizePx
                        layoutParams!!.x = max(0, min(maxX, layoutParams!!.x))
                        layoutParams!!.y = max(0, min(maxY, layoutParams!!.y))

                        try {
                            windowManager?.updateViewLayout(floatingHeadView, layoutParams)
                        } catch (e: Exception) {
                            // View might not be attached
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val touchDuration = System.currentTimeMillis() - touchStartTime
                        val dx = abs(event.rawX - initialTouchX)
                        val dy = abs(event.rawY - initialTouchY)

                        if (touchDuration < 250 && dx < 15 && dy < 15) {
                            // Click detected: launch application to the chat directly!
                            val openIntent = Intent(this@FloatingChatHeadService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("EXTRA_OPEN_CHAT_PLATFORM", currentPlatformName)
                            }
                            startActivity(openIntent)
                        } else {
                            // Snap to nearest screen edge (left or right) for clean Messenger UX
                            val currentX = layoutParams!!.x
                            val targetX = if (currentX + headSizePx / 2 < screenWidth / 2) {
                                (10 * displayMetrics.density).toInt()
                            } else {
                                screenWidth - headSizePx - (10 * displayMetrics.density).toInt()
                            }
                            layoutParams!!.x = targetX
                            try {
                                windowManager?.updateViewLayout(floatingHeadView, layoutParams)
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingHeadView = bubbleView

        try {
            windowManager?.addView(floatingHeadView, layoutParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeActiveChatHeads() {
        serviceScope.launch {
            try {
                val db = SocialDatabase.getDatabase(applicationContext)
                db.socialDao().getActiveChatHeads().collect { convs ->
                    currentStackCount = convs.size
                    if (convs.isNotEmpty()) {
                        val active = convs.first()
                        currentParticipantName = active.participantName
                        currentPlatformEmoji = active.participantAvatarEmoji
                        currentPlatformColor = active.avatarBgColor.toInt()
                        currentUnreadCount = convs.sumOf { it.unreadCount }
                        currentPlatformName = active.platformId
                    }
                    floatingHeadView?.invalidate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (floatingHeadView != null && windowManager != null) {
            try {
                windowManager?.removeView(floatingHeadView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val NOTIFICATION_ID = 10091
        const val ACTION_STOP = "com.example.service.STOP_FLOATING_HEADS"
        const val EXTRA_PARTICIPANT_NAME = "extra_participant_name"
        const val EXTRA_EMOJI = "extra_emoji"
        const val EXTRA_COLOR = "extra_color"
        const val EXTRA_UNREAD = "extra_unread"
        const val EXTRA_PLATFORM = "extra_platform"

        fun canDrawOverlays(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }

        fun start(
            context: Context,
            participantName: String? = null,
            emoji: String? = null,
            color: Int? = null,
            unread: Int? = null,
            platform: String? = null
        ) {
            val intent = Intent(context, FloatingChatHeadService::class.java).apply {
                participantName?.let { putExtra(EXTRA_PARTICIPANT_NAME, it) }
                emoji?.let { putExtra(EXTRA_EMOJI, it) }
                color?.let { putExtra(EXTRA_COLOR, it) }
                unread?.let { putExtra(EXTRA_UNREAD, it) }
                platform?.let { putExtra(EXTRA_PLATFORM, it) }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingChatHeadService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }
}
