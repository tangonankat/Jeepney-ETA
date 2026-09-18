package com.example.ui.admin

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuditLogEntity
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.DriverProfile
import com.example.data.model.JeepneyRoute
import com.example.data.model.Trip
import com.example.data.model.WaitingRequest

@Composable
fun AdminDashboardScreen(
  driverProfiles: List<DriverProfile>,
  routes: List<JeepneyRoute>,
  trips: List<Trip>,
  waitingRequests: List<WaitingRequest>,
  auditLogs: List<AuditLogEntity>,
  onApproveDriver: (String) -> Unit,
  onRejectDriver: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val pendingDrivers = driverProfiles.filter { it.approvalStatus == DriverApprovalStatus.PENDING }
  val activeTripsCount = trips.size
  val waitingCommutersCount = waitingRequests.size

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Admin Security Hero Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("admin_banner_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.error, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
              }
              Text(
                text = "LTFRB / Admin Console",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.error) {
              Text(
                text = "AUTHORIZED",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Fleet oversight, route corridor compliance, and driver credential verification.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f)
          )
        }
      }
    }

    // 2. High-level Fleet Stats Grid
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Pending Approvals Stat
        Card(
          modifier = Modifier.weight(1f).testTag("stat_pending_drivers"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (pendingDrivers.isNotEmpty()) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surface
          )
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Pending Review", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${pendingDrivers.size}",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = if (pendingDrivers.isNotEmpty()) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
            )
            Text("Drivers awaiting check", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
          }
        }

        // Active Trips Stat
        Card(
          modifier = Modifier.weight(1f).testTag("stat_active_trips"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Active Fleet", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$activeTripsCount",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text("Jeepneys on trips", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
          }
        }

        // Waiting Commuters Stat
        Card(
          modifier = Modifier.weight(1f).testTag("stat_waiting_commuters"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Waiting", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$waitingCommutersCount",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFEC4899)
            )
            Text("Commuters alert", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
          }
        }
      }
    }

    // 3. DRIVER APPROVAL WORKFLOW SECTION
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Driver Approval Queue (${pendingDrivers.size})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
      }
    }

    if (pendingDrivers.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
            Text("All driver applications have been reviewed!", style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    } else {
      items(pendingDrivers) { driver ->
        Card(
          modifier = Modifier.fillMaxWidth().testTag("pending_driver_card_${driver.uid}"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column {
                Text(driver.driverName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Assigned Route: ${driver.assignedRouteName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
              }
              Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFEF3C7)) {
                Text("PENDING", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Document audit box
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("License No: ${driver.licenseNumber} (Professional)", style = MaterialTheme.typography.bodySmall)
                Text("Plate Number: ${driver.vehiclePlate} (${driver.vehicleBodyNumber})", style = MaterialTheme.typography.bodySmall)
                Text("Seat Capacity: ${driver.seatCapacity} seats", style = MaterialTheme.typography.bodySmall)
                Text("Documents: Driver's License & Jeepney OR/CR verified in system", style = MaterialTheme.typography.bodySmall, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action buttons: Approve or Reject
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedButton(
                onClick = { onRejectDriver(driver.uid, "Incomplete LTFRB franchise papers") },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                modifier = Modifier.weight(1f).testTag("reject_driver_button_${driver.uid}")
              ) {
                Text("Reject")
              }

              Button(
                onClick = { onApproveDriver(driver.uid) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                modifier = Modifier.weight(1f).testTag("approve_driver_button_${driver.uid}")
              ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Approve Driver")
              }
            }
          }
        }
      }
    }

    // 4. Approved Drivers Roster
    item {
      Text(
        text = "Active Verified Drivers (${driverProfiles.filter { it.approvalStatus == DriverApprovalStatus.APPROVED }.size})",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
    }

    items(driverProfiles.filter { it.approvalStatus == DriverApprovalStatus.APPROVED }) { driver ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.size(36.dp).background(Color(0xFFD1FAE5), CircleShape), contentAlignment = Alignment.Center) {
              Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(18.dp))
            }
            Column {
              Text(driver.driverName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
              Text("${driver.vehiclePlate} • ${driver.assignedRouteName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFD1FAE5)) {
            Text("ACTIVE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF065F46), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
          }
        }
      }
    }

    // 5. System Audit Log Feed
    item {
      Text(
        text = "System Passenger Count & Offline Sync Audit Feed",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
    }

    items(auditLogs.take(8)) { log ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(log.eventType, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(log.timestamp.toString().takeLast(6), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
          }
          Text(log.description, style = MaterialTheme.typography.bodySmall)
          Text("Prev: ${log.previousValue} ➔ New: ${log.newValue}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
      }
    }
  }
}
