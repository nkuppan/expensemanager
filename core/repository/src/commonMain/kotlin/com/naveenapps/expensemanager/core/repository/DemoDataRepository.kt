package com.naveenapps.expensemanager.core.repository

/**
 * Fills the app with sample data for Play Store screenshots.
 *
 * Only debug builds provide an implementation (app/src/debug). Release builds have no binding,
 * so screens ask Koin for it with `getOrNull()` and hide the option when it's null.
 */
interface DemoDataRepository {
    /** Replaces ALL data with sample data in the current currency and app language. */
    suspend fun loadDemoData(months: Int = 3): Result<Int>
}
