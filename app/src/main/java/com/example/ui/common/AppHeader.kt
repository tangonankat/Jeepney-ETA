package com.example.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
  currentUser: User?,
  isOnline: Boolean,
  pendingSyncCount: Int,
  isDarkTheme: Boolean,
  unreadNotificationCount: Int,
  onToggleTheme: () -> Unit,
  onToggleOnline: () -> Unit,
  onForceSync: () -> Unit,
  onSwitchRole: (UserRole) -> Unit,
  onNotificationsClick: () -> Unit
) {
  var roleMenuExpanded by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxWidth()) {
    // 1. Offline Mode and Sync Status Ribbon
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = if (isOnline) {
        if (pendingSyncCount > 0) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      } else {
        Color(0xFFFEE2E2)
      },
      tonalElevation = 1.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
            contentDescription = null,
            tint = if (isOnline) Color(0xFF059669) else Color(0xFFDC2626),
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = if (isOnline) {
              if (pendingSyncCount > 0) "ONLINE • $pendingSyncCount events pending sync" else "ONLINE • Real-time Sync Active"
            } else {
              "OFFLINE MODE • $pendingSyncCount events queued locally"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isOnline) Color(0xFF065F46) else Color(0xFF991B1B)
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (pendingSyncCount > 0 && isOnline) {
            FilledTonalButton(
              onClick = onForceSync,
              modifier = Modifier.height(26.dp).testTag("sync_now_button"),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
              Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(12.dp))
              Spacer(Modifier.width(4.dp))
              Text("Sync Now", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
            }
          }
          // Online/Offline Simulator Toggle
          Surface(
            modifier = Modifier
              .clickable { onToggleOnline() }
              .testTag("toggle_online_status_button"),
            shape = RoundedCornerShape(12.dp),
            color = if (isOnline) Color(0xFFD1FAE5) else Color(0xFFFCA5A5)
          ) {
            Text(
              text = if (isOnline) "Simulate Offline" else "Go Online",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = if (isOnline) Color(0xFF065F46) else Color(0xFF7F1D1D),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 2. Primary Top App Bar
    TopAppBar(
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.DirectionsBus,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }
          Column {
            Text(
              text = "Para! Jeepney",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            // Role chip dropdown
            if (currentUser != null) {
              Box {
                Surface(
                  modifier = Modifier
                    .clickable { roleMenuExpanded = true }
                    .testTag("role_badge_selector"),
                  shape = RoundedCornerShape(6.dp),
                  color = when (currentUser.role) {
                    UserRole.COMMUTER -> Color(0xFF0284C7).copy(alpha = 0.15f)
                    UserRole.DRIVER -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                    UserRole.ADMIN -> Color(0xFFE11D48).copy(alpha = 0.15f)
                  }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                  ) {
                    Text(
                      text = currentUser.role.name + " ▾",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                      fontWeight = FontWeight.Bold,
                      color = when (currentUser.role) {
                        UserRole.COMMUTER -> Color(0xFF0369A1)
                        UserRole.DRIVER -> Color(0xFFB45309)
                        UserRole.ADMIN -> Color(0xFFBE123C)
                      }
                    )
                  }
                }

                DropdownMenu(
                  expanded = roleMenuExpanded,
                  onDismissRequest = { roleMenuExpanded = false }
                ) {
                  DropdownMenuItem(
                    text = { Text("Commuter App (Maria)") },
                    onClick = {
                      roleMenuExpanded = false
                      onSwitchRole(UserRole.COMMUTER)
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Driver App (Kuya Dante)") },
                    onClick = {
                      roleMenuExpanded = false
                      onSwitchRole(UserRole.DRIVER)
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Admin App (Officer Reyes)") },
                    onClick = {
                      roleMenuExpanded = false
                      onSwitchRole(UserRole.ADMIN)
                    }
                  )
                }
              }
            }
          }
        }
      },
      actions = {
        // Notification bell with badge
        IconButton(
          onClick = onNotificationsClick,
          modifier = Modifier.testTag("notifications_button")
        ) {
          if (unreadNotificationCount > 0) {
            BadgedBox(badge = { Badge { Text("$unreadNotificationCount") } }) {
              Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
          } else {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
          }
        }

        // Dark/Light Mode toggle
        IconButton(
          onClick = onToggleTheme,
          modifier = Modifier.testTag("theme_toggle_button")
        ) {
          Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDarkTheme) "Light Theme" else "Dark Theme"
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    )
  }
}
