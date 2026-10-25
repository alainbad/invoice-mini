import Foundation

enum InvoiceNumberGenerator {
    static func generateInvoiceNumber(existingInvoices: [Invoice]) -> String {
        let numbers: [Int] = existingInvoices.compactMap { invoice in
            let digits = invoice.invoiceNumber.filter(\.isNumber)
            return Int(digits)
        }
        let next = (numbers.max() ?? 0) + 1
        return String(format: "INV-%04d", next)
    }
}
