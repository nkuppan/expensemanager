package com.nkuppan.expensemanager.domain.usecase.settings.export

import com.nkuppan.expensemanager.domain.model.ExportFileType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.repository.ExportRepository
import javax.inject.Inject

class ExportFileUseCase @Inject constructor(
    private val exportRepository: ExportRepository
) {

    suspend operator fun invoke(exportFileType: ExportFileType): Resource<Boolean> {
        return when (exportFileType) {
            ExportFileType.CSV -> {
                exportRepository.createCsvFile()
            }

            ExportFileType.PDF -> {
                exportRepository.createPdfFile()
            }
        }
    }
}