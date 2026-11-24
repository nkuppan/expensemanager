package com.naveenapps.expensemanager.core.repository

/**
 * Something the user tried didn't work (a save, delete, export, restore, ...): logged as
 * action_failed with what was attempted ([action], e.g. "category_save") and the error's
 * class name as the reason, so failures can be counted per feature without sending any data.
 */
fun AnalyticsRepository.logFailure(action: String, error: Throwable?) {
    logEvent(
        AnalyticsEvents.ACTION_FAILED,
        mapOf(
            AnalyticsParams.TYPE to action,
            AnalyticsParams.REASON to (error?.let { it::class.simpleName }?.ifBlank { null } ?: "unknown"),
        ),
    )
}

/**
 * The user changed a setting: [setting] names it (e.g. "theme"), [value] is the choice made
 * (e.g. "dark", "true", "inr"), never free text.
 */
fun AnalyticsRepository.logSettingChanged(setting: String, value: String) {
    logEvent(
        AnalyticsEvents.SETTING_CHANGED,
        mapOf(AnalyticsParams.TYPE to setting, AnalyticsParams.METHOD to value),
    )
}
