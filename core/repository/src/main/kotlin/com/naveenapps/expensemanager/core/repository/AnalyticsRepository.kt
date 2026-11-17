package com.naveenapps.expensemanager.core.repository

interface AnalyticsRepository {

    fun trackAppOpenEvent()

    fun logEvent(eventName: String, params: Map<String, String>)

    fun setCurrentScreen(screenName: String)

    fun setUserProperties()

    /**
     * A user property: who the user is by behaviour (e.g. "reminder_on" = "true"), attached to
     * all their later events so funnels and retention can be split by it. Null clears it.
     * Names: at most 24 characters; values: at most 36.
     */
    fun setUserProperty(name: String, value: String?)
}
