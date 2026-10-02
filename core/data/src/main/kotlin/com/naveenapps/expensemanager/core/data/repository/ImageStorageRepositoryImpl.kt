package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImageStorageRepositoryImpl(
    private val context: Context,
) : ImageStorageRepository {

    override fun createImageCaptureUri(): Uri {
        val dir = File(context.cacheDir, CAPTURE_DIR).apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    override suspend fun saveCategoryImage(sourceUri: Uri): String? = saveImage(sourceUri, CATEGORY_IMAGE_DIR)

    override fun deleteCategoryImage(path: String) {
        runCatching { File(path).delete() }
    }

    override suspend fun saveAccountImage(sourceUri: Uri): String? = saveImage(sourceUri, ACCOUNT_IMAGE_DIR)

    override fun deleteAccountImage(path: String) {
        runCatching { File(path).delete() }
    }

    override suspend fun saveTransactionAttachment(sourceUri: Uri): String? = saveImage(sourceUri, TRANSACTION_ATTACHMENT_DIR)

    override fun deleteTransactionAttachment(path: String) {
        runCatching { File(path).delete() }
    }

    private suspend fun saveImage(sourceUri: Uri, folderName: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeSampledBitmap(sourceUri) ?: return@runCatching null
            val oriented = correctOrientation(bitmap, sourceUri)

            val dir = File(context.filesDir, folderName).apply { mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                oriented.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            if (oriented !== bitmap) bitmap.recycle()
            oriented.recycle()

            file.absolutePath
        }.getOrNull()
    }

    // Decodes at a downsampled resolution rather than full size — gallery photos and camera
    // captures are routinely 10+ megapixels, far more than a small circular avatar ever needs,
    // and decoding full-size risks OutOfMemoryError on the main heap for large images.
    private fun decodeSampledBitmap(uri: Uri): Bitmap? {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val boundsRead = resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        if (boundsRead == null && bounds.outWidth <= 0) return null

        var sampleSize = 1
        while (bounds.outWidth / sampleSize > MAX_DIMENSION ||
            bounds.outHeight / sampleSize > MAX_DIMENSION
        ) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return resolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    // Camera captures (and some gallery photos) carry their real orientation as EXIF metadata
    // rather than actually rotating the pixel data — ignoring this is the classic "photo shows up
    // sideways" bug.
    private fun correctOrientation(bitmap: Bitmap, uri: Uri): Bitmap {
        val orientation = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    companion object {
        private const val CAPTURE_DIR = "captured_images"
        private const val CATEGORY_IMAGE_DIR = "category_images"
        private const val ACCOUNT_IMAGE_DIR = "account_images"
        private const val TRANSACTION_ATTACHMENT_DIR = "transaction_attachments"
        private const val MAX_DIMENSION = 512
        private const val JPEG_QUALITY = 85
    }
}
