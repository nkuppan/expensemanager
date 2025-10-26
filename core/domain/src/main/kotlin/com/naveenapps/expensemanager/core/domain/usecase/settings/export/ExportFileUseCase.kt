package com.naveenapps.expensemanager.core.domain.usecase.settings.export

import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetExportTransactionsUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.ExportData
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.ExportRepository

class ExportFileUseCase(
    private val exportRepository: ExportRepository,
    private val getExportTransactionsUseCase: GetExportTransactionsUseCase,
) {

    suspend operator fun invoke(
        exportFileType: ExportFileType,
        uri: String?,
        dateRangeType: DateRangeType,
        accounts: List<AccountUiModel>,
        isAllAccountsSelected: Boolean,
    ): Resource<ExportData> {
        return when (
            val transactions = getExportTransactionsUseCase.invoke(
                dateRangeType,
                accounts.map { it.id },
                isAllAccountsSelected,
            )
        ) {
            is Resource.Error -> {
                transactions
            }

            is Resource.Success -> {
                when (exportFileType) {
                    ExportFileType.CSV -> {
                        exportRepository.createCsvFile(uri, transactions.data)
                    }

                    ExportFileType.PDF -> {
                        exportRepository.createPdfFile(uri, transactions.data)
                    }
                }
            }
        }
    }
}
