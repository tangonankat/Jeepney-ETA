package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JeepneyRoute
import com.example.data.model.UserRole
import com.example.ui.viewmodel.AuthMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
  authMode: AuthMode,
  routes: List<JeepneyRoute>,
  onSetAuthMode: (AuthMode) -> Unit,
  onQuickLogin: (role: UserRole) -> Unit,
  onRegisterCommuter: (name: String, email: String) -> Unit,
  onRegisterDriver: (name: String, email: String, license: String, plate: String, seats: Int, routeId: String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedRoleTab by remember { mutableIntStateOf(0) } // 0 = Commuter, 1 = Driver
  var emailInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var nameInput by remember { mutableStateOf("") }

  // Driver specific
  var licenseInput by remember { mutableStateOf("N01-20-891024") }
  var plateInput by remember { mutableStateOf("NDB-8492") }
  var seatCountInput by remember { mutableIntStateOf(18) }
  var selectedRouteId by remember { mutableStateOf(routes.firstOrNull()?.id ?: "route_cub_div") }
  var routeDropdownExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(20.dp))

    // Logo & App Name
    Box(
      modifier = Modifier
        .size(72.dp)
        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        Icons.Default.DirectionsBus,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(40.dp)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "Para! Jeepney",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Black,
      color = MaterialTheme.colorScheme.primary
    )
    Text(
      text = "Real-time Jeepney Tracking & Commuter Wait Network",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Quick Test Persona Demo Login
    Card(
      modifier = Modifier.fillMaxWidth().testTag("quick_login_card"),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
    ) {
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = "Quick 1-Tap Persona Sign-In:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilledTonalButton(
            onClick = { onQuickLogin(UserRole.COMMUTER) },
            modifier = Modifier.weight(1f).testTag("quick_login_commuter_button"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 6.dp)
          ) {
            Text("Commuter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
          FilledTonalButton(
            onClick = { onQuickLogin(UserRole.DRIVER) },
            modifier = Modifier.weight(1f).testTag("quick_login_driver_button"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 6.dp)
          ) {
            Text("Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
          FilledTonalButton(
            onClick = { onQuickLogin(UserRole.ADMIN) },
            modifier = Modifier.weight(1f).testTag("quick_login_admin_button"),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp, horizontal = 6.dp)
          ) {
            Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Auth Card
    Card(
      modifier = Modifier.fillMaxWidth().testTag("auth_form_card"),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        // Mode Tabs: Login vs Register
        TabRow(
          selectedTabIndex = if (authMode == AuthMode.LOGIN) 0 else 1,
          containerColor = Color.Transparent
        ) {
          Tab(
            selected = authMode == AuthMode.LOGIN,
            onClick = { onSetAuthMode(AuthMode.LOGIN) },
            text = { Text("Log In", fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("auth_tab_login")
          )
          Tab(
            selected = authMode == AuthMode.REGISTER,
            onClick = { onSetAuthMode(AuthMode.REGISTER) },
            text = { Text("Create Account", fontWeight = FontWeight.Bold) },
            modifier = Modifier.testTag("auth_tab_register")
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (authMode == AuthMode.REGISTER) {
          // Role Selection Tab for Registration
          Text("Select Account Type:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { selectedRoleTab = 0 }
                .testTag("role_tab_commuter"),
              shape = RoundedCornerShape(10.dp),
              color = if (selectedRoleTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = "Commuter",
                modifier = Modifier.padding(vertical = 10.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (selectedRoleTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Surface(
              modifier = Modifier
                .weight(1f)
                .clickable { selectedRoleTab = 1 }
                .testTag("role_tab_driver"),
              shape = RoundedCornerShape(10.dp),
              color = if (selectedRoleTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = "Jeepney Driver",
                modifier = Modifier.padding(vertical = 10.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = if (selectedRoleTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedTextField(
          value = emailInput,
          onValueChange = { emailInput = it },
          label = { Text("Email Address") },
          modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = passwordInput,
          onValueChange = { passwordInput = it },
          label = { Text("Password") },
          visualTransformation = PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        // Additional Driver Fields if Registering Driver
        if (authMode == AuthMode.REGISTER && selectedRoleTab == 1) {
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = licenseInput,
            onValueChange = { licenseInput = it },
            label = { Text("Professional Driver's License No.") },
            modifier = Modifier.fillMaxWidth().testTag("reg_license_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = plateInput,
              onValueChange = { plateInput = it },
              label = { Text("Plate Number") },
              modifier = Modifier.weight(1.2f).testTag("reg_plate_input"),
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
              value = seatCountInput.toString(),
              onValueChange = { seatCountInput = it.toIntOrNull() ?: 18 },
              label = { Text("Seat Limit") },
              modifier = Modifier.weight(0.8f).testTag("reg_seats_input"),
              singleLine = true,
              shape = RoundedCornerShape(12.dp)
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Route dropdown for driver
          ExposedDropdownMenuBox(
            expanded = routeDropdownExpanded,
            onExpandedChange = { routeDropdownExpanded = !routeDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = routes.find { it.id == selectedRouteId }?.name ?: "Select Route",
              onValueChange = {},
              readOnly = true,
              label = { Text("Assigned Route") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = routeDropdownExpanded) },
              modifier = Modifier.menuAnchor().fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(
              expanded = routeDropdownExpanded,
              onDismissRequest = { routeDropdownExpanded = false }
            ) {
              routes.forEach { route ->
                DropdownMenuItem(
                  text = { Text(route.name) },
                  onClick = {
                    selectedRouteId = route.id
                    routeDropdownExpanded = false
                  }
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Submit Button
        Button(
          onClick = {
            if (authMode == AuthMode.LOGIN) {
              onQuickLogin(UserRole.COMMUTER)
            } else {
              if (selectedRoleTab == 0) {
                onRegisterCommuter(
                  if (nameInput.isBlank()) "Maria Santos" else nameInput,
                  if (emailInput.isBlank()) "commuter@example.com" else emailInput
                )
              } else {
                onRegisterDriver(
                  if (nameInput.isBlank()) "Kuya Dante" else nameInput,
                  if (emailInput.isBlank()) "driver@example.com" else emailInput,
                  licenseInput,
                  plateInput,
                  seatCountInput,
                  selectedRouteId
                )
              }
            }
          },
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("auth_submit_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = if (authMode == AuthMode.LOGIN) "Sign In" else "Submit Registration",
            fontWeight = FontWeight.Bold
          )
        }

        if (authMode == AuthMode.LOGIN) {
          Spacer(modifier = Modifier.height(6.dp))
          TextButton(
            onClick = { onSetAuthMode(AuthMode.FORGOT_PASSWORD) },
            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("forgot_password_button")
          ) {
            Text("Forgot Password?", style = MaterialTheme.typography.bodySmall)
          }
        }
      }
    }
  }
}
