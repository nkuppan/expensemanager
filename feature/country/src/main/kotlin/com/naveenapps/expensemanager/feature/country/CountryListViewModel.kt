package com.naveenapps.expensemanager.feature.country

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.country.GetCountiesUseCase
import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.TextFieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryListViewModel @Inject constructor(
    private val getCountiesUseCase: GetCountiesUseCase
) : ViewModel() {

    private val _countryState = MutableStateFlow(
        CountryState(
            countries = emptyList(),
            searchText = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setSearchTextChange
            )
        )
    )
    val countryState = _countryState.asStateFlow()

    private var currentJob: Job? = null

    init {
        loadCountries()
    }

    private fun loadCountries(countryName: String? = "") {

        currentJob?.cancel()

        currentJob = viewModelScope.launch {

            val countryList = getCountiesUseCase.invoke()

            val filteredList = mutableListOf<Country>()

            if (countryName?.isNotBlank() == true && countryList.isNotEmpty()) {
                filteredList.addAll(
                    countryList.filter {
                        it.name.contains(countryName, ignoreCase = true)
                    }
                )
            } else {
                filteredList.addAll(countryList)
            }

            _countryState.update { it.copy(countries = filteredList) }
        }
    }

    private fun setSearchTextChange(searchString: String) {
        _countryState.update {
            it.copy(
                searchText = it.searchText.copy(value = searchString)
            )
        }
        loadCountries(searchString)
    }
}