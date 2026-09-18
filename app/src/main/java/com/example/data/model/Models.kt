package com.example.data.model

enum class UserRole {
  COMMUTER,
  DRIVER,
  ADMIN
}

enum class DriverApprovalStatus {
  PENDING,
  APPROVED,
  REJECTED,
  SUSPENDED
}

enum class WaitingStatus {
  WAITING,
  MATCHED,
  BOARDING,
  ONBOARD,
  COMPLETED,
  CANCELLED,
  EXPIRED
}

enum class TripStatus {
  SCHEDULED,
  ACTIVE,
  PAUSED,
  COMPLETED,
  CANCELLED
}

enum class PassengerSessionStatus {
  BOARDED,
  ONBOARD,
  DROPPED_OFF,
  CANCELLED
}

data class GeoCoord(
  val latitude: Double,
  val longitude: Double,
  val label: String = ""
)

data class RouteStop(
  val id: String,
  val name: String,
  val latitude: Double,
  val longitude: Double,
  val orderIndex: Int,
  val landmark: String
)

data class JeepneyRoute(
  val id: String,
  val name: String,
  val code: String,
  val origin: String,
  val destination: String,
  val fareRegular: Double,
  val fareDiscounted: Double,
  val stops: List<RouteStop>,
  val polylinePoints: List<GeoCoord> = emptyList(),
  val active: Boolean = true
)

data class Vehicle(
  val id: String,
  val plateNumber: String,
  val bodyNumber: String,
  val capacity: Int,
  val routeId: String,
  val driverId: String,
  val qrToken: String,
  val active: Boolean = true
)

data class User(
  val uid: String,
  val fullName: String,
  val email: String,
  val role: UserRole,
  val profilePhotoUrl: String = "",
  val bio: String = "",
  val emailVerified: Boolean = true,
  val notificationEnabled: Boolean = true,
  val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

data class DriverProfile(
  val uid: String,
  val driverName: String,
  val licenseNumber: String,
  val licenseDocumentUrl: String = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c",
  val vehicleRegistrationUrl: String = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957",
  val vehiclePlate: String,
  val vehicleBodyNumber: String,
  val vehicleId: String,
  val seatCapacity: Int = 18,
  val assignedRouteId: String,
  val assignedRouteName: String,
  val approvalStatus: DriverApprovalStatus = DriverApprovalStatus.PENDING,
  val approvedBy: String? = null,
  val approvedAt: Long? = null,
  val rejectionReason: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

data class Trip(
  val id: String,
  val driverId: String,
  val driverName: String,
  val vehicleId: String,
  val plateNumber: String,
  val routeId: String,
  val routeName: String,
  val status: TripStatus,
  val startTime: Long,
  val endTime: Long? = null,
  val passengerCount: Int,
  val seatCapacity: Int,
  val availableSeats: Int = (seatCapacity - passengerCount).coerceAtLeast(0),
  val isFull: Boolean = passengerCount >= seatCapacity,
  val currentLatitude: Double,
  val currentLongitude: Double,
  val currentSpeedKph: Double = 22.0,
  val currentHeading: Float = 45f,
  val lastLocationAt: Long = System.currentTimeMillis(),
  val currentStopIndex: Int = 0
)

data class WaitingRequest(
  val id: String,
  val commuterId: String,
  val commuterName: String,
  val routeId: String,
  val routeName: String,
  val stopId: String,
  val stopName: String,
  val latitude: Double,
  val longitude: Double,
  val passengerCount: Int = 1,
  val status: WaitingStatus,
  val matchedTripId: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val expiresAt: Long = System.currentTimeMillis() + 30 * 60 * 1000L
)

data class PassengerSession(
  val id: String,
  val commuterId: String,
  val commuterName: String,
  val tripId: String,
  val vehicleId: String,
  val routeId: String,
  val boardedAt: Long,
  val boardedLatitude: Double,
  val boardedLongitude: Double,
  val qrScanned: Boolean,
  val dropOffAt: Long? = null,
  val dropOffLatitude: Double? = null,
  val dropOffLongitude: Double? = null,
  val dropOffStopName: String? = null,
  val status: PassengerSessionStatus
)

data class AuditLog(
  val id: String,
  val eventType: String,
  val tripId: String? = null,
  val driverId: String? = null,
  val previousValue: String,
  val newValue: String,
  val description: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isOfflineQueued: Boolean = false
)

data class AppNotification(
  val id: String,
  val userId: String,
  val title: String,
  val message: String,
  val type: String, // ARRIVAL, BOARDING, DROP_OFF, APPROVAL, SYSTEM
  val timestamp: Long = System.currentTimeMillis(),
  val read: Boolean = false
)

data class OfflineSyncEvent(
  val id: String,
  val eventType: String,
  val payloadJson: String,
  val createdAt: Long = System.currentTimeMillis(),
  val synced: Boolean = false
)
