package com.nkuppan.expensemanager.feature.transaction.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    var searchText = MutableLiveData<String>()

    var searchHintText = MutableLiveData<String>()

    private val _backNavigation = Channel<Unit>()
    val backNavigation = _backNavigation.receiveAsFlow()

    private val _voiceSearch = Channel<Unit>()
    val voiceSearch = _voiceSearch.receiveAsFlow()

    init {
        searchText.value = ""
    }

    fun voiceSearch() {
        viewModelScope.launch {
            _voiceSearch.send(Unit)
        }
    }

    fun setSearchHintText() {
        viewModelScope.launch {
            _voiceSearch.send(Unit)
        }
    }

    fun clearSearch() {
        searchText.value = ""
    }

    fun backNavigation() {
        viewModelScope.launch {
            _backNavigation.send(Unit)
        }
    }
}
