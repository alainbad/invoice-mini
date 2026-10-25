import SwiftUI

struct LineItemEditorView: View {
    @Environment(\.dismiss) private var dismiss

    let item: LineItem?
    var currencyCode: String? = nil
    let onSave: (LineItem) -> Void

    @State private var itemDescription: String = ""
    @State private var quantity: Double = 1
    @State private var unitPrice: Double = 0

    private var total: Double { quantity * unitPrice }

    var body: some View {
        NavigationStack {
            Form {
                Section("Item") {
                    TextField("Description", text: $itemDescription)
                    HStack {
                        Text("Quantity")
                        Spacer()
                        TextField("1", value: $quantity, format: .number)
                            .keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing)
                    }
                    HStack {
                        Text("Unit Price")
                        Spacer()
                        TextField("0", value: $unitPrice, format: .number)
                            .keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing)
                    }
                    LabeledContent("Total", value: CurrencyFormatter.formatCurrency(total, code: currencyCode))
                        .font(.headline)
                }
            }
            .navigationTitle(item == nil ? "Add Item" : "Edit Item")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        let target = item ?? LineItem()
                        target.itemDescription = itemDescription
                        target.quantity = quantity
                        target.unitPrice = unitPrice
                        onSave(target)
                        dismiss()
                    }
                    .disabled(itemDescription.trimmingCharacters(in: .whitespaces).isEmpty || quantity <= 0)
                }
            }
            .onAppear {
                if let item {
                    itemDescription = item.itemDescription
                    quantity = item.quantity
                    unitPrice = item.unitPrice
                }
            }
        }
    }
}
