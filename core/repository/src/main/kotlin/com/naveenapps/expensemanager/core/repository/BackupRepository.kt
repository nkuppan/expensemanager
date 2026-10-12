package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Resource

interface BackupRepository {

    /** Suggested name for the file the user picks in the system "create document" picker. */
    fun backupFileName(): String

    /**
     * Writes an encrypted snapshot of the database and the app's images to [uri] (a content://
     * document). With a [passphrase] the backup can only be restored with it; without one it
     * uses the app's built-in key, as before.
     */
    suspend fun backupData(uri: String, passphrase: String? = null): Resource<Boolean>

    /**
     * Validates the backup at [uri] and stages it. On success the app must restart; the staged
     * database replaces the current one before Room opens it again.
     */
    suspend fun restoreData(uri: String, passphrase: String? = null): Resource<Boolean>
}

class BackupException(val reason: Reason, cause: Throwable? = null) : Exception(reason.name, cause) {
    enum class Reason {
        /** Not an Expense Manager backup, or it couldn't be decrypted. */
        INVALID_FILE,

        /** Made by a newer app version with a newer database schema. */
        NEWER_VERSION,

        /** The file couldn't be read or written. */
        IO,

        /** The backup is protected with a passphrase and none was given. */
        PASSPHRASE_REQUIRED,

        /** The given passphrase doesn't decrypt the backup. */
        WRONG_PASSPHRASE,
    }
}
