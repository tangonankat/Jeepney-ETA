package com.example.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Trip

@Composable
fun QRScannerModal(
  activeTrips: List<Trip>,
  onScan: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var manualCode by remember { mutableStateOf("") }
  val infiniteTransition = rememberInfiniteTransition(label = "laser")
  val laserPosition by infiniteTransition.animateFloat(
    initialValue = 0.1f,
    targetValue = 0.9f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser"
  )

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .testTag("qr_scanner_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              Icons.Default.QrCodeScanner,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Text(
              text = "Scan Jeepney QR Code",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("qr_close_button")) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Point camera at the QR sticker inside the jeepney to verify your ride and board.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scanner Viewport Box with animated laser
        Box(
          modifier = Modifier
            .size(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F172A))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            Icons.Default.QrCode2,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.size(120.dp)
          )

          Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height * laserPosition
            drawLine(
              color = Color(0xFFE11D48),
              start = Offset(20f, y),
              end = Offset(size.width - 20f, y),
              strokeWidth = 3.dp.toPx()
            )
          }

          Surface(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 12.dp),
            shape = RoundedCornerShape(10.dp),
            color = Color.Black.copy(alpha = 0.7f)
          ) {
            Text(
              text = "ALIGN QR IN FRAME",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Active Trip Quick Scan buttons (for testing / simulated scan)
        Text(
          text = "Quick Boarding Test Stickers:",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          activeTrips.take(3).forEach { trip ->
            val qrCode = "QR-${trip.plateNumber}"
            FilledTonalButton(
              onClick = {
                onScan(qrCode)
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("quick_qr_${trip.plateNumber}")
            ) {
              Text("Board: ${trip.plateNumber} (${trip.routeName})")
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Or manual code input
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = manualCode,
            onValueChange = { manualCode = it },
            label = { Text("Or enter sticker code") },
            placeholder = { Text("e.g. QR-JEEP-CUB-01") },
            modifier = Modifier.weight(1f).testTag("manual_qr_input"),
            singleLine = true
          )
          Button(
            onClick = {
              if (manualCode.isNotBlank()) {
                onScan(manualCode)
              }
            },
            modifier = Modifier.testTag("submit_manual_qr_button")
          ) {
            Text("Board")
          }
        }
      }
    }
  }
}
