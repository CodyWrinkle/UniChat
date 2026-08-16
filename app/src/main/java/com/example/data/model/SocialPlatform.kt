package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class SocialPlatform(
    val id: String,
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val iconSymbol: String,
    val handlePrefix: String,
    val isPrimaryNetwork: Boolean = false
) {
    FACEBOOK(
        id = "facebook",
        displayName = "Facebook",
        primaryColor = Color(0xFF1877F2),
        secondaryColor = Color(0xFF0084FF),
        iconSymbol = "🔵",
        handlePrefix = "",
        isPrimaryNetwork = true
    ),
    DISCORD(
        id = "discord",
        displayName = "Discord",
        primaryColor = Color(0xFF5865F2),
        secondaryColor = Color(0xFF7289DA),
        iconSymbol = "🎮",
        handlePrefix = "@",
        isPrimaryNetwork = true
    ),
    X_TWITTER(
        id = "x_twitter",
        displayName = "Twitter (X)",
        primaryColor = Color(0xFF0F1419),
        secondaryColor = Color(0xFF1DA1F2),
        iconSymbol = "𝕏",
        handlePrefix = "@",
        isPrimaryNetwork = true
    ),
    THREADS(
        id = "threads",
        displayName = "Threads",
        primaryColor = Color(0xFF101010),
        secondaryColor = Color(0xFF333333),
        iconSymbol = "🧵",
        handlePrefix = "@",
        isPrimaryNetwork = true
    ),
    INSTAGRAM(
        id = "instagram",
        displayName = "Instagram",
        primaryColor = Color(0xFFE1306C),
        secondaryColor = Color(0xFFF77737),
        iconSymbol = "📸",
        handlePrefix = "@",
        isPrimaryNetwork = true
    ),
    MESSENGER(
        id = "messenger",
        displayName = "Messenger",
        primaryColor = Color(0xFF0084FF),
        secondaryColor = Color(0xFF00C6FF),
        iconSymbol = "⚡",
        handlePrefix = "",
        isPrimaryNetwork = false
    ),
    WHATSAPP(
        id = "whatsapp",
        displayName = "WhatsApp",
        primaryColor = Color(0xFF25D366),
        secondaryColor = Color(0xFF128C7E),
        iconSymbol = "💬",
        handlePrefix = "+",
        isPrimaryNetwork = false
    ),
    TELEGRAM(
        id = "telegram",
        displayName = "Telegram",
        primaryColor = Color(0xFF2AABEE),
        secondaryColor = Color(0xFF229ED9),
        iconSymbol = "✈️",
        handlePrefix = "@",
        isPrimaryNetwork = false
    ),
    SLACK(
        id = "slack",
        displayName = "Slack",
        primaryColor = Color(0xFF4A154B),
        secondaryColor = Color(0xFFE01E5A),
        iconSymbol = "💼",
        handlePrefix = "@",
        isPrimaryNetwork = false
    ),
    REDDIT(
        id = "reddit",
        displayName = "Reddit",
        primaryColor = Color(0xFFFF4500),
        secondaryColor = Color(0xFFFF5700),
        iconSymbol = "🤖",
        handlePrefix = "u/",
        isPrimaryNetwork = false
    ),
    TIKTOK(
        id = "tiktok",
        displayName = "TikTok",
        primaryColor = Color(0xFF010101),
        secondaryColor = Color(0xFF00F2FE),
        iconSymbol = "🎵",
        handlePrefix = "@",
        isPrimaryNetwork = false
    ),
    LINKEDIN(
        id = "linkedin",
        displayName = "LinkedIn",
        primaryColor = Color(0xFF0A66C2),
        secondaryColor = Color(0xFF004182),
        iconSymbol = "👔",
        handlePrefix = "in/",
        isPrimaryNetwork = false
    );

    companion object {
        fun fromId(id: String): SocialPlatform {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MESSENGER
        }

        val primaryPlatforms: List<SocialPlatform>
            get() = listOf(FACEBOOK, DISCORD, X_TWITTER, THREADS, INSTAGRAM)
    }
}

