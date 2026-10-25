package com.alanbadrain.invoicemini.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alanbadrain.invoicemini.InvoiceViewModel
import com.alanbadrain.invoicemini.data.DiscountType
import com.alanbadrain.invoicemini.data.InvoiceEntity
import com.alanbadrain.invoicemini.data.InvoiceStatus
import com.alanbadrain.invoicemini.data.LineItemEntity
import com.alanbadrain.invoicemini.util.CurrencyFormatter
import com.alanbadrain.invoicemini.util.InvoiceCalculations
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@Composable
fun InvoiceFormScreen(
    invoiceId: String?,
    viewModel: InvoiceViewModel,
    onPreview: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isNew = invoiceId == null
    val actualId = remember { invoiceId ?: UUID.randomUUID().toString() }

    var invoiceNumber by remember { mutableStateOf("") }
    var invoiceDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var dueDateMillis by remember { mutableStateOf(System.currentTimeMillis() + 14L * 24 * 60 * 60 * 1000) }
    var businessName by remember { mutableStateOf("") }
    var businessEmail by remember { mutableStateOf("") }
    var businessAddress by remember { mutableStateOf("") }
    var clientName by remember { mutableStateOf("") }
    var clientEmail by remember { mutableStateOf("") }
    var clientAddress by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("0") }
    var discountType by remember { mutableStateOf(DiscountType.FIXED) }
    var taxRate by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(InvoiceStatus.DRAFT) }
    var createdAtMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val items = remember { mutableStateListOf<LineItemEntity>() }
    var loaded by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<LineItemEntity?>(null) }
    var showItemDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(actualId) {
        if (!loaded) {
            if (!isNew) {
                val existing = viewModel.getInvoice(actualId)
                if (existing != null) {
                    invoiceNumber = existing.invoice.invoiceNumber
                    invoiceDateMillis = existing.invoice.invoiceDateMillis
                    dueDateMillis = existing.invoice.dueDateMillis
                    businessName = existing.invoice.businessName
                    businessEmail = existing.invoice.businessEmail ?: ""
                    businessAddress = existing.invoice.businessAddress ?: ""
                    clientName = existing.invoice.clientName
                    clientEmail = existing.invoice.clientEmail ?: ""
                    clientAddress = existing.invoice.clientAddress ?: ""
                    discount = existing.invoice.discount.toString()
                    discountType = DiscountType.valueOf(existing.invoice.discountType)
                    taxRate = existing.invoice.taxRate.toString()
                    notes = existing.invoice.notes ?: ""
                    status = InvoiceStatus.valueOf(existing.invoice.status)
                    createdAtMillis = existing.invoice.createdAtMillis
                    items.clear()
                    items.addAll(existing.items)
                }
            } else {
                invoiceNumber = viewModel.generateNextInvoiceNumber()
                val profile = viewModel.getBusinessProfileOnce()
                if (profile != null) {
                    businessName = profile.businessName
                    businessEmail = profile.email ?: ""
                    businessAddress = profile.address ?: ""
                }
            }
            loaded = true
        }
    }

    fun currentTotals() = InvoiceCalculations.calculateTotals(
        items, taxRate.toDoubleOrNull() ?: 0.0, discount.toDoubleOrNull() ?: 0.0, discountType
    )

    fun buildEntity(): InvoiceEntity = InvoiceEntity(
        id = actualId,
        invoiceNumber = invoiceNumber,
        invoiceDateMillis = invoiceDateMillis,
        dueDateMillis = dueDateMillis,
        businessName = businessName,
        businessEmail = businessEmail.ifBlank { null },
        businessAddress = businessAddress.ifBlank { null },
        clientName = clientName,
        clientEmail = clientEmail.ifBlank { null },
        clientAddress = clientAddress.ifBlank { null },
        discount = discount.toDoubleOrNull() ?: 0.0,
        discountType = discountType.name,
        taxRate = taxRate.toDoubleOrNull() ?: 0.0,
        notes = notes.ifBlank { null },
        status = status.name,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = System.currentTimeMillis()
    )

    fun validate(): Boolean {
        if (clientName.isBlank()) { errorMessage = "Client name is required."; return false }
        if (items.isEmpty()) { errorMessage = "Add at least one line item."; return false }
        if (items.any { it.quantity <= 0 }) { errorMessage = "Quantity must be greater than 0."; return false }
        if (items.any { it.unitPrice < 0 }) { errorMessage = "Unit price must be 0 or greater."; return false }
        errorMessage = null
        return true
    }

    fun pickDate(current: Long, onPicked: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = current }
        DatePickerDialog(context, { _, year, month, day ->
            val newCal = Calendar.getInstance()
            newCal.set(year, month, day)
            onPicked(newCal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                androidx.compose.material3.IconButton(onClick = onBack) {
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Text(if (isNew) "New Invoice" else invoiceNumber, style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = {
                if (loaded) scope.launch { viewModel.saveInvoiceSync(buildEntity(), items.toList()) }
            }) { Text("Save Draft") }
        }

        LazyColumn(modifier = Modifier.weight(1f, fill = true)) {
            item {
                Text("Invoice Number: $invoiceNumber", modifier = Modifier.padding(vertical = 4.dp))
                OutlinedButton(onClick = { pickDate(invoiceDateMillis) { invoiceDateMillis = it } }) {
                    Text("Invoice Date: ${dateFormat.format(invoiceDateMillis)}")
                }
                OutlinedButton(onClick = { pickDate(dueDateMillis) { dueDateMillis = it } }) {
                    Text("Due Date: ${dateFormat.format(dueDateMillis)}")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Business Details", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(businessName, { businessName = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(businessEmail, { businessEmail = it }, label = { Text("Business Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(businessAddress, { businessAddress = it }, label = { Text("Business Address") }, modifier = Modifier.fillMaxWidth())

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Client Details", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(clientName, { clientName = it }, label = { Text("Client Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(clientEmail, { clientEmail = it }, label = { Text("Client Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(clientAddress, { clientAddress = it }, label = { Text("Client Address") }, modifier = Modifier.fillMaxWidth())

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Line Items", style = MaterialTheme.typography.titleMedium)
            }

            items(items, key = { it.id }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f).clickable {
                        editingItem = item; showItemDialog = true
                    }) {
                        Text(item.itemDescription.ifBlank { "Untitled item" })
                        Text("%.2f x %s".format(item.quantity, CurrencyFormatter.format(item.unitPrice)),
                            style = MaterialTheme.typography.labelSmall)
                    }
                    Text(CurrencyFormatter.format(item.total))
                    TextButton(onClick = { items.remove(item) }) { Text("✕") }
                }
            }

            item {
                TextButton(onClick = { editingItem = null; showItemDialog = true }) { Text("+ Add Item") }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Tax & Discount", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    DiscountType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            selected = discountType == type,
                            onClick = { discountType = type },
                            shape = SegmentedButtonDefaults.itemShape(index, DiscountType.entries.size)
                        ) { Text(type.label) }
                    }
                }
                OutlinedTextField(
                    value = discount, onValueChange = { discount = it },
                    label = { Text(if (discountType == DiscountType.PERCENTAGE) "Discount %" else "Discount Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(value = taxRate, onValueChange = { taxRate = it }, label = { Text("Tax %") }, modifier = Modifier.fillMaxWidth())

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Totals", style = MaterialTheme.typography.titleMedium)
                val totals = currentTotals()
                Text("Subtotal: ${CurrencyFormatter.format(totals.subtotal)}")
                Text("Discount: -${CurrencyFormatter.format(totals.discount)}")
                Text("Tax: ${CurrencyFormatter.format(totals.taxAmount)}")
                Text("Grand Total: ${CurrencyFormatter.format(totals.grandTotal)}", style = MaterialTheme.typography.titleMedium)

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())

                errorMessage?.let {
                    Text(it, color = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        Button(
            onClick = {
                if (validate()) {
                    scope.launch {
                        viewModel.saveInvoiceSync(buildEntity(), items.toList())
                        onPreview(actualId)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("Preview PDF") }
    }

    if (showItemDialog) {
        LineItemDialog(
            item = editingItem,
            onDismiss = { showItemDialog = false },
            onSave = { newItem ->
                if (editingItem == null) {
                    items.add(newItem)
                } else {
                    val idx = items.indexOfFirst { it.id == newItem.id }
                    if (idx >= 0) items[idx] = newItem
                }
                showItemDialog = false
            }
        )
    }
}

@Composable
private fun LineItemDialog(
    item: LineItemEntity?,
    onDismiss: () -> Unit,
    onSave: (LineItemEntity) -> Unit
) {
    var description by remember { mutableStateOf(item?.itemDescription ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "1") }
    var unitPrice by remember { mutableStateOf(item?.unitPrice?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Item" else "Edit Item") },
        text = {
            Column {
                OutlinedTextField(description, { description = it }, label = { Text("Description") })
                OutlinedTextField(quantity, { quantity = it }, label = { Text("Quantity") })
                OutlinedTextField(unitPrice, { unitPrice = it }, label = { Text("Unit Price") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val q = quantity.toDoubleOrNull() ?: 0.0
                val p = unitPrice.toDoubleOrNull() ?: 0.0
                if (description.isNotBlank() && q > 0) {
                    onSave(
                        LineItemEntity(
                            id = item?.id ?: UUID.randomUUID().toString(),
                            invoiceId = item?.invoiceId ?: "",
                            itemDescription = description,
                            quantity = q,
                            unitPrice = p
                        )
                    )
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
