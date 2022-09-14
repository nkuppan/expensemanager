package com.nkuppan.expensemanager.core.ui.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

/**
 * Extension method to show snackbar using the view
 */
fun View.showSnackBarMessage(@StringRes stringRes: Int, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, stringRes, length).show()
}

/**
 * Extension method to show snackbar using the view
 */
fun View.showSnackBarMessage(message: String, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, message, length).show()
}


fun Fragment.setSupportedActionBar(toolbar: Toolbar) {

    val activity = requireActivity()

    if (activity is AppCompatActivity) {
        activity.setSupportActionBar(toolbar)
    }
}

fun Fragment.setTitle(title: String) {

    val activity = requireActivity()

    if (activity is AppCompatActivity) {
        activity.setTitle(title)
    }
}

fun Fragment.setTitle(titleResId: Int) {

    val activity = requireActivity()

    if (activity is AppCompatActivity) {
        activity.setTitle(titleResId)
    }
}

fun View.hideKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(windowToken, 0)
    }
}

/**
 * Setting click listener to the constain
 */
fun Group.setAllOnClickListener(listener: View.OnClickListener?) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}

/**
 * Setting click listener to the constain
 */
fun Group.enable(enable: Boolean) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).isEnabled = enable
    }
}