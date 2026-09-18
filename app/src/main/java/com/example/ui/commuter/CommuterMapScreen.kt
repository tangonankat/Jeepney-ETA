package com.example.ui.commuter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.JeepneyRoute
import com.example.data.model.Trip
import com.example.data.model.WaitingRequest
import com.example.ui.common.MapCanvas

@Composable
fun CommuterMapScreen(
  routes: List<JeepneyRoute>,
  selectedRouteId: String?,
  trips: List<Trip>,
  waitingRequests: List<WaitingRequest>,
  userWaiting: WaitingRequest?,
  onSelectRoute: (String) -> Unit,
  onOpenQrScanner: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedRoute = routes.find { it.id == selectedRouteId } ?: routes.firstOrNull()
  val activeTripsOnRoute = trips.filter { it.routeId == selectedRoute?.id }

  Column(modifier = modifier.fillMaxSize()) {
    // Route Tabs
    if (routes.isNotEmpty()) {
      val selectedIndex = routes.indexOfFirst { it.id == selectedRoute?.id }.coerceAtLeast(0)
      ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 12.dp,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        routes.forEachIndexed { index, route ->
          Tab(
            selected = index == selectedIndex,
            onClick = { onSelectRoute(route.id) },
            text = { Text(route.code, fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("map_route_tab_${route.code}")
          )
        }
      }
    }

    // Interactive Map Canvas Area
    Box(modifier = Modifier.weight(1f)) {
      MapCanvas(
        route = selectedRoute,
        trips = trips,
        waitingRequests = waitingRequests,
        userWaiting = userWaiting,
        modifier = Modifier.fillMaxSize()
      )

      // Floating bottom sheet summary on map
      Card(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(16.dp)
          .testTag("map_bottom_info_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = selectedRoute?.name ?: "Route Map",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${activeTripsOnRoute.size} active jeepneys • ${selectedRoute?.stops?.size ?: 0} designated stops",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Button(
            onClick = onOpenQrScanner,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("map_board_qr_button")
          ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
            Text(text = " Board", modifier = Modifier.padding(start = 4.dp))
          }
        }
      }
    }
  }
}
