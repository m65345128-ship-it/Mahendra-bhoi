package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NotifLog
import com.example.ui.FitViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationScreen(viewModel: FitViewModel) {
    val logs by viewModel.notifications.collectAsState()
    val unreadCount = logs.filter { !it.isRead }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("notification_screen")
    ) {
        // TOP CONTROL PANEL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "REMINDERS & NOTIFICATION ALERTS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "$unreadCount unread notices",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Clear all / Read
                TextButton(
                    onClick = { viewModel.markAllNotificationsRead() },
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Read All", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Simulate new quote
                Button(
                    onClick = { viewModel.addManualMotivation() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.background)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inspire Me", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
                }
            }
        }

        // REMINDERS LIST VIEW
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Inbox is empty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Click 'Inspire Me' to mock trigger immediate motivational alerts!",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs.size) { index ->
                    val notice = logs[index]
                    NoticeItemRow(
                        notice = notice,
                        onDeleteClick = { viewModel.deleteNotification(notice.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NoticeItemRow(
    notice: NotifLog,
    onDeleteClick: () -> Unit
) {
    val (icon, color) = when(notice.category.lowercase()) {
        "workout" -> Pair("🏋️", Color(0xFFFF4757))
        "diet" -> Pair("🥗", MaterialTheme.colorScheme.primary)
        else -> Pair("⚡", MaterialTheme.colorScheme.secondary) // Motivation
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notice.isRead) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (!notice.isRead) color.copy(alpha = 0.25f) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Category Icon Block
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Notices column
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = notice.title,
                        fontWeight = if (notice.isRead) FontWeight.Bold else FontWeight.Black,
                        fontSize = 14.sp,
                        color = if (notice.isRead) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.61f) else MaterialTheme.colorScheme.onBackground
                    )
                    if (!notice.isRead) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(color, CircleShape)
                        )
                    }
                }
                
                Text(
                    text = notice.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )

                // Date stamp
                val timeFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(notice.timestamp))
                Text(
                    text = "$timeFormatted • ${notice.category}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = color.copy(alpha = 0.7f)
                )
            }

            // Remove Alert option
            IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Remove Notification", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}
