package com.naveenapps.expensemanager.core.common.utils

import androidx.compose.runtime.Stable

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
@Stable
sealed class UiState<out R> {

    data object Loading : UiState<Nothing>()

    data class Success<out T>(val data: T) : UiState<T>()

    data object Empty : UiState<Nothing>()
}
