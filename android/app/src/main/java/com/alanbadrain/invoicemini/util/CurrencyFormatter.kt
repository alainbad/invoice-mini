package com.alanbadrain.invoicemini.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(value: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return formatter.format(value)
    }
}
