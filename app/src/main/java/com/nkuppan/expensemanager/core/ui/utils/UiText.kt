package com.nkuppan.expensemanager.core.ui.utils

import android.content.Context

sealed class UiText {
    /**
     * It can be send from server side to the app
     */
    class DynamicString(val message: String) : UiText()

    /**
     * Constructed string resource from app
     */
    class StringResource(val resId: Int, vararg val args: Any) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is StringResource -> {
                val newValues = if (args.isNotEmpty()) {
                    args.map {
                        if (it is UiText) {
                            it.asString(context)
                        } else {
                            it
                        }
                    }.toTypedArray()
                } else {
                    args
                }

                context.getString(resId, *newValues)
            }

            is DynamicString -> message
        }
    }
}