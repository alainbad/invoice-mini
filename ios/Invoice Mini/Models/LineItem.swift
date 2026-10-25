import Foundation
import SwiftData

@Model
final class LineItem {
    var id: UUID
    var itemDescription: String
    var quantity: Double
    var unitPrice: Double
    var invoice: Invoice?

    var total: Double {
        quantity * unitPrice
    }

    init(itemDescription: String = "", quantity: Double = 1, unitPrice: Double = 0) {
        self.id = UUID()
        self.itemDescription = itemDescription
        self.quantity = quantity
        self.unitPrice = unitPrice
    }
}
