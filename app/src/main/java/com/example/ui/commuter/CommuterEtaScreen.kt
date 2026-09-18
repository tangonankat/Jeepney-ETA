package com.example.ui.commuter

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
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JeepneyRoute
import com.example.data.model.Trip
import com.example.data.model.WaitingRequest
import com.example.data.repository.JeepneyRepository

@Composable
fun CommuterEtaScreen(
  routes: List<JeepneyRoute>,
  selectedRouteId: String?,
  trips: List<Trip>,
  userWaiting: WaitingRequest?,
  modifier: Modifier = Modifier
) {
  val currentRoute = routes.find { it.id == selectedRouteId } ?: routes.firstOrNull()
  val activeTripsOnRoute = trips.filter { it.routeId == currentRoute?.id }

  // Target stop: user's chosen stop if waiting, or first stop
  val targetStop = if (userWaiting != null && userWaiting.stopId.isNotBlank()) {
    currentRoute?.stops?.find { it.id == userWaiting.stopId } ?: currentRoute?.stops?.firstOrNull()
  } else {
    currentRoute?.stops?.firstOrNull()
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // Header
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("eta_header_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Live Arrival Times (ETA)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Route: ${currentRoute?.name ?: "All Routes"}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "Estimated destination: ${targetStop?.name ?: "Designated Terminal"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
        }
      }
    }

    // Traffic condition indicator
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("traffic_condition_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Box(
            modifier = Modifier.size(36.dp).background(Color(0xFFFEF3C7), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Traffic, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
          }
          Column(modifier = Modifier.weight(1f)) {
            Text("Traffic Condition: Moderate Flow", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text("Average fleet corridor speed: ~22 km/h", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
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
            Text("No active jeepneys en route at this moment.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
          }
        }
      }
    } else {
      items(activeTripsOnRoute) { trip ->
        val distMeters = if (targetStop != null) {
          JeepneyRepository.calculateDistanceMeters(
            trip.currentLatitude,
            trip.currentLongitude,
            targetStop.latitude,
            targetStop.longitude
          )
        } else 400.0

        val etaMinutes = JeepneyRepository.estimateEtaMinutes(distMeters, trip.currentSpeedKph)

        Card(
          modifier = Modifier.fillMaxWidth().testTag("eta_trip_card_${trip.id}"),
          shape = RoundedCornerShape(16.dp),
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
                Text(
                  text = "${trip.plateNumber} • ${trip.driverName}",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Vehicle Capacity: ${trip.seatCapacity} seats",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              // Large ETA readout
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "$etaMinutes MIN",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Black,
                  color = if (etaMinutes <= 3) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                )
                Text(
                  text = if (etaMinutes <= 2) "ARRIVING NOW" else "Estimated Wait",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (etaMinutes <= 2) Color(0xFF059669) else MaterialTheme.colorScheme.outline
                )
              }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Details row: Distance, Speed, Seat capacity
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Text("${distMeters.toInt()}m away", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
              }

              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                Text("${trip.currentSpeedKph.toInt()} km/h", style = MaterialTheme.typography.bodySmall)
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (trip.isFull) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)
              ) {
                Text(
                  text = if (trip.isFull) "FULL" else "${trip.availableSeats} Seats Left",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (trip.isFull) Color(0xFFDC2626) else Color(0xFF065F46),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
