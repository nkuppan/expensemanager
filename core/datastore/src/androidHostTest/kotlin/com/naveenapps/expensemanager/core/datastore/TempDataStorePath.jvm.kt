package com.naveenapps.expensemanager.core.datastore

import java.io.File
import java.nio.file.Files

actual fun newTempDataStorePath(): String = File(Files.createTempDirectory("datastore").toFile(), "test.preferences_pb").path
