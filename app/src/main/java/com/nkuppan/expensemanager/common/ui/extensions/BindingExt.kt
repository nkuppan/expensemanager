package com.nkuppan.expensemanager.common.ui.extensions

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.nkuppan.expensemanager.common.ui.utils.UiText

@BindingAdapter("colorFilter")
fun colorFilter(view: ImageView, colorValue: String) {
    view.setColorFilter(
        Color.parseColor(colorValue.ifEmpty { "#000000" }),
        PorterDuff.Mode.SRC_ATOP
    )
}

@BindingAdapter("visible")
fun visible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("setIcon")
fun setIcon(view: ImageView, icon: String) {
    view.setImageResource(view.context.getDrawable(icon))
}

@BindingAdapter("checkErrorMessage")
fun checkErrorMessage(textInputLayout: TextInputLayout, textValue: String?) {
    if (textValue?.isNotEmpty() == true) {
        textInputLayout.error = ""
    }
}

@BindingAdapter("setUiText")
fun setUiText(textView: TextView, uiText: UiText) {
    textView.text = uiText.asString(textView.context)
}