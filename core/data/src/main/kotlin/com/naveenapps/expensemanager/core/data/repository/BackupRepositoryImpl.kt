package com.naveenapps.expensemanager.core.data.repository

import android.content.Intent
import android.util.Log
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.BackupRepository
import de.raphaelebner.roomdatabasebackup.core.RoomBackup

class BackupRepositoryImpl(
    private val roomBackup: RoomBackup,
    private val database: ExpenseManagerDatabase
) : BackupRepository {

    override fun backupData(uri: String?): Resource<Boolean> {
        roomBackup
            .database(database)
            .enableLogDebug(true)
            .backupIsEncrypted(true)
            .customEncryptPassword("YOUR_SECRET_PASSWORD")
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
            .maxFileCount(5)
            .apply {
                onCompleteListener { success, message, exitCode ->
                    Log.d(TAG, "$message $exitCode")
                    if (success) {
                        restartApp(Intent(context, LAUNCHER))
                    }
                }
            }
            .backup()

        return Resource.Success(true)
    }

    override fun restoreData(uri: String?): Resource<Boolean> {
        roomBackup
            .database(database)
            .enableLogDebug(true)
            .backupIsEncrypted(true)
            .customEncryptPassword("YOUR_SECRET_PASSWORD")
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
            .apply {
                onCompleteListener { success, message, exitCode ->
                    Log.d(TAG, "$message $exitCode")
                    if (success) {
                        restartApp(Intent(context, LAUNCHER))
                    }
                }
            }
            .restore()
        return Resource.Success(true)
    }

    companion object {
        private const val TAG = "Backup"
        private val LAUNCHER = Class.forName("com.naveenapps.expensemanager.MainActivity")
    }
}