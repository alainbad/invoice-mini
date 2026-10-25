import Foundation

struct InvoiceTotals {
    var subtotal: Double
    var discount: Double
    var taxAmount: Double
    var grandTotal: Double
}

enum InvoiceCalculations {
    /// Tax is computed on (subtotal - discount). If discountType is .percentage,
    /// `discount` is treated as a percent of subtotal; otherwise as a fixed amount.
    static func calculateInvoiceTotals(
        items: [LineItem],
        taxRate: Double,
        discount: Double,
        discountType: DiscountType
    ) -> InvoiceTotals {
        let subtotal = items.reduce(0) { $0 + $1.total }

        let discountAmount: Double
        switch discountType {
        case .fixed:
            discountAmount = min(max(discount, 0), subtotal)
        case .percentage:
            let clampedPercent = min(max(discount, 0), 100)
            discountAmount = subtotal * (clampedPercent / 100)
        }

        let discountedSubtotal = max(subtotal - discountAmount, 0)
        let taxAmount = discountedSubtotal * (taxRate / 100)
        let grandTotal = discountedSubtotal + taxAmount

        return InvoiceTotals(
            subtotal: subtotal,
            discount: discountAmount,
            taxAmount: taxAmount,
            grandTotal: grandTotal
        )
    }
}
