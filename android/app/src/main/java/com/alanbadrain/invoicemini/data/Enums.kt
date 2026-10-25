package com.alanbadrain.invoicemini.data

enum class InvoiceStatus(val label: String) {
    DRAFT("Draft"),
    SENT("Sent"),
    PAID("Paid")
}

enum class DiscountType(val label: String) {
    FIXED("Fixed Amount"),
    PERCENTAGE("Percentage")
}
