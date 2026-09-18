package com.example.ui.driver

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import com.example.data.local.AuditLogEntity
import com.example.data.model.DriverProfile
import com.example.data.model.JeepneyRoute
import com.example.data.model.Trip
import com.example.data.model.TripStatus
import com.example.data.model.WaitingRequest

@Composable
fun DriverDashboardScreen(
  driverProfile: DriverProfile?,
  activeTrip: Trip?,
  routes: List<JeepneyRoute>,
  waitingRequests: List<WaitingRequest>,
  auditLogs: List<AuditLogEntity>,
  onStartTrip: (routeId: String) -> Unit,
  onEndTrip: (tripId: String) -> Unit,
  onUpdateCount: (tripId: String, delta: Int) -> Unit,
  onViewRadarTab: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showAuditSheet by remember { mutableStateOf(false) }

  val waitingCount = waitingRequests.count { it.routeId == (activeTrip?.routeId ?: driverProfile?.assignedRouteId) }
  val assignedRoute = routes.find { it.id == (activeTrip?.routeId ?: driverProfile?.assignedRouteId) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Driver Greeting & Vehicle Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("driver_info_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = driverProfile?.driverName ?: "Jeepney Driver",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "Plate: ${driverProfile?.vehiclePlate ?: "NCF-1042"} • Body: ${driverProfile?.vehicleBodyNumber ?: "JEEP-01"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFD1FAE5)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                Text("APPROVED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Assigned Route: ${assignedRoute?.name ?: "Cubao - Divisoria"}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }

    // 2. SEAT LIMIT TRACKER & PASSENGER CAPACITY
    item {
      val seatCapacity = activeTrip?.seatCapacity ?: driverProfile?.seatCapacity ?: 18
      val currentCount = activeTrip?.passengerCount ?: 0
      val availableSeats = (seatCapacity - currentCount).coerceAtLeast(0)
      val isFull = currentCount >= seatCapacity

      Card(
        modifier = Modifier.fillMaxWidth().testTag("seat_limit_tracker_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isFull) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
          2.dp,
          if (isFull) Color(0xFFDC2626) else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "SEAT LIMIT TRACKER",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.outline
            )

            // FULL status badge
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isFull) Color(0xFFDC2626) else Color(0xFF10B981)
            ) {
              Text(
                text = if (isFull) "VEHICLE FULL" else "$availableSeats SEATS OPEN",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Big Passenger Readout
          Text(
            text = "$currentCount / $seatCapacity",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = if (isFull) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Total Passengers Onboard",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(20.dp))

          // MANUAL COUNT BUTTONS (+ and -) as requested
          Text(
            text = "Manual Count Adjuster (Fix GPS error):",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // [-] Button
            FilledTonalIconButton(
              onClick = {
                if (activeTrip != null) onUpdateCount(activeTrip.id, -1)
              },
              enabled = activeTrip != null && currentCount > 0,
              modifier = Modifier.size(64.dp).testTag("passenger_count_minus_button"),
              colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0xFFFEE2E2),
                contentColor = Color(0xFFDC2626)
              )
            ) {
              Icon(Icons.Default.Remove, contentDescription = "Decrease count", modifier = Modifier.size(32.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "$currentCount",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
              )
              Text("Pax", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            // [+] Button
            FilledTonalIconButton(
              onClick = {
                if (activeTrip != null) onUpdateCount(activeTrip.id, 1)
              },
              enabled = activeTrip != null && !isFull,
              modifier = Modifier.size(64.dp).testTag("passenger_count_plus_button"),
              colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color(0xFFD1FAE5),
                contentColor = Color(0xFF059669)
              )
            ) {
              Icon(Icons.Default.Add, contentDescription = "Increase count", modifier = Modifier.size(32.dp))
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Audit rule active: Every manual change logs previous count, new count, and timestamp.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.outline
          )
        }
      }
    }

    // 3. TRIP CONTROL BUTTONS (Start Trip / End Trip)
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("driver_trip_control_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Trip Lifecycle Controls",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))

          if (activeTrip != null && activeTrip.status == TripStatus.ACTIVE) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFFD1FAE5)
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(Modifier.size(10.dp).background(Color(0xFF059669), CircleShape))
                Text(
                  text = "TRIP IN PROGRESS: ${activeTrip.routeName}",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF065F46)
                )
              }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
              onClick = { onEndTrip(activeTrip.id) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("end_trip_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Stop, contentDescription = null)
              Spacer(Modifier.width(6.dp))
              Text("End Trip (Complete Run)")
            }
          } else {
            Button(
              onClick = {
                onStartTrip(assignedRoute?.id ?: "route_cub_div")
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_trip_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null)
              Spacer(Modifier.width(6.dp))
              Text("Start Trip (Activate GPS & Radar)")
            }
          }
        }
      }
    }

    // 4. RADAR QUICK PEEK & WAITING PASSENGERS
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("waiting_commuters_summary_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Waiting Commuters: $waitingCount",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Waiting along ${assignedRoute?.code ?: "route"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            FilledTonalButton(
              onClick = onViewRadarTab,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("open_radar_button")
            ) {
              Text("Open Radar")
            }
          }
        }
      }
    }

    // 5. AUDIT LOGS EXPANDER (Manual count events)
    item {
      OutlinedButton(
        onClick = { showAuditSheet = !showAuditSheet },
        modifier = Modifier.fillMaxWidth().testTag("toggle_audit_log_button")
      ) {
        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(if (showAuditSheet) "Hide Audit Event History" else "View Passenger Count Audit History (${auditLogs.size})")
      }
    }

    if (showAuditSheet) {
      if (auditLogs.isEmpty()) {
        item {
          Text(
            text = "No audit events logged yet.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 8.dp)
          )
        }
      } else {
        items(auditLogs.take(6).size) { index ->
          val log = auditLogs[index]
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(log.eventType, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(if (log.synced) "Synced" else "Queued", style = MaterialTheme.typography.labelSmall, color = if (log.synced) Color(0xFF059669) else Color(0xFFDC2626))
              }
              Text(log.description, style = MaterialTheme.typography.bodySmall)
              Text("Prev: ${log.previousValue} ➔ New: ${log.newValue}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    }
  }
}
