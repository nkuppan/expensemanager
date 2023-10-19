package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.Resource

interface ExportRepository {

    suspend fun createCsvFile(): Resource<Boolean>

    suspend fun createPdfFile(): Resource<Boolean>
}