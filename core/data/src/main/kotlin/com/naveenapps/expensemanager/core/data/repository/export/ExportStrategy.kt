package com.naveenapps.expensemanager.core.data.repository.export

import com.naveenapps.expensemanager.core.model.ExportData
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction

interface ExportStrategy {

    fun getFileType(): ExportFileType

    fun export(uri: String?, transactions: List<Transaction>): Resource<ExportData>
}
