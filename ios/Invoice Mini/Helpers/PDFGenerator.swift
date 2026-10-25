import Foundation
import UIKit

enum PDFGenerator {
    static func generateInvoicePDF(invoice: Invoice, logoData: Data? = nil) -> URL? {
        let pageWidth: CGFloat = 612   // 8.5in @ 72dpi
        let pageHeight: CGFloat = 792  // 11in @ 72dpi
        let margin: CGFloat = 48
        let pageRect = CGRect(x: 0, y: 0, width: pageWidth, height: pageHeight)

        let renderer = UIGraphicsPDFRenderer(bounds: pageRect)
        let fileName = "\(invoice.invoiceNumber.isEmpty ? "invoice" : invoice.invoiceNumber).pdf"
        let url = FileManager.default.temporaryDirectory.appendingPathComponent(fileName)

        let navy = UIColor(red: 0.055, green: 0.192, blue: 0.345, alpha: 1.0)
        let darkText = UIColor(white: 0.15, alpha: 1.0)
        let grayText = UIColor(white: 0.45, alpha: 1.0)

        let dateFormatter = DateFormatter()
        dateFormatter.dateStyle = .medium

        let titleAttrs: [NSAttributedString.Key: Any] = [
            .font: UIFont.boldSystemFont(ofSize: 28), .foregroundColor: navy
        ]
        let smallGrayAttrs: [NSAttributedString.Key: Any] = [
            .font: UIFont.systemFont(ofSize: 12), .foregroundColor: grayText
        ]
        let labelAttrs: [NSAttributedString.Key: Any] = [
            .font: UIFont.boldSystemFont(ofSize: 11), .foregroundColor: navy
        ]
        let bodyAttrs: [NSAttributedString.Key: Any] = [
            .font: UIFont.systemFont(ofSize: 11), .foregroundColor: darkText
        ]
        let headerAttrs: [NSAttributedString.Key: Any] = [
            .font: UIFont.boldSystemFont(ofSize: 11), .foregroundColor: UIColor.white
        ]

        do {
            try renderer.writePDF(to: url) { context in
                context.beginPage()
                var y: CGFloat = margin

                if let logoData, let logoImage = UIImage(data: logoData) {
                    let maxLogoHeight: CGFloat = 44
                    let aspect = logoImage.size.width / max(logoImage.size.height, 1)
                    let logoWidth = maxLogoHeight * aspect
                    logoImage.draw(in: CGRect(x: margin, y: y, width: logoWidth, height: maxLogoHeight))
                    "INVOICE".draw(
                        at: CGPoint(x: margin + logoWidth + 16, y: y + (maxLogoHeight - 28) / 2),
                        withAttributes: titleAttrs
                    )
                } else {
                    "INVOICE".draw(at: CGPoint(x: margin, y: y), withAttributes: titleAttrs)
                }

                let numberText = "Invoice #: \(invoice.invoiceNumber)"
                let numberSize = numberText.size(withAttributes: smallGrayAttrs)
                numberText.draw(
                    at: CGPoint(x: pageWidth - margin - numberSize.width, y: y + 6),
                    withAttributes: smallGrayAttrs
                )
                y += 44

                let dateText = "Date: \(dateFormatter.string(from: invoice.invoiceDate))   Due: \(dateFormatter.string(from: invoice.dueDate))"
                dateText.draw(at: CGPoint(x: margin, y: y), withAttributes: smallGrayAttrs)
                y += 20
                if let currency = invoice.currencyCode {
                    "Currency: \(currency)".draw(at: CGPoint(x: margin, y: y), withAttributes: smallGrayAttrs)
                    y += 20
                }
                y += 8

                let columnWidth = (pageWidth - margin * 2 - 20) / 2
                "FROM".draw(at: CGPoint(x: margin, y: y), withAttributes: labelAttrs)
                "BILL TO".draw(at: CGPoint(x: margin + columnWidth + 20, y: y), withAttributes: labelAttrs)
                y += 16

                let fromLines = [invoice.businessName, invoice.businessEmail, invoice.businessAddress, invoice.businessPhone, invoice.businessTaxNumber.map { "Tax registration: \($0)" }]
                    .compactMap { $0 }.filter { !$0.isEmpty }
                let toLines = [invoice.clientName, invoice.clientEmail, invoice.clientAddress, invoice.clientPhone]
                    .compactMap { $0 }.filter { !$0.isEmpty }

                let maxLines = max(fromLines.count, toLines.count)
                for i in 0..<maxLines {
                    if i < fromLines.count {
                        fromLines[i].draw(
                            in: CGRect(x: margin, y: y, width: columnWidth, height: 16),
                            withAttributes: bodyAttrs
                        )
                    }
                    if i < toLines.count {
                        toLines[i].draw(
                            in: CGRect(x: margin + columnWidth + 20, y: y, width: columnWidth, height: 16),
                            withAttributes: bodyAttrs
                        )
                    }
                    y += 16
                }
                y += 20

                let colDesc = margin
                let colQty = pageWidth - margin - 260
                let colPrice = pageWidth - margin - 170
                let colTotal = pageWidth - margin - 80

                context.cgContext.setFillColor(navy.cgColor)
                context.cgContext.fill(CGRect(x: margin, y: y, width: pageWidth - margin * 2, height: 22))
                "Description".draw(at: CGPoint(x: colDesc + 8, y: y + 5), withAttributes: headerAttrs)
                "Qty".draw(at: CGPoint(x: colQty, y: y + 5), withAttributes: headerAttrs)
                "Price".draw(at: CGPoint(x: colPrice, y: y + 5), withAttributes: headerAttrs)
                "Total".draw(at: CGPoint(x: colTotal, y: y + 5), withAttributes: headerAttrs)
                y += 22

                for (index, item) in invoice.items.enumerated() {
                    if y > pageHeight - margin - 160 {
                        context.beginPage()
                        y = margin
                    }
                    if index % 2 == 1 {
                        context.cgContext.setFillColor(UIColor(white: 0.95, alpha: 1.0).cgColor)
                        context.cgContext.fill(CGRect(x: margin, y: y, width: pageWidth - margin * 2, height: 22))
                    }
                    item.itemDescription.draw(at: CGPoint(x: colDesc + 8, y: y + 5), withAttributes: bodyAttrs)
                    String(format: "%.2f", item.quantity)
                        .draw(at: CGPoint(x: colQty, y: y + 5), withAttributes: bodyAttrs)
                    CurrencyFormatter.formatCurrency(item.unitPrice, code: invoice.currencyCode)
                        .draw(at: CGPoint(x: colPrice, y: y + 5), withAttributes: bodyAttrs)
                    CurrencyFormatter.formatCurrency(item.total, code: invoice.currencyCode)
                        .draw(at: CGPoint(x: colTotal, y: y + 5), withAttributes: bodyAttrs)
                    y += 22
                }
                y += 16

                let totals = invoice.totals
                func drawTotalLine(_ label: String, _ value: String, bold: Bool = false) {
                    let attrs: [NSAttributedString.Key: Any] = bold
                        ? [.font: UIFont.boldSystemFont(ofSize: 14), .foregroundColor: navy]
                        : [.font: UIFont.systemFont(ofSize: 12), .foregroundColor: darkText]
                    label.draw(at: CGPoint(x: colPrice - 40, y: y), withAttributes: attrs)
                    let valueSize = value.size(withAttributes: attrs)
                    value.draw(at: CGPoint(x: pageWidth - margin - valueSize.width, y: y), withAttributes: attrs)
                    y += bold ? 22 : 18
                }

                drawTotalLine("Subtotal", CurrencyFormatter.formatCurrency(totals.subtotal, code: invoice.currencyCode))
                if totals.discount > 0 {
                    let discountLabel = invoice.discountType == .percentage
                        ? "Discount (\(String(format: "%.1f", invoice.discount))%)"
                        : "Discount"
                    drawTotalLine(discountLabel, "-\(CurrencyFormatter.formatCurrency(totals.discount, code: invoice.currencyCode))")
                }
                if invoice.taxRate > 0 {
                    drawTotalLine(
                        "Tax (\(String(format: "%.1f", invoice.taxRate))%)",
                        CurrencyFormatter.formatCurrency(totals.taxAmount, code: invoice.currencyCode)
                    )
                }
                y += 6
                drawTotalLine("Total", CurrencyFormatter.formatCurrency(totals.grandTotal, code: invoice.currencyCode), bold: true)

                if let notes = invoice.notes, !notes.isEmpty {
                    y += 24
                    "Notes".draw(at: CGPoint(x: margin, y: y), withAttributes: labelAttrs)
                    y += 16
                    notes.draw(
                        in: CGRect(x: margin, y: y, width: pageWidth - margin * 2, height: 100),
                        withAttributes: bodyAttrs
                    )
                }
                // Reserve the notes block before placing the uploaded images.
                if let notes = invoice.notes, !notes.isEmpty { y += 100 }
                let images: [(String, Data?)] = [("Digital Stamp", invoice.stampData), ("Digital Signature", invoice.signatureData)]
                if images.contains(where: { $0.1 != nil }) {
                    if y + 150 > pageHeight - margin { context.beginPage(); y = margin }
                    for (index, entry) in images.enumerated() {
                        guard let data = entry.1, let image = UIImage(data: data) else { continue }
                        let x = margin + CGFloat(index) * 260
                        entry.0.draw(at: CGPoint(x: x, y: y + 16), withAttributes: labelAttrs)
                        let scale = min(220 / max(image.size.width, 1), 90 / max(image.size.height, 1))
                        image.draw(in: CGRect(x: x, y: y + 38, width: image.size.width * scale, height: image.size.height * scale))
                    }
                }
            }
            return url
        } catch {
            return nil
        }
    }
}
