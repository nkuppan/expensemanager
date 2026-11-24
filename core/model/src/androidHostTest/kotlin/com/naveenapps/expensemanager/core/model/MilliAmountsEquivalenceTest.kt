package com.naveenapps.expensemanager.core.model

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [toMilli] and [milliToDouble] against the java.math.BigDecimal code they replaced, over about
 * 4 million amounts inside the exact ranges (OpenSpec change make-core-model-multiplatform,
 * task 2.2). JVM only: BigDecimal doesn't exist on iOS.
 */
class MilliAmountsEquivalenceTest {

    private val limit39 = 549_755_813_888.0 // 2^39: every amount below is exact
    private val limit43 = 8_796_093_022_208.0 // 2^43: 3-decimal amounts below are exact

    private fun reference(amount: Double): Long = BigDecimal.valueOf(amount).setScale(3, RoundingMode.HALF_EVEN).unscaledValue().toLong()

    private fun assertNoDifferences(what: String, amounts: Sequence<Double>) {
        var checked = 0
        val differences = mutableListOf<String>()
        amounts.forEach { amount ->
            checked++
            val expected = reference(amount)
            val actual = amount.toMilli()
            if (actual != expected && differences.size < 10) differences += "$amount: $actual, BigDecimal $expected"
        }
        assertEquals(emptyList(), differences, "$what ($checked amounts)")
    }

    @Test
    fun everyKindOfAmountBelow2Pow39() {
        val random = Random(42)
        val amounts = sequence {
            for (k in -200_000..200_000) {
                yield(k / 10_000.0) // every 4-decimal amount within ±20
                yield(k / 1_000.0 + 0.0005) // ties
                yield(k / 100.0)
                yield(k * 1234.5677 / 1000.0)
            }
            repeat(400_000) {
                val big = (random.nextDouble() * 5e14).toLong()
                yield((10 * big + 5) / 10_000.0) // ties at large magnitudes
                yield(big / 1000.0)
            }
            repeat(600_000) {
                yield(random.nextDouble() * 1e6 - 5e5)
                yield(random.nextDouble() * 10 - 5)
                yield(random.nextDouble() * 5.4e11)
                val sign = if (random.nextBoolean()) 1 else -1
                yield(sign * random.nextDouble() * Math.scalb(1.0, random.nextInt(70) - 30))
            }
            yieldAll(
                listOf(
                    0.0, -0.0, 1e-5, -1e-5, 4e-4, 5e-4, 6e-4, 1.5e-3, 2.5e-3, 1.0015, 2.0015, 1.0005, 2.0025,
                    1e7, 1.2345678e7, 123456789.1235, 0.1 + 0.2, 20.499999999, -35.005, 1e9, 1e-10, Double.MIN_VALUE,
                ),
            )
        }.filter { abs(it) < limit39 }
        assertNoDifferences("below 2^39", amounts)
    }

    @Test
    fun threeDecimalAmountsBelow2Pow43() {
        val random = Random(7)
        val low = (limit39 * 1000).toLong()
        val span = limit43 * 1000 - limit39 * 1000
        val amounts = generateSequence { (random.nextDouble() * span).toLong() + low }
            .take(1_000_000)
            .map { it / 1000.0 }
            .filter { BigDecimal.valueOf(it).scale() <= 3 }
        assertNoDifferences("3 decimals, 2^39 to 2^43", amounts)
    }

    @Test
    fun milliToDoubleMatchesBigDecimal() {
        val random = Random(9)
        val limit53 = 1L shl 53
        val values = (-2_000_000L..2_000_000L).asSequence() +
            generateSequence { random.nextLong() % limit53 }.take(1_000_000)
        var checked = 0
        val differences = mutableListOf<String>()
        values.forEach { milli ->
            checked++
            val expected = BigDecimal.valueOf(milli, 3).toDouble()
            val actual = milli.milliToDouble()
            if (actual != expected && differences.size < 10) differences += "$milli: $actual, BigDecimal $expected"
        }
        assertEquals(emptyList(), differences, "milliToDouble ($checked values)")
    }
}
