package com.naveenapps.expensemanager.presentation.settings.utils

import android.content.Context
import android.os.Environment
import java.io.File

object FileUtils {

    private const val ROOT_FOLDER_NAME: String = "Expense Manager"
    private const val EXCEL_FOLDER_NAME: String = "Excel"
    private const val IMAGE_FOLDER_NAME: String = "Images"
    private const val BACKUP_FOLDER_NAME: String = "Backup"

    private fun getFileBasePath(aContext: Context): File {

        val state = Environment.getExternalStorageState()

        return if (Environment.MEDIA_MOUNTED == state) {
            Environment.getExternalStorageDirectory()
        } else {
            aContext.filesDir
        }
    }

    fun getExcelFilePath(aContext: Context): File {

        val excelFilePath = File(
            getFileBasePath(aContext),
            File.separator + ROOT_FOLDER_NAME + File.separator + EXCEL_FOLDER_NAME
        )

        if (!excelFilePath.exists()) {
            excelFilePath.mkdirs()
        }

        return excelFilePath
    }

    fun getImageFilePath(aContext: Context): File {

        val imageFilePath = File(
            getFileBasePath(aContext),
            File.separator + ROOT_FOLDER_NAME + File.separator + IMAGE_FOLDER_NAME
        )

        if (!imageFilePath.exists()) {
            imageFilePath.mkdirs()
        }

        return imageFilePath
    }

    fun getBackupFilePath(aContext: Context): File {

        val backupPath = File(
            getFileBasePath(aContext),
            File.separator + ROOT_FOLDER_NAME + File.separator + BACKUP_FOLDER_NAME
        )

        if (!backupPath.exists()) {
            backupPath.mkdirs()
        }

        return backupPath
    }
}