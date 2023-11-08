package com.naveenapps.expensemanager.domain.repository

import com.naveenapps.expensemanager.domain.model.Resource
import com.naveenapps.expensemanager.domain.model.Transaction

interface ExportRepository {

    suspend fun createCsvFile(uri: String?, transactions: List<Transaction>): Resource<String?>

    suspend fun createPdfFile(uri: String?, transactions: List<Transaction>): Resource<String?>
}