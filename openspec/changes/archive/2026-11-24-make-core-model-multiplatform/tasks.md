# Tasks: make-core-model-multiplatform

Each group stops at its verify task. Claude writes the code; the developer runs the command on
the Mac and shares `build-log.txt`. A task is checked off only after a green run.

## 1. Pure Kotlin milli maths and the module conversion

- [x] 1.1 Replace `Double.toMilli()` in `Debt.kt` with the D1 algorithm (exact comparisons; `-toMilli(-d)` for negatives; `require(isFinite())`; `kotlin.math.round` fallback beyond the exact ranges), with a KDoc that states the exact ranges
- [x] 1.2 Replace `Long.milliToDouble()` with `this / 1000.0` (D2); remove the `java.math` imports
- [x] 1.3 `core/model/build.gradle.kts`: apply `naveenapps.plugin.kmp.library`; `commonMain` gets `api(libs.kotlinx.datetime)` and `implementation(libs.androidx.compose.runtime.annotation)`; drop `kotlinx-serialization-json` and `lintChecks`
- [x] 1.4 Move `core/model/src/main/kotlin/**` to `core/model/src/commonMain/kotlin/**` (same packages) and delete `core/model/src/main/AndroidManifest.xml`
- [x] 1.5 Verify: `./gradlew assembleDebug test lintDebug > build-log.txt 2>&1` is green, `DebtLedgerTest` included, with no source changes in the consuming modules

## 2. Tests and iOS compilation

- [x] 2.1 Add `core/model/src/commonTest/.../MilliAmountsTest.kt` with fixed values from `BigDecimal`:
  - the spec scenarios: 20.5 → 20500, 1.0015 → 1002, 2.0025 → 2002, −35.005 → −35005, 700000000000.125 → 700000000000125, 20500 → 20.5;
  - 0 and −0 → 0, 0.0005 → 0, 0.0015 → 2, 1e-5 → 0;
  - one amount just below and one just above 2^39 and 2^43;
  - NaN and infinity throw
- [x] 2.2 Add `core/model/src/androidHostTest/.../MilliAmountsEquivalenceTest.kt`: the D1 corpus of about 4 M amounts inside the exact ranges, compared with `BigDecimal.valueOf(d).setScale(3, HALF_EVEN)`; and `milliToDouble` over ±2 M plus random values below 2^53, compared with `BigDecimal.valueOf(m, 3).toDouble()`. Expect 0 differences
- [x] 2.3 Verify on the Mac: `./gradlew :core:model:compileKotlinIosArm64 :core:model:allTests > build-log.txt 2>&1` - both tests pass on the Android host, and `MilliAmountsTest` passes on `iosSimulatorArm64`
  - Result (24 Nov): iOS simulator 6/6; Android host 6/6 + 3/3 equivalence (0 differences, 1.6 s). Follow-up: the KMP plugin no longer opts every module into the coroutine markers (they warned as unresolved in core:model, which has no coroutines); modules using experimental coroutine APIs opt in themselves.

## 3. Documentation

- [x] 3.1 CLAUDE.md:
  - mark core:model as a Kotlin Multiplatform module in the module list and in the KMP section;
  - the `JvmApiInSharedLayer` lint check now covers only core:repository and core:domain
- [x] 3.2 Update `claude/kmp-migration-roadmap.md` (status and progress log)

## 4. Final verification

- [x] 4.1 Full Android build on the Mac: `./gradlew assembleDebug test lintDebug spotlessCheck > build-log.txt 2>&1` ends with BUILD SUCCESSFUL
- [x] 4.2 iOS: `./gradlew :core:model:allTests :core:foundation:allTests` pass on `iosSimulatorArm64`
- [x] 4.3 Smoke test on an Android device:
  - add a debt entry of 1,234.56 and one of 0.5;
  - check the person's balance and the Home debt totals;
  - edit the first entry to 1,234.565 and check the amount shown
- [x] 4.4 `openspec validate make-core-model-multiplatform --strict` passes; then archive the change
