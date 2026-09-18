package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "offline_sync_queue")
data class OfflineEventEntity(
  @PrimaryKey val id: String,
  val eventType: String,
  val tripId: String,
  val driverId: String,
  val previousCount: Int,
  val newCount: Int,
  val note: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val synced: Boolean = false
)

@Entity(tableName = "trip_cache")
data class TripCacheEntity(
  @PrimaryKey val tripId: String,
  val driverId: String,
  val routeId: String,
  val passengerCount: Int,
  val seatCapacity: Int,
  val status: String,
  val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_log_cache")
data class AuditLogEntity(
  @PrimaryKey val id: String,
  val eventType: String,
  val tripId: String,
  val driverId: String,
  val previousValue: String,
  val newValue: String,
  val description: String,
  val timestamp: Long = System.currentTimeMillis(),
  val synced: Boolean = false
)

@Dao
interface OfflineDao {
  @Query("SELECT * FROM offline_sync_queue ORDER BY timestamp ASC")
  fun getAllQueuedEventsFlow(): Flow<List<OfflineEventEntity>>

  @Query("SELECT * FROM offline_sync_queue WHERE synced = 0 ORDER BY timestamp ASC")
  suspend fun getPendingEvents(): List<OfflineEventEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOfflineEvent(event: OfflineEventEntity)

  @Update
  suspend fun updateOfflineEvent(event: OfflineEventEntity)

  @Query("DELETE FROM offline_sync_queue WHERE synced = 1")
  suspend fun clearSyncedEvents()

  @Query("DELETE FROM offline_sync_queue")
  suspend fun clearAllEvents()

  // Trip cache
  @Query("SELECT * FROM trip_cache WHERE tripId = :tripId")
  suspend fun getTripCache(tripId: String): TripCacheEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveTripCache(trip: TripCacheEntity)

  // Audit logs
  @Query("SELECT * FROM audit_log_cache ORDER BY timestamp DESC")
  fun getAuditLogsFlow(): Flow<List<AuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuditLog(log: AuditLogEntity)
}

@Database(
  entities = [OfflineEventEntity::class, TripCacheEntity::class, AuditLogEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun offlineDao(): OfflineDao
}
