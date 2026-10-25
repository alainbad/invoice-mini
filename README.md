# Mini Invoice Maker

Offline invoicing app for freelancers and small businesses. Separate native apps for iOS and Android; no account or backend.

## Status — October 25, 2026

- iOS: live on the App Store, according to the supplied project summary. Swift / SwiftUI / SwiftData; minimum iOS 17. Bundle ID: `com.alanbadrain.Invoice-Mini`.
- Android: production release 1.0 confirmed by the owner's Play Console screenshot: Available on Play, 100% rollout, 173 countries/regions. This supersedes the supplied summary's older closed-testing status. Package: `com.alanbadrain.invoicemini`.
- Repository: initialized with this handoff. The released application source has not yet been supplied here. No feature implementation, migration, compilation, signing, or update submission has been completed.

## Source needed

Import the current released projects, preferably under `ios/` and `android/`, including their build configuration and dependency locks. The supplied summary identifies `InvoiceMini-verified.zip` (iOS) and `InvoiceMiniAndroid.zip` (Android); verify that each matches the released build before import. Prefer the latest local projects if those archives predate release fixes.

Keep signing keys, certificates, passwords, local configuration, provisioning profiles, and customer data out of Git. Retain the existing store identities and signing setup for updates.

## Requested update — both platforms

1. Business profile: searchable country dropdown covering all countries/territories supported by the chosen maintained country dataset; selecting a country sets the telephone calling-code selector. Preserve the entered national number and allow an explicit calling-code override for businesses using a foreign number. Add an optional tax registration number stored as text, preserving leading zeros and letters.
2. Invoice editor: editable mobile-number fields in both business and client details. Prefill the business number from the profile for new invoices; retain the saved invoice's details when editing an existing invoice.
3. Invoice editor: searchable currency dropdown covering current supported ISO 4217 currencies, displaying currency code and name. Persist the selected currency on each invoice and use it consistently in totals, preview, PDF, and sharing. Currency selection changes denomination/formatting, not exchange rates or entered amounts.
4. Invoice editor: separate digital stamp and digital signature image uploads, with preview, replacement, and removal. Store images durably in app-managed storage and include them in invoice preview and exported/shared PDFs, retaining transparency and aspect ratio. These are uploaded images, not cryptographic signatures.

## Implementation and acceptance criteria

- Preserve existing invoices and business profiles through explicit, tested SwiftData and Room schema migration paths; no destructive database reset.
- Preserve historical invoice values when profile defaults change. New optional fields must not prevent opening or exporting old invoices.
- Display business tax registration number and both mobile numbers in invoice previews/PDFs when provided; omit blank optional values.
- Persist country, calling code, phone numbers, currency, stamp, and signature across save/reopen and app restart. Verify duplicate-invoice behavior, including durable image references.
- Use appropriate currency precision, including zero- and three-decimal currencies, without changing the existing discount and tax calculation rules.
- Retain the prior iOS invoice-identity fix: the editable Invoice is held in `@State`; view reconstruction must not create blank invoices or change invoice numbering.
- Test sequential numbering, line-item changes, fixed/percentage discounts, tax after discount, duplicate/delete, search/filter, logo, PDF preview, and sharing alongside the new features.
- Test both platforms with long international phone numbers, shared calling codes, optional tax IDs, multiple currencies, transparent images, picker cancellation, missing/unreadable images, long invoices, and upgrade from existing populated databases.
- Inspect PDF pagination and layout with both stamp and signature present.

## Release preparation gate

After source import and implementation: run available automated checks and native builds, perform device/simulator and migration checks, then set a new marketing version and monotonically increasing platform build numbers based on the actual current project/store versions. Prepare release notes and Android AAB / iOS archive using the existing signing configuration. Record results and any checks that could not be run. Do not describe either update as built or ready for submission until verified artifacts exist.

Proposed release notes:

- Choose your business country with an automatic telephone prefix.
- Add an optional tax registration number and business/client mobile numbers.
- Choose invoice currency from a searchable list.
- Add stamp and signature images to your invoices and PDFs.
