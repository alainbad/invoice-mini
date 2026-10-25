package com.alanbadrain.invoicemini.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "",
    val email: String? = null,
    val address: String? = null,
    val phone: String? = null,
    val countryCode: String? = null,
    val callingCode: String? = null,
    val taxNumber: String? = null,
    val logoUri: String? = null
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val invoiceDateMillis: Long,
    val dueDateMillis: Long,
    val businessPhone: String? = null,
    val clientPhone: String? = null,
    val businessTaxNumber: String? = null,
    val currencyCode: String? = null,
    val stampPath: String? = null,
    val signaturePath: String? = null,
    val businessName: String = "",
    val businessEmail: String? = null,
    val businessAddress: String? = null,
    val clientName: String = "",
    val clientEmail: String? = null,
    val clientAddress: String? = null,
    val discount: Double = 0.0,
    val discountType: String = DiscountType.FIXED.name,
    val taxRate: Double = 0.0,
    val notes: String? = null,
    val status: String = InvoiceStatus.DRAFT.name,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(tableName = "line_items")
data class LineItemEntity(
    @PrimaryKey val id: String,
    val invoiceId: String,
    val itemDescription: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0
) {
    val total: Double get() = quantity * unitPrice
}

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val items: List<LineItemEntity>
)
