package com.naveenapps.expensemanager.core.repository

interface ActivityComponentProvider {

    fun getBackupRepository(): BackupRepository

    fun getShareRepository(): ShareRepository
}