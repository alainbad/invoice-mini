import SwiftUI
import PDFKit
import SwiftData

struct InvoicePreviewView: View {
    @Bindable var invoice: Invoice
    @Query private var businessProfiles: [BusinessProfile]
    @State private var pdfURL: URL?

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                HStack(alignment: .top, spacing: 12) {
                    if let logoData = businessProfiles.first?.logoData, let uiImage = UIImage(data: logoData) {
                        Image(uiImage: uiImage)
                            .resizable()
                            .scaledToFit()
                            .frame(width: 48, height: 48)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                    VStack(alignment: .leading, spacing: 8) {
                        Text(invoice.invoiceNumber).font(.headline)
                        Text("Client: \(invoice.clientName)")
                        Text("Total: \(CurrencyFormatter.formatCurrency(invoice.totals.grandTotal))")
                            .font(.title3.bold())
                        StatusBadge(status: invoice.status)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding()
                .background(Color(.secondarySystemBackground))
                .clipShape(RoundedRectangle(cornerRadius: 14))

                if let pdfURL {
                    PDFKitView(url: pdfURL)
                        .frame(height: 500)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                } else {
                    ProgressView("Generating PDF...")
                        .frame(height: 200)
                }

                if let pdfURL {
                    ShareLink(item: pdfURL) {
                        Label("Share PDF", systemImage: "square.and.arrow.up")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)
                }

                HStack(spacing: 12) {
                    Button("Mark as Sent") {
                        invoice.status = .sent
                        invoice.updatedAt = .now
                    }
                    .buttonStyle(.bordered)
                    .frame(maxWidth: .infinity)

                    Button("Mark as Paid") {
                        invoice.status = .paid
                        invoice.updatedAt = .now
                    }
                    .buttonStyle(.bordered)
                    .frame(maxWidth: .infinity)
                }
            }
            .padding()
        }
        .navigationTitle("Preview")
        .onAppear {
            pdfURL = PDFGenerator.generateInvoicePDF(invoice: invoice, logoData: businessProfiles.first?.logoData)
        }
    }
}

struct PDFKitView: UIViewRepresentable {
    let url: URL

    func makeUIView(context: Context) -> PDFView {
        let view = PDFView()
        view.autoScales = true
        view.document = PDFDocument(url: url)
        return view
    }

    func updateUIView(_ uiView: PDFView, context: Context) {
        uiView.document = PDFDocument(url: url)
    }
}
