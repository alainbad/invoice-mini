# Version 1.1 update

Both native projects now have country selection with linked calling-code field, an optional business tax registration number, business/client invoice phone fields, invoice currency selection, and separate stamp/signature image uploads. These fields are saved and included in PDF output. Currency changes do not perform foreign-exchange conversion. Images are visual marks, not certificate-backed digital signatures.

Version settings: iOS 1.1 (2); Android 1.1 / versionCode 2. Verify that build 2 remains unused in each store before uploading.

## Source provenance

- iOS imported from the supplied `Invoice Mini.zip`: bundle ID `com.alanbadrain.Invoice-Mini`, version 1.0 (1), previous invoice identity fix present.
- Android imported from the main Gradle project in `Invoice Mini(1).zip`: package `com.alanbadrain.invoicemini`, version 1.0 (1), target SDK 36.
- These are supplied source snapshots, not cryptographically verified against the store binaries.
- Build caches, nested older Android source copies, IDE state, and compiled releases are excluded.

## Data compatibility

Android database 1 → 2 adds nine nullable TEXT columns via an explicit Room migration. Existing records retain null for unset new fields. iOS adds only optional SwiftData attributes and uses automatic lightweight migration; test this against an actual version 1 database before release. No reset or destructive migration is configured.

For legacy invoices with no stored currency, the prior device-locale display is retained. New invoices save the selected currency. Profile changes affect defaults for new invoices, not saved invoice snapshots. Duplication preserves the extra fields/images. Android stores a unique PNG for each uploaded stamp/signature so replacement/removal on one invoice does not overwrite another invoice's image. Unreferenced image cleanup is not yet implemented.

Country/currency names come from platform locale data. Calling codes are a bundled snapshot from python-phonenumbers 9.0.41; territories without a dedicated calling-code entry allow manual prefix entry. The calling-code field can be overridden for a foreign business number. Existing phone strings are not rewritten automatically.

## Validation

- Swift/Kotlin syntax parsed with tree-sitter: passed (syntax checks are not native type checking).
- Actual Android migration SQL executed against populated legacy SQLite tables: passed; existing values preserved and new fields can be written/read.
- Native Android build attempted locally: blocked because the host Java binary cannot load `libjli.so`.
- iOS native build unavailable locally because this host has no Xcode.
- GitHub Actions workflow added for Android unit tests/debug build and unsigned iOS Simulator build. Consult the workflow results; configuration alone does not establish a passing build.
- No signed store artifacts have been produced or submitted.

## Required device/release checks

Install version 1 with existing invoices/profile, then upgrade without uninstalling. Confirm old data and numbering, save/reopen all new fields, restart the app, duplicate an invoice, and confirm its image and currency independence. Test USD, JPY, KWD, and AED; confirm totals and tax-after-discount rules are unchanged. Test picker cancellation, invalid images, transparent stamp/signature images, replacement/removal, and PDF sharing including a multipage invoice. Review long business/client details and PDF layout on both platforms.

On the Mac, build/archive the Xcode project using the existing Apple signing identity. On Android, build the release AAB with the original upload key. Store review submissions remain pending native validation and signing.

## Release notes

- Select your business country with a linked telephone prefix.
- Add an optional tax registration number and invoice contact numbers.
- Choose the currency for each invoice.
- Add digital stamp and signature images to invoices and shared PDFs.
