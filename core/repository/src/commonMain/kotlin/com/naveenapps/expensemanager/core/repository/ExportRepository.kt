package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.ExportData
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction

interface ExportRepository {

    suspend fun createCsvFile(uri: String?, transactions: List<Transaction>): Resource<ExportData>

    suspend fun createPdfFile(uri: String?, transactions: List<Transaction>): Resource<ExportData>
}
