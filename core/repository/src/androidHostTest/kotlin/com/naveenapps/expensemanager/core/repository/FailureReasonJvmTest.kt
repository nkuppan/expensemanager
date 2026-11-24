package com.naveenapps.expensemanager.core.repository

import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * On Android, `action_failed` reasons are exactly what the old `javaClass.simpleName` code sent
 * (OpenSpec change make-core-repository-multiplatform, task 2.2, design D2). JVM only: the old
 * code used Java reflection.
 */
class FailureReasonJvmTest {

    private class NestedFailure : RuntimeException()

    /** The code this change replaced, verbatim. */
    private fun oldReason(error: Throwable?): String = error?.javaClass?.simpleName?.ifBlank { null } ?: "unknown"

    private fun newReason(error: Throwable?): String? {
        val analytics = RecordingAnalytics()
        analytics.logFailure("test", error)
        return analytics.events.single().second[AnalyticsParams.REASON]
    }

    @Test
    fun sameReasonsAsTheJavaClassName() {
        class LocalFailure : Exception()

        val anonymous = object : IllegalStateException() {}
        val errors: List<Throwable?> = listOf(
            IllegalStateException(),
            IllegalArgumentException(),
            IOException(),
            NullPointerException(),
            UnsupportedOperationException(),
            Exception(),
            RuntimeException(),
            Throwable(),
            SaveFailed(),
            NestedFailure(),
            LocalFailure(),
            anonymous,
            null,
        )
        errors.forEach { error ->
            assertEquals(oldReason(error), newReason(error), "reason for ${error?.javaClass?.name}")
        }
        assertEquals("unknown", newReason(anonymous), "anonymous classes have no name")
    }
}
