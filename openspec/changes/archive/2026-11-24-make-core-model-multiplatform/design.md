# Design: make-core-model-multiplatform

## Context

- **core:model today:** an Android library (`naveenapps.plugin.android.library`) with 41 Kotlin
  files and an empty manifest. Its dependencies are `api(kotlinx-datetime)`, `@Stable` from
  `androidx.compose.runtime:runtime-annotation` 1.12.1 (published for iOS), and an unused
  `kotlinx-serialization-json`.
- **The only JVM API** is in `Debt.kt`:
  - `Double.toMilli() = BigDecimal.valueOf(this).setScale(3, HALF_EVEN).unscaledValue().toLong()`
  - `Long.milliToDouble() = BigDecimal.valueOf(this, 3).toDouble()`

  Debt entries store `amountMilli` from `toMilli`, so its results must not change.
- **What `BigDecimal.valueOf(d)` actually rounds:** it uses `Double.toString(d)`, the shortest
  decimal that converts back to `d`, not the double's exact binary value. So `1.0015` is a tie
  that rounds to even (1002), while `d * 1000` would give `1001.4999…` and round to 1001.
- **Why `toString()` can't be reused on iOS:** Kotlin/Native documents `Double.toString()`'s
  format as unstable. It only guarantees that the string converts back to the same `Double`, not
  that it is the shortest. A longer string decides ties differently: "2.0014999999999999" instead
  of "2.0015".
- **Consumers:** core:repository, core:domain, core:data, core:database, core:datastore,
  core:designsystem, core:notification, core:testing and app, all Android modules.

## Goals / Non-Goals

**Goals:**
- core:model compiles for Android, iosArm64 and iosSimulatorArm64, with no source changes outside
  `Debt.kt`.
- `toMilli` and `milliToDouble` keep today's results, proven against `BigDecimal`, and give the
  same results on iOS.

**Non-Goals:**
- The `Money` type and the Double → milli migration of other tables (`claude/money-migration-plan.md`).
  This change only keeps the two existing functions working on iOS.
- Removing Android-specific values that are still plain `Int`s, such as `titleResId` in `Theme` and
  `AppLocale` (B2b). They compile on iOS and are handled with the UI work.

## Decisions

### D1. `toMilli` from exact comparisons, not from text
For a finite, non-negative `d` (negatives use `-toMilli(-d)`; `BigDecimal` rounds half-even
symmetrically):
1. Find `k` with `k/1000.0 ≤ d < (k+1)/1000.0`. Start from `floor(d * 1000)` and step until the
   bounds hold.
2. If `k/1000.0 == d`, the amount's shortest decimal has at most 3 decimals: return `k`.
3. Otherwise let `t = (10k + 5) / 10000.0`, the tie `k.5` milli-units as a double. If `t == d`, the
   shortest decimal is exactly that tie: return `k` if it's even, else `k + 1`.
4. Otherwise the shortest decimal lies on the same side of the tie as `d` (the tie converts to a
   different double, so it lies outside `d`'s rounding interval, which contains the shortest
   decimal). Return `k + 1` if `d > t`, else `k`.

Each division divides two exactly representable numbers, so it is correctly rounded, exactly what
parsing the decimal would give. This holds on every IEEE 754 platform, iOS included.

**Where it is exact.** Steps 2–4 assume no two 4-decimal numbers convert to the same double. That
holds while doubles are less than 0.0001 apart, i.e. below 2^39 (549,755,813,888). From 2^39 up
to 2^43, doubles are still less than 0.001 apart, so step 2 alone stays exact. Amounts with at
most 3 decimals (everything the keypad produces) are therefore exact up to 2^43. Beyond those
limits the function rounds the binary value ×1000 half to even with `kotlin.math.round` (ties to
even on every platform), which may differ by one milli-unit.

**Measured** with a Java model of this exact algorithm against `BigDecimal` on JDK 21:
- 4,258,894 amounts below 2^39: 0 differences. The mix was every 4-decimal value in
  ±20, ties `k/1000 + 0.0005`, random amounts at several scales, exponent-form values (1e-5,
  1.2345678e7) and the edge cases 0, −0, the smallest double, 0.1+0.2 and 20.499999999.
- 2,000,000 three-decimal amounts between 2^39 and 2^43: 0 differences.
- Above 2^39 with 4+ decimals: about 3% differ by one milli-unit, as expected.

*Alternatives:*
- **Parse `toString()`.** Exact on the JVM (0 differences in 2.1 M), but Kotlin/Native doesn't
  promise the shortest text, so iOS could round ties differently. Rejected.
- **A multiplatform BigDecimal library (ionspin bignum).** Rejected when the approach was chosen:
  a new dependency for two functions, and it goes against the money plan's integer-maths
  direction.
- **`round(d * 1000)`.** Rejected because it changes results at ties.

### D2. `milliToDouble` = `milli / 1000.0`
Both operands are exact below 2^53, so the quotient is the correctly rounded value of the exact
decimal, which is what `BigDecimal.valueOf(milli, 3).toDouble()` computes. Measured: 7,000,001
values, 0 differences. Above 2^53 milli-units (about 9 trillion), `Long.toDouble()` rounds first,
so the last bit can differ. That's out of range for real amounts, and stated in the spec.

### D3. Converting the module
- **Build:** `core/model/build.gradle.kts` applies `naveenapps.plugin.kmp.library`. `commonMain`
  gets `api(libs.kotlinx.datetime)` and `implementation(libs.androidx.compose.runtime.annotation)`.
- **Removed:**
  - `kotlinx-serialization-json`: no file uses it;
  - the `lintChecks(project(":lint-rules"))` line: the iOS compile enforces the rule now;
  - `AndroidManifest.xml`: it's empty, and the KMP Android target doesn't need it.
- **Sources:** move `src/main/kotlin/**` to `src/commonMain/kotlin/**` with `git mv`-style moves,
  so history follows. Packages are unchanged.
- **Namespace:** the plugin derives it from the module path. It stays
  `com.naveenapps.expensemanager.core.model`.
- **Consumers:** they keep `implementation(project(":core:model"))`. Gradle picks the Android
  variant of a KMP module automatically, as it already does for core:foundation through
  core:common.

### D4. Tests
- `MilliAmountsTest` in `commonTest` has fixed expected values: the scenarios in the spec, plus
  0, −0, 0.0005 (a tie, gives 0), 0.0015 (gives 2), 1e-5, and amounts just below and above both
  limits. It runs on the Android host and on the iOS simulator. Its expected values come from
  `BigDecimal` on the JVM.
- `MilliAmountsEquivalenceTest` in `androidHostTest` runs the D1 corpus, about 4 M amounts,
  against `BigDecimal`. It's JVM-only because it needs `java.math`, and is sized to finish in a
  few seconds.
- The existing `DebtLedgerTest` in core:domain keeps running unchanged against the new functions.

## Risks / Trade-offs

- **Kotlin/Native and JVM floating point differ:** IEEE 754 binary64 division and comparison are
  exact operations on both. `floor` and `round` are specified identically. → `MilliAmountsTest`
  runs on the iOS simulator; any platform difference fails there.
- **A consumer relied on something only the Android library exposed:** for example, Android
  resources or a `BuildConfig`. core:model has none (empty manifest, no `res/`). → The full
  Android build in task group 1 proves it.
- **The equivalence test's runtime:** 4 M `BigDecimal` operations take a few seconds on the JVM.
  → Keep the corpus at about 4 M and don't run it on iOS.
- **Amounts outside the exact ranges** (4+ decimals above about 550 billion, or anything above
  about 8.8 trillion) may store one milli-unit differently from before. → No input path produces
  them. The spec states the limits, so they're a known property, not a surprise.

## Migration Plan

1. In `Debt.kt`, replace the two functions while the module is still an Android library, and
   verify the full Android build and `DebtLedgerTest`.
2. In the same task group, convert the module (build file, source move, manifest and dependency
   removal) and verify the full Android build again.
3. Add the two tests and run `:core:model:allTests` on the Mac (JVM + iOS simulator).
4. Update the docs, run the final verification and archive.

**Rollback:** revert the change's commits. No data or schema impact.

## Open Questions

- None.
