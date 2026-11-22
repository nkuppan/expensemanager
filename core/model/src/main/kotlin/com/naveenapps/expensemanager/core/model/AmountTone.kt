package com.naveenapps.expensemanager.core.model

/**
 * Whether an amount reads as money in (or zero) or money out. Models carry this meaning; the UI
 * picks the colour (`emColorOf(tone)`: income or expense from `EmTheme.colors`).
 */
enum class AmountTone {
    POSITIVE,
    NEGATIVE,
    ;

    companion object {
        fun of(value: Double): AmountTone = if (value < 0) NEGATIVE else POSITIVE
    }
}
