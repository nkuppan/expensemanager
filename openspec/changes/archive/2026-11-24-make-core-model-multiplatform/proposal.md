# Proposal: make-core-model-multiplatform

## Why

core:model holds every domain model, from transactions and accounts to budgets and debts. Every
other shared layer depends on it, so it has to compile for iOS before core:repository and
core:domain can. One file stands in the way: the debt ledger in `Debt.kt` converts amounts to
and from stored milli-units (₹20.50 → 20500) with `java.math.BigDecimal`, which doesn't exist on
iOS. Everything else in the module is already plain Kotlin, kotlinx-datetime, and the Compose
`@Stable` annotation, which is published for iOS.

## What Changes

- `Double.toMilli()` and `Long.milliToDouble()` in `Debt.kt` become pure Kotlin with no
  `BigDecimal`, and give the same results as today (decision: pure Kotlin decimal rounding):
  - `toMilli` rounds the amount's shortest decimal value half to even at 3 decimals, as
    `BigDecimal.valueOf(d).setScale(3, HALF_EVEN)` did. It uses exact IEEE comparisons, not
    `Double.toString()`, whose format Kotlin/Native documents as unstable. Results are identical
    to the old code for every amount below 2^39 (about 550 billion), and for every amount with up
    to 3 decimals below 2^43 (about 8.8 trillion). Beyond that, it rounds the binary value.
  - `milliToDouble` is `milli / 1000.0`, identical to `BigDecimal.valueOf(milli, 3).toDouble()`
    for every value below 2^53 milli-units (about 9 trillion).
- core:model becomes a Kotlin Multiplatform module with `naveenapps.plugin.kmp.library`:
  - sources move from `src/main/kotlin` to `src/commonMain/kotlin`, with no code changes besides
    `Debt.kt`;
  - the empty `AndroidManifest.xml` is removed;
  - so is the unused `kotlinx-serialization-json` dependency;
  - the `JvmApiInSharedLayer` lint check is removed for this module, because the iOS compile now
    rejects JVM and Android APIs outright.
- New tests:
  - `MilliAmountsTest` in `commonTest`, with fixed values covering ties, negatives, tiny amounts
    and the range limits; it runs on the Android host and the iOS simulator;
  - `MilliAmountsEquivalenceTest` in `androidHostTest`, which compares both functions with
    `BigDecimal` over about 4 million amounts.
- The nine modules that depend on core:model are unchanged. They consume its Android variant
  exactly as before.

User-visible impact: none. Stored debt amounts are unchanged (see the limits above; amounts the
keypad can produce are always within the exact range).

## Capabilities

### New Capabilities
- `milli-amounts`: how amounts convert to and from the stored milli-units, and where that is
  exact.

### Modified Capabilities
- `shared-code-boundaries`: core:model joins the shared modules. The iOS compile, not the lint
  check, now keeps JVM and Android APIs out of it.

## Impact

- **Module:** `core/model`. Its build file changes; sources move to `src/commonMain/kotlin`;
  `AndroidManifest.xml` is deleted; `Debt.kt` changes; tests are added.
- **Consumers:** core:repository, core:domain, core:data, core:database, core:datastore,
  core:designsystem, core:notification, core:testing and app. No source changes are expected;
  the build proves it.
- **Dependencies:** removes `kotlinx-serialization-json` from core:model; adds nothing.
- **Tooling:** the iOS compile and tests need the Mac, as in change 1.

**Rollback:** revert this change's commits on `feature/kmp-migration`. No stored data or schema
changes.
