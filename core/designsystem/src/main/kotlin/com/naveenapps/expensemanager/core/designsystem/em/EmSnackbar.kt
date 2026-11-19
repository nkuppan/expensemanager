package com.naveenapps.expensemanager.core.designsystem.em

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

/*
 * Snackbars are shown through EmScaffold's host. Features use these names so they never import
 * Material; behaviour is Material's (showSnackbar(message, actionLabel, duration) -> result).
 */
typealias EmSnackbarHostState = SnackbarHostState
typealias EmSnackbarResult = SnackbarResult
typealias EmSnackbarDuration = SnackbarDuration
