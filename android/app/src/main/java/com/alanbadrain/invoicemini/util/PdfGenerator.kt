package com.alanbadrain.invoicemini.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.alanbadrain.invoicemini.data.DiscountType
import com.alanbadrain.invoicemini.data.InvoiceWithItems
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object PdfGenerator {

    private const val PAGE_WIDTH = 612
    private const val PAGE_HEIGHT = 792
    private const val MARGIN = 48f

    fun generateInvoicePdf(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        logoBitmap: Bitmap?
    ): File? {
        val invoice = invoiceWithItems.invoice
        val items = invoiceWithItems.items

        val navy = Color.rgb(14, 49, 88)
        val darkText = Color.rgb(38, 38, 38)
        val grayText = Color.rgb(115, 115, 115)
        val white = Color.WHITE

        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

        val document = PdfDocument()
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var y = MARGIN

        val titlePaint = Paint().apply { color = navy; textSize = 28f; isFakeBoldText = true; isAntiAlias = true }
        val smallGrayPaint = Paint().apply { color = grayText; textSize = 12f; isAntiAlias = true }
        val labelPaint = Paint().apply { color = navy; textSize = 11f; isFakeBoldText = true; isAntiAlias = true }
        val bodyPaint = Paint().apply { color = darkText; textSize = 11f; isAntiAlias = true }
        val headerPaint = Paint().apply { color = white; textSize = 11f; isFakeBoldText = true; isAntiAlias = true }

        // Logo + Title
        if (logoBitmap != null) {
            val maxLogoHeight = 44f
            val aspect = logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
            val logoWidth = maxLogoHeight * aspect
            val destRect = RectF(MARGIN, y, MARGIN + logoWidth, y + maxLogoHeight)
            canvas.drawBitmap(logoBitmap, null, destRect, null)
            canvas.drawText("INVOICE", MARGIN + logoWidth + 16f, y + maxLogoHeight / 2 + 10f, titlePaint)
        } else {
            canvas.drawText("INVOICE", MARGIN, y + 28f, titlePaint)
        }

        val numberText = "Invoice #: ${invoice.invoiceNumber}"
        val numberWidth = smallGrayPaint.measureText(numberText)
        canvas.drawText(numberText, PAGE_WIDTH - MARGIN - numberWidth, y + 16f, smallGrayPaint)
        y += 56f

        val dateText = "Date: ${dateFormat.format(invoice.invoiceDateMillis)}   Due: ${dateFormat.format(invoice.dueDateMillis)}"
        canvas.drawText(dateText, MARGIN, y, smallGrayPaint)
        y += 20f
        invoice.currencyCode?.let { canvas.drawText("Currency: $it", MARGIN, y, smallGrayPaint); y += 20f }
        y += 8f

        val columnWidth = (PAGE_WIDTH - MARGIN * 2 - 20f) / 2
        canvas.drawText("FROM", MARGIN, y, labelPaint)
        canvas.drawText("BILL TO", MARGIN + columnWidth + 20f, y, labelPaint)
        y += 16f

        val fromLines = listOfNotNull(
            invoice.businessName.ifBlank { null },
            invoice.businessEmail?.ifBlank { null },
            invoice.businessAddress?.ifBlank { null },
            invoice.businessPhone?.ifBlank { null },
            invoice.businessTaxNumber?.takeIf { it.isNotBlank() }?.let { "Tax registration: $it" }
        )
        val toLines = listOfNotNull(
            invoice.clientName.ifBlank { null },
            invoice.clientEmail?.ifBlank { null },
            invoice.clientAddress?.ifBlank { null },
            invoice.clientPhone?.ifBlank { null }
        )
        val maxLines = maxOf(fromLines.size, toLines.size)
        for (i in 0 until maxLines) {
            if (i < fromLines.size) canvas.drawText(fromLines[i], MARGIN, y, bodyPaint)
            if (i < toLines.size) canvas.drawText(toLines[i], MARGIN + columnWidth + 20f, y, bodyPaint)
            y += 16f
        }
        y += 20f

        val colDesc = MARGIN
        val colQty = PAGE_WIDTH - MARGIN - 260f
        val colPrice = PAGE_WIDTH - MARGIN - 170f
        val colTotal = PAGE_WIDTH - MARGIN - 80f

        val headerBgPaint = Paint().apply { color = navy }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22f, headerBgPaint)
        canvas.drawText("Description", colDesc + 8f, y + 15f, headerPaint)
        canvas.drawText("Qty", colQty, y + 15f, headerPaint)
        canvas.drawText("Price", colPrice, y + 15f, headerPaint)
        canvas.drawText("Total", colTotal, y + 15f, headerPaint)
        y += 22f

        val altRowPaint = Paint().apply { color = Color.rgb(242, 242, 242) }
        items.forEachIndexed { index, item ->
            if (y > PAGE_HEIGHT - MARGIN - 160f) {
                document.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.pages.size + 1).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN
            }
            if (index % 2 == 1) canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22f, altRowPaint)
            canvas.drawText(item.itemDescription, colDesc + 8f, y + 15f, bodyPaint)
            canvas.drawText("%.2f".format(item.quantity), colQty, y + 15f, bodyPaint)
            canvas.drawText(CurrencyFormatter.format(item.unitPrice, invoice.currencyCode), colPrice, y + 15f, bodyPaint)
            canvas.drawText(CurrencyFormatter.format(item.total, invoice.currencyCode), colTotal, y + 15f, bodyPaint)
            y += 22f
        }
        y += 16f

        val discountType = DiscountType.valueOf(invoice.discountType)
        val totals = InvoiceCalculations.calculateTotals(items, invoice.taxRate, invoice.discount, discountType)

        fun drawTotalLine(label: String, value: String, bold: Boolean) {
            val paint = if (bold) Paint().apply { color = navy; textSize = 14f; isFakeBoldText = true; isAntiAlias = true }
            else Paint().apply { color = darkText; textSize = 12f; isAntiAlias = true }
            canvas.drawText(label, colPrice - 40f, y + 12f, paint)
            val valueWidth = paint.measureText(value)
            canvas.drawText(value, PAGE_WIDTH - MARGIN - valueWidth, y + 12f, paint)
            y += if (bold) 22f else 18f
        }

        drawTotalLine("Subtotal", CurrencyFormatter.format(totals.subtotal, invoice.currencyCode), false)
        if (totals.discount > 0) {
            val label = if (discountType == DiscountType.PERCENTAGE)
                "Discount (${"%.1f".format(invoice.discount)}%)" else "Discount"
            drawTotalLine(label, "-${CurrencyFormatter.format(totals.discount, invoice.currencyCode)}", false)
        }
        if (invoice.taxRate > 0) {
            drawTotalLine("Tax (${"%.1f".format(invoice.taxRate)}%)", CurrencyFormatter.format(totals.taxAmount, invoice.currencyCode), false)
        }
        y += 6f
        drawTotalLine("Total", CurrencyFormatter.format(totals.grandTotal, invoice.currencyCode), true)

        if (!invoice.notes.isNullOrBlank()) {
            y += 24f
            canvas.drawText("Notes", MARGIN, y, labelPaint)
            y += 16f
            canvas.drawText(invoice.notes, MARGIN, y, bodyPaint)
        }

        val images = listOf("Digital Stamp" to invoice.stampPath, "Digital Signature" to invoice.signaturePath)
        if (images.any { it.second != null }) {
            if (y + 150f > PAGE_HEIGHT - MARGIN) {
                document.finishPage(page)
                page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.pages.size + 1).create())
                canvas = page.canvas
                y = MARGIN
            }
            images.forEachIndexed { index, (label, path) ->
                loadBitmapFromUri(context, path)?.let { bitmap ->
                    val x = MARGIN + index * 260f
                    canvas.drawText(label, x, y + 24f, labelPaint)
                    val scale = minOf(220f / bitmap.width, 90f / bitmap.height)
                    canvas.drawBitmap(bitmap, null, RectF(x, y + 38f, x + bitmap.width * scale, y + 38f + bitmap.height * scale), null)
                    bitmap.recycle()
                }
            }
        }
        document.finishPage(page)

        return try {
            val fileName = "${invoice.invoiceNumber.ifBlank { "invoice" }}.pdf"
            val outFile = File(context.cacheDir, fileName)
            FileOutputStream(outFile).use { document.writeTo(it) }
            document.close()
            outFile
        } catch (e: Exception) {
            document.close()
            null
        }
    }

    /** logoUri may be a plain filesystem path (the app's own copy of a picked logo image,
     * stored via BusinessProfileScreen) or, less commonly, a content:// URI. Handle both. */
    fun loadBitmapFromUri(context: Context, uriString: String?): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return try {
            if (uriString.startsWith("content://")) {
                val uri = android.net.Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            } else {
                BitmapFactory.decodeFile(uriString)
            }
        } catch (e: Exception) {
            null
        }
    }
}
