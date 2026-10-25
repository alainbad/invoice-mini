package com.alanbadrain.invoicemini.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(value: Double, code: String? = null): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        if (code != null) {
            val currency = java.util.Currency.getInstance(code)
            formatter.currency = currency
            formatter.minimumFractionDigits = currency.defaultFractionDigits.coerceAtLeast(0)
            formatter.maximumFractionDigits = currency.defaultFractionDigits.coerceAtLeast(0)
        }
        return formatter.format(value)
    }
}
