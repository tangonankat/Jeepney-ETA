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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import com.example.data.model.Trip
import com.example.data.model.WaitingRequest
import com.example.data.repository.JeepneyRepository
import com.example.ui.common.RadarCanvas

@Composable
fun DriverRadarScreen(
  activeTrip: Trip?,
  waitingRequests: List<WaitingRequest>,
  onSimulateGeofenceBoarding: (tripId: String, commuterId: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val eligibleWaiting = waitingRequests.filter { it.routeId == activeTrip?.routeId }.map { req ->
    val dist = if (activeTrip != null) {
      JeepneyRepository.calculateDistanceMeters(
        activeTrip.currentLatitude,
        activeTrip.currentLongitude,
        req.latitude,
        req.longitude
      )
    } else 300.0
    Pair(req, dist)
  }.sortedBy { it.second }

  Column(modifier = modifier.fillMaxSize()) {
    // 1. Interactive Tactical Radar Visualizer
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(310.dp)
        .testTag("driver_radar_canvas_container")
    ) {
      RadarCanvas(
        driverTrip = activeTrip,
        waitingRequests = waitingRequests,
        modifier = Modifier.fillMaxSize()
      )
    }

    // 2. Commuter Distance Manifest Header
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colorScheme.surfaceVariant,
      tonalElevation = 2.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Icon(Icons.Default.Sensors, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
          Text(
            text = "PROXIMITY MANIFEST",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
          )
        }
        Text(
          text = "${eligibleWaiting.size} Commuters in Sector",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // 3. Commuter List with exact distance (e.g. 100m, 250m, 400m)
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (eligibleWaiting.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text("No commuters waiting along this route right now.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
              Text("Radar continuously sweeps for 'I'm Waiting' beacons.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
          }
        }
      } else {
        items(eligibleWaiting) { (req, dist) ->
          val distInt = dist.toInt()
          Card(
            modifier = Modifier.fillMaxWidth().testTag("radar_commuter_card_${req.commuterId}"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFFEF3C7), CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                }
                Column {
                  Text(
                    text = req.commuterName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Waiting at ${req.stopName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "$distInt meters away • ${req.passengerCount} pax",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD97706)
                  )
                }
              }

              // Geofence Auto-Pick Up simulation trigger
              if (activeTrip != null) {
                FilledTonalButton(
                  onClick = {
                    onSimulateGeofenceBoarding(activeTrip.id, req.commuterId)
                  },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier.testTag("geofence_pickup_${req.commuterId}")
                ) {
                  Text("Pick Up", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }
  }
}
