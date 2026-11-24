# milli-amounts Specification

## Purpose

How money amounts convert to and from the milli-units the debt ledger stores (₹20.50 → 20500),
and the ranges where that conversion is exact, so the same stored values come out on Android and
iOS.

## Requirements

### Requirement: Amount to milli-units
`Double.toMilli()` SHALL return the amount's shortest decimal value (the shortest decimal that
converts back to the same `Double`) times 1000, rounded half to even. Negative amounts SHALL round
symmetrically. A non-finite amount (NaN or infinity) SHALL throw.

#### Scenario: Two-decimal amount
- **WHEN** `20.5.toMilli()` is called
- **THEN** it returns 20500

#### Scenario: Exact tie rounds to even
- **WHEN** `1.0015.toMilli()` and `2.0025.toMilli()` are called
- **THEN** they return 1002 and 2002

#### Scenario: Negative amount
- **WHEN** `(-35.005).toMilli()` is called
- **THEN** it returns -35005 (35.005 ×1000 is exactly 35005, so no rounding is needed)

#### Scenario: Not a number
- **WHEN** `Double.NaN.toMilli()` is called
- **THEN** it throws

### Requirement: Exact range of toMilli
`toMilli` SHALL give exactly the result of `BigDecimal.valueOf(amount).setScale(3, HALF_EVEN)`
for every amount whose magnitude is below 2^39 (549,755,813,888), and for every amount with at
most 3 decimals whose magnitude is below 2^43 (8,796,093,022,208). Beyond that it SHALL round the
binary value ×1000 half to even, which may differ by one milli-unit.

#### Scenario: Equivalence with BigDecimal
- **WHEN** `MilliAmountsEquivalenceTest` compares `toMilli` with `BigDecimal` over about 4 million
  amounts inside the exact ranges
- **THEN** there are no differences

#### Scenario: Large three-decimal amount
- **WHEN** `700000000000.125.toMilli()` is called
- **THEN** it returns 700000000000125

### Requirement: Milli-units to amount
`Long.milliToDouble()` SHALL return `milli / 1000.0`, which SHALL equal
`BigDecimal.valueOf(milli, 3).toDouble()` for every value whose magnitude is below 2^53.

#### Scenario: Back to a Double
- **WHEN** `20500L.milliToDouble()` is called
- **THEN** it returns 20.5

### Requirement: Same results on every platform
`toMilli` and `milliToDouble` SHALL NOT depend on `Double.toString()` or any platform formatter,
so they give the same results on Android and iOS.

#### Scenario: Shared test on both targets
- **WHEN** `MilliAmountsTest` runs through `./gradlew :core:model:allTests`
- **THEN** it passes on the Android host and on `iosSimulatorArm64` with the same expected values
