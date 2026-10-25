import Foundation

enum DiscountType: String, Codable, CaseIterable, Identifiable {
    case fixed = "Fixed Amount"
    case percentage = "Percentage"

    var id: String { rawValue }
}
