package com.example.ui.driver

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.model.DriverProfile
import com.example.data.model.JeepneyRoute
import com.example.data.model.Trip
import com.example.data.model.TripStatus

@Composable
fun DriverTripScreen(
  driverProfile: DriverProfile?,
  activeTrip: Trip?,
  routes: List<JeepneyRoute>,
  onStartTrip: (routeId: String) -> Unit,
  onEndTrip: (tripId: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val assignedRoute = routes.find { it.id == (activeTrip?.routeId ?: driverProfile?.assignedRouteId) } ?: routes.firstOrNull()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(vertical = 16.dp)
  ) {
    // 1. Current Trip Status Header
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("trip_status_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (activeTrip != null) "ACTIVE TRIP RUNNING" else "NO ACTIVE TRIP",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = if (activeTrip != null) Color(0xFF065F46) else MaterialTheme.colorScheme.outline
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (activeTrip != null) Color(0xFF10B981) else Color(0xFF94A3B8)
            ) {
              Text(
                text = if (activeTrip != null) "GPS BROADCASTING" else "STANDBY",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = assignedRoute?.name ?: "Selected Route",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            text = "Vehicle: ${driverProfile?.vehiclePlate ?: "NCF-1042"} (${driverProfile?.vehicleBodyNumber ?: "JEEP-01"})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )

          Spacer(modifier = Modifier.height(14.dp))

          if (activeTrip != null) {
            Button(
              onClick = { onEndTrip(activeTrip.id) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trip_screen_end_trip_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Stop, contentDescription = null)
              Spacer(Modifier.width(6.dp))
              Text("End Active Trip")
            }
          } else {
            Button(
              onClick = { onStartTrip(assignedRoute?.id ?: "route_cub_div") },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trip_screen_start_trip_button"),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null)
              Spacer(Modifier.width(6.dp))
              Text("Start Trip Now")
            }
          }
        }
      }
    }

    // 2. Real-time Telemetry Card
    if (activeTrip != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("telemetry_card"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Real-Time Telemetry Feed", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Latitude", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(String.format("%.5f", activeTrip.currentLatitude), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
              }
              Column {
                Text("Longitude", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(String.format("%.5f", activeTrip.currentLongitude), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
              }
              Column {
                Text("Speed", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("${activeTrip.currentSpeedKph.toInt()} km/h", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // 3. Route Corridor & Sequence of Stops
    item {
      Text(
        text = "Corridor Stops Sequence",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
    }

    if (assignedRoute != null) {
      itemsIndexed(assignedRoute.stops) { index, stop ->
        val isCurrentStop = activeTrip != null && activeTrip.currentStopIndex == index
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isCurrentStop) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
          )
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .background(
                  if (isCurrentStop) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                  CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentStop) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(stop.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text(stop.landmark, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (isCurrentStop) {
              Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary) {
                Text(
                  text = "APPROACHING",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                  color = MaterialTheme.colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
