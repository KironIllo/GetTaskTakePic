package com.kironillo.domain.usecase.photo

import com.kironillo.domain.hardware.GeoPoint
import com.kironillo.domain.testutil.FakeLocationProvider
import com.kironillo.domain.testutil.FakeSettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetCurrentLocationUseCaseTest {

    @Test
    fun `returns null when geolocation disabled in settings`() = runBlocking {
        val settings = FakeSettingsRepository(geolocationEnabled = false)
        val provider = FakeLocationProvider(nextLocation = GeoPoint(1.0, 2.0))
        val useCase = GetCurrentLocationUseCase(provider, settings)

        assertNull(useCase())
        assertEquals(0, provider.callCount) // провайдер даже не дёргали
    }

    @Test
    fun `returns location when enabled`() = runBlocking {
        val settings = FakeSettingsRepository(geolocationEnabled = true)
        val provider = FakeLocationProvider(nextLocation = GeoPoint(55.75, 37.61))
        val useCase = GetCurrentLocationUseCase(provider, settings)

        assertEquals(GeoPoint(55.75, 37.61), useCase())
        assertEquals(1, provider.callCount)
    }

    @Test
    fun `returns null when provider returns null`() = runBlocking {
        val settings = FakeSettingsRepository(geolocationEnabled = true)
        val provider = FakeLocationProvider(nextLocation = null)
        val useCase = GetCurrentLocationUseCase(provider, settings)

        assertNull(useCase())
    }
}
