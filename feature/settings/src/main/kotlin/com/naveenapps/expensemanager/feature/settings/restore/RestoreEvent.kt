package com.naveenapps.expensemanager.feature.settings.restore

import androidx.annotation.StringRes

sealed class RestoreEvent {

    /** Open the system "open document" picker. */
    data object PickFile : RestoreEvent()

    data class ShowMessage(@StringRes val messageResId: Int) : RestoreEvent()

    /** A restore was staged; restarting swaps it in before the database is opened. */
    data object RestartApp : RestoreEvent()
}
