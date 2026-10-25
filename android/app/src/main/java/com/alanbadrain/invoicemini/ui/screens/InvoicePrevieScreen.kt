package com.alanbadrain.invoicemini.ui.screens

import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.alanbadrain.invoicemini.InvoiceViewModel
import com.alanbadrain.invoicemini.data.DiscountType
import com.alanbadrain.invoicemini.data.InvoiceStatus
import com.alanbadrain.invoicemini.data.InvoiceWithItems
import com.alanbadrain.invoicemini.ui.components.StatusBadge
import com.alanbadrain.invoicemini.util.CurrencyFormatter
import com.alanbadrain.invoicemini.util.InvoiceCalculations
import com.alanbadrain.invoicemini.util.PdfGenerator
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun InvoicePreviewScreen(invoiceId: String, viewModel: InvoiceViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profile by viewModel.businessProfile.collectAsState()

    var invoiceWithItems by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var previewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    var pageIndex by remember { mutableStateOf(0) }
    var pageCount by remember { mutableStateOf(0) }

    suspend fun regenerate() {
        val inv = viewModel.getInvoice(invoiceId) ?: return
        invoiceWithItems = inv
        val logoBitmap = PdfGenerator.loadBitmapFromUri(context, profile?.logoUri)
        val file = PdfGenerator.generateInvoicePdf(context, inv, logoBitmap)
        pdfFile = file
        if (file != null) {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            pageCount = renderer.pageCount
            pageIndex = pageIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
            val page = renderer.openPage(pageIndex)
            val bitmap = android.graphics.Bitmap.createBitmap(page.width * 2, page.height * 2, android.graphics.Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            pfd.close()
            previewBitmap = bitmap
        }
    }

    LaunchedEffect(invoiceId, pageIndex) { regenerate() }

    val inv = invoiceWithItems
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            androidx.compose.material3.IconButton(onClick = onBack) {
                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Preview", style = MaterialTheme.typography.titleLarge)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))

        if (inv != null) {
            val status = InvoiceStatus.valueOf(inv.invoice.status)
            val discountType = DiscountType.valueOf(inv.invoice.discountType)
            val totals = InvoiceCalculations.calculateTotals(inv.items, inv.invoice.taxRate, inv.invoice.discount, discountType)

            Text(inv.invoice.invoiceNumber, style = MaterialTheme.typography.titleMedium)
            Text("Client: ${inv.invoice.clientName}")
            Text("Total: ${CurrencyFormatter.format(totals.grandTotal, inv.invoice.currencyCode)}", style = MaterialTheme.typography.titleMedium)
            StatusBadge(status)

            androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))

            if (pageCount > 1) {
                Row {
                    androidx.compose.material3.TextButton(enabled = pageIndex > 0, onClick = { pageIndex-- }) { Text("Previous") }
                    Text("Page ${pageIndex + 1} of $pageCount")
                    androidx.compose.material3.TextButton(enabled = pageIndex + 1 < pageCount, onClick = { pageIndex++ }) { Text("Next") }
                }
            }
            previewBitmap?.let { bmp ->
                Image(bitmap = bmp.asImageBitmap(), contentDescription = "PDF preview", modifier = Modifier.fillMaxWidth())
            }

            androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))

            Button(
                onClick = {
                    pdfFile?.let { file ->
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Invoice PDF"))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Share PDF") }

            androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            viewModel.saveInvoiceSync(inv.invoice.copy(status = InvoiceStatus.SENT.name), inv.items)
                            regenerate()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Mark as Sent") }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            viewModel.saveInvoiceSync(inv.invoice.copy(status = InvoiceStatus.PAID.name), inv.items)
                            regenerate()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Mark as Paid") }
            }
        } else {
            Text("Generating PDF...")
        }
    }
}
