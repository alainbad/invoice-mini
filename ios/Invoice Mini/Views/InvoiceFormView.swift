import SwiftUI
import SwiftData

struct InvoiceFormView: View {
    @Environment(\.modelContext) private var modelContext
    @Query private var allInvoices: [Invoice]
    @Query private var businessProfiles: [BusinessProfile]

    @State private var invoice: Invoice
    private let isNew: Bool

    @State private var showingPreview = false
    @State private var showingLineItemSheet = false
    @State private var editingItem: LineItem?
    @State private var errorMessage: String?

    init(invoice: Invoice? = nil) {
        if let invoice {
            _invoice = State(initialValue: invoice)
            self.isNew = false
        } else {
            _invoice = State(initialValue: Invoice(invoiceNumber: ""))
            self.isNew = true
        }
    }

    var body: some View {
        Form {
            Section("Invoice Details") {
                SelectionField(title: "Currency", value: Binding(get: { invoice.currencyCode ?? Locale.current.currency?.identifier ?? "USD" }, set: { invoice.currencyCode = $0 }), options: InternationalOptions.currencies)
                LabeledContent("Invoice Number", value: invoice.invoiceNumber)
                DatePicker("Invoice Date", selection: Bindable(invoice).invoiceDate, displayedComponents: .date)
                DatePicker("Due Date", selection: Bindable(invoice).dueDate, displayedComponents: .date)
            }

            Section("Business Details") {
                TextField("Tax Registration Number (optional)", text: Binding(get: { invoice.businessTaxNumber ?? "" }, set: { invoice.businessTaxNumber = $0.isEmpty ? nil : $0 }))
                TextField("Business Mobile Number", text: Binding(get: { invoice.businessPhone ?? "" }, set: { invoice.businessPhone = $0.isEmpty ? nil : $0 })).keyboardType(.phonePad)
                TextField("Business Name", text: Bindable(invoice).businessName)
                TextField("Business Email", text: Binding(
                    get: { invoice.businessEmail ?? "" },
                    set: { invoice.businessEmail = $0.isEmpty ? nil : $0 }
                ))
                .keyboardType(.emailAddress)
                TextField("Business Address", text: Binding(
                    get: { invoice.businessAddress ?? "" },
                    set: { invoice.businessAddress = $0.isEmpty ? nil : $0 }
                ))
            }

            Section("Client Details") {
                TextField("Client Mobile Number", text: Binding(get: { invoice.clientPhone ?? "" }, set: { invoice.clientPhone = $0.isEmpty ? nil : $0 })).keyboardType(.phonePad)
                TextField("Client Name", text: Bindable(invoice).clientName)
                TextField("Client Email", text: Binding(
                    get: { invoice.clientEmail ?? "" },
                    set: { invoice.clientEmail = $0.isEmpty ? nil : $0 }
                ))
                .keyboardType(.emailAddress)
                TextField("Client Address", text: Binding(
                    get: { invoice.clientAddress ?? "" },
                    set: { invoice.clientAddress = $0.isEmpty ? nil : $0 }
                ))
            }

            Section("Line Items") {
                ForEach(invoice.items) { item in
                    Button {
                        editingItem = item
                        showingLineItemSheet = true
                    } label: {
                        HStack {
                            VStack(alignment: .leading) {
                                Text(item.itemDescription.isEmpty ? "Untitled item" : item.itemDescription)
                                    .foregroundStyle(.primary)
                                Text("\(item.quantity, specifier: "%.2f") x \(CurrencyFormatter.formatCurrency(item.unitPrice, code: invoice.currencyCode))")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            Text(CurrencyFormatter.formatCurrency(item.total, code: invoice.currencyCode))
                                .foregroundStyle(.primary)
                        }
                    }
                }
                .onDelete(perform: deleteItems)

                Button {
                    editingItem = nil
                    showingLineItemSheet = true
                } label: {
                    Label("Add Item", systemImage: "plus")
                }
            }

            Section("Tax & Discount") {
                Picker("Discount Type", selection: Bindable(invoice).discountType) {
                    ForEach(DiscountType.allCases) { type in
                        Text(type.rawValue).tag(type)
                    }
                }
                .pickerStyle(.segmented)

                HStack {
                    Text(invoice.discountType == .percentage ? "Discount %" : "Discount Amount")
                    Spacer()
                    TextField("0", value: Bindable(invoice).discount, format: .number)
                        .keyboardType(.decimalPad)
                        .multilineTextAlignment(.trailing)
                }
                HStack {
                    Text("Tax %")
                    Spacer()
                    TextField("0", value: Bindable(invoice).taxRate, format: .number)
                        .keyboardType(.decimalPad)
                        .multilineTextAlignment(.trailing)
                }
            }

            Section("Totals") {
                let totals = invoice.totals
                LabeledContent("Subtotal", value: CurrencyFormatter.formatCurrency(totals.subtotal, code: invoice.currencyCode))
                LabeledContent("Discount", value: "-\(CurrencyFormatter.formatCurrency(totals.discount, code: invoice.currencyCode))")
                LabeledContent("Tax", value: CurrencyFormatter.formatCurrency(totals.taxAmount, code: invoice.currencyCode))
                LabeledContent("Grand Total", value: CurrencyFormatter.formatCurrency(totals.grandTotal, code: invoice.currencyCode))
                    .font(.headline)
            }

            Section("Stamp & Signature") {
                InvoiceImageField(title: "Digital Stamp", data: Bindable(invoice).stampData)
                InvoiceImageField(title: "Digital Signature", data: Bindable(invoice).signatureData)
            }

            Section("Notes") {
                TextField("Notes", text: Binding(
                    get: { invoice.notes ?? "" },
                    set: { invoice.notes = $0.isEmpty ? nil : $0 }
                ), axis: .vertical)
                .lineLimit(3...6)
            }

            if let errorMessage {
                Section {
                    Text(errorMessage)
                        .foregroundStyle(.red)
                        .font(.subheadline)
                }
            }
        }
        .navigationTitle(isNew ? "New Invoice" : invoice.invoiceNumber)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Save Draft") {
                    saveInvoice()
                }
            }
            ToolbarItem(placement: .bottomBar) {
                Button("Preview PDF") {
                    if validate() {
                        if saveInvoice() { showingPreview = true }
                    }
                }
            }
        }
        .sheet(isPresented: $showingLineItemSheet) {
            LineItemEditorView(item: editingItem, currencyCode: invoice.currencyCode) { newItem in
                if editingItem == nil {
                    newItem.invoice = invoice
                    invoice.items.append(newItem)
                    modelContext.insert(newItem)
                }
                invoice.updatedAt = .now
            }
        }
        .navigationDestination(isPresented: $showingPreview) {
            InvoicePreviewView(invoice: invoice)
        }
        .onAppear {
            guard isNew, invoice.invoiceNumber.isEmpty else { return }
            invoice.currencyCode = Locale.current.currency?.identifier ?? "USD"
            modelContext.insert(invoice)
            invoice.invoiceNumber = InvoiceNumberGenerator.generateInvoiceNumber(existingInvoices: allInvoices)
            if let profile = businessProfiles.first {
                invoice.businessName = profile.businessName
                invoice.businessEmail = profile.email
                invoice.businessAddress = profile.address
                invoice.businessPhone = (profile.phone?.trimmingCharacters(in: .whitespaces).hasPrefix("+") == true) ? profile.phone : [profile.callingCode, profile.phone].compactMap { $0 }.filter { !$0.isEmpty }.joined(separator: " ")
                invoice.businessTaxNumber = profile.taxNumber
            }
        }
    }

    private func deleteItems(at offsets: IndexSet) {
        for index in offsets {
            modelContext.delete(invoice.items[index])
        }
        invoice.items.remove(atOffsets: offsets)
    }

    private func validate() -> Bool {
        if invoice.clientName.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = "Client name is required."
            return false
        }
        if invoice.items.isEmpty {
            errorMessage = "Add at least one line item."
            return false
        }
        if invoice.items.contains(where: { $0.quantity <= 0 }) {
            errorMessage = "Quantity must be greater than 0."
            return false
        }
        if invoice.items.contains(where: { $0.unitPrice < 0 }) {
            errorMessage = "Unit price must be 0 or greater."
            return false
        }
        errorMessage = nil
        return true
    }

    @discardableResult
    private func saveInvoice() -> Bool {
        guard validate() else { return false }
        invoice.updatedAt = .now
        do { try modelContext.save(); return true }
        catch { errorMessage = "Unable to save this invoice. Please try again."; return false }
    }
}
