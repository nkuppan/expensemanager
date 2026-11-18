package com.naveenapps.expensemanager.feature.about.licenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OpenSourceLicensesState(
    val isLoading: Boolean = true,
    val query: String = "",
    /** Every library, before searching; empty when the build has no licence files. */
    val totalCount: Int = 0,
    val licenses: List<OpenSourceLicense> = emptyList(),
)

sealed interface OpenSourceLicensesAction {
    data class Search(val query: String) : OpenSourceLicensesAction

    data class Open(val license: OpenSourceLicense) : OpenSourceLicensesAction

    data object ClosePage : OpenSourceLicensesAction
}

/** The licences list: every bundled library, searchable by name or licence. */
class OpenSourceLicensesViewModel(
    repository: OpenSourceLicensesRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val all = MutableStateFlow<List<OpenSourceLicense>?>(null)

    val state: StateFlow<OpenSourceLicensesState> = combine(all, query) { licenses, query ->
        OpenSourceLicensesState(
            isLoading = licenses == null,
            query = query,
            totalCount = licenses?.size ?: 0,
            licenses = licenses.orEmpty().filter { it.matches(query) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), OpenSourceLicensesState())

    init {
        viewModelScope.launch { all.value = repository.getLicenses() }
    }

    fun processAction(action: OpenSourceLicensesAction) {
        when (action) {
            is OpenSourceLicensesAction.Search -> query.update { action.query }

            is OpenSourceLicensesAction.Open ->
                appComposeNavigator.navigate(ExpenseManagerScreens.OpenSourceLicenseDetail(action.license.name))

            OpenSourceLicensesAction.ClosePage -> appComposeNavigator.popBackStack()
        }
    }

    private fun OpenSourceLicense.matches(query: String): Boolean {
        val words = query.trim().lowercase(Locale.ROOT)
        if (words.isEmpty()) return true
        return words in name.lowercase(Locale.ROOT) || kind.label?.lowercase(Locale.ROOT)?.contains(words) == true
    }

    private companion object {
        const val STOP_TIMEOUT = 5_000L
    }
}

data class OpenSourceLicenseDetailState(
    val isLoading: Boolean = true,
    val name: String = "",
    val license: OpenSourceLicense? = null,
)

/** One library's licence: its full text, or a link when that's all the library ships. */
class OpenSourceLicenseDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: OpenSourceLicensesRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val name = savedStateHandle.get<String>(ExpenseManagerArgsNames.LICENSE_NAME).orEmpty()

    private val _state = MutableStateFlow(OpenSourceLicenseDetailState(name = name))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val license = repository.findLicense(name)
            _state.update { it.copy(isLoading = false, license = license) }
        }
    }

    fun closePage() {
        appComposeNavigator.popBackStack()
    }
}
