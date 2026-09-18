package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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

@Composable
fun UserProfileScreen(
  currentUser: User?,
  isDarkTheme: Boolean,
  onToggleTheme: () -> Unit,
  onUpdateBio: (name: String, bio: String, notifs: Boolean) -> Unit,
  onSwitchRole: (UserRole) -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  var nameInput by remember(currentUser) { mutableStateOf(currentUser?.fullName ?: "") }
  var bioInput by remember(currentUser) { mutableStateOf(currentUser?.bio ?: "") }
  var notificationsEnabled by remember(currentUser) { mutableStateOf(currentUser?.notificationEnabled ?: true) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Profile Avatar & Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("profile_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(80.dp)
              .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.Person,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(48.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = currentUser?.fullName ?: "User",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = currentUser?.email ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(8.dp))

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = when (currentUser?.role) {
              UserRole.COMMUTER -> Color(0xFFE0F2FE)
              UserRole.DRIVER -> Color(0xFFFEF3C7)
              UserRole.ADMIN -> Color(0xFFFFE4E6)
              null -> Color.LightGray
            }
          ) {
            Text(
              text = "ROLE: ${currentUser?.role?.name ?: "COMMUTER"}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = when (currentUser?.role) {
                UserRole.COMMUTER -> Color(0xFF0369A1)
                UserRole.DRIVER -> Color(0xFFB45309)
                UserRole.ADMIN -> Color(0xFFBE123C)
                null -> Color.DarkGray
              },
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // 2. Profile Details & Bio Editor
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("edit_profile_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text("Personal Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = bioInput,
            onValueChange = { bioInput = it },
            label = { Text("Bio / Commute Notes") },
            modifier = Modifier.fillMaxWidth().testTag("profile_bio_input"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { onUpdateBio(nameInput, bioInput, notificationsEnabled) },
            modifier = Modifier.fillMaxWidth().testTag("save_profile_button"),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Save Profile Changes")
          }
        }
      }
    }

    // 3. App Settings & Theme Mode
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text("App Preferences", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Icon(
                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Column {
                Text("Dark Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(if (isDarkTheme) "Dark mode enabled" else "Light mode enabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Switch(
              checked = isDarkTheme,
              onCheckedChange = { onToggleTheme() },
              modifier = Modifier.testTag("theme_switch")
            )
          }

          HorizontalDivider()

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
              Column {
                Text("Arrival & Radar Alerts", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Notify on jeepney approaching & drop-offs", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
            Switch(
              checked = notificationsEnabled,
              onCheckedChange = {
                notificationsEnabled = it
                onUpdateBio(nameInput, bioInput, it)
              },
              modifier = Modifier.testTag("notifications_switch")
            )
          }
        }
      }
    }

    // 4. Role & Account Demo Switcher
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("demo_accounts_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Switch Active Role / Test Persona", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text("Quickly test commuter waiting, driver radar/manual counts, and admin approvals in one place.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

          Spacer(modifier = Modifier.height(14.dp))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(
              onClick = { onSwitchRole(UserRole.COMMUTER) },
              modifier = Modifier.fillMaxWidth().testTag("switch_to_commuter_button")
            ) {
              Text("Commuter Mode (Maria Santos)")
            }
            FilledTonalButton(
              onClick = { onSwitchRole(UserRole.DRIVER) },
              modifier = Modifier.fillMaxWidth().testTag("switch_to_driver_button")
            ) {
              Text("Driver Mode (Kuya Dante - Approved)")
            }
            FilledTonalButton(
              onClick = { onSwitchRole(UserRole.ADMIN) },
              modifier = Modifier.fillMaxWidth().testTag("switch_to_admin_button")
            ) {
              Text("Admin Mode (Officer Ronald Reyes)")
            }
          }
        }
      }
    }

    // 5. Sign Out Button
    item {
      OutlinedButton(
        onClick = onSignOut,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
        modifier = Modifier.fillMaxWidth().testTag("sign_out_button")
      ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text("Sign Out")
      }
    }
  }
}
