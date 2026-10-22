package com.naveenapps.expensemanager.demo

import androidx.appcompat.app.AppCompatDelegate
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.domain.usecase.account.AddAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.AddBudgetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.AddCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.country.GetCountriesUseCase
import com.naveenapps.expensemanager.core.domain.usecase.recurring.CreateRecurringTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.SetPreloadStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import com.naveenapps.expensemanager.initializer.BASE_CATEGORY_LIST
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext

/**
 * DEBUG BUILDS ONLY. Replaces everything in the app with [DemoDataGenerator]'s sample data,
 * going through the normal use cases so balances, budgets and recurring rules behave exactly
 * as if a person had entered them.
 */
class DemoDataSeeder {

    data class Result(val transactions: Int, val currencyCode: String, val language: String)

    /**
     * @param currencyCode ISO code such as "INR"; null = the currency already chosen in the app.
     * @param language "de", "ja"…; null = the app's chosen language (or the device's).
     */
    suspend fun seed(currencyCode: String?, language: String?, months: Int): Result {
        val koin = GlobalContext.get()
        val now = Date()

        // 1. Start from an empty database (same tables a fresh install has).
        withContext(Dispatchers.IO) { koin.get<ExpenseManagerDatabase>().clearAllTables() }

        // 2. Built-in categories, exactly like a first launch.
        val addCategory = koin.get<AddCategoryUseCase>()
        BASE_CATEGORY_LIST.forEach { addCategory(it.copy(createdOn = now, updatedOn = now)) }

        // 3. Currency.
        val lang = (language ?: appLanguage()).lowercase(Locale.ROOT)
        val currency = resolveCurrency(currencyCode)
        koin.get<SaveCurrencyUseCase>()(currency)
        val fractionDigits = runCatching { java.util.Currency.getInstance(currency.code).defaultFractionDigits }.getOrDefault(2)
        fun money(usd: Double) = DemoCurrency.convert(usd, currency.code, fractionDigits.coerceAtLeast(0))

        // 4. Accounts, with names in the screenshot language.
        val addAccount = koin.get<AddAccountUseCase>()
        val plan = DemoDataGenerator.generate(today = now, months = months)
        plan.accounts.forEachIndexed { index, demo ->
            addAccount(
                Account(
                    id = demo.id,
                    name = DemoAccountNames.name(demo.kind, lang),
                    type = demo.type,
                    storedIcon = StoredIcon(name = demo.iconName, backgroundColor = demo.color),
                    createdOn = now,
                    updatedOn = now,
                    sequence = index,
                    amount = money(demo.openingUsd),
                    creditLimit = money(demo.creditLimitUsd),
                ),
            )
        }

        // 5. Transactions (oldest first, so balances build up in order) and recurring rules.
        val addTransaction = koin.get<AddTransactionUseCase>()
        val createRecurring = koin.get<CreateRecurringTransactionUseCase>()
        plan.transactions.forEach { demo ->
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                notes = "",
                categoryId = demo.categoryId,
                fromAccountId = demo.fromAccountId,
                toAccountId = demo.toAccountId,
                amount = Amount(money(demo.amountUsd)),
                imagePath = "",
                type = demo.type,
                createdOn = demo.date,
                updatedOn = demo.date,
            )
            addTransaction(transaction)
            demo.repeat?.let { createRecurring(transaction, it) }
        }

        // 6. Budgets for every month shown.
        val addBudget = koin.get<AddBudgetUseCase>()
        plan.budgets.forEach { demo ->
            addBudget(
                Budget(
                    id = UUID.randomUUID().toString(),
                    amount = money(demo.amountUsd),
                    selectedMonth = demo.month.toMonthAndYearKey(),
                    categories = demo.categoryIds,
                    accounts = emptyList(),
                    isAllAccountsSelected = true,
                    isAllCategoriesSelected = demo.categoryIds.isEmpty(),
                    createdOn = now,
                    updatedOn = now,
                ),
            )
        }

        // 7. Skip first-run prompts that would cover the screenshots.
        koin.get<SetPreloadStatusUseCase>()(true)
        koin.get<SetOnboardingStatusUseCase>()(true)
        koin.get<ReminderTimeRepository>().setNotificationPrimerShown()
        koin.get<FeedbackRepository>().setFirstSaveCelebrationPending(false)

        return Result(plan.transactions.size, currency.code, lang)
    }

    private fun appLanguage(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        val locale = if (!appLocales.isEmpty) appLocales[0] else null
        return (locale ?: Locale.getDefault()).language
    }

    private suspend fun resolveCurrency(requested: String?): Currency {
        val koin = GlobalContext.get()
        if (requested == null) {
            // Keep whatever the user picked in Settings → Currency (symbol position included).
            val current = koin.get<GetCurrencyUseCase>()().first()
            if (current.code.isNotBlank()) return current
        }
        val code = requested?.uppercase(Locale.ROOT)
            ?: runCatching { java.util.Currency.getInstance(Locale.getDefault()).currencyCode }.getOrNull()
            ?: "USD"
        val countries = koin.get<GetCountriesUseCase>()()
        return countries.firstOrNull { it.currency.code.equals(code, ignoreCase = true) }?.currency
            ?: countries.first { it.currency.code == "USD" }.currency
    }
}

/** Account names in each of the app's 16 languages, so localized screenshots don't show English. */
object DemoAccountNames {
    private val NAMES: Map<String, Triple<String, String, String>> = mapOf(
        // language to (bank, credit card, cash)
        "en" to Triple("Bank account", "Credit card", "Cash"),
        "ar" to Triple("الحساب البنكي", "بطاقة ائتمان", "نقدًا"),
        "de" to Triple("Girokonto", "Kreditkarte", "Bargeld"),
        "es" to Triple("Cuenta bancaria", "Tarjeta de crédito", "Efectivo"),
        "fr" to Triple("Compte bancaire", "Carte de crédit", "Espèces"),
        "hi" to Triple("बैंक खाता", "क्रेडिट कार्ड", "नकद"),
        "id" to Triple("Rekening bank", "Kartu kredit", "Tunai"),
        "it" to Triple("Conto corrente", "Carta di credito", "Contanti"),
        "ja" to Triple("銀行口座", "クレジットカード", "現金"),
        "ko" to Triple("은행 계좌", "신용카드", "현금"),
        "pl" to Triple("Konto bankowe", "Karta kredytowa", "Gotówka"),
        "pt" to Triple("Conta bancária", "Cartão de crédito", "Dinheiro"),
        "ru" to Triple("Банковский счёт", "Кредитная карта", "Наличные"),
        "tr" to Triple("Banka hesabı", "Kredi kartı", "Nakit"),
        "vi" to Triple("Tài khoản ngân hàng", "Thẻ tín dụng", "Tiền mặt"),
        "zh" to Triple("银行账户", "信用卡", "现金"),
    )

    fun name(kind: DemoAccountKind, language: String): String {
        val names = NAMES[language.substringBefore('-')] ?: NAMES.getValue("en")
        return when (kind) {
            DemoAccountKind.BANK -> names.first
            DemoAccountKind.CARD -> names.second
            DemoAccountKind.CASH -> names.third
        }
    }
}
