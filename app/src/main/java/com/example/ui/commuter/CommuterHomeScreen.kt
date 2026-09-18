package com.example.ui.commuter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.JeepneyRoute
import com.example.data.model.PassengerSession
import com.example.data.model.Trip
import com.example.data.model.User
import com.example.data.model.WaitingRequest
import com.example.data.model.WaitingStatus
import com.example.data.repository.JeepneyRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommuterHomeScreen(
  currentUser: User?,
  routes: List<JeepneyRoute>,
  selectedRouteId: String?,
  trips: List<Trip>,
  waitingRequest: WaitingRequest?,
  currentSession: PassengerSession?,
  onSelectRoute: (String) -> Unit,
  onAlertWaiting: (routeId: String, stopId: String, passengerCount: Int) -> Unit,
  onCancelWaiting: () -> Unit,
  onOpenQrScanner: () -> Unit,
  onDropOff: () -> Unit,
  onViewMapTab: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentRoute = routes.find { it.id == selectedRouteId } ?: routes.firstOrNull()
  var selectedStopId by remember(currentRoute) {
    mutableStateOf(currentRoute?.stops?.firstOrNull()?.id ?: "")
  }
  var passengerCount by remember { mutableIntStateOf(1) }
  var routeDropdownExpanded by remember { mutableStateOf(false) }

  val activeTripsOnRoute = trips.filter { it.routeId == currentRoute?.id }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Welcome Greeting & Status Hero
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("commuter_greeting_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer
        )
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(
            text = "Magandang Araw, ${currentUser?.fullName?.split(" ")?.firstOrNull() ?: "Commuter"}!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (currentSession != null) {
              "You are currently ONBOARD active jeepney. Safe travels!"
            } else if (waitingRequest != null) {
              "Waiting for jeepney at ${waitingRequest.stopName}. Drivers have been notified!"
            } else {
              "Find your route, alert nearby jeepney drivers, and track live arrival times."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Quick Action Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            if (currentSession != null) {
              Button(
                onClick = onDropOff,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                modifier = Modifier.weight(1f).testTag("commuter_dropoff_button")
              ) {
                Text("Para! (Drop Off)")
              }
            } else {
              Button(
                onClick = onOpenQrScanner,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f).testTag("commuter_scan_qr_button")
              ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Scan Jeepney QR")
              }
            }
          }
        }
      }
    }

    // 2. Active Ride Session Banner (if onboard)
    if (currentSession != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("onboard_session_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5)),
          border = BorderStroke(1.dp, Color(0xFF10B981))
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier.size(44.dp).background(Color(0xFF10B981), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            }
            Column(modifier = Modifier.weight(1f)) {
              Text("CURRENTLY ONBOARD", style = MaterialTheme.typography.labelSmall, color = Color(0xFF065F46), fontWeight = FontWeight.Bold)
              Text("Vehicle: ${currentSession.vehicleId} • ${currentRoute?.name ?: "Active Route"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text("Boarded with QR scan confirmation", style = MaterialTheme.typography.bodySmall, color = Color(0xFF047857))
            }
          }
        }
      }
    }

    // 3. Route Selector & Stop Picker
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("route_selector_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Select Jeepney Route",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))

          // Route dropdown
          ExposedDropdownMenuBox(
            expanded = routeDropdownExpanded,
            onExpandedChange = { routeDropdownExpanded = !routeDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = currentRoute?.name ?: "Choose a route",
              onValueChange = {},
              readOnly = true,
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = routeDropdownExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth().testTag("route_dropdown_field"),
              shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
              expanded = routeDropdownExpanded,
              onDismissRequest = { routeDropdownExpanded = false }
            ) {
              routes.forEach { route ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(route.name, fontWeight = FontWeight.SemiBold)
                      Text("Fare: ₱${route.fareRegular.toInt()} • ${route.stops.size} stops", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  },
                  onClick = {
                    onSelectRoute(route.id)
                    selectedStopId = route.stops.firstOrNull()?.id ?: ""
                    routeDropdownExpanded = false
                  },
                  modifier = Modifier.testTag("route_item_${route.code}")
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Waiting stop selection
          Text(
            text = "Your Boarding Stop:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(currentRoute?.stops ?: emptyList()) { stop ->
              val isSelected = stop.id == selectedStopId
              Surface(
                modifier = Modifier
                  .clickable { selectedStopId = stop.id }
                  .testTag("stop_chip_${stop.id}"),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
              ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                  Text(
                    text = stop.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = stop.landmark,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 4. "I'm Waiting" Button & Waiting Controls
          val isCurrentlyWaiting = waitingRequest != null && waitingRequest.status == WaitingStatus.WAITING
          if (isCurrentlyWaiting) {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFFFEF3C7),
              border = BorderStroke(1.dp, Color(0xFFF59E0B))
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.PanTool, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                    Text(
                      text = "ALERT ACTIVE: Waiting at ${waitingRequest?.stopName}",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFB45309)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Drivers approaching this stop can see your radar beacon. When the jeepney pulls up, scan the inside QR to board.",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color(0xFF92400E)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                  onClick = onCancelWaiting,
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                  modifier = Modifier.fillMaxWidth().testTag("cancel_waiting_button")
                ) {
                  Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("Cancel Waiting Alert")
                }
              }
            }
          } else {
            Button(
              onClick = {
                if (currentRoute != null) {
                  onAlertWaiting(currentRoute.id, selectedStopId, passengerCount)
                }
              },
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("im_waiting_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
            ) {
              Icon(Icons.Default.PanTool, contentDescription = null, tint = Color.Black)
              Spacer(Modifier.width(8.dp))
              Text("Yes, I'm Waiting (Alert Drivers)", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // 5. Nearby Active Jeepneys along this Route
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Active Jeepneys on Route (${activeTripsOnRoute.size})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "View Map",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { onViewMapTab() }.testTag("quick_view_map_text")
        )
      }
    }

    if (activeTripsOnRoute.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text("No active jeepneys on this route yet.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text("Switch to Driver role in header to start a trip!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    } else {
      items(activeTripsOnRoute) { trip ->
        val userStop = currentRoute?.stops?.find { it.id == selectedStopId } ?: currentRoute?.stops?.firstOrNull()
        val distMeters = if (userStop != null) {
          JeepneyRepository.calculateDistanceMeters(
            trip.currentLatitude,
            trip.currentLongitude,
            userStop.latitude,
            userStop.longitude
          )
        } else 350.0
        val etaMinutes = JeepneyRepository.estimateEtaMinutes(distMeters, trip.currentSpeedKph)

        Card(
          modifier = Modifier.fillMaxWidth().testTag("trip_card_${trip.id}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .background(
                      if (trip.isFull) Color(0xFFFEE2E2) else Color(0xFFE0F2FE),
                      CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = if (trip.isFull) Color(0xFFDC2626) else Color(0xFF0284C7),
                    modifier = Modifier.size(20.dp)
                  )
                }
                Column {
                  Text(
                    text = trip.plateNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = trip.driverName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              // Seat Availability Chip
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (trip.isFull) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)
              ) {
                Text(
                  text = if (trip.isFull) "FULL" else "${trip.availableSeats} Seats Left",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (trip.isFull) Color(0xFFDC2626) else Color(0xFF065F46),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // ETA Badge
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Text(
                  text = "ETA: ~$etaMinutes min",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "(${distMeters.toInt()}m away)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              // Seat capacity meter
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.AirlineSeatReclineNormal, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                Text(
                  text = "${trip.passengerCount}/${trip.seatCapacity}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }
  }
}
