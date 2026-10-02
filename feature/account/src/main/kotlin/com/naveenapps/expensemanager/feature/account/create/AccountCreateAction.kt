package com.naveenapps.expensemanager.feature.account.create

import android.net.Uri

sealed class AccountCreateAction {

    data object ShowDeleteDialog : AccountCreateAction()

    data object DismissDeleteDialog : AccountCreateAction()

    data object ClosePage : AccountCreateAction()

    data object Save : AccountCreateAction()

    data object Delete : AccountCreateAction()

    data class ImagePicked(val uri: Uri) : AccountCreateAction()

    data object RemoveImage : AccountCreateAction()
}
