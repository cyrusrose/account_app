package com.cyril.account

import com.cyril.account.home.domain.Card
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun equality() {
        val a = Card("1", "hi", "content", 0, 0)
        val b = Card("1", "hi", "content", 0, 0, isChecked = true)
        assertNotEquals(a, b)
    }
}