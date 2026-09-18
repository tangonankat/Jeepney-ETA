package com.example.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.DriverProfile
import com.example.data.model.UserRole

@Composable
fun DriverPendingScreen(
  driverProfile: DriverProfile?,
  onSwitchToAdmin: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box(
      modifier = Modifier
        .size(80.dp)
        .background(Color(0xFFFEF3C7), CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        Icons.Default.HourglassTop,
        contentDescription = null,
        tint = Color(0xFFD97706),
        modifier = Modifier.size(44.dp)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    Text(
      text = "Driver Approval Pending",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = "New driver accounts cannot see the map or operate trips right away. An LTFRB / Fleet Admin must check and approve your driver's license and jeepney registration papers first.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
      modifier = Modifier.fillMaxWidth().testTag("pending_driver_docs_card"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Document Submission Status:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Driver Name:", style = MaterialTheme.typography.bodySmall)
          Text(driverProfile?.driverName ?: "Applicant", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Professional License:", style = MaterialTheme.typography.bodySmall)
          Text(driverProfile?.licenseNumber ?: "N01-19-450198", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Vehicle Plate:", style = MaterialTheme.typography.bodySmall)
          Text(driverProfile?.vehiclePlate ?: "NBB-9321", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("LTFRB Registration:", style = MaterialTheme.typography.bodySmall)
          Text("Under Review", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD97706), fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Quick button to switch to Admin role to inspect & approve this driver application!
    FilledTonalButton(
      onClick = onSwitchToAdmin,
      modifier = Modifier.fillMaxWidth().testTag("switch_to_admin_to_approve_button")
    ) {
      Icon(Icons.Default.Shield, contentDescription = null)
      Spacer(Modifier.size(8.dp))
      Text("Log in as Admin to Review & Approve")
    }
  }
}
