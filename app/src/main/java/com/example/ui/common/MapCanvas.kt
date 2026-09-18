package com.example.ui.common

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JeepneyRoute
import com.example.data.model.Trip
import com.example.data.model.WaitingRequest
import kotlin.math.max
import kotlin.math.min

@Composable
fun MapCanvas(
  route: JeepneyRoute?,
  trips: List<Trip>,
  waitingRequests: List<WaitingRequest>,
  userWaiting: WaitingRequest?,
  modifier: Modifier = Modifier
) {
  var zoom by remember { mutableFloatStateOf(1f) }
  var panOffset by remember { mutableStateOf(Offset.Zero) }

  val textMeasurer = rememberTextMeasurer()
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseRadius by infiniteTransition.animateFloat(
    initialValue = 10f,
    targetValue = 28f,
    animationSpec = infiniteRepeatable(tween(1400), repeatMode = RepeatMode.Restart),
    label = "pulseRadius"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 0.0f,
    animationSpec = infiniteRepeatable(tween(1400), repeatMode = RepeatMode.Restart),
    label = "pulseAlpha"
  )

  val isDark = MaterialTheme.colorScheme.background.red < 0.2f
  val mapBgColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9)
  val roadColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
  val routeLineColor = MaterialTheme.colorScheme.primary

  Box(modifier = modifier.clipToBoundsSafely()) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .background(mapBgColor)
        .pointerInput(Unit) {
          detectTransformGestures { _, pan, gestureZoom, _ ->
            zoom = (zoom * gestureZoom).coerceIn(0.6f, 3.0f)
            panOffset += pan
          }
        }
    ) {
      val w = size.width
      val h = size.height
      val centerX = w / 2f + panOffset.x
      val centerY = h / 2f + panOffset.y

      // Background decorative grid / roads
      val gridSpacing = 60f * zoom
      var x = (panOffset.x % gridSpacing)
      while (x < w) {
        drawLine(
          color = roadColor,
          start = Offset(x, 0f),
          end = Offset(x, h),
          strokeWidth = 1.5f * zoom
        )
        x += gridSpacing
      }
      var y = (panOffset.y % gridSpacing)
      while (y < h) {
        drawLine(
          color = roadColor,
          start = Offset(0f, y),
          end = Offset(w, y),
          strokeWidth = 1.5f * zoom
        )
        y += gridSpacing
      }

      if (route == null || route.stops.isEmpty()) {
        drawText(
          textMeasurer = textMeasurer,
          text = "No route selected",
          topLeft = Offset(centerX - 60, centerY),
          style = TextStyle(color = Color.Gray, fontSize = 14.sp)
        )
        return@Canvas
      }

      // Geo coordinate normalization bounds
      val lats = route.stops.map { it.latitude }
      val lngs = route.stops.map { it.longitude }
      val minLat = lats.minOrNull() ?: 14.0
      val maxLat = lats.maxOrNull() ?: 14.1
      val minLng = lngs.minOrNull() ?: 120.0
      val maxLng = lngs.maxOrNull() ?: 121.0

      val latSpan = max(maxLat - minLat, 0.005)
      val lngSpan = max(maxLng - minLng, 0.005)

      fun geoToScreen(lat: Double, lng: Double): Offset {
        val relX = ((lng - minLng) / lngSpan).toFloat()
        val relY = (1f - ((lat - minLat) / latSpan).toFloat()) // Invert Y
        val padding = 90f
        val usableW = (w - padding * 2) * zoom
        val usableH = (h - padding * 2) * zoom

        val sx = (w / 2f) + (relX - 0.5f) * usableW + panOffset.x
        val sy = (h / 2f) + (relY - 0.5f) * usableH + panOffset.y
        return Offset(sx, sy)
      }

      // 1. Draw Route Polyline
      val path = Path()
      val stopOffsets = route.stops.map { geoToScreen(it.latitude, it.longitude) }

      if (stopOffsets.isNotEmpty()) {
        path.moveTo(stopOffsets.first().x, stopOffsets.first().y)
        for (i in 1 until stopOffsets.size) {
          path.lineTo(stopOffsets[i].x, stopOffsets[i].y)
        }
        // Outer glow
        drawPath(
          path = path,
          color = routeLineColor.copy(alpha = 0.25f),
          style = Stroke(width = 12f * zoom)
        )
        // Main route line
        drawPath(
          path = path,
          color = routeLineColor,
          style = Stroke(
            width = 5f * zoom,
            pathEffect = PathEffect.cornerPathEffect(16f)
          )
        )
      }

      // 2. Draw Route Stops
      route.stops.forEachIndexed { index, stop ->
        val pos = stopOffsets.getOrNull(index) ?: return@forEachIndexed
        val stopColor = if (index == 0) Color(0xFF10B981) else if (index == route.stops.size - 1) Color(0xFFEF4444) else Color(0xFF64748B)

        // Stop circle
        drawCircle(
          color = Color.White,
          radius = 7f * zoom,
          center = pos
        )
        drawCircle(
          color = stopColor,
          radius = 5f * zoom,
          center = pos
        )

        // Stop Label (show only at higher zoom or for start/end)
        if (zoom >= 0.85f || index == 0 || index == route.stops.size - 1) {
          val label = stop.name.take(16)
          drawText(
            textMeasurer = textMeasurer,
            text = label,
            topLeft = Offset(pos.x + 10f, pos.y - 12f),
            style = TextStyle(
              color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
              fontSize = (10 * zoom).coerceIn(9f, 13f).sp,
              fontWeight = FontWeight.Medium
            )
          )
        }
      }

      // 3. Draw Waiting Commuter Marker & Pulse
      waitingRequests.filter { it.routeId == route.id }.forEach { req ->
        val pos = geoToScreen(req.latitude, req.longitude)
        val isMe = req.id == userWaiting?.id

        if (isMe) {
          // Radar pulse for current user
          drawCircle(
            color = Color(0xFFF59E0B).copy(alpha = pulseAlpha),
            radius = pulseRadius * zoom,
            center = pos
          )
        }

        drawCircle(
          color = if (isMe) Color(0xFFF59E0B) else Color(0xFFEC4899),
          radius = 8f * zoom,
          center = pos
        )
        drawCircle(
          color = Color.White,
          radius = 4f * zoom,
          center = pos
        )

        // Passenger Count Chip
        drawText(
          textMeasurer = textMeasurer,
          text = if (isMe) "YOU (WAITING)" else "Waiting (${req.passengerCount})",
          topLeft = Offset(pos.x - 30f, pos.y + 10f),
          style = TextStyle(
            color = if (isMe) Color(0xFFD97706) else Color(0xFFDB2777),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        )
      }

      // 4. Draw Active Jeepneys
      trips.filter { it.routeId == route.id }.forEach { trip ->
        val pos = geoToScreen(trip.currentLatitude, trip.currentLongitude)
        val isFull = trip.isFull

        // Jeepney halo
        drawCircle(
          color = if (isFull) Color(0xFFEF4444).copy(alpha = 0.25f) else Color(0xFF0284C7).copy(alpha = 0.25f),
          radius = 16f * zoom,
          center = pos
        )

        // Jeepney marker
        drawCircle(
          color = if (isFull) Color(0xFFDC2626) else Color(0xFF0284C7),
          radius = 11f * zoom,
          center = pos
        )
        drawCircle(
          color = Color.White,
          radius = 4f * zoom,
          center = pos
        )

        // Info Badge above jeepney
        val badgeText = "${trip.plateNumber} [${trip.passengerCount}/${trip.seatCapacity}]"
        drawText(
          textMeasurer = textMeasurer,
          text = badgeText,
          topLeft = Offset(pos.x - 40f, pos.y - 24f * zoom),
          style = TextStyle(
            color = if (isFull) Color(0xFFDC2626) else Color(0xFF0284C7),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        )
      }
    }

    // Map Floating Controls (Zoom in / out / reset pan)
    Column(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilledTonalIconButton(
        onClick = { zoom = (zoom * 1.25f).coerceAtMost(3.0f) },
        modifier = Modifier.size(40.dp).testTag("map_zoom_in_button")
      ) {
        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
      }
      FilledTonalIconButton(
        onClick = { zoom = (zoom / 1.25f).coerceAtLeast(0.6f) },
        modifier = Modifier.size(40.dp).testTag("map_zoom_out_button")
      ) {
        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
      }
      FilledTonalIconButton(
        onClick = {
          zoom = 1f
          panOffset = Offset.Zero
        },
        modifier = Modifier.size(40.dp).testTag("map_center_button")
      ) {
        Icon(Icons.Default.MyLocation, contentDescription = "Center Map")
      }
    }

    // Map Legend Overlay
    Surface(
      modifier = Modifier
        .align(Alignment.TopStart)
        .padding(12.dp),
      shape = RoundedCornerShape(12.dp),
      tonalElevation = 4.dp,
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Box(Modifier.size(8.dp).background(Color(0xFF0284C7), CircleShape))
          Text("Jeepney", style = MaterialTheme.typography.labelSmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Box(Modifier.size(8.dp).background(Color(0xFFF59E0B), CircleShape))
          Text("Waiting", style = MaterialTheme.typography.labelSmall)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Box(Modifier.size(8.dp).background(Color(0xFFEF4444), CircleShape))
          Text("Full", style = MaterialTheme.typography.labelSmall)
        }
      }
    }
  }
}

private fun Modifier.clipToBoundsSafely(): Modifier = this
