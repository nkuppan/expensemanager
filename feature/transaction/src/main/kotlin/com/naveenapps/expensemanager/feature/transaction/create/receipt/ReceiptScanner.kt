package com.naveenapps.expensemanager.feature.transaction.create.receipt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState.STATE_CANCELED
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState.STATE_COMPLETED
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState.STATE_FAILED
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

/**
 * Google's document scanner (Play services): its own camera screen with edge detection, crop and
 * clean-up, so the app needs no CAMERA permission. One page, and a photo can be picked from the
 * gallery instead. Returns a function that opens it; [onScanned] gets the cleaned-up image.
 *
 * The scanner lives in an optional Play services module. When it isn't on the phone, the
 * library's own launch screen crashes the app (ActivityNotFoundException for
 * ACTION_SCAN_DOCUMENT), and no try/catch of ours can reach that. So before opening it we check
 * Play services, ask it to download the module if needed ([onPreparing], then it opens on its
 * own), and only launch when Play services can actually handle the scan; otherwise [onFailed].
 */
@Composable
internal fun rememberReceiptScanner(
    onScanned: (Uri) -> Unit,
    onPreparing: () -> Unit,
    onFailed: () -> Unit,
): () -> Unit {
    val activity = LocalActivity.current
    val currentOnScanned by rememberUpdatedState(onScanned)
    val currentOnPreparing by rememberUpdatedState(onPreparing)
    val currentOnFailed by rememberUpdatedState(onFailed)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            ?.pages
            ?.firstOrNull()
            ?.imageUri
            ?.let { currentOnScanned(it) }
    }
    val scanner = remember { GmsDocumentScanning.getClient(scannerOptions()) }
    return remember(activity, launcher, scanner) {
        {
            if (activity == null) {
                currentOnFailed()
            } else {
                ReceiptScannerStarter(
                    activity = activity,
                    scanner = scanner,
                    // The screen may be gone by the time a download finishes.
                    launch = { sender ->
                        runCatching { launcher.launch(IntentSenderRequest.Builder(sender).build()) }
                            .onFailure { currentOnFailed() }
                    },
                    onPreparing = { currentOnPreparing() },
                    onFailed = { currentOnFailed() },
                ).start()
            }
        }
    }
}

private class ReceiptScannerStarter(
    private val activity: Activity,
    private val scanner: GmsDocumentScanner,
    private val launch: (IntentSender) -> Unit,
    private val onPreparing: () -> Unit,
    private val onFailed: () -> Unit,
) {
    private val modules = ModuleInstall.getClient(activity)

    fun start() {
        val availability = GoogleApiAvailability.getInstance()
        val status = availability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            // Missing, disabled or outdated Play services: let Play explain and offer the fix.
            val dialog = if (availability.isUserResolvableError(status)) {
                availability.getErrorDialog(activity, status, REQUEST_PLAY_SERVICES)
            } else {
                null
            }
            dialog?.show() ?: onFailed()
            return
        }
        modules.areModulesAvailable(scanner)
            .addOnSuccessListener { response ->
                if (response.areModulesAvailable()) open() else installThenOpen()
            }
            .addOnFailureListener { onFailed() }
    }

    private fun installThenOpen() {
        onPreparing()
        val listener = object : InstallStatusListener {
            override fun onInstallStatusUpdated(update: ModuleInstallStatusUpdate) {
                when (update.installState) {
                    STATE_COMPLETED -> {
                        modules.unregisterListener(this)
                        open()
                    }

                    STATE_FAILED, STATE_CANCELED -> {
                        modules.unregisterListener(this)
                        onFailed()
                    }
                }
            }
        }
        val request = ModuleInstallRequest.newBuilder()
            .addApi(scanner)
            .setListener(listener)
            .build()
        modules.installModules(request)
            .addOnSuccessListener { response ->
                if (response.areModulesAlreadyInstalled()) {
                    modules.unregisterListener(listener)
                    open()
                }
            }
            .addOnFailureListener {
                modules.unregisterListener(listener)
                onFailed()
            }
    }

    private fun open() {
        if (!canHandleScan(activity)) {
            onFailed()
            return
        }
        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { launch(it) }
            .addOnFailureListener { onFailed() }
    }

    private companion object {
        const val REQUEST_PLAY_SERVICES = 9_101
    }
}

// The intent Google's launch screen forwards to Play services. Declared in this module's
// manifest <queries> so it's visible on Android 11+.
private const val ACTION_SCAN_DOCUMENT = "com.google.android.gms.mlkit.ACTION_SCAN_DOCUMENT"
private const val PLAY_SERVICES_PACKAGE = "com.google.android.gms"

private fun canHandleScan(context: Context): Boolean {
    val intent = Intent(ACTION_SCAN_DOCUMENT).setPackage(PLAY_SERVICES_PACKAGE)
    val packageManager = context.packageManager
    val matches = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
    } else {
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(intent, 0)
    }
    return matches.isNotEmpty()
}

private fun scannerOptions() = GmsDocumentScannerOptions.Builder()
    .setGalleryImportAllowed(true)
    .setPageLimit(1)
    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
    .build()
