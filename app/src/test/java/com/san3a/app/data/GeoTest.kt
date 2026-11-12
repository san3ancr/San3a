package com.san3a.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTest {

    @Test
    fun distanceKm_samePoint_isZero() {
        assertEquals(0.0, distanceKm(24.7, 46.7, 24.7, 46.7), 0.001)
    }

    @Test
    fun distanceKm_knownRoughDistance() {
        val km = distanceKm(24.7136, 46.6753, 21.4225, 39.8262) // Riyadh -> Makkah
        assertTrue(km > 700 && km < 1000)
    }

    @Test
    fun formatDistance_meters() {
        assertEquals("500 م", formatDistance(0.5))
    }

    @Test
    fun formatDistance_kilometers() {
        assertEquals("1.5 كم", formatDistance(1.5))
    }
}
