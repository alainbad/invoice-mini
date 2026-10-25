import SwiftUI

struct StatusBadge: View {
    let status: InvoiceStatus

    private var color: Color {
        switch status {
        case .draft: return .gray
        case .sent: return .orange
        case .paid: return .green
        }
    }

    var body: some View {
        Text(status.rawValue)
            .font(.caption.bold())
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(color.opacity(0.15))
            .foregroundStyle(color)
            .clipShape(Capsule())
    }
}
