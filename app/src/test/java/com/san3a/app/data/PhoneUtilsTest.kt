package com.san3a.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneUtilsTest {

    @Test
    fun normalizePhone_blank() {
        assertEquals("", normalizePhone(""))
        assertEquals("", normalizePhone("   "))
    }

    @Test
    fun normalizePhone_withPlus() {
        assertEquals("+966512345678", normalizePhone("+966 512 345 678"))
    }

    @Test
    fun normalizePhone_withoutPlus() {
        assertEquals("0512345678", normalizePhone("0512345678"))
    }
}
