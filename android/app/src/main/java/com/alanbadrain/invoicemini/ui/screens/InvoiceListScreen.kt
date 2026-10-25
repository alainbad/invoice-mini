package com.alanbadrain.invoicemini.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alanbadrain.invoicemini.InvoiceViewModel
import com.alanbadrain.invoicemini.data.InvoiceStatus

@Composable
fun InvoiceListScreen(
    viewModel: InvoiceViewModel,
    onOpenInvoice: (String) -> Unit,
    onBack: () -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf<InvoiceStatus?>(null) }

    val filtered = invoices.filter { inv ->
        val matchesStatus = statusFilter == null || InvoiceStatus.valueOf(inv.invoice.status) == statusFilter
        val matchesSearch = searchText.isBlank() ||
                inv.invoice.clientName.contains(searchText, ignoreCase = true) ||
                inv.invoice.invoiceNumber.contains(searchText, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Invoices", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search client or invoice #") },
            modifier = Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            val options = listOf<InvoiceStatus?>(null) + InvoiceStatus.entries
            options.forEachIndexed { index, status ->
                SegmentedButton(
                    selected = statusFilter == status,
                    onClick = { statusFilter = status },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                ) {
                    Text(status?.label ?: "All")
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))

        LazyColumn {
            items(filtered, key = { it.invoice.id }) { inv ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f).clickable { onOpenInvoice(inv.invoice.id) }
                    ) {
                        com.alanbadrain.invoicemini.ui.components.InvoiceRowCard(inv)
                    }
                    IconButton(onClick = { viewModel.duplicateInvoice(inv) }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate")
                    }
                    IconButton(onClick = { viewModel.deleteInvoice(inv) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
