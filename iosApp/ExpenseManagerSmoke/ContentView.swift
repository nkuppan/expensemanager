import Shared
import SwiftUI

/// Paste receipt text; the shared Kotlin receipt parser reads it and Swift shows the result.
/// Shared code returns plain values, and formatting (amount, date) happens here.
struct ContentView: View {
    @State private var text = ""

    var body: some View {
        let summary = ReceiptReader.shared.read(text: text)
        NavigationStack {
            Form {
                Section("Receipt text") {
                    TextEditor(text: $text)
                        .frame(minHeight: 160)
                        .font(.system(.body, design: .monospaced))
                    HStack {
                        Button("GPay sample") { text = Samples.gpay }
                        Spacer()
                        Button("Shop bill sample") { text = Samples.shopBill }
                    }
                    .buttonStyle(.borderless)
                }
                Section("Read by shared Kotlin code") {
                    LabeledContent("Amount", value: summary.amount.map { Formats.amount($0.doubleValue) } ?? "–")
                    LabeledContent("Payee", value: summary.payee ?? "–")
                    LabeledContent("Type", value: summary.isIncome ? "Income" : "Expense")
                    LabeledContent("Date", value: summary.dateEpochMillis.map { Formats.date($0.int64Value) } ?? "–")
                    LabeledContent("Account", value: summary.accountDigits.isEmpty ? "–" : summary.accountDigits.joined(separator: ", "))
                }
            }
            .navigationTitle("Receipt reader")
        }
    }
}

private enum Formats {
    private static let amountFormatter: NumberFormatter = {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return formatter
    }()

    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter
    }()

    static func amount(_ value: Double) -> String {
        amountFormatter.string(from: NSNumber(value: value)) ?? String(value)
    }

    static func date(_ epochMillis: Int64) -> String {
        dateFormatter.string(from: Date(timeIntervalSince1970: Double(epochMillis) / 1000))
    }
}

/// The same samples as the shared ReceiptParserTest.
private enum Samples {
    static let gpay = """
        ₹250
        Paid to
        SWIGGY
        swiggy.in@icici
        Completed
        7 Nov 2026, 7:15 pm
        UPI transaction ID
        631234567890
        From: Naveen Kumar (HDFC Bank)
        """

    static let shopBill = """
        FRESH MART SUPERMARKET
        Tax Invoice
        GSTIN 33ABCDE1234F1Z5
        Date: 05/11/2026 18:42
        Milk 1L        2   56.00
        Bread               45.00
        Sub Total          101.00
        CGST 2.5%            2.53
        SGST 2.5%            2.52
        Total Qty: 3
        Grand Total        106.05
        Card **** 4821
        """
}
