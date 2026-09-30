package com.naveenapps.expensemanager.feature.transaction.import.review

import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date

sealed class ImportAction {
    data object ClosePage : ImportAction()
    data object StartParsing : ImportAction()
    data class ParsedTextReceived(val text: String) : ImportAction()
    data class ParseFailed(val message: String) : ImportAction()

    /** Extraction is waiting on the user (a PDF password), so stop the spinner. */
    data object ParsingFinished : ImportAction()

    /** Read the remembered password so the password dialog can prefill it. */
    data object LoadRememberedPassword : ImportAction()
    data class UpdateRememberPassword(val remember: Boolean) : ImportAction()

    /** Unlock was tapped: persist the password only if the box is checked. */
    data class SubmitPassword(val password: String) : ImportAction()
    data object SwitchToCard : ImportAction()
    data class OpenCard(val draftId: String) : ImportAction()
    data object SwitchToList : ImportAction()
    data object AcceptCurrent : ImportAction()
    data object RejectCurrent : ImportAction()
    data object UndoLast : ImportAction()
    data class UpdateAmount(val draftId: String, val amount: String) : ImportAction()
    data class UpdateNotes(val draftId: String, val notes: String) : ImportAction()
    data class UpdateDateTime(val draftId: String, val dateTime: Date) : ImportAction()
    data class UpdateType(val draftId: String, val type: TransactionType) : ImportAction()
    data class SelectAccount(val draftId: String, val account: AccountUiModel) : ImportAction()
    data class SelectCategory(val draftId: String, val category: Category) : ImportAction()
    data class ToggleSelection(val draftId: String) : ImportAction()
    data object SelectAll : ImportAction()
    data object DeselectAll : ImportAction()
    data object ConfirmSelected : ImportAction()
    data object ClearLastAction : ImportAction()
    data class ShowAccountSelection(val draftId: String) : ImportAction()
    data class ShowCategorySelection(val draftId: String) : ImportAction()
    data class ShowDateSelection(val draftId: String) : ImportAction()
    data class ShowTimeSelection(val draftId: String) : ImportAction()
    data class DismissSelectors(val draftId: String) : ImportAction()
    data object ClearError : ImportAction()
}
