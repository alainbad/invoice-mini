import Foundation
import SwiftData

@Model
final class Invoice {
    var id: UUID
    var invoiceNumber: String
    var invoiceDate: Date
    var dueDate: Date

    var businessName: String
    var businessEmail: String?
    var businessAddress: String?
    var businessTaxNumber: String?

    var clientName: String
    var clientEmail: String?
    var clientAddress: String?

    @Relationship(deleteRule: .cascade, inverse: \LineItem.invoice)
    var items: [LineItem] = []

    var discount: Double
    var discountTypeRaw: String
    var taxRate: Double
    var notes: String?
    var statusRaw: String
    var createdAt: Date
    var updatedAt: Date

    var status: InvoiceStatus {
        get { InvoiceStatus(rawValue: statusRaw) ?? .draft }
        set { statusRaw = newValue.rawValue }
    }

    var discountType: DiscountType {
        get { DiscountType(rawValue: discountTypeRaw) ?? .fixed }
        set { discountTypeRaw = newValue.rawValue }
    }

    var totals: InvoiceTotals {
        InvoiceCalculations.calculateInvoiceTotals(
            items: items,
            taxRate: taxRate,
            discount: discount,
            discountType: discountType
        )
    }

    init(
        invoiceNumber: String,
        invoiceDate: Date = .now,
        dueDate: Date = Calendar.current.date(byAdding: .day, value: 14, to: .now) ?? .now,
        businessName: String = "",
        businessEmail: String? = nil,
        businessAddress: String? = nil,
        businessTaxNumber: String? = nil,
        clientName: String = "",
        clientEmail: String? = nil,
        clientAddress: String? = nil,
        discount: Double = 0,
        discountType: DiscountType = .fixed,
        taxRate: Double = 0,
        notes: String? = nil,
        status: InvoiceStatus = .draft
    ) {
        self.id = UUID()
        self.invoiceNumber = invoiceNumber
        self.invoiceDate = invoiceDate
        self.dueDate = dueDate
        self.businessName = businessName
        self.businessEmail = businessEmail
        self.businessAddress = businessAddress
        self.businessTaxNumber = businessTaxNumber
        self.clientName = clientName
        self.clientEmail = clientEmail
        self.clientAddress = clientAddress
        self.discount = discount
        self.discountTypeRaw = discountType.rawValue
        self.taxRate = taxRate
        self.notes = notes
        self.statusRaw = status.rawValue
        self.createdAt = .now
        self.updatedAt = .now
    }
}
