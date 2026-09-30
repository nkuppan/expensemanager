package com.naveenapps.expensemanager.core.model

import java.util.Date
import java.util.UUID

data class ParsedTransaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val transactionType: TransactionType,
    val dateTime: Date,
    val senderVpa: String,
    val senderName: String,
    val receiverVpa: String,
    val receiverName: String,
    val referenceId: String,
    val bankName: String,
    val accountNumber: String,
    val payOrCollect: String,
    val status: String,
    val rawText: String = "",
    val parseError: String? = null,
    /**
     * The source statement listed a date but no time of day (an SBI YONO
     * account statement, for example). Duplicate detection widens to the whole
     * day for these rows instead of requiring an exact timestamp match.
     */
    val isDateOnly: Boolean = false,
) {
    val isSuccess: Boolean
        get() = status.equals("SUCCESS", ignoreCase = true)

    val counterpartyVpa: String
        get() = if (transactionType == TransactionType.INCOME) senderVpa else receiverVpa

    val counterpartyName: String
        get() = if (transactionType == TransactionType.INCOME) senderName else receiverName

    val defaultNotes: String
        get() = buildList {
            if (counterpartyName.isNotBlank() && counterpartyName != "N/A") add(counterpartyName)
            if (counterpartyVpa.isNotBlank() && counterpartyVpa != "N/A") add(counterpartyVpa)
            if (referenceId.isNotBlank()) add("Ref:$referenceId")
            if (payOrCollect.isNotBlank()) add(payOrCollect)
        }.joinToString(" ")

    val hasParseError: Boolean
        get() = parseError != null
}
