import SwiftUI
import SwiftData

struct InvoiceListView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Invoice.updatedAt, order: .reverse) private var invoices: [Invoice]
    @State private var searchText: String = ""
    @State private var statusFilter: InvoiceStatus?

    private var filteredInvoices: [Invoice] {
        invoices.filter { invoice in
            let matchesStatus = statusFilter == nil || invoice.status == statusFilter
            let matchesSearch = searchText.isEmpty
                || invoice.clientName.localizedCaseInsensitiveContains(searchText)
                || invoice.invoiceNumber.localizedCaseInsensitiveContains(searchText)
            return matchesStatus && matchesSearch
        }
    }

    var body: some View {
        List {
            Section {
                Picker("Status", selection: $statusFilter) {
                    Text("All").tag(InvoiceStatus?.none)
                    ForEach(InvoiceStatus.allCases) { status in
                        Text(status.rawValue).tag(Optional(status))
                    }
                }
                .pickerStyle(.segmented)
            }
            .listRowSeparator(.hidden)

            ForEach(filteredInvoices) { invoice in
                NavigationLink {
                    InvoiceFormView(invoice: invoice)
                } label: {
                    InvoiceRowCard(invoice: invoice)
                }
                .swipeActions(edge: .trailing) {
                    Button(role: .destructive) {
                        modelContext.delete(invoice)
                    } label: {
                        Label("Delete", systemImage: "trash")
                    }
                }
                .swipeActions(edge: .leading) {
                    Button {
                        duplicate(invoice)
                    } label: {
                        Label("Duplicate", systemImage: "doc.on.doc")
                    }
                    .tint(.blue)
                }
            }
        }
        .searchable(text: $searchText, prompt: "Search client or invoice #")
        .navigationTitle("Invoices")
    }

    private func duplicate(_ invoice: Invoice) {
        let newNumber = InvoiceNumberGenerator.generateInvoiceNumber(existingInvoices: invoices)
        let copy = Invoice(
            invoiceNumber: newNumber,
            businessName: invoice.businessName,
            businessEmail: invoice.businessEmail,
            businessAddress: invoice.businessAddress,
            businessTaxNumber: invoice.businessTaxNumber,
            clientName: invoice.clientName,
            clientEmail: invoice.clientEmail,
            clientAddress: invoice.clientAddress,
            discount: invoice.discount,
            discountType: invoice.discountType,
            taxRate: invoice.taxRate,
            notes: invoice.notes,
            status: .draft
        )
        copy.businessPhone = invoice.businessPhone
        copy.clientPhone = invoice.clientPhone
        copy.currencyCode = invoice.currencyCode
        copy.stampData = invoice.stampData
        copy.signatureData = invoice.signatureData
        modelContext.insert(copy)
        for item in invoice.items {
            let newItem = LineItem(
                itemDescription: item.itemDescription,
                quantity: item.quantity,
                unitPrice: item.unitPrice
            )
            newItem.invoice = copy
            copy.items.append(newItem)
            modelContext.insert(newItem)
        }
    }
}
