package com.alanbadrain.invoicemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alanbadrain.invoicemini.data.BusinessProfileEntity
import com.alanbadrain.invoicemini.data.InvoiceEntity
import com.alanbadrain.invoicemini.data.InvoiceRepository
import com.alanbadrain.invoicemini.data.InvoiceWithItems
import com.alanbadrain.invoicemini.data.LineItemEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {

    val invoices: StateFlow<List<InvoiceWithItems>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessProfile: StateFlow<BusinessProfileEntity?> = repository.businessProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    suspend fun getInvoice(id: String): InvoiceWithItems? = repository.getInvoice(id)

    suspend fun generateNextInvoiceNumber(): String = repository.generateNextInvoiceNumber()

    suspend fun getBusinessProfileOnce(): BusinessProfileEntity? = repository.getBusinessProfileOnce()

    fun saveInvoice(invoice: InvoiceEntity, items: List<LineItemEntity>) {
        viewModelScope.launch { repository.saveInvoice(invoice, items) }
    }

    suspend fun saveInvoiceSync(invoice: InvoiceEntity, items: List<LineItemEntity>) {
        repository.saveInvoice(invoice, items)
    }

    fun deleteInvoice(invoiceWithItems: InvoiceWithItems) {
        viewModelScope.launch { repository.deleteInvoice(invoiceWithItems) }
    }

    fun duplicateInvoice(original: InvoiceWithItems) {
        viewModelScope.launch { repository.duplicateInvoice(original) }
    }

    fun saveBusinessProfile(profile: BusinessProfileEntity) {
        viewModelScope.launch { repository.saveBusinessProfile(profile) }
    }

    class Factory(private val repository: InvoiceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InvoiceViewModel(repository) as T
        }
    }
}