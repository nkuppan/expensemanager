package com.naveenapps.expensemanager.domain.usecase.settings.export

import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.domain.repository.ExportRepository
import com.naveenapps.expensemanager.domain.usecase.transaction.GetExportTransactionsUseCase
import com.naveenapps.expensemanager.presentation.account.list.AccountUiModel
import javax.inject.Inject

class ExportFileUseCase @Inject constructor(
    private val exportRepository: ExportRepository,
    private val getExportTransactionsUseCase: GetExportTransactionsUseCase
) {

    suspend operator fun invoke(
        exportFileType: ExportFileType,
        uri: String?,
        dateRangeType: DateRangeType,
        accounts: List<AccountUiModel>,
        isAllAccountsSelected: Boolean
    ): Resource<String?> {

        return when (
            val transactions = getExportTransactionsUseCase.invoke(
                dateRangeType,
                accounts.map { it.id },
                isAllAccountsSelected
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