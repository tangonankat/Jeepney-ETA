package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AuditLogEntity
import com.example.data.local.OfflineEventEntity
import com.example.data.model.AppNotification
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.DriverProfile
import com.example.data.model.JeepneyRoute
import com.example.data.model.PassengerSession
import com.example.data.model.Trip
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import com.example.data.model.WaitingRequest
import com.example.data.repository.JeepneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CommuterNavTab {
  HOME,
  MAP,
  ETA,
  PROFILE
}

enum class DriverNavTab {
  RADAR,
  DASHBOARD,
  TRIP,
  PROFILE
}

enum class AdminNavTab {
  DASHBOARD,
  DRIVERS,
  ROUTES,
  LOGS
}

enum class AuthMode {
  LOGIN,
  REGISTER,
  VERIFY_EMAIL,
  FORGOT_PASSWORD
}

class JeepneyViewModel(application: Application) : AndroidViewModel(application) {
  val repository = JeepneyRepository(application)

  // Current User
  val currentUser: StateFlow<User?> = repository.currentUser
  val isOnline: StateFlow<Boolean> = repository.isOnline

  // Navigation State
  private val _commuterTab = MutableStateFlow(CommuterNavTab.HOME)
  val commuterTab: StateFlow<CommuterNavTab> = _commuterTab.asStateFlow()

  private val _driverTab = MutableStateFlow(DriverNavTab.DASHBOARD)
  val driverTab: StateFlow<DriverNavTab> = _driverTab.asStateFlow()

  private val _adminTab = MutableStateFlow(AdminNavTab.DASHBOARD)
  val adminTab: StateFlow<AdminNavTab> = _adminTab.asStateFlow()

  private val _authMode = MutableStateFlow(AuthMode.LOGIN)
  val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

  // Selected Route for filtering
  private val _selectedRouteId = MutableStateFlow<String?>("route_cub_div")
  val selectedRouteId: StateFlow<String?> = _selectedRouteId.asStateFlow()

  // QR Scanner Modal State
  private val _showQrScanner = MutableStateFlow(false)
  val showQrScanner: StateFlow<Boolean> = _showQrScanner.asStateFlow()

  // Message Toast / Snack
  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  // Dark Theme Mode
  private val _isDarkTheme = MutableStateFlow(false)
  val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

  // Data streams
  val routes: StateFlow<List<JeepneyRoute>> = repository.routes
  val trips: StateFlow<List<Trip>> = repository.trips
  val vehicles: StateFlow<List<Vehicle>> = repository.vehicles
  val waitingRequests: StateFlow<List<WaitingRequest>> = repository.waitingRequests
  val driverProfiles: StateFlow<List<DriverProfile>> = repository.driverProfiles
  val notifications: StateFlow<List<AppNotification>> = repository.notifications

  val currentCommuterWaiting: StateFlow<WaitingRequest?> = repository.currentCommuterWaiting
  val currentCommuterSession: StateFlow<PassengerSession?> = repository.currentCommuterSession

  val offlineQueue: StateFlow<List<OfflineEventEntity>> = repository.offlineQueueFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogsFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setCommuterTab(tab: CommuterNavTab) {
    _commuterTab.value = tab
  }

  fun setDriverTab(tab: DriverNavTab) {
    _driverTab.value = tab
  }

  fun setAdminTab(tab: AdminNavTab) {
    _adminTab.value = tab
  }

  fun setAuthMode(mode: AuthMode) {
    _authMode.value = mode
  }

  fun selectRoute(routeId: String) {
    _selectedRouteId.value = routeId
  }

  fun toggleTheme() {
    _isDarkTheme.value = !_isDarkTheme.value
  }

  fun toggleOnlineStatus() {
    val newStatus = !isOnline.value
    repository.toggleOnlineMode(newStatus)
    _userMessage.value = if (newStatus) "Connected to server. Auto-syncing queue..." else "Offline mode active. Passenger events will queue locally."
  }

  fun forceSyncNow() {
    repository.syncOfflineQueueToServer()
    _userMessage.value = "Synced offline event queue."
  }

  fun clearMessage() {
    _userMessage.value = null
  }

  fun showQrScanner(show: Boolean) {
    _showQrScanner.value = show
  }

  // Auth Operations
  fun login(email: String, role: UserRole) {
    repository.switchRoleForDemo(role)
    _userMessage.value = "Signed in as ${role.name.lowercase()}."
  }

  fun registerCommuter(name: String, email: String) {
    val user = User(
      uid = "usr_${System.currentTimeMillis() % 10000}",
      fullName = name,
      email = email,
      role = UserRole.COMMUTER
    )
    repository.registerUser(user)
    _userMessage.value = "Registration successful! Welcome to Para! Jeepney."
  }

  fun registerDriver(
    name: String,
    email: String,
    license: String,
    plate: String,
    seats: Int,
    routeId: String
  ) {
    val uid = "drv_${System.currentTimeMillis() % 10000}"
    val user = User(
      uid = uid,
      fullName = name,
      email = email,
      role = UserRole.DRIVER
    )
    val driverProfile = DriverProfile(
      uid = uid,
      driverName = name,
      licenseNumber = license,
      vehiclePlate = plate,
      vehicleBodyNumber = "JEEP-${plate.takeLast(4)}",
      vehicleId = "veh_${plate.takeLast(4)}",
      seatCapacity = seats,
      assignedRouteId = routeId,
      assignedRouteName = routes.value.find { it.id == routeId }?.name ?: "Selected Route",
      approvalStatus = DriverApprovalStatus.PENDING
    )
    repository.registerUser(user, driverProfile)
    _userMessage.value = "Driver credentials submitted for Admin review. Approval pending."
  }

  fun signOut() {
    repository.signOut()
    _userMessage.value = "Signed out."
  }

  fun switchRole(role: UserRole) {
    repository.switchRoleForDemo(role)
    _userMessage.value = "Switched to ${role.name} mode."
  }

  // Commuter Actions
  fun alertWaiting(routeId: String, stopId: String, count: Int = 1) {
    repository.requestWaiting(routeId, stopId, count)
    _userMessage.value = "Waiting alert sent to drivers on this route!"
  }

  fun cancelWaiting() {
    repository.cancelWaiting()
    _userMessage.value = "Waiting request cancelled."
  }

  fun scanQrCode(qrToken: String) {
    val result = repository.scanQrToBoard(qrToken)
    result.onSuccess {
      _showQrScanner.value = false
      _userMessage.value = "Boarding confirmed! You are now onboard."
    }.onFailure { err ->
      _userMessage.value = "Boarding Failed: ${err.message}"
    }
  }

  fun dropOff() {
    repository.requestDropOff()
    _userMessage.value = "Para! Safely dropped off. Passenger count updated."
  }

  // Driver Actions
  fun startTrip(routeId: String) {
    val user = currentUser.value ?: return
    repository.startTrip(user.uid, routeId)
    _userMessage.value = "Trip started! Real-time GPS location tracking is now active."
  }

  fun endTrip(tripId: String) {
    repository.endTrip(tripId)
    _userMessage.value = "Trip completed."
  }

  fun updatePassengerCount(tripId: String, delta: Int) {
    repository.updatePassengerCountManual(tripId, delta)
  }

  // Geofence Simulation
  fun simulateGeofencePickUp(tripId: String, commuterId: String) {
    repository.simulateGeofencingBoarding(tripId, commuterId)
    _userMessage.value = "Geofence co-location trigger: Commuter boarded automatically."
  }

  // Admin Actions
  fun approveDriver(driverUid: String) {
    val adminUid = currentUser.value?.uid ?: "admin_sys"
    repository.approveDriver(driverUid, adminUid)
    _userMessage.value = "Driver approved! Driver can now operate trips."
  }

  fun rejectDriver(driverUid: String, reason: String) {
    repository.rejectDriver(driverUid, reason)
    _userMessage.value = "Driver application rejected."
  }
}
