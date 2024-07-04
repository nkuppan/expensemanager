package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Resource

interface BackupRepository {

    fun backupData(uri: String?): Resource<Boolean>

    fun restoreData(uri: String?): Resource<Boolean>
}