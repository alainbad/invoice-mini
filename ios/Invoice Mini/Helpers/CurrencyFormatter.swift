import Foundation

enum CurrencyFormatter {
    static func formatCurrency(_ value: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = .current
        return formatter.string(from: NSNumber(value: value)) ?? String(format: "$%.2f", value)
    }
}
