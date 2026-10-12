# Expense Manager (Android) — Improvement Plan

_Audit date: 12 Oct 2026. Findings were checked against the code; file paths are relative to the repo root (module paths shortened)._

Effort: **S** ≈ under a day · **M** ≈ 2–5 days · **L** ≈ 1–2+ weeks

---

## Phase 1 — Data integrity & money correctness (do first)

These can silently corrupt balances or lose user data.

| # | Issue | Evidence | Fix | Effort |
|---|---|---|---|---|
| 1.1 ✅ | Balance updates not atomic on edit/delete | `core/data/.../TransactionRepositoryImpl.kt:65-90` — `removePreviousEnteredAmount()` and `updateTransaction()`/`delete()` run as separate DB calls | One `@Transaction` DAO method each for update and delete | S |
| 1.2 ✅ | Editing a transaction can change its amount | `NumberFormatRepositoryImpl.formatForEditing()` returns `value.toString()` (`.` decimal, `1.0E7` for ≥10M); `save()` parses with the locale formatter | Format with the same locale formatter (no grouping, 2 decimals); add locale unit tests (de, fr, ar, hi) | S |
| 1.3 ✅ | Amounts displayed with 1 decimal | `NumberFormatRepositoryImpl.kt:80-95` — `maximumFractionDigits = 1` | Use `Currency.defaultFractionDigits` | S |
| 1.4 | Deleting an account silently deletes all its transactions | `TransactionEntity.kt:18-32` `onDelete = CASCADE`; generic delete dialog in `AccountCreateScreen.kt` | Show count of affected transactions; offer "Move transactions to…" / archive; block deleting the last account | S–M |
| 1.5 | Recurring occurrences can be duplicated | `RecurringTransactionUseCases.kt:57-70` — `nextOccurrence` advanced only after the loop | Insert each occurrence + advance rule in one Room transaction | M |
| 1.6 | Restore can brick the app | `BackupRepositoryImpl.kt:61-67`, `PendingDatabaseRestore.kt:43-47` — no min-version/schema check, no rollback | Validate version + tables; keep `.bak` and roll back if open fails | M |
| 1.7 | Backup excludes attachments & custom images | `BackupRepositoryImpl.kt:47-86` snapshots the DB only | Zip DB + image folders | M |
| 1.8 | Money stored as `Double` | `Amount.kt`, all entities, ~20 `sumOf` in `core/domain`; running balances in `TransactionDao.kt:110,121` | Migrate to `Long` minor units + `BigDecimal` in domain (interim: round to currency decimals before each write) | L |

## Phase 2 — Security & privacy

| # | Issue | Evidence | Fix | Effort |
|---|---|---|---|---|
| 2.1 ✅ | App lock never re-locks | `MainViewModel.kt:39,73` — `_isAuthenticated` never reset | Re-lock on `ProcessLifecycleOwner` ON_STOP (+ optional timeout); `FLAG_SECURE` when lock enabled | S |
| 2.2 | Backup "encryption" key hard-coded | `BackupRepositoryImpl.kt:121` `BACKUP_PASSWORD = "YOUR_SECRET_PASSWORD"` | Versioned format with user passphrase (keep reading v1 files) | M |
| 2.3 | FileProvider exposes all external storage | `app/src/main/res/xml/provider_paths.xml` `<external-path path=".">` | Restrict to export dir / `cache-path` | S |
| 2.4 | CSV formula injection | `BaseExportStrategy` / CSV export writes notes unescaped | Escape quotes; prefix `= + - @` with `'` | S |

## Phase 3 — UX bugs & rough edges

| # | Issue | Evidence | Fix | Effort |
|---|---|---|---|---|
| 3.1 ✅ | Account/category selection reset on transaction screen | `TransactionCreateViewModel.kt:146-195` resets selection on every list emission | Only default when nothing selected; auto-select newly created item | S |
| 3.2 ✅ | Transfers fail silently | From/To both default to same account; `save()` returns early (`:263-268`); snackbar host unused | Pre-select a different "to" account; show error | S |
| 3.3 | Errors swallowed app-wide | 14× `is Resource.Error -> Unit` (e.g. `AccountCreateViewModel.kt:171,184`, `BudgetCreateViewModel.kt:165,220`); existing error strings unused | Shared error event → snackbar | M |
| 3.4 | Export reports success when nothing was written | `BaseExportStrategy.kt:38-49` (legacy external storage fallback, swallowed `IOException`) | Write via SAF/MediaStore; propagate errors | S |
| 3.5 | No search / swipe actions / undo on transaction list (README promises them) | `TransactionListScreen.kt:266-283`; `GetTransactionByNameUseCase` unused | Search on notes/category; swipe edit/delete with Undo snackbar | M |
| 3.6 | Number pad not pre-filled when editing | `NumberPadDialogView` has no initial value | Pass current amount in | S |
| 3.7 | Empty-state flash on cold start | `TransactionListViewModel.kt:34` starts with `emptyList()`; dashboard/analysis lack `isLoading` | Add loading state | S |
| 3.8 | No "Discard changes?" when leaving a filled form | `TransactionCreateViewModel.kt:400` | Confirm dialog when dirty | S |
| 3.9 | Dashboard account tap opens edit form | `DashboardViewModel.kt:253` | Account detail/history screen | M |
| ✅ | Number pad Cancel reset amount to 0 | `NumberPadScreenView.kt` | **Fixed** (cancel now passes `null`) | — |

## Phase 4 — Widget

| # | Issue | Fix | Effort |
|---|---|---|---|
| 4.1 | Never refreshed after a save (only 30-min period / manual button) | Call `refreshExpenseWidgetData` after any transaction change | S |
| 4.2 | "Total balance" is actually income − expense for the in-app filter period; "saved" shown when negative | Own fixed period + correct labels | S |
| 4.3 | Hard-coded English strings & fixed dark colours (`ExpenseWidget.kt`) | Move to string resources; follow system theme | S |
| 4.4 | 756-line file inside `:app` | Extract `:feature:widget` | M |

## Phase 5 — Accessibility, RTL, localisation

- Add `contentDescription` to actionable icons: `FilterView.kt:114-160`, `TransactionCreateScreen.kt:383-388`, back buttons in `CategoryListScreen.kt:93`, `CategoryDetailScreen.kt:91`, `BudgetDetailScreen.kt:86`, `IconSelectionScreen.kt:140`, `AppTopNavigationBar.kt:47`. **S**
- Touch targets ≥ 48dp (`FilterView` 36dp, widget 22dp). **S**
- RTL: use `Icons.AutoMirrored` chevrons (`FilterView.kt:120,159`, `DashboardWidgetTitle.kt:49`); fix swipe-row direction. **S**
- TalkBack custom actions for swipe-only actions; restore click indication in `TransactionItem.kt`. **S**
- Move remaining hard-coded strings ("Edit"/"Delete" in `TransactionItem.kt`, PDF title, widget) to resources. Translations otherwise complete across 16 languages. **S**
- Delete dead `OverallStatusOverview.kt`. **S**

## Phase 6 — Engineering health

| # | Item | Effort |
|---|---|---|
| 6.1 ✅ | CI: run on `pull_request`, add `spotlessCheck`, lint, unit tests; JDK 17/21 instead of 19; bump actions to v4 (`.github/workflows/build.yml`) | S |
| 6.2 | Tests: 25 of 30 modules have none. Priority: `NumberFormatRepositoryImpl` (multi-locale), recurring use case, backup/restore, `TransactionDao` balance invariants, migration 2→3 | M |
| 6.3 | Regenerate stale baseline profile (`app/src/main/baseline-prof.txt` is from 2022, references `com/nkuppan`, Hilt, Fragments); wire `androidx.baselineprofile` plugin to existing macrobenchmark generator | M |
| 6.4 | Remove `-keep class com.naveenapps.expensemanager.** { *; }` and leftover Gson/Pusher rules in `app/proguard-rules.pro` | S |
| 6.5 | `collectAsState()` → `collectAsStateWithLifecycle()` (51 call sites) | S |
| 6.6 | Room indices on `created_on`, `from_account_id`, `category_id`; move aggregates to SQL `SUM … GROUP BY` | S / M |
| 6.7 | Replace shared `SimpleDateFormat`/`NumberFormat` singletons with `java.time` formatters per locale (`DateUtils.kt`); stop parse failures defaulting to "today" | S |
| 6.8 | Layering: drop unused `:core:data` dep from `core/domain`, `:core:settings` from `core/model`; reduce feature→feature deps by moving shared UI to `core:ui` | S / M |
| 6.9 | Remove dead `BootAlarmReceiver` (disabled, WorkManager covers reboots) | S |
| 6.10 | Dependencies: replace MPAndroidChart (unmaintained, JitPack) with Vico; drop deprecated accompanist-permissions; coil3; remove Sonatype snapshots repo; add detekt; drop 2023 lint baseline | M |
| 6.11 | Split large files: `DashboardScreen.kt` (734), `TransactionCreateScreen.kt` (718), `TransactionCreateViewModel.kt` (604) | M |

## Phase 7 — Feature roadmap (missing vs. competitors)

1. Restore from backup during onboarding + scheduled auto-backup to a user-picked folder
2. Recurring: edit/pause, end date, custom intervals, "bill due" reminders; make existing transactions recurring
3. Transaction search & tags
4. Per-account currency with exchange rates
5. Split transactions
6. Savings goals
7. CSV import (pairs with existing CSV export)
8. Budget roll-over

---

## Suggested sequencing

| Sprint | Scope |
|---|---|
| 1 ✅ | 1.1, 1.2, 1.3, 2.1, 3.1, 3.2, 6.1 + tests for each — done 12 Oct 2026, uncommitted; needs local build + test run |
| 2 | 1.4, 1.5, 1.6, 2.3, 2.4, 3.4, 4.1–4.3, Phase 5 |
| 3 | 1.7, 2.2, 3.3, 3.5, 6.3–6.7 |
| 4+ | 1.8 (money migration), 6.8–6.11, Phase 7 roadmap |
