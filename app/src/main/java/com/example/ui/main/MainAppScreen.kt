package com.example.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.UserRole
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.common.AppHeader
import com.example.ui.common.QRScannerModal
import com.example.ui.common.UserProfileScreen
import com.example.ui.commuter.CommuterEtaScreen
import com.example.ui.commuter.CommuterHomeScreen
import com.example.ui.commuter.CommuterMapScreen
import com.example.ui.driver.DriverDashboardScreen
import com.example.ui.driver.DriverPendingScreen
import com.example.ui.driver.DriverRadarScreen
import com.example.ui.driver.DriverTripScreen
import com.example.ui.viewmodel.CommuterNavTab
import com.example.ui.viewmodel.DriverNavTab
import com.example.ui.viewmodel.JeepneyViewModel

@Composable
fun MainAppScreen(
  viewModel: JeepneyViewModel,
  modifier: Modifier = Modifier
) {
  val currentUser by viewModel.currentUser.collectAsState()
  val isOnline by viewModel.isOnline.collectAsState()
  val commuterTab by viewModel.commuterTab.collectAsState()
  val driverTab by viewModel.driverTab.collectAsState()
  val authMode by viewModel.authMode.collectAsState()
  val selectedRouteId by viewModel.selectedRouteId.collectAsState()
  val showQrScanner by viewModel.showQrScanner.collectAsState()
  val userMessage by viewModel.userMessage.collectAsState()
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()

  val routes by viewModel.routes.collectAsState()
  val trips by viewModel.trips.collectAsState()
  val waitingRequests by viewModel.waitingRequests.collectAsState()
  val driverProfiles by viewModel.driverProfiles.collectAsState()
  val notifications by viewModel.notifications.collectAsState()
  val currentCommuterWaiting by viewModel.currentCommuterWaiting.collectAsState()
  val currentCommuterSession by viewModel.currentCommuterSession.collectAsState()
  val offlineQueue by viewModel.offlineQueue.collectAsState()
  val auditLogs by viewModel.auditLogs.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }
  var showNotificationsDialog by remember { mutableStateOf(false) }

  LaunchedEffect(userMessage) {
    userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearMessage()
    }
  }

  // Active Driver profile (if current user is driver)
  val currentDriverProfile = remember(currentUser, driverProfiles) {
    driverProfiles.find { it.uid == currentUser?.uid } ?: driverProfiles.firstOrNull()
  }

  // Active trip for this driver
  val activeDriverTrip = remember(trips, currentUser) {
    trips.find { it.driverId == currentUser?.uid }
  }

  // QR Scanner Modal
  if (showQrScanner) {
    QRScannerModal(
      activeTrips = trips,
      onScan = { code -> viewModel.scanQrCode(code) },
      onDismiss = { viewModel.showQrScanner(false) }
    )
  }

  // Notifications Dialog
  if (showNotificationsDialog) {
    AlertDialog(
      onDismissRequest = { showNotificationsDialog = false },
      title = { Text("Recent Alerts & Notifications") },
      text = {
        Column {
          if (notifications.isEmpty()) {
            Text("No new notifications.", style = MaterialTheme.typography.bodyMedium)
          } else {
            notifications.take(5).forEach { notif ->
              Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Text(notif.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                  Text(notif.message, style = MaterialTheme.typography.bodySmall)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showNotificationsDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  if (currentUser == null) {
    AuthScreen(
      authMode = authMode,
      routes = routes,
      onSetAuthMode = { viewModel.setAuthMode(it) },
      onQuickLogin = { role -> viewModel.login("demo@example.com", role) },
      onRegisterCommuter = { name, email -> viewModel.registerCommuter(name, email) },
      onRegisterDriver = { name, email, license, plate, seats, routeId ->
        viewModel.registerDriver(name, email, license, plate, seats, routeId)
      }
    )
  } else {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        AppHeader(
          currentUser = currentUser,
          isOnline = isOnline,
          pendingSyncCount = offlineQueue.size,
          isDarkTheme = isDarkTheme,
          unreadNotificationCount = notifications.count { !it.read },
          onToggleTheme = { viewModel.toggleTheme() },
          onToggleOnline = { viewModel.toggleOnlineStatus() },
          onForceSync = { viewModel.forceSyncNow() },
          onSwitchRole = { viewModel.switchRole(it) },
          onNotificationsClick = { showNotificationsDialog = true }
        )
      },
      bottomBar = {
        when (currentUser?.role) {
          UserRole.COMMUTER -> {
            NavigationBar(modifier = Modifier.testTag("commuter_bottom_nav")) {
              NavigationBarItem(
                selected = commuterTab == CommuterNavTab.HOME,
                onClick = { viewModel.setCommuterTab(CommuterNavTab.HOME) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                modifier = Modifier.testTag("tab_commuter_home")
              )
              NavigationBarItem(
                selected = commuterTab == CommuterNavTab.MAP,
                onClick = { viewModel.setCommuterTab(CommuterNavTab.MAP) },
                icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                label = { Text("Map") },
                modifier = Modifier.testTag("tab_commuter_map")
              )
              NavigationBarItem(
                selected = commuterTab == CommuterNavTab.ETA,
                onClick = { viewModel.setCommuterTab(CommuterNavTab.ETA) },
                icon = { Icon(Icons.Default.Timer, contentDescription = "ETA") },
                label = { Text("ETA") },
                modifier = Modifier.testTag("tab_commuter_eta")
              )
              NavigationBarItem(
                selected = commuterTab == CommuterNavTab.PROFILE,
                onClick = { viewModel.setCommuterTab(CommuterNavTab.PROFILE) },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text("Profile") },
                modifier = Modifier.testTag("tab_commuter_profile")
              )
            }
          }
          UserRole.DRIVER -> {
            // If approved, show full 4-tab bar; if pending, profile is accessible
            NavigationBar(modifier = Modifier.testTag("driver_bottom_nav")) {
              NavigationBarItem(
                selected = driverTab == DriverNavTab.DASHBOARD,
                onClick = { viewModel.setDriverTab(DriverNavTab.DASHBOARD) },
                icon = { Icon(Icons.Default.Speed, contentDescription = "Dashboard") },
                label = { Text("Dashboard") },
                modifier = Modifier.testTag("tab_driver_dashboard")
              )
              NavigationBarItem(
                selected = driverTab == DriverNavTab.RADAR,
                onClick = { viewModel.setDriverTab(DriverNavTab.RADAR) },
                icon = { Icon(Icons.Default.Sensors, contentDescription = "Radar") },
                label = { Text("Radar") },
                modifier = Modifier.testTag("tab_driver_radar")
              )
              NavigationBarItem(
                selected = driverTab == DriverNavTab.TRIP,
                onClick = { viewModel.setDriverTab(DriverNavTab.TRIP) },
                icon = { Icon(Icons.Default.DirectionsBus, contentDescription = "Trip") },
                label = { Text("Trip") },
                modifier = Modifier.testTag("tab_driver_trip")
              )
              NavigationBarItem(
                selected = driverTab == DriverNavTab.PROFILE,
                onClick = { viewModel.setDriverTab(DriverNavTab.PROFILE) },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                label = { Text("Profile") },
                modifier = Modifier.testTag("tab_driver_profile")
              )
            }
          }
          UserRole.ADMIN -> {
            NavigationBar(modifier = Modifier.testTag("admin_bottom_nav")) {
              NavigationBarItem(
                selected = true,
                onClick = {},
                icon = { Icon(Icons.Default.Speed, contentDescription = "Console") },
                label = { Text("Console") },
                modifier = Modifier.testTag("tab_admin_console")
              )
              NavigationBarItem(
                selected = false,
                onClick = { viewModel.switchRole(UserRole.COMMUTER) },
                icon = { Icon(Icons.Default.Home, contentDescription = "Commuter View") },
                label = { Text("Commuter") }
              )
              NavigationBarItem(
                selected = false,
                onClick = { viewModel.switchRole(UserRole.DRIVER) },
                icon = { Icon(Icons.Default.DirectionsBus, contentDescription = "Driver View") },
                label = { Text("Driver") }
              )
            }
          }
          null -> {}
        }
      }
    ) { innerPadding ->
      Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
        when (currentUser?.role) {
          UserRole.COMMUTER -> {
            when (commuterTab) {
              CommuterNavTab.HOME -> {
                CommuterHomeScreen(
                  currentUser = currentUser,
                  routes = routes,
                  selectedRouteId = selectedRouteId,
                  trips = trips,
                  waitingRequest = currentCommuterWaiting,
                  currentSession = currentCommuterSession,
                  onSelectRoute = { viewModel.selectRoute(it) },
                  onAlertWaiting = { routeId, stopId, count ->
                    viewModel.alertWaiting(routeId, stopId, count)
                  },
                  onCancelWaiting = { viewModel.cancelWaiting() },
                  onOpenQrScanner = { viewModel.showQrScanner(true) },
                  onDropOff = { viewModel.dropOff() },
                  onViewMapTab = { viewModel.setCommuterTab(CommuterNavTab.MAP) }
                )
              }
              CommuterNavTab.MAP -> {
                CommuterMapScreen(
                  routes = routes,
                  selectedRouteId = selectedRouteId,
                  trips = trips,
                  waitingRequests = waitingRequests,
                  userWaiting = currentCommuterWaiting,
                  onSelectRoute = { viewModel.selectRoute(it) },
                  onOpenQrScanner = { viewModel.showQrScanner(true) }
                )
              }
              CommuterNavTab.ETA -> {
                CommuterEtaScreen(
                  routes = routes,
                  selectedRouteId = selectedRouteId,
                  trips = trips,
                  userWaiting = currentCommuterWaiting
                )
              }
              CommuterNavTab.PROFILE -> {
                UserProfileScreen(
                  currentUser = currentUser,
                  isDarkTheme = isDarkTheme,
                  onToggleTheme = { viewModel.toggleTheme() },
                  onUpdateBio = { _, _, _ -> },
                  onSwitchRole = { viewModel.switchRole(it) },
                  onSignOut = { viewModel.signOut() }
                )
              }
            }
          }
          UserRole.DRIVER -> {
            // Check driver approval status
            if (currentDriverProfile?.approvalStatus != DriverApprovalStatus.APPROVED) {
              DriverPendingScreen(
                driverProfile = currentDriverProfile,
                onSwitchToAdmin = { viewModel.switchRole(UserRole.ADMIN) }
              )
            } else {
              when (driverTab) {
                DriverNavTab.DASHBOARD -> {
                  DriverDashboardScreen(
                    driverProfile = currentDriverProfile,
                    activeTrip = activeDriverTrip,
                    routes = routes,
                    waitingRequests = waitingRequests,
                    auditLogs = auditLogs,
                    onStartTrip = { viewModel.startTrip(it) },
                    onEndTrip = { viewModel.endTrip(it) },
                    onUpdateCount = { tripId, delta -> viewModel.updatePassengerCount(tripId, delta) },
                    onViewRadarTab = { viewModel.setDriverTab(DriverNavTab.RADAR) }
                  )
                }
                DriverNavTab.RADAR -> {
                  DriverRadarScreen(
                    activeTrip = activeDriverTrip,
                    waitingRequests = waitingRequests,
                    onSimulateGeofenceBoarding = { tripId, commuterId ->
                      viewModel.simulateGeofencePickUp(tripId, commuterId)
                    }
                  )
                }
                DriverNavTab.TRIP -> {
                  DriverTripScreen(
                    driverProfile = currentDriverProfile,
                    activeTrip = activeDriverTrip,
                    routes = routes,
                    onStartTrip = { viewModel.startTrip(it) },
                    onEndTrip = { viewModel.endTrip(it) }
                  )
                }
                DriverNavTab.PROFILE -> {
                  UserProfileScreen(
                    currentUser = currentUser,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleTheme() },
                    onUpdateBio = { _, _, _ -> },
                    onSwitchRole = { viewModel.switchRole(it) },
                    onSignOut = { viewModel.signOut() }
                  )
                }
              }
            }
          }
          UserRole.ADMIN -> {
            AdminDashboardScreen(
              driverProfiles = driverProfiles,
              routes = routes,
              trips = trips,
              waitingRequests = waitingRequests,
              auditLogs = auditLogs,
              onApproveDriver = { viewModel.approveDriver(it) },
              onRejectDriver = { uid, reason -> viewModel.rejectDriver(uid, reason) }
            )
          }
          null -> {}
        }
      }
    }
  }
}
