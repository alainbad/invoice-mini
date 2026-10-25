package com.alanbadrain.invoicemini.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val lineItemDao: LineItemDao,
    private val businessProfileDao: BusinessProfileDao
) {
    val allInvoices: Flow<List<InvoiceWithItems>> = invoiceDao.getAllInvoicesWithItems()
    val businessProfile: Flow<BusinessProfileEntity?> = businessProfileDao.getProfile()

    suspend fun getInvoice(id: String): InvoiceWithItems? = invoiceDao.getInvoiceWithItemsOnce(id)

    suspend fun generateNextInvoiceNumber(): String {
        val existing = invoiceDao.getAllInvoicesOnce()
        val numbers = existing.mapNotNull { inv ->
            inv.invoiceNumber.filter { it.isDigit() }.toIntOrNull()
        }
        val next = (numbers.maxOrNull() ?: 0) + 1
        return "INV-%04d".format(next)
    }

    suspend fun getBusinessProfileOnce(): BusinessProfileEntity? = businessProfileDao.getProfileOnce()

    suspend fun saveBusinessProfile(profile: BusinessProfileEntity) {
        businessProfileDao.upsert(profile)
    }

    suspend fun saveInvoice(invoice: InvoiceEntity, items: List<LineItemEntity>) {
        invoiceDao.upsertInvoice(invoice)
        lineItemDao.deleteAllForInvoice(invoice.id)
        items.forEach { lineItemDao.upsert(it.copy(invoiceId = invoice.id)) }
    }

    suspend fun deleteInvoice(invoiceWithItems: InvoiceWithItems) {
        invoiceDao.deleteInvoice(invoiceWithItems.invoice)
    }

    suspend fun duplicateInvoice(original: InvoiceWithItems): String {
        val newId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val newNumber = generateNextInvoiceNumber()
        val copy = original.invoice.copy(
            id = newId,
            invoiceNumber = newNumber,
            status = InvoiceStatus.DRAFT.name,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        val copiedItems = original.items.map {
            it.copy(id = UUID.randomUUID().toString(), invoiceId = newId)
        }
        saveInvoice(copy, copiedItems)
        return newId
    }
}
