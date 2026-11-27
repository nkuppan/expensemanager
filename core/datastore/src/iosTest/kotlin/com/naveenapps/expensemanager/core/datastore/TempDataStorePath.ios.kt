package com.naveenapps.expensemanager.core.datastore

import kotlin.uuid.Uuid
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory

@OptIn(ExperimentalForeignApi::class)
actual fun newTempDataStorePath(): String {
    val folder = NSTemporaryDirectory().trimEnd('/') + "/datastore-" + Uuid.random().toString()
    NSFileManager.defaultManager.createDirectoryAtPath(folder, withIntermediateDirectories = true, attributes = null, error = null)
    return "$folder/test.preferences_pb"
}
