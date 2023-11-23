package com.naveenapps.expensemanager.core.domain.repository

import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction

interface ExportRepository {

    suspend fun createCsvFile(uri: String?, transactions: List<Transaction>): Resource<String?>

    suspend fun createPdfFile(uri: String?, transactions: List<Transaction>): Resource<String?>
}