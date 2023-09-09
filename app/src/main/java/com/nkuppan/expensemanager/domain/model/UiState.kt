package com.nkuppan.expensemanager.domain.model

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class UiState<out R> {

    object Loading : UiState<Nothing>()

    data class Success<out T>(val data: T) : UiState<T>()

    object Empty : UiState<Nothing>()
}