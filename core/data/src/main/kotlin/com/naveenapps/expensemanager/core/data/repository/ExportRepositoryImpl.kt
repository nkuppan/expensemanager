package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.repository.export.ExportStrategy
import com.naveenapps.expensemanager.core.model.ExportData
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.ExportRepository
import kotlinx.coroutines.withContext

class ExportRepositoryImpl(
    private val dispatchers: AppCoroutineDispatchers,
    private val strategies: List<ExportStrategy>,
) : ExportRepository {

    override suspend fun createCsvFile(
        uri: String?,
        transactions: List<Transaction>,
    ): Resource<ExportData> = withContext(dispatchers.io) {
        strategy(ExportFileType.CSV).export(uri, transactions)
    }

    override suspend fun createPdfFile(
        uri: String?,
        transactions: List<Transaction>,
    ): Resource<ExportData> = withContext(dispatchers.io) {
        strategy(ExportFileType.PDF).export(uri, transactions)
    }

    private fun strategy(type: ExportFileType): ExportStrategy =
        strategies.first { it.getFileType() == type }
}
