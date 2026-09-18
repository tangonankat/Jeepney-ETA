package com.example.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trip
import com.example.data.model.WaitingRequest
import com.example.data.repository.JeepneyRepository
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class RadarCommuterItem(
  val request: WaitingRequest,
  val distanceMeters: Double,
  val bearingAngleDeg: Double
)

@Composable
fun RadarCanvas(
  driverTrip: Trip?,
  waitingRequests: List<WaitingRequest>,
  modifier: Modifier = Modifier,
  onCommuterClick: (WaitingRequest) -> Unit = {}
) {
  val textMeasurer = rememberTextMeasurer()
  val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
  val sweepAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 3500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "sweepAngle"
  )

  val radarBg = Color(0xFF030712) // Night tactical deep slate
  val gridGreen = Color(0xFF10B981) // Radar Emerald

  // Calculate distances & relative bearings for eligible commuters on route
  val radarItems = waitingRequests.filter { it.routeId == driverTrip?.routeId }.map { req ->
    val dist = if (driverTrip != null) {
      JeepneyRepository.calculateDistanceMeters(
        driverTrip.currentLatitude,
        driverTrip.currentLongitude,
        req.latitude,
        req.longitude
      )
    } else {
      250.0
    }
    // Calculate approximate bearing
    val latDiff = req.latitude - (driverTrip?.currentLatitude ?: 14.6)
    val lngDiff = req.longitude - (driverTrip?.currentLongitude ?: 121.0)
    val angle = Math.toDegrees(kotlin.math.atan2(latDiff, lngDiff))
    RadarCommuterItem(req, dist, angle)
  }.sortedBy { it.distanceMeters }

  Box(modifier = modifier) {
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        .background(radarBg)
    ) {
      val w = size.width
      val h = size.height
      val center = Offset(w / 2f, h / 2f)
      val maxRadius = min(w, h) / 2f - 24.dp.toPx()

      // 1. Radar Concentric Rings (100m, 250m, 400m, 1km)
      val ringDistances = listOf(100, 250, 400, 1000)
      val ringFractions = listOf(0.25f, 0.50f, 0.75f, 1.0f)

      ringFractions.forEachIndexed { i, fraction ->
        val r = maxRadius * fraction
        drawCircle(
          color = gridGreen.copy(alpha = 0.22f),
          radius = r,
          center = center,
          style = Stroke(width = 1.5f)
        )
        // Distance label along top
        val label = "${ringDistances[i]}m"
        drawText(
          textMeasurer = textMeasurer,
          text = label,
          topLeft = Offset(center.x + 6f, center.y - r - 14f),
          style = TextStyle(
            color = gridGreen.copy(alpha = 0.65f),
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold
          )
        )
      }

      // 2. Crosshairs
      drawLine(
        color = gridGreen.copy(alpha = 0.25f),
        start = Offset(center.x, center.y - maxRadius),
        end = Offset(center.x, center.y + maxRadius),
        strokeWidth = 1f
      )
      drawLine(
        color = gridGreen.copy(alpha = 0.25f),
        start = Offset(center.x - maxRadius, center.y),
        end = Offset(center.x + maxRadius, center.y),
        strokeWidth = 1f
      )

      // 3. Radar Beam Sweep
      val sweepRad = Math.toRadians(sweepAngle.toDouble())
      val beamX = center.x + maxRadius * cos(sweepRad).toFloat()
      val beamY = center.y + maxRadius * sin(sweepRad).toFloat()

      drawLine(
        color = gridGreen.copy(alpha = 0.8f),
        start = center,
        end = Offset(beamX, beamY),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
      )

      // Sweep gradient fan
      drawArc(
        brush = Brush.sweepGradient(
          0.0f to Color.Transparent,
          0.85f to Color.Transparent,
          1.0f to gridGreen.copy(alpha = 0.28f),
          center = center
        ),
        startAngle = sweepAngle - 45f,
        sweepAngle = 45f,
        useCenter = true,
        topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
        size = androidx.compose.ui.geometry.Size(maxRadius * 2, maxRadius * 2)
      )

      // 4. Center Driver Vehicle
      drawCircle(
        color = Color(0xFF0284C7),
        radius = 12.dp.toPx(),
        center = center
      )
      drawCircle(
        color = Color.White,
        radius = 4.dp.toPx(),
        center = center
      )

      // 5. Plot Waiting Commuters on Radar
      radarItems.forEach { item ->
        // Scale distance to radar radius (1000m max view)
        val clampedDist = item.distanceMeters.coerceIn(20.0, 1000.0)
        val distFraction = (clampedDist / 1000.0).toFloat()
        val itemR = maxRadius * distFraction
        val itemAngleRad = Math.toRadians(item.bearingAngleDeg)

        val posX = center.x + itemR * cos(itemAngleRad).toFloat()
        val posY = center.y + itemR * sin(itemAngleRad).toFloat()
        val pos = Offset(posX, posY)

        // Commuter blip halo
        drawCircle(
          color = Color(0xFFF59E0B).copy(alpha = 0.35f),
          radius = 10.dp.toPx(),
          center = pos
        )
        // Commuter blip center
        drawCircle(
          color = Color(0xFFF59E0B),
          radius = 5.dp.toPx(),
          center = pos
        )

        // Exact distance tag
        val distText = "${item.distanceMeters.toInt()}m"
        drawText(
          textMeasurer = textMeasurer,
          text = distText,
          topLeft = Offset(pos.x + 10f, pos.y - 8f),
          style = TextStyle(
            color = Color(0xFFFDE047),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
          )
        )
      }
    }

    // Top status pill overlay
    Surface(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(14.dp),
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF1E293B).copy(alpha = 0.9f)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          Icons.Default.Navigation,
          contentDescription = null,
          tint = gridGreen,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = "RADAR SCANNING: ${radarItems.size} Commuters in Range",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
