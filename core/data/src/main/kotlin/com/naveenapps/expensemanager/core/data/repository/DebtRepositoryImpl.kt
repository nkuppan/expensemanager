package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.database.dao.DebtDao
import com.naveenapps.expensemanager.core.database.entity.DebtEntryEntity
import com.naveenapps.expensemanager.core.database.entity.DebtPersonEntity
import com.naveenapps.expensemanager.core.model.DebtEntry
import com.naveenapps.expensemanager.core.model.DebtEntryKind
import com.naveenapps.expensemanager.core.model.DebtPerson
import com.naveenapps.expensemanager.core.repository.DebtRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DebtRepositoryImpl(
    private val dao: DebtDao,
    private val dispatchers: AppCoroutineDispatchers,
) : DebtRepository {

    override fun getPeople(): Flow<List<DebtPerson>> = dao.getPeople().map { people -> people.map { it.toDomain() } }

    override fun getEntries(): Flow<List<DebtEntry>> = dao.getEntries().map { entries -> entries.mapNotNull { it.toDomain() } }

    override fun getEntries(personId: String): Flow<List<DebtEntry>> = dao.getEntriesForPerson(personId).map { entries -> entries.mapNotNull { it.toDomain() } }

    override suspend fun getAllEntries(): List<DebtEntry> = withContext(dispatchers.io) {
        dao.getEntriesOnce().mapNotNull { it.toDomain() }
    }

    override suspend fun findPerson(id: String): DebtPerson? = withContext(dispatchers.io) {
        dao.findPerson(id)?.toDomain()
    }

    override suspend fun findEntry(id: String): DebtEntry? = withContext(dispatchers.io) {
        dao.findEntry(id)?.toDomain()
    }

    override suspend fun savePerson(person: DebtPerson) = withContext(dispatchers.io) {
        dao.upsertPerson(person.toEntity())
    }

    override suspend fun deletePerson(id: String) = withContext(dispatchers.io) {
        dao.deletePerson(id)
    }

    override suspend fun addEntry(entry: DebtEntry) = withContext(dispatchers.io) {
        dao.insertEntry(entry.toEntity())
    }

    override suspend fun updateEntry(entry: DebtEntry): Boolean = withContext(dispatchers.io) {
        dao.updateEntry(entry.toEntity())
    }

    override suspend fun deleteEntry(id: String) = withContext(dispatchers.io) {
        dao.deleteEntry(id)
    }
}

internal fun DebtPersonEntity.toDomain() = DebtPerson(id = id, name = name, createdOn = createdOn, updatedOn = updatedOn)

internal fun DebtPerson.toEntity() = DebtPersonEntity(id = id, name = name, createdOn = createdOn, updatedOn = updatedOn)

/** Null for a kind this version doesn't know (a backup from a newer app); it's skipped. */
internal fun DebtEntryEntity.toDomain(): DebtEntry? {
    val kind = DebtEntryKind.entries.find { it.name == kind } ?: return null
    return DebtEntry(
        id = id,
        personId = personId,
        kind = kind,
        amountMilli = amountMilli,
        accountId = accountId,
        date = date,
        dueDate = dueDate,
        note = note,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

internal fun DebtEntry.toEntity() = DebtEntryEntity(
    id = id,
    personId = personId,
    kind = kind.name,
    amountMilli = amountMilli,
    accountId = accountId,
    date = date,
    dueDate = dueDate,
    note = note,
    createdOn = createdOn,
    updatedOn = updatedOn,
)
