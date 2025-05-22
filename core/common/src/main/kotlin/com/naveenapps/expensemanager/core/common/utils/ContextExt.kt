package com.naveenapps.expensemanager.core.common.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.naveenapps.expensemanager.core.common.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

fun openEmailToOption(context: Context, emailId: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, "mailto:$emailId".toUri())
        intent.putExtra(Intent.EXTRA_SUBJECT, "email_subject")
        intent.putExtra(Intent.EXTRA_TEXT, "email_body")
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.e("Share", e.message ?: "")
    }
}

fun Context.shareThisFile(fileUri: Uri) {
    try {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "*/*"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(shareIntent, null))
    } catch (e: ActivityNotFoundException) {
        Log.e("Share", e.message ?: "")
    }
}

fun Context.openShareOption(pdfFile: File?) {
    try {
        if (pdfFile != null) {
            val printIntent = Intent(Intent.ACTION_SEND)
            printIntent.setType("application/pdf")
            val fileUri = FileProvider.getUriForFile(
                this,
                this.packageName + ".fileprovider",
                pdfFile.absoluteFile
            )
            printIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            printIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            this.startActivity(Intent.createChooser(printIntent, "Print"))
        }
    } catch (e: ActivityNotFoundException) {
        // Define what your app should do if no activity can handle the intent.
    }
}

fun Context.openEmailOption(pdfFile: File?) {
    try {
        if (pdfFile != null) {

            FileProvider.getUriForFile(
                this,
                "${this.packageName}.fileprovider",
                pdfFile
            )

            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject here")
            emailIntent.putExtra(Intent.EXTRA_TEXT, "body text")
            emailIntent.setType("application/pdf")
            val fileUri = FileProvider.getUriForFile(
                this,
                this.packageName + ".fileprovider",
                pdfFile.absoluteFile
            )
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            this.startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"))
        }
    } catch (e: ActivityNotFoundException) {
        // Define what your app should do if no activity can handle the intent.
    }
}

fun Context.openPrintOption(pdfFile: File?) {
    try {
        if (pdfFile != null) {
            val printAdapter: PrintDocumentAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: CancellationSignal,
                    callback: LayoutResultCallback,
                    extras: Bundle?
                ) {
                    if (cancellationSignal.isCanceled) {
                        callback.onLayoutCancelled()
                        return
                    }

                    val info = PrintDocumentInfo.Builder(pdfFile.name)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build()
                    callback.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<PageRange?>?,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                    try {
                        FileInputStream(pdfFile).use { input ->
                            FileOutputStream(destination.fileDescriptor).use { output ->
                                val buffer = ByteArray(1024)
                                var bytesRead: Int
                                while (input.read(buffer).also { bytesRead = it } != -1) {
                                    output.write(buffer, 0, bytesRead)
                                }
                                callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                            }
                        }
                    } catch (e: IOException) {
                        callback.onWriteFailed(e.message)
                    }
                }
            }

            val printManager = this.getSystemService(Context.PRINT_SERVICE) as? PrintManager?

            val jobName = "Document"
            printManager?.print(
                jobName, printAdapter,
                PrintAttributes.Builder().build()
            )
        }
    } catch (_: ActivityNotFoundException) {

    }
}

fun Context.isNightModeOn(): Boolean {
    return this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun Context.convertFileToString(fileName: String): String? {
    return kotlin.runCatching {
        return this.assets.open(fileName).reader().readText()
    }.getOrNull()
}

fun Context.openRateUs() {
    try {
        val packageName = this.packageName
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "market://details?id=$packageName".toUri()
            )
        )
    } catch (e: ActivityNotFoundException) {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()
            )
        )
    }
}

fun Context.openWebPage(webpage: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, webpage.toUri())
        Intent.createChooser(intent, this.getString(R.string.choose_web_browser))
        this.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Define what your app should do if no activity can handle the intent.
    }
}