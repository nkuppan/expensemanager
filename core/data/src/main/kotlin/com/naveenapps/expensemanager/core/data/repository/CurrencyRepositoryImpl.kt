package com.naveenapps.expensemanager.core.data.repository

import androidx.annotation.VisibleForTesting
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.CurrencyDataStore
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private const val MINUS_SYMBOL = "-"
private const val LEFT_TO_RIGHT_MARK = "\u200E"
private const val DEFAULT_CURRENCY_SYMBOL = "$"
private const val DEFAULT_CURRENCY_CODE = "USD"
private const val DEFAULT_CURRENCY_NAME = "US Dollars"

@VisibleForTesting
val defaultCurrency = Currency(
    name = DEFAULT_CURRENCY_NAME,
    symbol = DEFAULT_CURRENCY_SYMBOL,
    position = CurrencyPosition.SUFFIX,
    code = DEFAULT_CURRENCY_CODE,
)

class CurrencyRepositoryImpl(
    private val dataStore: CurrencyDataStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val numberFormatRepository: NumberFormatRepository,
) : CurrencyRepository {

    override fun getDefaultCurrency(): Currency = defaultCurrency

    override suspend fun saveCurrency(currency: Currency): Boolean = withContext(dispatchers.io) {
        dataStore.setCurrency(
            name = currency.name,
            symbol = currency.symbol,
            position = currency.position.ordinal,
            code = currency.code,
        )
        true
    }

    override suspend fun setRestoreCurrencyCode(code: String?) = withContext(dispatchers.io) {
        dataStore.setRestoreCurrencyCode(code)
        Unit
    }

    override suspend fun getRestoreCurrencyCode(): String? = withContext(dispatchers.io) {
        dataStore.getRestoreCurrencyCode()
    }

    override fun getSelectedCurrency(): Flow<Currency> = dataStore.getCurrency(defaultCurrency = getDefaultCurrency())

    override fun getFormattedCurrency(amount: Amount): Amount {
        val currency = amount.currency ?: getDefaultCurrency()
        return amount.copy(
            amountString = getCurrency(
                currency = currency,
                amount = amount.amount,
            ),
        )
    }

    private fun getCurrency(
        currency: Currency,
        amount: Double,
    ): String {
        // The sign is placed explicitly below, so format the number without it.
        val formatted = numberFormatRepository.formatForDisplay(amount)
        val isNegative = formatted.contains(MINUS_SYMBOL)
        val currencyFormatted = formatted.replace(MINUS_SYMBOL, "")
        val sign = if (isNegative) MINUS_SYMBOL else ""

        // Sign-like symbols ($, €, £) attach to the number; word symbols (ليرة, ALL, DA) need a
        // space, otherwise they run into the digits ("10,585ليرة").
        val separator = if (currency.symbol.any { it.isLetter() }) " " else ""

        // A right-to-left symbol (ليرة) would otherwise make the whole string lay out RTL and pull
        // the digits to its left in both positions. A left-to-right mark at the start keeps the
        // line LTR, and one between symbol and number stops the digits joining the RTL run, so
        // prefix really shows before the number and suffix after it.
        val isRtlSymbol = currency.symbol.any {
            val direction = Character.getDirectionality(it)
            direction == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                direction == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
        }
        val mark = if (isRtlSymbol) LEFT_TO_RIGHT_MARK else ""

        return when (currency.position) {
            CurrencyPosition.PREFIX -> {
                if (separator.isEmpty()) {
                    // Sign-like symbol: the minus leads the whole amount (-$100).
                    "$sign${currency.symbol}$currencyFormatted"
                } else {
                    // Word symbol: the minus belongs to the number (ليرة -208), not the word.
                    "$mark${currency.symbol}$mark$separator$sign$currencyFormatted"
                }
            }

            CurrencyPosition.SUFFIX -> {
                "$mark$sign${currencyFormatted}$separator${currency.symbol}$mark"
            }
        }
    }
}
