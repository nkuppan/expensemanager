package com.naveenapps.expensemanager.feature.about.licenses

import android.annotation.SuppressLint
import android.content.Context
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** The open source libraries in the app and their licences. */
interface OpenSourceLicensesRepository {
    suspend fun getLicenses(): List<OpenSourceLicense>

    suspend fun findLicense(name: String): OpenSourceLicense? = getLicenses().find { it.name == name }
}

/**
 * Reads the licence files the oss-licenses plugin generates into the app's raw resources (kept
 * from resource shrinking by app/src/main/res/raw/keep.xml). Read once, then kept in memory.
 * Debug builds only contain a single "Debug License Info" placeholder.
 */
class RawResourceLicensesRepository(
    private val context: Context,
    private val dispatchers: AppCoroutineDispatchers,
) : OpenSourceLicensesRepository {

    private val mutex = Mutex()
    private var cache: List<OpenSourceLicense>? = null

    override suspend fun getLicenses(): List<OpenSourceLicense> = mutex.withLock {
        cache ?: withContext(dispatchers.io) { load() }.also { cache = it }
    }

    // The files are generated into the app module, so this feature module can't use R.raw.
    @SuppressLint("DiscouragedApi")
    private fun load(): List<OpenSourceLicense> = runCatching {
        val resources = context.resources
        val metadataId = resources.getIdentifier(METADATA, "raw", context.packageName)
        val licensesId = resources.getIdentifier(LICENSES, "raw", context.packageName)
        if (metadataId == 0 || licensesId == 0) return@runCatching emptyList()
        val metadata = resources.openRawResource(metadataId).bufferedReader().use { it.readText() }
        val licenses = resources.openRawResource(licensesId).use { it.readBytes() }
        OpenSourceLicenseParser.parse(metadata, licenses)
    }.getOrDefault(emptyList())

    private companion object {
        const val METADATA = "third_party_license_metadata"
        const val LICENSES = "third_party_licenses"
    }
}
