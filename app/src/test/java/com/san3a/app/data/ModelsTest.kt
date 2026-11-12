package com.san3a.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {

    @Test
    fun userRole_from_knownValue() {
        assertEquals(UserRole.CRAFTSMAN, UserRole.from("craftsman"))
        assertEquals(UserRole.CUSTOMER, UserRole.from("customer"))
    }

    @Test
    fun userRole_from_unknown_defaultsCustomer() {
        assertEquals(UserRole.CUSTOMER, UserRole.from(null))
        assertEquals(UserRole.CUSTOMER, UserRole.from("admin"))
    }

    @Test
    fun userProfile_ratingAvg_zeroWhenNoRatings() {
        val profile = UserProfile(ratingSum = 0, ratingCount = 0)
        assertEquals(0.0, profile.ratingAvg, 0.001)
    }

    @Test
    fun userProfile_ratingAvg_computed() {
        val profile = UserProfile(ratingSum = 15, ratingCount = 3)
        assertEquals(5.0, profile.ratingAvg, 0.001)
    }

    @Test
    fun userProfile_hasLocation() {
        assertFalse(UserProfile().hasLocation)
        assertTrue(UserProfile(latitude = 1.0, longitude = 2.0).hasLocation)
    }

    @Test
    fun userProfile_isCraftsman() {
        assertTrue(UserProfile(role = UserRole.CRAFTSMAN.value).isCraftsman)
        assertFalse(UserProfile(role = UserRole.CUSTOMER.value).isCraftsman)
    }
}
