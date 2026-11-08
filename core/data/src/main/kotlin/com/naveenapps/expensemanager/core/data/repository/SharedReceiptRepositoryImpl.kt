package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.repository.SharedReceipt
import com.naveenapps.expensemanager.core.repository.SharedReceiptRepository
import java.util.concurrent.atomic.AtomicReference

class SharedReceiptRepositoryImpl : SharedReceiptRepository {

    private val pending = AtomicReference<SharedReceipt?>(null)

    override fun put(receipt: SharedReceipt) {
        pending.set(receipt)
    }

    override fun take(): SharedReceipt? = pending.getAndSet(null)
}
