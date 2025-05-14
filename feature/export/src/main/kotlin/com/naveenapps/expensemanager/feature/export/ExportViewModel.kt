package com.naveenapps.expensemanager.feature.export

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.designsystem.ui.utils.UiText
import com.naveenapps.expensemanager.core.domain.usecase.settings.export.ExportFileUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    getDateRangeUseCase: GetDateRangeUseCase,
    private val exportFileUseCase: ExportFileUseCase,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _event = Channel<ExportEvent>()
    val event = _event.receiveAsFlow()

    private val _state = MutableStateFlow(
        ExportState(
            isLoading = true,
            selectedDateRange = DateRangeType.TODAY,
            selectedDateRangeText = UiText.StringResource(com.naveenapps.expensemanager.core.data.R.string.today),
            fileType = ExportFileType.CSV,
            accountCount = UiText.StringResource(R.string.all),
            isAllAccountSelected = true,
            selectedAccounts = emptyList(),
            showAccountSelection = false,
        )
    )
    val state = _state.asStateFlow()

    init {
        getDateRangeUseCase.invoke().map { dateRange ->
            _state.update {
                it.copy(
                    selectedDateRange = dateRange.type,
                    selectedDateRangeText = if (dateRange.description.isBlank()) {
                        UiText.StringResource(R.string.all_time)
                    } else {
                        UiText.DynamicString(dateRange.description)
                    }
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun setExportFileType(exportFileType: ExportFileType) {
        _state.update {
            it.copy(fileType = exportFileType)
        }
    }

    private fun setAccounts(selectedAccounts: List<AccountUiModel>, isAllSelected: Boolean) {
        _state.update {
            it.copy(
                selectedAccounts = selectedAccounts,
                isAllAccountSelected = isAllSelected,
                accountCount = if (isAllSelected) {
                    UiText.StringResource(R.string.all)
                } else {
                    UiText.DynamicString(selectedAccounts.size.toString())
                },
                showAccountSelection = false
            )
        }
    }

    fun export(uri: Uri?) {
        viewModelScope.launch {

            val response = exportFileUseCase.invoke(
                _state.value.fileType,
                uri?.toString(),
                _state.value.selectedDateRange,
                _state.value.selectedAccounts,
                _state.value.isAllAccountSelected,
            )
            when (response) {
                is Resource.Error -> {
                    _event.send(
                        ExportEvent.Error(
                            UiText.StringResource(R.string.export_error_message)
                        )
                    )
                }

                is Resource.Success -> {
                    _event.send(
                        ExportEvent.FileExported(
                            message = UiText.StringResource(R.string.export_success_message),
                            exportData = response.data,
                        )
                    )
                }
            }
        }
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    fun processAction(action: ExportAction) {
        when (action) {
            ExportAction.ClosePage -> closePage()
            is ExportAction.StartExport -> {
                export(action.uri?.toUri())
            }

            ExportAction.CloseAccountSelection -> {
                _state.update { it.copy(showAccountSelection = false) }
            }

            ExportAction.OpenAccountSelection -> {
                _state.update { it.copy(showAccountSelection = true) }
            }

            is ExportAction.ChangeFileType -> {
                setExportFileType(action.fileType)
            }

            is ExportAction.AccountSelection -> {
                setAccounts(
                    selectedAccounts = action.accounts,
                    isAllSelected = action.isAllAccountSelected
                )
            }
        }
    }
}
