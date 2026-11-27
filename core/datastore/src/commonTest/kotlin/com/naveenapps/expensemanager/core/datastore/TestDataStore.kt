package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.test.TestScope
import okio.Path.Companion.toPath

/** A path to a `.preferences_pb` file in a new, empty temporary folder (one per call). */
expect fun newTempDataStorePath(): String

/** A real file-backed DataStore for one test, closed when the test ends. */
fun TestScope.testDataStore(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    scope = backgroundScope,
    produceFile = { newTempDataStorePath().toPath() },
)
