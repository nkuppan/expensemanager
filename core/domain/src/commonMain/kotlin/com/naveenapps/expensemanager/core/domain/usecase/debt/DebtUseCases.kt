package com.naveenapps.expensemanager.core.domain.usecase.debt

import com.naveenapps.expensemanager.core.model.DebtEntry
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.core.model.DebtPerson
import com.naveenapps.expensemanager.core.model.DebtPersonBalance
import com.naveenapps.expensemanager.core.model.DebtTotals
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.DebtRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Everyone with their open balance, ordered for the People list. */
class GetDebtBalancesUseCase(private val repository: DebtRepository) {
    operator fun invoke(): Flow<List<DebtPersonBalance>> = combine(
        repository.getPeople(),
        repository.getEntries(),
    ) { people, entries -> DebtLedger.balances(people, entries) }
}

/** What the user will get back and what they owe, across everyone. */
class GetDebtTotalsUseCase(private val getDebtBalancesUseCase: GetDebtBalancesUseCase) {
    operator fun invoke(): Flow<DebtTotals> = getDebtBalancesUseCase().map { DebtLedger.totals(it) }
}

/**
 * Adds or edits an entry. A new person is created on the fly from [personName] when
 * [personId] is null (the user typed a name rather than picking someone). Due dates are only
 * kept on lent / borrowed entries.
 */
class SaveDebtEntryUseCase(private val repository: DebtRepository) {

    suspend operator fun invoke(
        entryId: String?,
        personId: String?,
        personName: String,
        kind: DebtEntryKind,
        amountMilli: Long,
        accountId: String?,
        date: Instant,
        dueDate: Instant?,
        note: String,
        now: Instant = Clock.System.now(),
    ): Resource<DebtEntry> {
        if (amountMilli <= 0L) return Resource.Error(IllegalArgumentException("Amount must be above zero"))
        val name = personName.trim()
        val person = when {
            personId != null -> repository.findPerson(personId)
                ?: return Resource.Error(IllegalStateException("Person not found"))

            name.isNotEmpty() -> DebtPerson(Uuid.random().toString(), name, now, now).also {
                repository.savePerson(it)
            }

            else -> return Resource.Error(IllegalArgumentException("Person is required"))
        }
        val existing = entryId?.let { repository.findEntry(it) }
        val entry = DebtEntry(
            id = existing?.id ?: Uuid.random().toString(),
            personId = person.id,
            kind = kind,
            amountMilli = amountMilli,
            accountId = accountId,
            date = date,
            dueDate = dueDate.takeIf { kind.isNewDebt },
            note = note.trim(),
            createdOn = existing?.createdOn ?: now,
            updatedOn = now,
        )
        return try {
            if (existing == null) {
                repository.addEntry(entry)
            } else if (!repository.updateEntry(entry)) {
                return Resource.Error(IllegalStateException("Entry no longer exists"))
            }
            Resource.Success(entry)
        } catch (error: Exception) {
            Resource.Error(error)
        }
    }
}

/**
 * Clears a person's open balance with one repayment entry: "received" when they owed the user,
 * "paid" when the user owed them. [accountId] is where the money went or came from (optional).
 */
class SettleUpDebtUseCase(private val repository: DebtRepository) {

    suspend operator fun invoke(personId: String, accountId: String?, date: Instant = Clock.System.now()): Resource<DebtEntry?> {
        val entries = repository.getAllEntries().filter { it.personId == personId }
        val balance = DebtLedger.balanceOf(entries)
        if (balance == 0L) return Resource.Success(null)
        val entry = DebtEntry(
            id = Uuid.random().toString(),
            personId = personId,
            kind = if (balance > 0) DebtEntryKind.RECEIVED else DebtEntryKind.PAID,
            amountMilli = kotlin.math.abs(balance),
            accountId = accountId,
            date = date,
            dueDate = null,
            note = "",
            createdOn = date,
            updatedOn = date,
        )
        return try {
            repository.addEntry(entry)
            Resource.Success(entry)
        } catch (error: Exception) {
            Resource.Error(error)
        }
    }
}
