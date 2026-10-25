import SwiftUI
import SwiftData
import PhotosUI

struct BusinessProfileView: View {
    @Environment(\.modelContext) private var modelContext
    @Query private var profiles: [BusinessProfile]

    @State private var businessName = ""
    @State private var email = ""
    @State private var address = ""
    @State private var phone = ""
    @State private var logoData: Data?
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var savedMessage: String?
    @FocusState private var focusedField: Field?

    private enum Field {
        case businessName, email, address, phone
    }

    var body: some View {
        Form {
            Section("Logo") {
                if let logoData, let uiImage = UIImage(data: logoData) {
                    HStack {
                        Image(uiImage: uiImage)
                            .resizable()
                            .scaledToFit()
                            .frame(height: 60)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                        Spacer()
                        Button("Remove", role: .destructive) {
                            self.logoData = nil
                            selectedPhotoItem = nil
                        }
                    }
                }
                PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                    Label(logoData == nil ? "Add Logo" : "Change Logo", systemImage: "photo")
                }
                .onChange(of: selectedPhotoItem) { _, newItem in
                    Task {
                        if let data = try? await newItem?.loadTransferable(type: Data.self) {
                            logoData = data
                        }
                    }
                }
            }

            Section("Business Profile") {
                TextField("Business Name", text: $businessName)
                    .focused($focusedField, equals: .businessName)
                TextField("Email", text: $email)
                    .keyboardType(.emailAddress)
                    .focused($focusedField, equals: .email)
                TextField("Address", text: $address)
                    .focused($focusedField, equals: .address)
                TextField("Phone", text: $phone)
                    .keyboardType(.phonePad)
                    .focused($focusedField, equals: .phone)
            }

            if let savedMessage {
                Text(savedMessage).foregroundStyle(.green)
            }
        }
        .navigationTitle("Business Profile")
        .toolbar {
            ToolbarItem(placement: .keyboard) {
                HStack {
                    Spacer()
                    Button("Done") { focusedField = nil }
                }
            }
            ToolbarItem(placement: .confirmationAction) {
                Button("Save") {
                    focusedField = nil
                    save()
                }
            }
        }
        .onAppear(perform: load)
    }

    private func load() {
        if profiles.count > 1 {
            for extra in profiles.dropFirst() {
                modelContext.delete(extra)
            }
            try? modelContext.save()
        }

        guard let profile = profiles.first else { return }
        businessName = profile.businessName
        email = profile.email ?? ""
        address = profile.address ?? ""
        phone = profile.phone ?? ""
        logoData = profile.logoData
    }

    private func save() {
        let profile = profiles.first ?? BusinessProfile()
        profile.businessName = businessName
        profile.email = email.isEmpty ? nil : email
        profile.address = address.isEmpty ? nil : address
        profile.phone = phone.isEmpty ? nil : phone
        profile.logoData = logoData
        if profiles.isEmpty {
            modelContext.insert(profile)
        }
        try? modelContext.save()
        savedMessage = "Saved"
    }
}
