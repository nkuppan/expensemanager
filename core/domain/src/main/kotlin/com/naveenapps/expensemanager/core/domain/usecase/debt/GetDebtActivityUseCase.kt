package com.naveenapps.expensemanager.core.domain.usecase.debt

import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.DebtEntry
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.DebtRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** A debt entry that moved money in one of the user's accounts, ready to list beside transactions. */
data class DebtActivity(
    val entry: DebtEntry,
    val personName: String,
    val account: Account,
)

/** The Transactions screen's filters, as they apply to debt entries. */
data class DebtActivityFilter(
    val transactionTypes: List<TransactionType>,
    val categories: List<String>,
    val accounts: List<String>,
    /** Null for all dates. */
    val range: LongRange?,
)

/**
 * Debt entries that moved money in an account, so the Transactions list can explain why an
 * account's balance changed ("Lent to Ravi · Cash"). They follow the same account filter and
 * period; they have no category or transaction type, so they're hidden while either of those
 * filters is on. Entries that didn't touch an account aren't listed (nothing to explain).
 */
class GetDebtActivityUseCase(
    private val debtRepository: DebtRepository,
    private val accountRepository: AccountRepository,
    private val settingsRepository: SettingsRepository,
    private val getDateRangeUseCase: GetDateRangeUseCase,
) {

    /** [allTime]: ignore the selected period (search looks across all dates). */
    operator fun invoke(allTime: Boolean): Flow<List<DebtActivity>> {
        val data = combine(
            debtRepository.getEntries(),
            debtRepository.getPeople(),
            accountRepository.getAccounts(),
        ) { entries, people, accounts -> Triple(entries, people, accounts) }
        val filter = combine(
            settingsRepository.getTransactionTypes(),
            settingsRepository.getCategories(),
            settingsRepository.getAccounts(),
            getDateRangeUseCase.invoke(),
        ) { types, categories, accounts, dateRange ->
            DebtActivityFilter(
                transactionTypes = types.orEmpty(),
                categories = categories.orEmpty(),
                accounts = accounts.orEmpty(),
                range = if (allTime || dateRange.type == DateRangeType.ALL || dateRange.dateRanges.size < 2) {
                    null
                } else {
                    dateRange.dateRanges[0]..dateRange.dateRanges[1]
                },
            )
        }
        return combine(data, filter) { (entries, people, accounts), activeFilter ->
            val names = people.associate { it.id to it.name }
            val accountsById = accounts.associateBy { it.id }
            filterDebtActivity(entries, activeFilter).mapNotNull { entry ->
                val account = entry.accountId?.let { accountsById[it] } ?: return@mapNotNull null
                DebtActivity(entry = entry, personName = names[entry.personId].orEmpty(), account = account)
            }
        }
    }
}

/** Which entries the filters let through (pure, for tests). */
fun filterDebtActivity(entries: List<DebtEntry>, filter: DebtActivityFilter): List<DebtEntry> {
    if (filter.transactionTypes.isNotEmpty() || filter.categories.isNotEmpty()) return emptyList()
    return entries.filter { entry ->
        val accountId = entry.accountId ?: return@filter false
        (filter.accounts.isEmpty() || accountId in filter.accounts) &&
            (filter.range == null || entry.date.time in filter.range)
    }
}
