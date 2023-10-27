package com.nkuppan.expensemanager.domain.repository

import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.model.Transaction

interface ExportRepository {

    suspend fun createCsvFile(uri: String?, transactions: List<Transaction>): Resource<Boolean>

    suspend fun createPdfFile(uri: String?, transactions: List<Transaction>): Resource<Boolean>
}