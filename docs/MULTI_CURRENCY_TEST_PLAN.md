# Multi-Currency Support — Implementation & Test Plan

Oct 8, 2026 · @Omar

## Overview

Expense Manager was single-currency: one app-wide currency applied to every account and transaction. This work adds **per-account currency**, **cross-currency transfers with a recorded exchange rate**, and **conversion of totals** under a user-chosen **exchange method**: an exchange-rate API, the user's own fixed rates, or no conversion at all.

## Design rules

These are enforced in the domain layer (use-case validation), not only in the UI.

| Area | Rule |
|---|---|
| No unknown currency | Every account, transaction and budget has a currency code. Saving one without a code is rejected. |
| Accounts | Currency is set at creation and never changes. After creation only the **color and icon** can be edited (name, type, balance, credit limit and currency are locked; the DAO can only write appearance, list order and the archived flag). |
| Archiving | An account with transactions can't be deleted, only **archived**: hidden from pickers and totals, listed under "Archived" in Accounts, restorable. Archived accounts' transactions can't be edited or deleted. An account with no transactions can still be deleted. |
| Transactions | Store `from_currency_code` and `to_currency_code`, always equal to their accounts' currencies. Only **transfers** can involve two currencies; income and expense are single-currency. |
| Rate snapshots | A cross-currency transfer links to exactly one `exchange_rate_snapshot` row: `source` (FETCHED / USER), `provider`, `base_code`, `quote_code`, `rate`, `captured_at`. Same shape whether copied from the fetched rate or typed by the user. The destination account is credited `amount × rate`. Same-currency transactions have no snapshot. |
| Transfer rate | Depends on the exchange method (below). The rate actually used is always recorded on the transfer as its snapshot. |
| Views | Dashboard, analysis, category and transaction-list totals are converted into the display currency under the exchange method, and say how. Rows show each record in **its own currency**. |
| Missing rate | A total that needs an unavailable rate shows **"Rate unavailable (USD→SYP)"**, never an unconverted number, and is not labelled as converted. Charts that can't be fully converted aren't drawn. |
| Budgets | Each budget has its own currency, fixed at creation. The limit is in that currency; linked spending is converted into it. The app's display currency doesn't affect it. |
| Exports | CSV/PDF convert nothing: each row carries its currency (and received amount / rate for cross-currency transfers); totals are listed per currency. |

## Exchange method

Settings → Exchange method. One setting decides where every rate comes from.

| Method | Totals, charts | Budgets | Cross-currency transfer |
|---|---|---|---|
| **Exchange API** | Fetched rate; note "converted at today's rates". | Converted into the budget's currency. | Pre-filled with the fetched rate for a transfer **dated today** (FETCHED snapshot). Backdated, offline or pair not offered: typed by hand. Changing the date asks for the rate again. |
| **Fixed rates** | The pair's midpoint; note "converted at your fixed rates". | Converted with the pair's midpoint. | Pre-filled for **any date** (STATIC snapshot), editable. No record for the pair: typed by hand. |
| **No conversion** | One total per currency, shown side by side (`250$ · 23,375 ليرة`), never added together; note "conversion is off". Charts needing more than one currency aren't drawn. | Only spending in the budget's own currency counts; the row says other currencies aren't counted. | Always typed by hand; recorded on that transfer only (USER snapshot). |

**Fixed rates.** One record per currency pair with a **buy** and a **sell** value (`1 USD: buy 138 / sell 140 SYP`), valid for all dates until changed. A pair has exactly one record whichever way round it is entered. Transfer USD→SYP uses buy (138); SYP→USD uses sell (1/140); views use the midpoint (139) in both directions. Cross rates are not derived: a pair without a record is "Rate unavailable".

**Defaults.** A fresh install is on No conversion; creating the first account in a second currency opens the Exchange method screen. An install that already had a provider configured stays on Exchange API.

## Exchange-rate providers

Used by the Exchange API method; configured in Settings → Exchange method → Set up provider: Frankfurter, exchangerate.host, Open Exchange Rates, **Buy/sell quotes** (no bundled endpoint — the user enters the feed URL; first-listed region buy/sell mid, converted from old to new lira ÷100, cross rates derived through SYP), or Custom.

The rate cache (`exchange_rates`) is scoped per provider and cleared when the provider changes; rates are refreshed after 1 hour. A pair the provider doesn't offer is remembered until the next refresh, a failed fetch isn't retried for 1 minute, and concurrent requests for the same base share one fetch.

Note: Frankfurter has no SYP rate. No provider URLs or keys for SYP market-rate feeds are bundled; users add their own.

## Database (schema 11)

- **8 → 10 migration** (`Migration8To10`): v8 was single-currency, so every existing account, transaction (both sides) and budget is stamped with that one currency inside the migration. The code comes from the saved currency's name + symbol matched against the country list (repairing a stale saved code), or from the currency the user confirmed when restoring a backup. Tables are altered in place, never rebuilt, so ON DELETE CASCADE children survive.
- **v9 is dropped**: it only existed in unreleased development builds. A v9 database is wiped and recreated; the app then re-seeds the default accounts and runs onboarding again.
- New tables: `exchange_rate_snapshot` (permanent, linked from `transaction.rate_snapshot_id`), `exchange_rates` (temporary cache, per provider).
- **10 → 11 migration** (`MIGRATION_10_11`): adds `static_exchange_rate` (`base_code`, `quote_code`, `buy`, `sell`, `updated_on`; one row per pair). Additive only, so the fixed rates travel inside backups.
- Schema files: `core/database/schemas/.../10.json`, `11.json`.

## Restoring backups

Before a restore, the app asks which currency the backup's amounts are in. Backups from before multi-currency (schema 8) don't record it, so the 8 → 10 migration that runs on the next start uses the answer. Newer backups carry their own currencies and ignore it. v9 backups are wiped like any v9 database.

## Verification status

- **Unit tests:** 260 pass across all modules (run Oct 8, uncommitted work included), including new tests for transaction validation (currencies, snapshot pair/rate, fetched vs backdated, archived accounts), account immutability/archive/delete, snapshot building in the transaction screen, and the buy/sell quotes adapter, amount formatting (sign and symbol placement), signed row amounts, and the three exchange methods (rate source, buy/sell/midpoint, per-currency totals, budgets, transfer pre-fill, fixed-rate validation).
- **Instrumented (emulator, Android 12, Oct 8):** 26 of 27 `core:database` tests pass, including `Migration8To10Test` and `Migration10To11Test` (new table shape, existing data untouched, one row per pair). The one failure, `Migration7To8Test.afterMigration_deletingTransactionCascadesToAttachments`, predates this work: its test database doesn't enable SQLite foreign keys.
- `spotlessCheck` crashes repo-wide (ktlint/JDK tooling mismatch), unrelated to this work.

### Checked live on an emulator

- [x] Upgrading a v9 dev install wipes it, recreates defaults, and reruns onboarding
- [x] Changing the main currency in onboarding recreates the default accounts in that currency
- [x] New account defaults to the app currency; picking USD saves it; balance shown as `$100.0`
- [x] Editing an existing account: only color/icon editable, details shown read-only
- [x] USD → SYP transfer dated today: rate fetched from a buy/sell quotes feed (matched the live mid), destination credited `amount × rate`, FETCHED snapshot linked
- [x] Backdated transfer: nothing fetched, save blocked until a rate is entered, saved as a USER snapshot
- [x] Account with transactions offers Archive (not Delete); archived account moves to "Archived" and disappears from pickers
- [x] Restore asks for the backup's currency first

### Checked on Oct 7 (emulator, Android 12, app currency USD, saved 45-record database, buy/sell quotes feed)

- [x] Budget in a currency other than the app's: a SYP budget for September showed 58,125 of 100,000 ليرة (23,375 SYP of expenses + 250 USD converted at 139); the app currency stayed USD
- [x] "Rate unavailable (SYP→USD)" on the dashboard with no provider configured; with a buy/sell quotes feed set, the same total showed 1.5$ plus the "converted at today's rates" note
- [x] Editing a cross-currency transfer: an amount-only edit kept the same snapshot and rebalanced both accounts; changing the date cleared the rate, blocked saving ("Enter a rate greater than 0") and saved the typed rate as a USER snapshot
- [x] Suffix position: `22,135 ليرة`, `100$`, `-1.5$`, and `−208 ليرة` on rows

### Checked on Oct 8 (emulator, Android 12, exchange methods)

- [x] Upgrading the schema-10 install kept all data and stayed on Exchange API (a provider was configured)
- [x] Fixed rates: added `1 USD: buy 138 / sell 140 SYP`; the dashboard refreshed at once to `1.5$` with "converted at your fixed rates"
- [x] Fixed rates on a transfer: USD→SYP pre-filled 138, SYP→USD pre-filled 0.00714 (1/140), both labelled as the fixed rate
- [x] No conversion: October showed `208 ليرة` (not relabelled as dollars); September showed expense `250$ · 23,375 ليرة` and total `125,711 ليرة · -250$`, with "conversion is off"

### Still to check on a device

- [ ] Restoring an actual v8 backup with a different currency than the app's (needs the system file picker; not attempted)
- [ ] CSV/PDF export with mixed currencies. Not verifiable on the Android 12 emulator (see the export issue below); the CSV code writes per-currency totals and currency / received amount / rate columns, but no exported file has been inspected
- [ ] "Rate unavailable" and per-currency totals on the Analysis and Category screens (only the dashboard was checked)
- [ ] No conversion: the budget row's "other currencies aren't counted" note and the hand-typed transfer hint (unit-tested only)
- [ ] The prompt after creating the first account in a second currency (not exercised)
- [ ] Fixed-rate editor on a real phone (layout was only seen on a 320 dp emulator)

### Issues found during these checks

- **Fixed: "Converted at today's rates" note showed while the rate was unavailable.** A total now counts as converted only when it was actually produced.
- **Fixed: old snapshot was left behind.** Replacing a transfer's rate, or deleting the transfer, now removes the snapshot nothing links to. Both fixes are covered by unit tests, not yet re-checked on a device.
- **Export reports success without writing a file on Android 12 and below.** With no file picker on those versions the export writes to a legacy storage path, the failure is swallowed, and the screen says "File exported successfully". This predates the multi-currency work.
- **Month arrow.** After a fresh start the first tap on the month-back arrow doesn't refresh the screen and the next tap jumps two months (seen three times on the phone, not investigated).

## Known gaps

- Statistics are converted at today's rate by design; there is no historical-rate conversion for views.
- The buy/sell quotes adapter always uses the first region listed for each currency.
- A reversed fixed-rate transfer pre-fills the raw inverse (`0.007142857142857143`); it is correct but unrounded.
- New screen and note texts are English only (no translations yet).
- The sign and spacing rules apply to every currency whose symbol contains a letter (129 of the 150 in the country list, e.g. `R$`, `AU$`, `kr`), not only the Syrian pound: prefix shows `R$ -5`, suffix `-5 R$`.
- With "No conversion", a category's share of the total shows 0%: amounts in different currencies can't be compared, so no percentage is computed.
