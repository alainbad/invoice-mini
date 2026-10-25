package com.alanbadrain.invoicemini.util

import com.alanbadrain.invoicemini.data.DiscountType
import com.alanbadrain.invoicemini.data.LineItemEntity

data class InvoiceTotals(
    val subtotal: Double,
    val discount: Double,
    val taxAmount: Double,
    val grandTotal: Double
)

object InvoiceCalculations {
    /** Tax is computed on (subtotal - discount). Percentage discount is a % of subtotal;
     * fixed discount is an absolute amount, clamped to not exceed the subtotal. */
    fun calculateTotals(
        items: List<LineItemEntity>,
        taxRate: Double,
        discount: Double,
        discountType: DiscountType
    ): InvoiceTotals {
        val subtotal = items.sumOf { it.total }
        val discountAmount = when (discountType) {
            DiscountType.FIXED -> discount.coerceIn(0.0, subtotal)
            DiscountType.PERCENTAGE -> subtotal * (discount.coerceIn(0.0, 100.0) / 100.0)
        }
        val discountedSubtotal = (subtotal - discountAmount).coerceAtLeast(0.0)
        val taxAmount = discountedSubtotal * (taxRate / 100.0)
        val grandTotal = discountedSubtotal + taxAmount
        return InvoiceTotals(subtotal, discountAmount, taxAmount, grandTotal)
    }
}
