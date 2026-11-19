package com.naveenapps.expensemanager.feature.settings.exchangemethod

import com.naveenapps.expensemanager.core.model.ExchangeMethod
import com.naveenapps.expensemanager.core.model.StaticExchangeRate

sealed class ExchangeMethodAction {

    data object ClosePage : ExchangeMethodAction()

    data class SelectMethod(val method: ExchangeMethod) : ExchangeMethodAction()

    data object OpenApiProfile : ExchangeMethodAction()

    data object AddStaticRate : ExchangeMethodAction()

    data class EditStaticRate(val rate: StaticExchangeRate) : ExchangeMethodAction()

    data class DeleteStaticRate(val rate: StaticExchangeRate) : ExchangeMethodAction()

    data class ChangeEditorBase(val code: String) : ExchangeMethodAction()

    data class ChangeEditorQuote(val code: String) : ExchangeMethodAction()

    data class ChangeEditorBuy(val text: String) : ExchangeMethodAction()

    data class ChangeEditorSell(val text: String) : ExchangeMethodAction()

    data object SaveEditor : ExchangeMethodAction()

    data object DismissEditor : ExchangeMethodAction()
}
