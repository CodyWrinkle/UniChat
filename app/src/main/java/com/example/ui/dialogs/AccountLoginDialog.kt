package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SocialPlatform

@Composable
fun AccountLoginDialog(
    platform: SocialPlatform,
    onDismiss: () -> Unit,
    onConnect: (String, String) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(platform) }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("••••••••") }
    var serverOrGuild by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(selectedPlatform.primaryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = selectedPlatform.iconSymbol, fontSize = 20.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Connect ${selectedPlatform.displayName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "OAuth2 & Real-Time Sync",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Platform Switcher
                Text(
                    text = "Select Network:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SocialPlatform.entries) { plat ->
                        val isSel = plat == selectedPlatform
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) plat.primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                selectedPlatform = plat
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = plat.iconSymbol, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = plat.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Demo Autofill Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = selectedPlatform.primaryColor.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (selectedPlatform) {
                                SocialPlatform.FACEBOOK -> {
                                    username = "cody.wrin.fb"
                                    displayName = "Cody Wrin"
                                }
                                SocialPlatform.DISCORD -> {
                                    username = "cody_developer#2024"
                                    displayName = "Cody [Guild Admin]"
                                    serverOrGuild = "AI Studio Developers"
                                }
                                SocialPlatform.X_TWITTER -> {
                                    username = "cody_builds"
                                    displayName = "Cody ⚡"
                                }
                                SocialPlatform.THREADS -> {
                                    username = "cody_threads"
                                    displayName = "Cody Wrin"
                                }
                                SocialPlatform.INSTAGRAM -> {
                                    username = "cody_creative"
                                    displayName = "Cody Wrin"
                                }
                                SocialPlatform.SLACK -> {
                                    username = "cody.eng@workspace.slack.com"
                                    displayName = "Cody (Engineering)"
                                }
                                SocialPlatform.REDDIT -> {
                                    username = "cody_builds"
                                    displayName = "u/cody_builds"
                                }
                                SocialPlatform.MESSENGER -> {
                                    username = "cody.wrin.fb"
                                    displayName = "Cody Wrin"
                                }
                                SocialPlatform.WHATSAPP -> {
                                    username = "+1 (555) 382-9011"
                                    displayName = "Cody Wrin (Mobile)"
                                }
                                else -> {
                                    username = "cody_${selectedPlatform.id}"
                                    displayName = "Cody Wrin"
                                }
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = selectedPlatform.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⚡ Click to autofill demo ${selectedPlatform.displayName} credentials",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = selectedPlatform.primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Credentials inputs
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            when (selectedPlatform) {
                                SocialPlatform.DISCORD -> "Discord Tag (e.g. name#1234 or @handle)"
                                SocialPlatform.WHATSAPP -> "Phone Number"
                                SocialPlatform.REDDIT -> "Reddit Username (u/...)"
                                else -> "Account Handle / Email"
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            when (selectedPlatform) {
                                SocialPlatform.DISCORD -> "alex_dev#4092"
                                SocialPlatform.WHATSAPP -> "+1 (555) 000-0000"
                                else -> "username"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name / Nickname") },
                    placeholder = { Text("e.g. Cody Wrin") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (selectedPlatform == SocialPlatform.DISCORD) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = serverOrGuild,
                        onValueChange = { serverOrGuild = it },
                        label = { Text("Primary Discord Server / Guild") },
                        placeholder = { Text("e.g. AI Studio Developers") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password or OAuth Bot Token") },
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Permissions preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = selectedPlatform.primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grants read/write sync for Direct Messages, Server Alerts, and floating Messenger Chat Heads.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ElevatedButton(
                        onClick = {
                            val finalUsername = username.ifBlank {
                                when (selectedPlatform) {
                                    SocialPlatform.DISCORD -> "cody_w#4092"
                                    else -> "cody.social"
                                }
                            }
                            val finalDisplay = displayName.ifBlank {
                                when (selectedPlatform) {
                                    SocialPlatform.DISCORD -> "Cody [Dev]"
                                    else -> "Cody Wrin"
                                }
                            }
                            onConnect(finalUsername, finalDisplay)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = selectedPlatform.primaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Log In & Sync", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
