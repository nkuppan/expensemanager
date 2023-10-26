package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.Resource

interface ExportRepository {

    suspend fun createCsvFile(uri: String?): Resource<Boolean>

    suspend fun createPdfFile(uri: String?): Resource<Boolean>
}