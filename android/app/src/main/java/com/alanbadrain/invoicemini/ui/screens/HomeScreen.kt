package com.alanbadrain.invoicemini.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alanbadrain.invoicemini.InvoiceViewModel
import com.alanbadrain.invoicemini.data.InvoiceStatus
import com.alanbadrain.invoicemini.ui.components.InvoiceRowCard
import com.alanbadrain.invoicemini.ui.components.SummaryCard

@Composable
fun HomeScreen(
    viewModel: InvoiceViewModel,
    onCreateInvoice: () -> Unit,
    onOpenInvoice: (String) -> Unit,
    onOpenList: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val invoices by viewModel.invoices.collectAsState()
    val draftCount = invoices.count { InvoiceStatus.valueOf(it.invoice.status) == InvoiceStatus.DRAFT }
    val paidCount = invoices.count { InvoiceStatus.valueOf(it.invoice.status) == InvoiceStatus.PAID }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenProfile) { Icon(Icons.Filled.Apartment, contentDescription = "Business Profile") }
            IconButton(onClick = onOpenList) { Icon(Icons.Filled.List, contentDescription = "Invoice List") }
        }

        Text("Invoice Mini", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCreateInvoice() }
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text("Create Invoice", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Total Invoices", "${invoices.size}", modifier = Modifier.weight(1f))
            SummaryCard("Drafts", "$draftCount", accent = Color.Gray, modifier = Modifier.weight(1f))
            SummaryCard("Paid", "$paidCount", accent = Color(0xFF2E9E4A), modifier = Modifier.weight(1f))
        }

        androidx.compose.foundation.layout.Spacer(Modifier.size(20.dp))
        Text("Recent Invoices", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        if (invoices.isEmpty()) {
            Text(
                "No invoices yet. Create your first one above.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn {
                items(invoices.take(5)) { inv ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenInvoice(inv.invoice.id) }
                    ) {
                        InvoiceRowCard(inv)
                    }
                }
            }
        }
    }
}
