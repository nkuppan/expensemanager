package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.RecurringTransactionDao
import com.naveenapps.expensemanager.core.model.RecurringTransaction
import com.naveenapps.expensemanager.core.repository.RecurringTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date

class RecurringTransactionRepositoryImpl(
    private val dao: RecurringTransactionDao,
    private val dispatchers: AppCoroutineDispatchers,
) : RecurringTransactionRepository {

    override fun getAll(): Flow<List<RecurringTransaction>> =
        dao.getAll().map { entities -> entities.mapNotNull { it.toDomainModel() } }

    override suspend fun getDue(dueBefore: Date): List<RecurringTransaction> = withContext(dispatchers.io) {
        dao.getDue(dueBefore).mapNotNull { it.toDomainModel() }
    }

    override suspend fun add(recurring: RecurringTransaction) = withContext(dispatchers.io) {
        dao.insert(recurring.toEntityModel())
        Unit
    }

    override suspend fun update(recurring: RecurringTransaction) = withContext(dispatchers.io) {
        dao.update(recurring.toEntityModel())
    }

    override suspend fun delete(id: String) = withContext(dispatchers.io) {
        dao.deleteById(id)
    }
}
