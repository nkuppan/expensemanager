package com.nkuppan.expensemanager.presentation.settings.export

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nkuppan.expensemanager.R
import com.nkuppan.expensemanager.domain.model.DateRangeType
import com.nkuppan.expensemanager.domain.model.ExportFileType
import com.nkuppan.expensemanager.domain.model.Resource
import com.nkuppan.expensemanager.domain.usecase.settings.daterange.GetDateRangeUseCase
import com.nkuppan.expensemanager.domain.usecase.settings.export.ExportFileUseCase
import com.nkuppan.expensemanager.presentation.account.list.AccountUiModel
import com.nkuppan.expensemanager.ui.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    getDateRangeUseCase: GetDateRangeUseCase,
    private val exportFileUseCase: ExportFileUseCase,
) : ViewModel() {

    private val _error = MutableSharedFlow<UiText?>()
    val error = _error.asSharedFlow()

    private val _success = MutableSharedFlow<ExportData?>()
    val success = _success.asSharedFlow()

    private val _selectedDateRange = MutableStateFlow<String?>(null)
    val selectedDateRange = _selectedDateRange.asStateFlow()

    private val _exportFileType = MutableStateFlow(ExportFileType.CSV)
    val exportFileType = _exportFileType.asStateFlow()

    private val _accountCount = MutableStateFlow<UiText>(UiText.StringResource(R.string.all_time))
    val accountCount = _accountCount.asStateFlow()

    private var selectedDateRangeType = DateRangeType.TODAY
    private var selectedAccounts = emptyList<AccountUiModel>()
    private var isAllAccountsSelected = true

    init {
        getDateRangeUseCase.invoke().map {
            selectedDateRangeType = it.type
            _selectedDateRange.value = it.description
        }.launchIn(viewModelScope)
    }

    fun setExportFileType(exportFileType: ExportFileType) {
        this._exportFileType.value = exportFileType
    }

    fun setAccounts(selectedAccounts: List<AccountUiModel>, isAllSelected: Boolean) {
        this.selectedAccounts = selectedAccounts
        this.isAllAccountsSelected = isAllSelected
        _accountCount.value = if (isAllSelected) {
            UiText.StringResource(R.string.all_time)
        } else {
            UiText.DynamicString(selectedAccounts.size.toString())
        }
    }

    fun export(uri: Uri?) {
        viewModelScope.launch {
            val response = exportFileUseCase.invoke(
                _exportFileType.value,
                uri?.toString(),
                selectedDateRangeType,
                selectedAccounts,
                isAllAccountsSelected
            )
            when (response) {
                is Resource.Error -> {
                    _error.emit(UiText.StringResource(R.string.export_error_message))
                }

                is Resource.Success -> {
                    _success.emit(
                        ExportData(
                            message = UiText.StringResource(R.string.export_success_message),
                            fileUri = response.data ?: ""
                        )
                    )
                }
            }
        }
    }
}

data class ExportData(
    val message: UiText,
    val fileUri: String
)