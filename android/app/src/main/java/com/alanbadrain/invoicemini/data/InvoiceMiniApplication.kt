package com.alanbadrain.invoicemini.data

import android.app.Application

class InvoiceMiniApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: InvoiceRepository by lazy {
        InvoiceRepository(database.invoiceDao(), database.lineItemDao(), database.businessProfileDao())
    }
}
