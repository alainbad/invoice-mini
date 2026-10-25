package com.alanbadrain.invoicemini

import com.alanbadrain.invoicemini.util.CurrencyFormatter
import com.alanbadrain.invoicemini.util.InternationalOptions
import com.alanbadrain.invoicemini.data.InvoiceEntity
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class InvoiceUpdateTest {
    @Test fun currencyPrecisionFollowsInvoiceRatherThanDevice() {
        val before = Locale.getDefault()
        try {
            Locale.setDefault(Locale.US)
            assertTrue(CurrencyFormatter.format(12.345, "KWD").endsWith("12.345"))
            assertFalse(CurrencyFormatter.format(12.345, "JPY").contains(".345"))
            assertTrue(CurrencyFormatter.format(12.5, "USD").endsWith("12.50"))
        } finally { Locale.setDefault(before) }
    }
    @Test fun countryPrefixesIncludeSharedCallingCodes() {
        assertEquals("+971", InternationalOptions.callingCodes["AE"])
        assertEquals("+961", InternationalOptions.callingCodes["LB"])
        assertEquals("+1", InternationalOptions.callingCodes["CA"])
        assertEquals("+1", InternationalOptions.callingCodes["US"])
        assertTrue(InternationalOptions.currencies.any { it.first == "KWD" })
    }
    @Test fun copyingAnInvoiceRetainsItsNewFields() {
        val invoice = InvoiceEntity(id="original", invoiceNumber="INV-0001", invoiceDateMillis=0,
            dueDateMillis=0, createdAtMillis=0, updatedAtMillis=0, currencyCode="AED",
            businessPhone="+971 501234567", clientPhone="+961 3123456", businessTaxNumber="00123",
            stampPath="/stamp.png", signaturePath="/signature.png")
        val copy = invoice.copy(id="copy", invoiceNumber="INV-0002")
        assertEquals("AED", copy.currencyCode)
        assertEquals("00123", copy.businessTaxNumber)
        assertEquals(invoice.businessPhone, copy.businessPhone)
        assertEquals(invoice.clientPhone, copy.clientPhone)
        assertEquals(invoice.stampPath, copy.stampPath)
        assertEquals(invoice.signaturePath, copy.signaturePath)
    }
}
