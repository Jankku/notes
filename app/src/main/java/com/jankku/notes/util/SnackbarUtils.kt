package com.jankku.notes.util

import android.view.View
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

fun showSnackBar(view: View, message: String, duration: Int = BaseTransientBottomBar.LENGTH_SHORT) {
    Snackbar.make(view, message, duration).show()
}