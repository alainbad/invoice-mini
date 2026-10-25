import Foundation
import SwiftData

@Model
final class BusinessProfile {
    var businessName: String
    var email: String?
    var address: String?
    var phone: String?
    var taxNumber: String?
    var countryCode: String?
    var callingCode: String?
    var logoData: Data?

    init(
        businessName: String = "",
        email: String? = nil,
        address: String? = nil,
        phone: String? = nil,
        taxNumber: String? = nil,
        logoData: Data? = nil
    ) {
        self.businessName = businessName
        self.email = email
        self.address = address
        self.phone = phone
        self.taxNumber = taxNumber
        self.logoData = logoData
    }
}
