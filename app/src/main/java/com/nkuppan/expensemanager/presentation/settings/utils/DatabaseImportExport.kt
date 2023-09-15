package com.nkuppan.expensemanager.presentation.settings.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.fragment.app.Fragment
import com.nkuppan.expensemanager.R
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.FileChannel


const val REQUEST_CODE_IMPORT_EXPORT: Int = 1000

object DatabaseImportExport {

    private const val DATA_BASE_NAME = "expense_manager_database.db"

    private const val PACKAGE_NAME = "com.nkuppan.expensemanager"

    private val DATABASE_DATA_DIRECTORY = Environment.getDataDirectory().toString() +
            "/data/" + PACKAGE_NAME +
            "/databases/"

    suspend fun exportDB(aFragment: Fragment, aDatabaseFile: String): String? {

        if (!ExcelFileExport.verifyStoragePermissions(aFragment)) return null

        return exportDatabase(aFragment.requireContext(), aDatabaseFile)
    }

    private suspend fun exportDatabase(aContext: Context, aDatabaseFile: String): String? {

        delay(1000)

        val source: FileChannel?

        val destination: FileChannel?

        val backupDBPath = "${(System.currentTimeMillis())}_${aDatabaseFile}"

        val backupPath = FileUtils.getBackupFilePath(aContext)

        val currentDB = File(DATABASE_DATA_DIRECTORY, aDatabaseFile)

        val backupDBFile = File(backupPath, backupDBPath)

        try {

            if (!backupDBFile.exists()) {
                backupDBFile.createNewFile()
            }

            source = FileInputStream(currentDB).channel
            destination = FileOutputStream(backupDBFile).channel
            destination.transferFrom(source, 0, source.size())
            source.close()
            destination.close()
            return backupDBPath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    suspend fun extractImportFile(aContext: Context, data: Intent): Boolean {

        delay(1000)

        val uri: Uri? = data.data

        if (uri != null) {

            var myOutput: OutputStream? = null

            val myInput: InputStream? = aContext.contentResolver.openInputStream(uri)

            if (myInput != null) {

                try {

                    aContext.deleteDatabase(DATA_BASE_NAME)

                    delay(1000)

                    val outputPath = File(DATABASE_DATA_DIRECTORY)

                    if (!outputPath.exists()) {
                        outputPath.mkdirs()
                    }

                    val outFileName = File(outputPath, DATA_BASE_NAME)

                    if (!outFileName.exists()) {
                        outFileName.createNewFile()
                    }

                    myOutput = FileOutputStream(outFileName)
                    val buffer = ByteArray(1024)
                    var length: Int

                    while (myInput.read(buffer).also { length = it } > 0) {
                        myOutput.write(buffer, 0, length)
                    }

                    return true
                } catch (aException: Exception) {
                    aException.printStackTrace()
                } finally {
                    myOutput?.flush()
                    myInput.close()
                }
            }
        }

        return false
    }

    fun readImportFile(aFragment: Fragment) {

        if (!ExcelFileExport.verifyStoragePermissions(aFragment)) return

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = ("*/*")
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            aFragment.startActivityForResult(
                Intent.createChooser(
                    intent,
                    aFragment.getString(R.string.backup_file_message)
                ),
                REQUEST_CODE_IMPORT_EXPORT
            )
        } catch (aException: ActivityNotFoundException) {
            aException.printStackTrace()
        }
    }
}