package com.naveenapps.expensemanager.core.repository

import kotlin.test.Test
import kotlin.test.assertEquals

/** Records what [logFailure] sends, without any analytics SDK. */
internal class RecordingAnalytics : AnalyticsRepository {
    val events = mutableListOf<Pair<String, Map<String, String>>>()

    override fun trackAppOpenEvent() = Unit

    override fun logEvent(eventName: String, params: Map<String, String>) {
        events += eventName to params
    }

    override fun setCurrentScreen(screenName: String) = Unit

    override fun setUserProperties() = Unit

    override fun setUserProperty(name: String, value: String?) = Unit
}

/** An exception the app defines itself. */
internal class SaveFailed : Exception()

/**
 * `action_failed` reports the error's class name, on Android and iOS alike (OpenSpec change
 * make-core-repository-multiplatform, task 2.1). The comparison with the old JVM-only
 * `javaClass.simpleName` code is FailureReasonJvmTest.
 */
class FailureReasonTest {

    private fun reasonFor(error: Throwable?): Map<String, String> {
        val analytics = RecordingAnalytics()
        analytics.logFailure("category_save", error)
        val (name, params) = analytics.events.single()
        assertEquals(AnalyticsEvents.ACTION_FAILED, name)
        return params
    }

    @Test
    fun standardException() {
        val params = reasonFor(IllegalStateException("boom"))
        assertEquals("category_save", params[AnalyticsParams.TYPE])
        assertEquals("IllegalStateException", params[AnalyticsParams.REASON])
    }

    @Test
    fun appDefinedException() {
        assertEquals("SaveFailed", reasonFor(SaveFailed())[AnalyticsParams.REASON])
    }

    @Test
    fun noErrorIsUnknown() {
        assertEquals("unknown", reasonFor(null)[AnalyticsParams.REASON])
    }

    @Test
    fun onlyTypeAndReasonAreSent() {
        assertEquals(setOf(AnalyticsParams.TYPE, AnalyticsParams.REASON), reasonFor(SaveFailed()).keys)
    }
}
