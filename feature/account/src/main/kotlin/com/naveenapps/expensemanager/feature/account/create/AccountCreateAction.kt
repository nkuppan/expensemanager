package com.naveenapps.expensemanager.feature.account.create

import android.net.Uri
import com.naveenapps.expensemanager.core.model.Country

sealed class AccountCreateAction {

    data object OpenCurrencySelection : AccountCreateAction()

    data object DismissCurrencySelection : AccountCreateAction()

    data class SelectCurrency(val country: Country) : AccountCreateAction()

    data object ShowDeleteDialog : AccountCreateAction()

    data object DismissDeleteDialog : AccountCreateAction()

    data object ClosePage : AccountCreateAction()

    data object Save : AccountCreateAction()

    data object Delete : AccountCreateAction()

    data object Archive : AccountCreateAction()

    data class ImagePicked(val uri: Uri) : AccountCreateAction()

    data object RemoveImage : AccountCreateAction()
}
