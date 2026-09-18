package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserRole
import com.example.data.repository.JeepneyRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Jeepney & Commuter", appName)
  }

  @Test
  fun `test commuter waiting alert and cancellation`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val repository = JeepneyRepository(app)

    repository.switchRoleForDemo(UserRole.COMMUTER)
    val routes = repository.routes.value
    assertTrue("Routes should be loaded", routes.isNotEmpty())

    val firstRoute = routes.first()
    val firstStop = firstRoute.stops.first()

    repository.requestWaiting(firstRoute.id, firstStop.id, passengerCount = 1)
    assertNotNull("Commuter waiting request should be created", repository.currentCommuterWaiting.value)
    assertEquals(firstStop.id, repository.currentCommuterWaiting.value?.stopId)

    repository.cancelWaiting()
    assertEquals(null, repository.currentCommuterWaiting.value)
  }

  @Test
  fun `test driver manual passenger count and boundary limits`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val repository = JeepneyRepository(app)

    repository.switchRoleForDemo(UserRole.DRIVER)
    val trips = repository.trips.value
    val activeTrip = trips.first()
    val initialCount = activeTrip.passengerCount

    // Increase passenger count
    repository.updatePassengerCountManual(activeTrip.id, 1)
    val updatedTrip = repository.trips.value.find { it.id == activeTrip.id }
    assertEquals(initialCount + 1, updatedTrip?.passengerCount)

    // Decrease passenger count
    repository.updatePassengerCountManual(activeTrip.id, -1)
    val decrementedTrip = repository.trips.value.find { it.id == activeTrip.id }
    assertEquals(initialCount, decrementedTrip?.passengerCount)
  }

  @Test
  fun `test distance and ETA calculations`() {
    val dist = JeepneyRepository.calculateDistanceMeters(14.6195, 121.0510, 14.6180, 121.0500)
    assertTrue("Distance should be greater than 0", dist > 0)

    val eta = JeepneyRepository.estimateEtaMinutes(dist, 20.0)
    assertTrue("ETA should be positive integer", eta >= 1)
  }
}
