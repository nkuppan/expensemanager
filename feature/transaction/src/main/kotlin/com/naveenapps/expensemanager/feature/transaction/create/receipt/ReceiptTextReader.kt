package com.naveenapps.expensemanager.feature.transaction.create.receipt

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.naveenapps.expensemanager.core.domain.usecase.receipt.ReceiptRows
import com.naveenapps.expensemanager.core.domain.usecase.receipt.RecognisedLine
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Reads the text in a receipt screenshot. Null when the image can't be read. */
fun interface ReceiptTextReader {
    suspend fun read(imageUri: Uri): String?
}

/**
 * On-device OCR with ML Kit's Latin model from Google Play services (downloaded at install via
 * the manifest's com.google.mlkit.vision.DEPENDENCIES): nothing leaves the phone. Until the model
 * is ready, or on phones without Play services, this returns null and the user enters the amount.
 * Reads the original image (attachments are downsampled too far for text), then puts the text
 * back into the receipt's printed rows (see [ReceiptRows]) so a total and its amount stay on one
 * line.
 */
class MlKitReceiptTextReader(
    private val context: Context,
) : ReceiptTextReader {

    override suspend fun read(imageUri: Uri): String? {
        val image = runCatching { InputImage.fromFilePath(context, imageUri) }.getOrNull() ?: return null
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            suspendCancellableCoroutine { continuation ->
                recognizer.process(image)
                    .addOnSuccessListener { result -> continuation.resume(result.toRows()) }
                    .addOnFailureListener { continuation.resume(null) }
            }
        } finally {
            recognizer.close()
        }
    }
}

private fun Text.toRows(): String {
    val lines = textBlocks.flatMap { it.lines }.mapNotNull { line ->
        val box = line.boundingBox ?: return@mapNotNull null
        RecognisedLine(line.text, box.left, box.top, box.right, box.bottom, line.angle)
    }
    // Without positions there's nothing to rebuild; ML Kit's own reading order is all we have.
    return if (lines.isEmpty()) text else ReceiptRows.build(lines)
}
