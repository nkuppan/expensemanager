package com.naveenapps.expensemanager.feature.transaction.create.receipt

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult

/**
 * Google's document scanner (Play services): its own camera screen with edge detection, crop and
 * clean-up, so the app needs no CAMERA permission. One page, and a photo can be picked from the
 * gallery instead. Returns a function that opens it; [onScanned] gets the cleaned-up image.
 */
@Composable
internal fun rememberReceiptScanner(
    onScanned: (Uri) -> Unit,
    onFailed: () -> Unit,
): () -> Unit {
    val activity = LocalActivity.current
    val currentOnScanned by rememberUpdatedState(onScanned)
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
                scanner.getStartScanIntent(activity)
                    .addOnSuccessListener { launcher.launch(IntentSenderRequest.Builder(it).build()) }
                    .addOnFailureListener { currentOnFailed() }
            }
        }
    }
}

private fun scannerOptions() = GmsDocumentScannerOptions.Builder()
    .setGalleryImportAllowed(true)
    .setPageLimit(1)
    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
    .build()
