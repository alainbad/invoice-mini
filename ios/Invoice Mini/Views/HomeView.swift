import SwiftUI
import SwiftData

struct HomeView: View {
    @Query(sort: \Invoice.updatedAt, order: .reverse) private var invoices: [Invoice]
    @State private var showingNewInvoice = false

    private var draftCount: Int { invoices.filter { $0.status == .draft }.count }
    private var paidCount: Int { invoices.filter { $0.status == .paid }.count }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Button {
                        showingNewInvoice = true
                    } label: {
                        Label("Create Invoice", systemImage: "plus.circle.fill")
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.accentColor)
                            .foregroundStyle(.white)
                            .clipShape(RoundedRectangle(cornerRadius: 14))
                    }

                    HStack(spacing: 12) {
                        SummaryCard(title: "Total Invoices", value: "\(invoices.count)")
                        SummaryCard(title: "Drafts", value: "\(draftCount)", accent: .gray)
                        SummaryCard(title: "Paid", value: "\(paidCount)", accent: .green)
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Recent Invoices")
                            .font(.headline)
                        if invoices.isEmpty {
                            Text("No invoices yet. Create your first one above.")
                                .foregroundStyle(.secondary)
                                .font(.subheadline)
                        } else {
                            ForEach(invoices.prefix(5)) { invoice in
                                NavigationLink {
                                    InvoiceFormView(invoice: invoice)
                                } label: {
                                    InvoiceRowCard(invoice: invoice)
                                }
                                .buttonStyle(.plain)
                                Divider()
                            }
                        }
                    }
                }
                .padding()
            }
            .navigationTitle("Invoice Mini")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink {
                        InvoiceListView()
                    } label: {
                        Image(systemName: "list.bullet")
                    }
                }
                ToolbarItem(placement: .topBarLeading) {
                    NavigationLink {
                        BusinessProfileView()
                    } label: {
                        Image(systemName: "building.2")
                    }
                }
            }
            .sheet(isPresented: $showingNewInvoice) {
                NavigationStack {
                    InvoiceFormView()
                }
            }
        }
    }
}
