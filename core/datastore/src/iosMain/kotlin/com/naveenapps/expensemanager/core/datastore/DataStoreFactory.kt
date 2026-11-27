package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * The iOS settings file `<Application Support>/datastore/<fileName>.preferences_pb`, the
 * counterpart of Android's `Context.preferencesDataStore(fileName)` (files/datastore/). Application
 * Support, not Documents: app data the user shouldn't see in the Files app. DataStore needs one
 * instance per file, so call this once per file and keep the result (Koin, later in Phase 2).
 */
fun createDataStore(fileName: String): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    produceFile = { "${dataStoreDirectory()}/$fileName.preferences_pb".toPath() },
)

@OptIn(ExperimentalForeignApi::class)
private fun dataStoreDirectory(): String {
    val fileManager = NSFileManager.defaultManager
    val supportDirectory = fileManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val directory = requireNotNull(supportDirectory?.path) { "No Application Support directory" } + "/datastore"
    fileManager.createDirectoryAtPath(directory, withIntermediateDirectories = true, attributes = null, error = null)
    return directory
}
