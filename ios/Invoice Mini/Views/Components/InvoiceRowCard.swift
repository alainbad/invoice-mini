import SwiftUI

struct InvoiceRowCard: View {
    let invoice: Invoice

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(invoice.clientName.isEmpty ? "No client" : invoice.clientName)
                    .font(.headline)
                Text(invoice.invoiceNumber)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(invoice.invoiceDate, style: .date)
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 6) {
                Text(CurrencyFormatter.formatCurrency(invoice.totals.grandTotal, code: invoice.currencyCode))
                    .font(.subheadline.bold())
                StatusBadge(status: invoice.status)
            }
        }
        .padding(.vertical, 6)
    }
}
