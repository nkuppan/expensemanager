package com.naveenapps.expensemanager.core.repository

/** What another app shared with us: a UPI / bank message, a receipt screenshot, or both. */
data class SharedReceipt(
    val text: String?,
    // The shared image's content Uri, as a string (`uri.toString()`).
    val imageUri: String?,
)

/**
 * Hand-off between the share intent (MainActivity) and the add-transaction screen. Holds at
 * most one receipt in memory; [take] hands it out once, so a recreated screen doesn't re-apply it.
 */
interface SharedReceiptRepository {
    fun put(receipt: SharedReceipt)

    fun take(): SharedReceipt?
}
