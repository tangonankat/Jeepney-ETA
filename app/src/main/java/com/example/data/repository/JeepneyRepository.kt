package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.AuditLogEntity
import com.example.data.local.OfflineEventEntity
import com.example.data.local.TripCacheEntity
import com.example.data.model.AppNotification
import com.example.data.model.AuditLog
import com.example.data.model.DriverApprovalStatus
import com.example.data.model.DriverProfile
import com.example.data.model.GeoCoord
import com.example.data.model.JeepneyRoute
import com.example.data.model.PassengerSession
import com.example.data.model.PassengerSessionStatus
import com.example.data.model.RouteStop
import com.example.data.model.Trip
import com.example.data.model.TripStatus
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.Vehicle
import com.example.data.model.WaitingRequest
import com.example.data.model.WaitingStatus
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JeepneyRepository(context: Context) {
  private val scope = CoroutineScope(Dispatchers.IO)
  private val database: AppDatabase = Room.databaseBuilder(
    context.applicationContext,
    AppDatabase::class.java,
    "jeepney_offline.db"
  ).fallbackToDestructiveMigration().build()

  private val offlineDao = database.offlineDao()

  // Connectivity State (User can toggle to test offline behaviors as in PDF Page 7 & 8)
  private val _isOnline = MutableStateFlow(true)
  val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

  val offlineQueueFlow: Flow<List<OfflineEventEntity>> = offlineDao.getAllQueuedEventsFlow()
  val auditLogsFlow: Flow<List<AuditLogEntity>> = offlineDao.getAuditLogsFlow()

  // Current Auth State
  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  // Routes
  private val _routes = MutableStateFlow<List<JeepneyRoute>>(emptyList())
  val routes: StateFlow<List<JeepneyRoute>> = _routes.asStateFlow()

  // Vehicles
  private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
  val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

  // Trips
  private val _trips = MutableStateFlow<List<Trip>>(emptyList())
  val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

  // Waiting Requests
  private val _waitingRequests = MutableStateFlow<List<WaitingRequest>>(emptyList())
  val waitingRequests: StateFlow<List<WaitingRequest>> = _waitingRequests.asStateFlow()

  // Passenger Sessions
  private val _passengerSessions = MutableStateFlow<List<PassengerSession>>(emptyList())
  val passengerSessions: StateFlow<List<PassengerSession>> = _passengerSessions.asStateFlow()

  // Driver Profiles
  private val _driverProfiles = MutableStateFlow<List<DriverProfile>>(emptyList())
  val driverProfiles: StateFlow<List<DriverProfile>> = _driverProfiles.asStateFlow()

  // Notifications
  private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
  val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

  // Current Commuter Active Waiting / Ride
  private val _currentCommuterWaiting = MutableStateFlow<WaitingRequest?>(null)
  val currentCommuterWaiting: StateFlow<WaitingRequest?> = _currentCommuterWaiting.asStateFlow()

  private val _currentCommuterSession = MutableStateFlow<PassengerSession?>(null)
  val currentCommuterSession: StateFlow<PassengerSession?> = _currentCommuterSession.asStateFlow()

  init {
    initializeSeedData()
    startLocationSimulationLoop()
  }

  private fun initializeSeedData() {
    val cubaoStops = listOf(
      RouteStop("cub_1", "Gateway Mall Cubao", 14.6191, 121.0526, 0, "Aurora Blvd Terminal"),
      RouteStop("cub_2", "Betty Go-Belmonte", 14.6180, 121.0425, 1, "LRT-2 Station"),
      RouteStop("cub_3", "Broadway Centrum", 14.6155, 121.0330, 2, "Aurora Blvd"),
      RouteStop("cub_4", "Gilmore IT Center", 14.6134, 121.0267, 3, "Cyberzone Corner"),
      RouteStop("cub_5", "V. Mapa", 14.6041, 121.0175, 4, "Magsaysay Blvd"),
      RouteStop("cub_6", "Pureza / PUP", 14.5990, 121.0050, 5, "Sta. Mesa"),
      RouteStop("cub_7", "Legarda San Beda", 14.5985, 120.9940, 6, "University Belt"),
      RouteStop("cub_8", "Recto Isetann", 14.6033, 120.9840, 7, "Avenida Junction"),
      RouteStop("cub_9", "Tutuban Center Divisoria", 14.6062, 120.9715, 8, "Divisoria Wholesale")
    )

    val monumentoStops = listOf(
      RouteStop("mon_1", "Monumento Circle", 14.6575, 120.9836, 0, "Bonifacio Monument"),
      RouteStop("mon_2", "5th Avenue Caloocan", 14.6468, 120.9839, 1, "Rizal Ave Ext"),
      RouteStop("mon_3", "Tayuman Junction", 14.6205, 120.9829, 2, "SM San Lazaro access"),
      RouteStop("mon_4", "Doroteo Jose / Recto", 14.6052, 120.9818, 3, "LRT Transfer"),
      RouteStop("mon_5", "Manila City Hall", 14.5896, 120.9815, 4, "Padre Burgos"),
      RouteStop("mon_6", "Pedro Gil Robinson", 14.5772, 120.9880, 5, "Ermita Taft"),
      RouteStop("mon_7", "Buendia Gil Puyat", 14.5542, 120.9972, 6, "Pasay Bus Terminal"),
      RouteStop("mon_8", "Baclaran Church", 14.5312, 120.9935, 7, "Redemptorist Terminal")
    )

    val commonwealthStops = listOf(
      RouteStop("com_1", "Fairview Center Mall", 14.7073, 121.0664, 0, "Commonwealth North"),
      RouteStop("com_2", "Commonwealth Market", 14.6852, 121.0775, 1, "Batasan Overpass"),
      RouteStop("com_3", "Philcoa Terminal", 14.6534, 121.0535, 2, "Masaya St Entrance"),
      RouteStop("com_4", "Quezon Memorial Circle", 14.6515, 121.0492, 3, "Elliptical Road"),
      RouteStop("com_5", "Fisher Mall Q. Ave", 14.6368, 121.0180, 4, "Roosevelt Junction"),
      RouteStop("com_6", "Welcome Rotonda", 14.6202, 121.0012, 5, "Mabuhay Rotonda"),
      RouteStop("com_7", "UST España Blvd", 14.6080, 120.9902, 6, "Arch of the Centuries"),
      RouteStop("com_8", "Quiapo Church", 14.5986, 120.9837, 7, "Plaza Miranda Terminal")
    )

    val updStops = listOf(
      RouteStop("upd_1", "Philcoa Terminal", 14.6534, 121.0535, 0, "Overpass Jeep Stop"),
      RouteStop("upd_2", "CP Garcia Gate", 14.6548, 121.0601, 1, "Fine Arts / CHE"),
      RouteStop("upd_3", "Vinzons Hall", 14.6540, 121.0722, 2, "Student Center"),
      RouteStop("upd_4", "Palma Hall (AS)", 14.6528, 121.0690, 3, "College of Arts & Sciences"),
      RouteStop("upd_5", "UP Sunken Garden", 14.6518, 121.0673, 4, "Grandstand"),
      RouteStop("upd_6", "UP Shopping Center", 14.6565, 121.0694, 5, "Laurel St Canteen"),
      RouteStop("upd_7", "College of Science", 14.6495, 121.0690, 6, "NSRI Complex")
    )

    val seedRoutes = listOf(
      JeepneyRoute(
        id = "route_cub_div",
        name = "Cubao - Divisoria via Aurora / Recto",
        code = "CUB-DIV",
        origin = "Cubao Araneta",
        destination = "Tutuban Divisoria",
        fareRegular = 15.00,
        fareDiscounted = 12.00,
        stops = cubaoStops,
        polylinePoints = cubaoStops.map { GeoCoord(it.latitude, it.longitude, it.name) }
      ),
      JeepneyRoute(
        id = "route_mon_bac",
        name = "Monumento - Baclaran via Taft",
        code = "MON-BAC",
        origin = "Monumento Circle",
        destination = "Baclaran",
        fareRegular = 18.00,
        fareDiscounted = 14.50,
        stops = monumentoStops,
        polylinePoints = monumentoStops.map { GeoCoord(it.latitude, it.longitude, it.name) }
      ),
      JeepneyRoute(
        id = "route_com_qpo",
        name = "Commonwealth - Quiapo via Q. Ave",
        code = "COM-QPO",
        origin = "Fairview / Commonwealth",
        destination = "Quiapo Church",
        fareRegular = 17.00,
        fareDiscounted = 13.50,
        stops = commonwealthStops,
        polylinePoints = commonwealthStops.map { GeoCoord(it.latitude, it.longitude, it.name) }
      ),
      JeepneyRoute(
        id = "route_upd_phi",
        name = "UP Diliman Campus - Philcoa",
        code = "UPD-PHI",
        origin = "Philcoa",
        destination = "UP Sunken Garden",
        fareRegular = 13.00,
        fareDiscounted = 11.00,
        stops = updStops,
        polylinePoints = updStops.map { GeoCoord(it.latitude, it.longitude, it.name) }
      )
    )
    _routes.value = seedRoutes

    val seedVehicles = listOf(
      Vehicle("veh_1", "NCF-1042", "JEEP-CUB-01", 18, "route_cub_div", "drv_dante", "QR-JEEP-CUB-01"),
      Vehicle("veh_2", "NDB-8831", "JEEP-MON-02", 20, "route_mon_bac", "drv_tomas", "QR-JEEP-MON-02"),
      Vehicle("veh_3", "NFA-5620", "JEEP-COM-03", 16, "route_com_qpo", "drv_marlon", "QR-JEEP-COM-03"),
      Vehicle("veh_4", "NQK-4209", "JEEP-UPD-04", 14, "route_upd_phi", "drv_pedro", "QR-JEEP-UPD-04")
    )
    _vehicles.value = seedVehicles

    val seedDrivers = listOf(
      DriverProfile(
        uid = "drv_dante",
        driverName = "Dante 'Kuya Dan' Ramos",
        licenseNumber = "N02-14-089234",
        licenseDocumentUrl = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c",
        vehicleRegistrationUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957",
        vehiclePlate = "NCF-1042",
        vehicleBodyNumber = "JEEP-CUB-01",
        vehicleId = "veh_1",
        seatCapacity = 18,
        assignedRouteId = "route_cub_div",
        assignedRouteName = "Cubao - Divisoria",
        approvalStatus = DriverApprovalStatus.APPROVED,
        approvedBy = "admin_reyes",
        approvedAt = System.currentTimeMillis() - 86400000L
      ),
      DriverProfile(
        uid = "drv_cardo",
        driverName = "Ricardo 'Cardo' Dalisay",
        licenseNumber = "N01-19-450198",
        licenseDocumentUrl = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c",
        vehicleRegistrationUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957",
        vehiclePlate = "NBB-9321",
        vehicleBodyNumber = "JEEP-CUB-05",
        vehicleId = "veh_pending_1",
        seatCapacity = 16,
        assignedRouteId = "route_cub_div",
        assignedRouteName = "Cubao - Divisoria",
        approvalStatus = DriverApprovalStatus.PENDING,
        createdAt = System.currentTimeMillis() - 7200000L
      ),
      DriverProfile(
        uid = "drv_marlon",
        driverName = "Marlon Fernandez",
        licenseNumber = "N03-16-778812",
        licenseDocumentUrl = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c",
        vehicleRegistrationUrl = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957",
        vehiclePlate = "NFA-5620",
        vehicleBodyNumber = "JEEP-COM-03",
        vehicleId = "veh_3",
        seatCapacity = 16,
        assignedRouteId = "route_com_qpo",
        assignedRouteName = "Commonwealth - Quiapo",
        approvalStatus = DriverApprovalStatus.APPROVED,
        approvedBy = "admin_reyes",
        approvedAt = System.currentTimeMillis() - 43200000L
      )
    )
    _driverProfiles.value = seedDrivers

    // Active Trips
    val seedTrips = listOf(
      Trip(
        id = "trip_cub_1",
        driverId = "drv_dante",
        driverName = "Dante 'Kuya Dan' Ramos",
        vehicleId = "veh_1",
        plateNumber = "NCF-1042",
        routeId = "route_cub_div",
        routeName = "Cubao - Divisoria",
        status = TripStatus.ACTIVE,
        startTime = System.currentTimeMillis() - 15 * 60 * 1000L,
        passengerCount = 12,
        seatCapacity = 18,
        currentLatitude = 14.6155, // Near Broadway Centrum
        currentLongitude = 121.0330,
        currentSpeedKph = 24.5,
        currentStopIndex = 2
      ),
      Trip(
        id = "trip_mon_1",
        driverId = "drv_tomas",
        driverName = "Tomas Dela Cruz",
        vehicleId = "veh_2",
        plateNumber = "NDB-8831",
        routeId = "route_mon_bac",
        routeName = "Monumento - Baclaran",
        status = TripStatus.ACTIVE,
        startTime = System.currentTimeMillis() - 25 * 60 * 1000L,
        passengerCount = 19,
        seatCapacity = 20,
        currentLatitude = 14.6052, // Near Doroteo Jose
        currentLongitude = 120.9818,
        currentSpeedKph = 18.0,
        currentStopIndex = 3
      ),
      Trip(
        id = "trip_com_1",
        driverId = "drv_marlon",
        driverName = "Marlon Fernandez",
        vehicleId = "veh_3",
        plateNumber = "NFA-5620",
        routeId = "route_com_qpo",
        routeName = "Commonwealth - Quiapo",
        status = TripStatus.ACTIVE,
        startTime = System.currentTimeMillis() - 10 * 60 * 1000L,
        passengerCount = 16, // FULL
        seatCapacity = 16,
        currentLatitude = 14.6534, // Near Philcoa
        currentLongitude = 121.0535,
        currentSpeedKph = 20.0,
        currentStopIndex = 2
      )
    )
    _trips.value = seedTrips

    // Waiting Requests from commuters
    val seedWaiting = listOf(
      WaitingRequest(
        id = "wait_maria",
        commuterId = "usr_maria",
        commuterName = "Maria Santos",
        routeId = "route_cub_div",
        routeName = "Cubao - Divisoria",
        stopId = "cub_4",
        stopName = "Gilmore IT Center",
        latitude = 14.6134,
        longitude = 121.0267,
        passengerCount = 1,
        status = WaitingStatus.WAITING
      ),
      WaitingRequest(
        id = "wait_josh",
        commuterId = "usr_josh",
        commuterName = "Joshua Cruz",
        routeId = "route_cub_div",
        routeName = "Cubao - Divisoria",
        stopId = "cub_5",
        stopName = "V. Mapa",
        latitude = 14.6041,
        longitude = 121.0175,
        passengerCount = 2,
        status = WaitingStatus.WAITING
      ),
      WaitingRequest(
        id = "wait_bea",
        commuterId = "usr_bea",
        commuterName = "Bea Gomez",
        routeId = "route_cub_div",
        routeName = "Cubao - Divisoria",
        stopId = "cub_3",
        stopName = "Broadway Centrum",
        latitude = 14.6155,
        longitude = 121.0330,
        passengerCount = 1,
        status = WaitingStatus.WAITING
      )
    )
    _waitingRequests.value = seedWaiting

    // Default logged-in user: Commuter Maria Santos
    val defaultCommuter = User(
      uid = "usr_maria",
      fullName = "Maria Santos",
      email = "maria.santos@commuter.ph",
      role = UserRole.COMMUTER,
      bio = "Daily commuter from Cubao to V. Mapa",
      emailVerified = true
    )
    _currentUser.value = defaultCommuter
    _currentCommuterWaiting.value = seedWaiting.find { it.commuterId == defaultCommuter.uid }
  }

  // --- Network / Offline Simulation ---
  fun toggleOnlineMode(online: Boolean) {
    _isOnline.value = online
    if (online) {
      syncOfflineQueueToServer()
    }
  }

  fun syncOfflineQueueToServer() {
    scope.launch {
      val pending = offlineDao.getPendingEvents()
      if (pending.isNotEmpty()) {
        for (event in pending) {
          // Process event into server state
          when (event.eventType) {
            "MANUAL_COUNT_CHANGE" -> {
              applyTripCountChange(event.tripId, event.newCount, isSynced = true)
            }
            "TRIP_START" -> {
              _trips.value = _trips.value.map {
                if (it.id == event.tripId) it.copy(status = TripStatus.ACTIVE) else it
              }
            }
            "TRIP_END" -> {
              _trips.value = _trips.value.map {
                if (it.id == event.tripId) it.copy(status = TripStatus.COMPLETED) else it
              }
            }
          }
          offlineDao.updateOfflineEvent(event.copy(synced = true))
        }
        // Notify user
        addNotification(
          userId = _currentUser.value?.uid ?: "",
          title = "Offline Sync Completed",
          message = "Synced ${pending.size} queued passenger & trip events to server.",
          type = "SYSTEM"
        )
      }
    }
  }

  // --- User / Role Management ---
  fun signIn(user: User) {
    _currentUser.value = user
    if (user.role == UserRole.COMMUTER) {
      _currentCommuterWaiting.value = _waitingRequests.value.find { it.commuterId == user.uid }
      _currentCommuterSession.value = _passengerSessions.value.find { it.commuterId == user.uid && it.status == PassengerSessionStatus.ONBOARD }
    }
  }

  fun signOut() {
    _currentUser.value = null
    _currentCommuterWaiting.value = null
    _currentCommuterSession.value = null
  }

  fun registerUser(user: User, driverProfile: DriverProfile? = null): Boolean {
    _currentUser.value = user
    if (driverProfile != null) {
      _driverProfiles.value = _driverProfiles.value + driverProfile
    }
    return true
  }

  fun updateUserProfile(fullName: String, bio: String, notificationEnabled: Boolean, themeMode: String) {
    _currentUser.value = _currentUser.value?.copy(
      fullName = fullName,
      bio = bio,
      notificationEnabled = notificationEnabled,
      themeMode = themeMode,
      updatedAt = System.currentTimeMillis()
    )
  }

  // Quick switch role for testing all facets of the application
  fun switchRoleForDemo(role: UserRole) {
    when (role) {
      UserRole.COMMUTER -> {
        val user = User(
          uid = "usr_maria",
          fullName = "Maria Santos",
          email = "maria.santos@commuter.ph",
          role = UserRole.COMMUTER,
          bio = "Daily Commuter - LRT/Jeepney",
          emailVerified = true
        )
        signIn(user)
      }
      UserRole.DRIVER -> {
        val user = User(
          uid = "drv_dante",
          fullName = "Dante 'Kuya Dan' Ramos",
          email = "dante.ramos@driver.ph",
          role = UserRole.DRIVER,
          bio = "Jeepney Driver 12 yrs, Cubao-Divisoria",
          emailVerified = true
        )
        signIn(user)
      }
      UserRole.ADMIN -> {
        val user = User(
          uid = "admin_reyes",
          fullName = "Officer Ronald Reyes",
          email = "admin@ltfrb.transit.gov.ph",
          role = UserRole.ADMIN,
          bio = "Metro Manila Fleet Admin & Inspector",
          emailVerified = true
        )
        signIn(user)
      }
    }
  }

  // --- Commuter Operations ---
  fun requestWaiting(routeId: String, stopId: String, passengerCount: Int = 1) {
    val user = _currentUser.value ?: return
    val route = _routes.value.find { it.id == routeId } ?: return
    val stop = route.stops.find { it.id == stopId } ?: route.stops.first()

    val request = WaitingRequest(
      id = "wait_${UUID.randomUUID().toString().take(8)}",
      commuterId = user.uid,
      commuterName = user.fullName,
      routeId = route.id,
      routeName = route.name,
      stopId = stop.id,
      stopName = stop.name,
      latitude = stop.latitude,
      longitude = stop.longitude,
      passengerCount = passengerCount,
      status = WaitingStatus.WAITING
    )

    _waitingRequests.value = _waitingRequests.value.filter { it.commuterId != user.uid } + request
    _currentCommuterWaiting.value = request

    addNotification(
      userId = user.uid,
      title = "Waiting Alert Sent",
      message = "Drivers on ${route.code} have been alerted you're waiting at ${stop.name}.",
      type = "ARRIVAL"
    )
  }

  fun cancelWaiting() {
    val current = _currentCommuterWaiting.value ?: return
    _waitingRequests.value = _waitingRequests.value.map {
      if (it.id == current.id) it.copy(status = WaitingStatus.CANCELLED) else it
    }
    _currentCommuterWaiting.value = null
  }

  // QR Boarding validation
  fun scanQrToBoard(qrCode: String): Result<PassengerSession> {
    val user = _currentUser.value ?: return Result.failure(Exception("Not logged in"))

    // Check vehicle match
    val vehicle = _vehicles.value.find { it.qrToken.equals(qrCode.trim(), ignoreCase = true) || it.bodyNumber.equals(qrCode.trim(), ignoreCase = true) }
      ?: return Result.failure(Exception("Invalid QR code. No active jeepney found matching '$qrCode'."))

    val activeTrip = _trips.value.find { it.vehicleId == vehicle.id && it.status == TripStatus.ACTIVE }
      ?: return Result.failure(Exception("Jeepney ${vehicle.bodyNumber} has no active trip right now."))

    if (activeTrip.passengerCount >= activeTrip.seatCapacity) {
      return Result.failure(Exception("Jeepney is already at maximum capacity (${activeTrip.seatCapacity} seats full)."))
    }

    // Check duplicate
    val existing = _passengerSessions.value.find { it.commuterId == user.uid && it.tripId == activeTrip.id && it.status == PassengerSessionStatus.ONBOARD }
    if (existing != null) {
      return Result.failure(Exception("You are already checked-in onboard this jeepney!"))
    }

    val waiting = _currentCommuterWaiting.value

    val session = PassengerSession(
      id = "sess_${UUID.randomUUID().toString().take(8)}",
      commuterId = user.uid,
      commuterName = user.fullName,
      tripId = activeTrip.id,
      vehicleId = vehicle.id,
      routeId = activeTrip.routeId,
      boardedAt = System.currentTimeMillis(),
      boardedLatitude = activeTrip.currentLatitude,
      boardedLongitude = activeTrip.currentLongitude,
      qrScanned = true,
      status = PassengerSessionStatus.ONBOARD
    )

    _passengerSessions.value = _passengerSessions.value + session
    _currentCommuterSession.value = session

    // Update waiting request to BOARDED
    if (waiting != null) {
      _waitingRequests.value = _waitingRequests.value.map {
        if (it.id == waiting.id) it.copy(status = WaitingStatus.ONBOARD, matchedTripId = activeTrip.id) else it
      }
      _currentCommuterWaiting.value = null
    }

    // Increase jeepney passenger count
    incrementPassengerCount(activeTrip.id, reason = "Commuter QR Boarding: ${user.fullName}")

    addNotification(
      userId = user.uid,
      title = "Boarding Confirmed!",
      message = "Welcome aboard Jeepney ${vehicle.bodyNumber}! Have a safe trip.",
      type = "BOARDING"
    )

    return Result.success(session)
  }

  // Commuter Drop-off
  fun requestDropOff() {
    val session = _currentCommuterSession.value ?: return
    val trip = _trips.value.find { it.id == session.tripId }

    val updatedSession = session.copy(
      dropOffAt = System.currentTimeMillis(),
      dropOffLatitude = trip?.currentLatitude,
      dropOffLongitude = trip?.currentLongitude,
      status = PassengerSessionStatus.DROPPED_OFF
    )

    _passengerSessions.value = _passengerSessions.value.map {
      if (it.id == session.id) updatedSession else it
    }
    _currentCommuterSession.value = null

    if (trip != null) {
      decrementPassengerCount(trip.id, reason = "Commuter dropped off: ${session.commuterName}")
    }

    addNotification(
      userId = session.commuterId,
      title = "Para! Drop-off Completed",
      message = "You have safely alighted from the jeepney. Thank you!",
      type = "DROP_OFF"
    )
  }

  // --- Driver Operations ---
  fun getDriverActiveTrip(driverId: String): Trip? {
    return _trips.value.find { it.driverId == driverId && it.status == TripStatus.ACTIVE }
  }

  fun startTrip(driverId: String, routeId: String): Trip {
    val driver = _driverProfiles.value.find { it.uid == driverId }
    val route = _routes.value.find { it.id == routeId } ?: _routes.value.first()
    val firstStop = route.stops.first()

    val newTrip = Trip(
      id = "trip_${UUID.randomUUID().toString().take(8)}",
      driverId = driverId,
      driverName = driver?.driverName ?: "Driver",
      vehicleId = driver?.vehicleId ?: "veh_1",
      plateNumber = driver?.vehiclePlate ?: "NCF-1042",
      routeId = route.id,
      routeName = route.name,
      status = TripStatus.ACTIVE,
      startTime = System.currentTimeMillis(),
      passengerCount = 0,
      seatCapacity = driver?.seatCapacity ?: 18,
      currentLatitude = firstStop.latitude,
      currentLongitude = firstStop.longitude,
      currentSpeedKph = 20.0,
      currentStopIndex = 0
    )

    _trips.value = _trips.value + newTrip

    logAuditEvent(
      eventType = "TRIP_START",
      tripId = newTrip.id,
      driverId = driverId,
      previousValue = "IDLE",
      newValue = "ACTIVE",
      description = "Started trip on route ${route.code}"
    )

    if (!_isOnline.value) {
      queueOfflineEvent(
        eventType = "TRIP_START",
        tripId = newTrip.id,
        driverId = driverId,
        previousCount = 0,
        newCount = 0,
        note = "Started trip offline"
      )
    }

    return newTrip
  }

  fun endTrip(tripId: String) {
    val trip = _trips.value.find { it.id == tripId } ?: return
    _trips.value = _trips.value.map {
      if (it.id == tripId) it.copy(status = TripStatus.COMPLETED, endTime = System.currentTimeMillis()) else it
    }

    logAuditEvent(
      eventType = "TRIP_END",
      tripId = tripId,
      driverId = trip.driverId,
      previousValue = "ACTIVE",
      newValue = "COMPLETED",
      description = "Completed trip on ${trip.routeName} with final count ${trip.passengerCount}"
    )

    if (!_isOnline.value) {
      queueOfflineEvent(
        eventType = "TRIP_END",
        tripId = tripId,
        driverId = trip.driverId,
        previousCount = trip.passengerCount,
        newCount = trip.passengerCount,
        note = "Ended trip offline"
      )
    }
  }

  // Driver Manual Passenger Count (+ / -) with mandatory audit logging
  fun updatePassengerCountManual(tripId: String, delta: Int) {
    val trip = _trips.value.find { it.id == tripId } ?: return
    val newCount = (trip.passengerCount + delta).coerceIn(0, trip.seatCapacity)
    if (newCount == trip.passengerCount) return

    val prevCount = trip.passengerCount
    applyTripCountChange(tripId, newCount, isSynced = _isOnline.value)

    logAuditEvent(
      eventType = "MANUAL_COUNT_CHANGE",
      tripId = tripId,
      driverId = trip.driverId,
      previousValue = "$prevCount",
      newValue = "$newCount",
      description = "Driver manual adjustment (${if (delta > 0) "+$delta" else "$delta"})"
    )

    if (!_isOnline.value) {
      queueOfflineEvent(
        eventType = "MANUAL_COUNT_CHANGE",
        tripId = tripId,
        driverId = trip.driverId,
        previousCount = prevCount,
        newCount = newCount,
        note = "Driver manual counter adjusted offline"
      )
    }
  }

  private fun incrementPassengerCount(tripId: String, reason: String) {
    val trip = _trips.value.find { it.id == tripId } ?: return
    if (trip.passengerCount >= trip.seatCapacity) return
    val newCount = trip.passengerCount + 1
    applyTripCountChange(tripId, newCount, isSynced = _isOnline.value)

    logAuditEvent(
      eventType = "PASSENGER_ADD",
      tripId = tripId,
      driverId = trip.driverId,
      previousValue = "${trip.passengerCount}",
      newValue = "$newCount",
      description = reason
    )
  }

  private fun decrementPassengerCount(tripId: String, reason: String) {
    val trip = _trips.value.find { it.id == tripId } ?: return
    if (trip.passengerCount <= 0) return
    val newCount = trip.passengerCount - 1
    applyTripCountChange(tripId, newCount, isSynced = _isOnline.value)

    logAuditEvent(
      eventType = "PASSENGER_DROP",
      tripId = tripId,
      driverId = trip.driverId,
      previousValue = "${trip.passengerCount}",
      newValue = "$newCount",
      description = reason
    )
  }

  private fun applyTripCountChange(tripId: String, newCount: Int, isSynced: Boolean) {
    _trips.value = _trips.value.map {
      if (it.id == tripId) {
        it.copy(
          passengerCount = newCount,
          availableSeats = (it.seatCapacity - newCount).coerceAtLeast(0),
          isFull = newCount >= it.seatCapacity
        )
      } else it
    }

    // Also update local Room trip cache
    scope.launch {
      val t = _trips.value.find { it.id == tripId }
      if (t != null) {
        offlineDao.saveTripCache(
          TripCacheEntity(
            tripId = t.id,
            driverId = t.driverId,
            routeId = t.routeId,
            passengerCount = t.passengerCount,
            seatCapacity = t.seatCapacity,
            status = t.status.name
          )
        )
      }
    }
  }

  private fun queueOfflineEvent(
    eventType: String,
    tripId: String,
    driverId: String,
    previousCount: Int,
    newCount: Int,
    note: String
  ) {
    scope.launch {
      offlineDao.insertOfflineEvent(
        OfflineEventEntity(
          id = UUID.randomUUID().toString(),
          eventType = eventType,
          tripId = tripId,
          driverId = driverId,
          previousCount = previousCount,
          newCount = newCount,
          note = note,
          timestamp = System.currentTimeMillis(),
          synced = false
        )
      )
    }
  }

  private fun logAuditEvent(
    eventType: String,
    tripId: String,
    driverId: String,
    previousValue: String,
    newValue: String,
    description: String
  ) {
    scope.launch {
      offlineDao.insertAuditLog(
        AuditLogEntity(
          id = UUID.randomUUID().toString(),
          eventType = eventType,
          tripId = tripId,
          driverId = driverId,
          previousValue = previousValue,
          newValue = newValue,
          description = description,
          timestamp = System.currentTimeMillis(),
          synced = _isOnline.value
        )
      )
    }
  }

  // --- Automatic Geofencing Passenger Detection (PDF Page 7) ---
  // If commuter and driver are co-located at stop for a time window -> automatic boarding candidate
  fun simulateGeofencingBoarding(tripId: String, commuterId: String) {
    val trip = _trips.value.find { it.id == tripId } ?: return
    val commuter = _waitingRequests.value.find { it.commuterId == commuterId && it.status == WaitingStatus.WAITING } ?: return

    if (trip.passengerCount >= trip.seatCapacity) return

    val session = PassengerSession(
      id = "sess_auto_${UUID.randomUUID().toString().take(6)}",
      commuterId = commuter.commuterId,
      commuterName = commuter.commuterName,
      tripId = trip.id,
      vehicleId = trip.vehicleId,
      routeId = trip.routeId,
      boardedAt = System.currentTimeMillis(),
      boardedLatitude = trip.currentLatitude,
      boardedLongitude = trip.currentLongitude,
      qrScanned = false,
      status = PassengerSessionStatus.ONBOARD
    )

    _passengerSessions.value = _passengerSessions.value + session
    _waitingRequests.value = _waitingRequests.value.map {
      if (it.id == commuter.id) it.copy(status = WaitingStatus.ONBOARD, matchedTripId = trip.id) else it
    }

    incrementPassengerCount(trip.id, reason = "Auto Geofence Proximity: ${commuter.commuterName} boarded")

    addNotification(
      userId = commuter.commuterId,
      title = "Automatic Boarding Detected",
      message = "Geofence confirmed you boarded ${trip.plateNumber} at ${commuter.stopName}.",
      type = "BOARDING"
    )
  }

  // --- Admin Operations ---
  fun approveDriver(driverUid: String, adminUid: String) {
    _driverProfiles.value = _driverProfiles.value.map {
      if (it.uid == driverUid) {
        it.copy(
          approvalStatus = DriverApprovalStatus.APPROVED,
          approvedBy = adminUid,
          approvedAt = System.currentTimeMillis()
        )
      } else it
    }

    logAuditEvent(
      eventType = "DRIVER_APPROVED",
      tripId = "N/A",
      driverId = driverUid,
      previousValue = "PENDING",
      newValue = "APPROVED",
      description = "Admin $adminUid approved driver credentials"
    )

    addNotification(
      userId = driverUid,
      title = "Driver Account Approved!",
      message = "Your driver license and jeepney registration have been verified. You may now start trips and view the Radar.",
      type = "APPROVAL"
    )
  }

  fun rejectDriver(driverUid: String, reason: String) {
    _driverProfiles.value = _driverProfiles.value.map {
      if (it.uid == driverUid) {
        it.copy(
          approvalStatus = DriverApprovalStatus.REJECTED,
          rejectionReason = reason
        )
      } else it
    }

    logAuditEvent(
      eventType = "DRIVER_REJECTED",
      tripId = "N/A",
      driverId = driverUid,
      previousValue = "PENDING",
      newValue = "REJECTED",
      description = "Rejected reason: $reason"
    )

    addNotification(
      userId = driverUid,
      title = "Application Update",
      message = "Your driver application was rejected: $reason. Please re-upload required documents.",
      type = "APPROVAL"
    )
  }

  // Notification helper
  fun addNotification(userId: String, title: String, message: String, type: String) {
    val notif = AppNotification(
      id = "notif_${UUID.randomUUID().toString().take(6)}",
      userId = userId,
      title = title,
      message = message,
      type = type
    )
    _notifications.value = listOf(notif) + _notifications.value
  }

  // --- Location & Movement Simulation ---
  private fun startLocationSimulationLoop() {
    scope.launch {
      while (true) {
        delay(4000) // update movement every 4 seconds as per throttled Firestore recommendation in PDF
        val activeTrips = _trips.value.filter { it.status == TripStatus.ACTIVE }
        if (activeTrips.isNotEmpty()) {
          _trips.value = _trips.value.map { trip ->
            if (trip.status == TripStatus.ACTIVE) {
              val route = _routes.value.find { it.id == trip.routeId }
              if (route != null && route.stops.isNotEmpty()) {
                val nextStopIndex = (trip.currentStopIndex + 1) % route.stops.size
                val currentStop = route.stops[trip.currentStopIndex]
                val targetStop = route.stops[nextStopIndex]

                // Step 15% towards target stop
                val newLat = currentStop.latitude + (targetStop.latitude - currentStop.latitude) * 0.2
                val newLng = currentStop.longitude + (targetStop.longitude - currentStop.longitude) * 0.2

                trip.copy(
                  currentLatitude = newLat,
                  currentLongitude = newLng,
                  currentStopIndex = if (Math.random() > 0.7) nextStopIndex else trip.currentStopIndex,
                  lastLocationAt = System.currentTimeMillis()
                )
              } else trip
            } else trip
          }
        }
      }
    }
  }

  // Distance calculator helper (Haversine formula in meters)
  companion object {
    fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
      val r = 6371000.0 // Earth radius in meters
      val dLat = Math.toRadians(lat2 - lat1)
      val dLon = Math.toRadians(lon2 - lon1)
      val a = sin(dLat / 2) * sin(dLat / 2) +
          cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
          sin(dLon / 2) * sin(dLon / 2)
      val c = 2 * atan2(sqrt(a), sqrt(1 - a))
      return r * c
    }

    // ETA estimator based on distance and average speed (traffic-aware)
    fun estimateEtaMinutes(distanceMeters: Double, speedKph: Double = 20.0): Int {
      if (distanceMeters <= 50) return 1
      val speedMetersPerMinute = (speedKph * 1000.0) / 60.0
      val minutes = distanceMeters / speedMetersPerMinute
      return minutes.roundToInt().coerceAtLeast(1)
    }
  }
}
