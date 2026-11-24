package com.naveenapps.expensemanager.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Milli-unit conversions with fixed expected values (each taken from
 * `BigDecimal.valueOf(d).setScale(3, HALF_EVEN)` on the JVM), so the same assertions run on the
 * Android host and the iOS simulator (OpenSpec change make-core-model-multiplatform, task 2.1).
 * The large comparison with BigDecimal itself is MilliAmountsEquivalenceTest (JVM only).
 */
class MilliAmountsTest {

    private val limit39 = 549_755_813_888.0 // 2^39
    private val limit43 = 8_796_093_022_208.0 // 2^43

    @Test
    fun everydayAmounts() {
        assertEquals(20_500L, 20.5.toMilli())
        assertEquals(1_234_560L, 1234.56.toMilli())
        assertEquals(-35_005L, (-35.005).toMilli())
        assertEquals(300L, (0.1 + 0.2).toMilli())
        assertEquals(20_500L, 20.499999999.toMilli())
    }

    @Test
    fun exactTiesRoundToEven() {
        assertEquals(1_002L, 1.0015.toMilli())
        assertEquals(2_002L, 2.0015.toMilli())
        assertEquals(2_002L, 2.0025.toMilli())
        assertEquals(2L, 0.0015.toMilli())
        assertEquals(0L, 0.0005.toMilli())
    }

    @Test
    fun zeroAndTinyAmounts() {
        assertEquals(0L, 0.0.toMilli())
        assertEquals(0L, (-0.0).toMilli())
        assertEquals(0L, 1e-5.toMilli())
    }

    @Test
    fun aroundTheExactRangeLimits() {
        assertEquals(549_755_813_888_000L, (limit39 - 0.0005).toMilli())
        assertEquals(549_755_813_887_998L, (limit39 - 0.0015).toMilli())
        assertEquals(549_755_813_888_125L, (limit39 + 0.125).toMilli())
        assertEquals(700_000_000_000_125L, 700_000_000_000.125.toMilli())
        assertEquals(8_796_093_022_207_875L, (limit43 - 0.125).toMilli())
        assertEquals(8_796_093_022_208_125L, (limit43 + 0.125).toMilli())
    }

    @Test
    fun notFiniteAmountsThrow() {
        assertFailsWith<IllegalArgumentException> { Double.NaN.toMilli() }
        assertFailsWith<IllegalArgumentException> { Double.POSITIVE_INFINITY.toMilli() }
        assertFailsWith<IllegalArgumentException> { Double.NEGATIVE_INFINITY.toMilli() }
    }

    @Test
    fun backToDouble() {
        assertEquals(20.5, 20_500L.milliToDouble())
        assertEquals(-0.001, (-1L).milliToDouble())
        assertEquals(0.0, 0L.milliToDouble())
        assertEquals(1234.56, 1_234_560L.milliToDouble())
    }
}
