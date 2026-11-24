package com.naveenapps.expensemanager.core.domain

import kotlin.test.assertEquals

/**
 * [actual] has exactly the [expected] elements, each as many times, in any order (Truth's
 * `containsExactly` without `inOrder`), for tests that run on every platform.
 */
fun <T> assertContainsExactly(expected: Collection<T>, actual: Collection<T>?) {
    val want = expected.groupingBy { it }.eachCount()
    val got = actual.orEmpty().groupingBy { it }.eachCount()
    assertEquals(want, got, "expected exactly $expected in any order, got $actual")
}
