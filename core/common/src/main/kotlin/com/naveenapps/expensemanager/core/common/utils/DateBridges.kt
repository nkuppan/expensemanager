package com.naveenapps.expensemanager.core.common.utils

import java.util.Date
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/*
 * Temporary bridges between java.util.Date (today's models) and the multiplatform date types the
 * code is moving to (KMP groundwork C2). They let one module switch at a time: convert at the
 * boundary with these, never by hand. Delete this file once no java.util.Date is left (C3–C5).
 *
 * These convert real instants only. The database's stored values are "floating" local times, not
 * instants (see DateConverter / DateConverterTest); don't use these to read or write raw column
 * values.
 */

/** The same instant, millisecond for millisecond. */
fun Date.toKotlinInstant(): Instant = Instant.fromEpochMilliseconds(time)

/** The same instant. Sub-millisecond precision is dropped, since Date only holds milliseconds. */
fun Instant.toJavaDate(): Date = Date(toEpochMilliseconds())

/** Wall-clock date and time of this instant in [timeZone] (the device zone by default). */
fun Date.toKotlinLocalDateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime = toKotlinInstant().toLocalDateTime(timeZone)

/** Calendar date of this instant in [timeZone] (the device zone by default). */
fun Date.toKotlinLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate = toKotlinLocalDateTime(timeZone).date

/** That wall-clock time in [timeZone]; a time inside a daylight-saving gap moves later by the gap. */
fun LocalDateTime.toJavaDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): Date = toInstant(timeZone).toJavaDate()

/**
 * Start of that day in [timeZone]. Usually midnight, but the first existing moment on days where
 * midnight is skipped by a daylight-saving change (as `toExactStartOfTheDay` already does).
 */
fun LocalDate.toJavaDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): Date = atStartOfDayIn(timeZone).toJavaDate()
