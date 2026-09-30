package com.naveenapps.expensemanager.feature.transaction.import.parser

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.IOException
import java.io.Writer

sealed interface PdfExtractResult {
    data class Text(val text: String) : PdfExtractResult

    /**
     * [needsPassword] marks a PDF that is encrypted, or a wrong password, so the
     * caller can ask for the password and retry instead of showing a dead end.
     */
    data class Failure(val reason: String, val needsPassword: Boolean = false) : PdfExtractResult
}

object StatementPdfTextExtractor {

    const val MAX_PDF_BYTES = 15 * 1024 * 1024L
    const val MAX_PDF_PAGES = 50
    const val MAX_TEXT_CHARS = 5_000_000

    const val PASSWORD_REQUIRED = "This PDF is password protected."

    /**
     * SBI does not use one password format for every statement: older statements
     * are sealed with a different format, or with the mobile number registered
     * at the time. Saying so beats a bare "incorrect", which otherwise looks
     * like the app or the user got it wrong.
     */
    const val PASSWORD_INCORRECT =
        "Incorrect password. The password format can differ per statement, and the mobile " +
            "number used may be the one registered at the time - check this statement's " +
            "password in the YONO app."

    @Volatile
    private var initialized = false

    private fun ensureInitialized(context: Context) {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    PDFBoxResourceLoader.init(context.applicationContext)
                    initialized = true
                }
            }
        }
    }

    /**
     * @param password the PDF's user password, or null to try opening it
     *   without one. Bank statements are commonly encrypted, so a retry with the
     *   password the user typed is expected to reuse the same content uri.
     */
    fun extractText(context: Context, uri: Uri, password: String? = null): PdfExtractResult {
        ensureInitialized(context)
        val tempFile = File.createTempFile("statement_import_", ".pdf", context.cacheDir)
        try {
            val bytesCopied = context.contentResolver.openInputStream(uri)?.use { input ->
                var total = 0L
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_PDF_BYTES) return PdfExtractResult.Failure(
                            "PDF is larger than 15 MB. Please use a smaller statement file.",
                        )
                        output.write(buffer, 0, read)
                    }
                }
                total
            } ?: return PdfExtractResult.Failure("Could not open the selected file.")

            if (bytesCopied == 0L) {
                return PdfExtractResult.Failure("The selected file is empty.")
            }
            return extractTextFromFile(tempFile, password)
        } catch (_: Exception) {
            return PdfExtractResult.Failure("Could not read this PDF. Please try another file.")
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    internal fun extractTextFromFile(file: File, password: String? = null): PdfExtractResult {
        var document: PDDocument? = null
        val attemptedPassword = !password.isNullOrEmpty()
        return try {
            document = if (attemptedPassword) {
                // PDFBox keeps the security handler attached after decrypting,
                // so success is signalled by returning, not by isEncrypted.
                PDDocument.load(file, password)
            } else {
                PDDocument.load(file)
            }
            if (document.numberOfPages > MAX_PDF_PAGES) {
                return PdfExtractResult.Failure(
                    "PDF has more than $MAX_PDF_PAGES pages. Please use a shorter date range.",
                )
            }
            val stripper = PDFTextStripper()
            stripper.sortByPosition = true
            // Bounded output: the writer throws as soon as extracted text
            // exceeds MAX_TEXT_CHARS, so a compression-bomb PDF cannot force
            // allocation of the full unbounded string.
            val bounded = BoundedWriter(MAX_TEXT_CHARS)
            stripper.writeText(document, bounded)
            val text = bounded.result()
            if (text.isBlank()) {
                PdfExtractResult.Failure("No readable text found in this PDF.")
            } else {
                PdfExtractResult.Text(text)
            }
        } catch (_: InvalidPasswordException) {
            PdfExtractResult.Failure(
                reason = if (attemptedPassword) PASSWORD_INCORRECT else PASSWORD_REQUIRED,
                needsPassword = true,
            )
        } catch (e: TextTooLargeException) {
            PdfExtractResult.Failure(
                "Extracted text is too large. Please use a shorter date range.",
            )
        } catch (_: Exception) {
            PdfExtractResult.Failure("Could not parse this PDF. Please try another file.")
        } finally {
            try {
                document?.close()
            } catch (_: Exception) {
            }
        }
    }

    internal class TextTooLargeException : IOException()

    internal class BoundedWriter(private val maxChars: Int) : Writer() {
        private val sb = StringBuilder()

        override fun write(cbuf: CharArray, off: Int, len: Int) {
            if (sb.length + len > maxChars) throw TextTooLargeException()
            sb.append(cbuf, off, off + len)
        }

        override fun flush() = Unit

        override fun close() = Unit

        fun result(): String = sb.toString()
    }
}
