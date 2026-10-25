package com.alanbadrain.invoicemini.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alanbadrain.invoicemini.data.InvoiceStatus
import com.alanbadrain.invoicemini.data.InvoiceWithItems
import com.alanbadrain.invoicemini.ui.theme.Navy
import com.alanbadrain.invoicemini.util.CurrencyFormatter
import com.alanbadrain.invoicemini.util.InvoiceCalculations
import com.alanbadrain.invoicemini.data.DiscountType
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SummaryCard(title: String, value: String, accent: Color = Navy, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color(0xFFF2F2F7), RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = accent)
        Text(title, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun StatusBadge(status: InvoiceStatus) {
    val color = when (status) {
        InvoiceStatus.DRAFT -> Color.Gray
        InvoiceStatus.SENT -> Color(0xFFE0912B)
        InvoiceStatus.PAID -> Color(0xFF2E9E4A)
    }
    Text(
        text = status.label,
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun InvoiceRowCard(invoiceWithItems: InvoiceWithItems, modifier: Modifier = Modifier) {
    val invoice = invoiceWithItems.invoice
    val status = InvoiceStatus.valueOf(invoice.status)
    val discountType = DiscountType.valueOf(invoice.discountType)
    val totals = InvoiceCalculations.calculateTotals(invoiceWithItems.items, invoice.taxRate, invoice.discount, discountType)
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                invoice.clientName.ifBlank { "No client" },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(invoice.invoiceNumber, fontSize = 12.sp, color = Color.Gray)
            Text(dateFormat.format(invoice.invoiceDateMillis), fontSize = 11.sp, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(CurrencyFormatter.format(totals.grandTotal), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            StatusBadge(status)
        }
    }
}
