import Foundation

enum CurrencyFormatter {
    static func formatCurrency(_ value: Double, code: String? = nil) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .currency
        formatter.locale = .current
        if let code {
            formatter.currencyCode = code
            let currencyLocale = Locale(identifier: "en_US@currency=\(code)")
            let precision = NumberFormatter()
            precision.numberStyle = .currency
            precision.locale = currencyLocale
            precision.currencyCode = code
            formatter.minimumFractionDigits = precision.minimumFractionDigits
            formatter.maximumFractionDigits = precision.maximumFractionDigits
        }
        return formatter.string(from: NSNumber(value: value)) ?? String(format: "$%.2f", value)
    }
}
