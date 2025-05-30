package com.naveenapps.expensemanager.feature.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.country.GetCountiesUseCase
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.TextFieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryListViewModel @Inject constructor(
    private val getCountiesUseCase: GetCountiesUseCase
) : ViewModel() {

    private val _event = Channel<CountrySelectionEvent>()
    val event = _event.receiveAsFlow()

    private val _state = MutableStateFlow(
        CountryState(
            countries = emptyList(),
            showClearButton = false,
            searchText = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setSearchTextChange
            )
        )
    )
    val countryState = _state.asStateFlow()

    private var currentJob: Job? = null

    init {
        loadCountries()
    }

    private fun loadCountries(searchName: String? = "") {

        currentJob?.cancel()

        currentJob = viewModelScope.launch {

            val countryList = getCountiesUseCase.invoke()

            val filteredList = mutableListOf<Country>()

            if (searchName?.isNotBlank() == true && countryList.isNotEmpty()) {
                filteredList.addAll(
                    countryList.filter {
                        it.name.contains(
                            searchName,
                            ignoreCase = true
                        ) || (it.currency?.name?.contains(searchName, ignoreCase = true) == true)
                    }
                )
            } else {
                filteredList.addAll(countryList)
            }

            _state.update { it.copy(countries = filteredList) }
        }
    }

    private fun setSearchTextChange(searchString: String) {
        _state.update {
            it.copy(
                searchText = it.searchText.copy(value = searchString),
                showClearButton = searchString.isNotBlank()
            )
        }
        loadCountries(searchString)
    }

    fun processAction(action: CountrySelectionAction) {
        viewModelScope.launch {
            when (action) {
                CountrySelectionAction.ClearText -> {
                    setSearchTextChange("")
                }

                CountrySelectionAction.ClosePage -> {
                    _event.send(CountrySelectionEvent.Dismiss)
                }

                is CountrySelectionAction.SelectCountry -> {
                    _event.send(CountrySelectionEvent.CountrySelected(action.country))
                }
            }
        }
    }
}