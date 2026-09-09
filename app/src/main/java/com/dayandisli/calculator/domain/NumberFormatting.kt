package com.dayandisli.calculator.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Rounds like JavaScript's Number.prototype.toFixed but returns a Double instead of a string,
 * for use as an intermediate value (mirrors `parseFloat(x.toFixed(n))` call sites in the
 * source, e.g. gearHelical.ts's helicalGearRatio). Uses HALF_UP, which matches toFixed's
 * round-half-away-from-zero behavior for the decimal magnitudes this app deals with; note that
 * JS's actual toFixed is defined over the binary floating point value and can disagree with
 * decimal HALF_UP rounding in rare edge cases (e.g. values that are not exactly representable).
 */
fun roundToFixed(value: Double, digits: Int): Double {
    if (value.isNaN() || value.isInfinite()) return value
    return BigDecimal(value).setScale(digits, RoundingMode.HALF_UP).toDouble()
}

/** Formats like JavaScript's Number.prototype.toFixed(digits), for display parity. */
fun Double.toFixedJs(digits: Int): String {
    if (this.isNaN()) return "NaN"
    val bd = BigDecimal(this).setScale(digits, RoundingMode.HALF_UP)
    return bd.toPlainString()
}

/**
 * Parses a numeric string the way the web inputs expect Turkish users to type them: a comma is
 * treated as the decimal separator (not a thousands separator), matching how Android's tr-TR
 * numeric keyboard produces ',' for the decimal point. Falls back to '.' as the decimal
 * separator so pasted/typed English-style values keep working too.
 */
fun parseLocaleDouble(raw: String): Double? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    val normalized = if (trimmed.contains(',') && !trimmed.contains('.')) {
        trimmed.replace(',', '.')
    } else {
        trimmed
    }
    return normalized.toDoubleOrNull()
}

fun parseLocaleInt(raw: String): Int? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    return trimmed.toIntOrNull() ?: parseLocaleDouble(trimmed)?.toInt()
}

/**
 * Formats a signed decimal-degree value as D° M′ S″ CS (degree, minute, second, centisecond),
 * e.g. `19° 59′ 59″ 95` or, for a small negative difference, `-0° 00′ 00″ 15`. Minute, second and
 * centisecond are zero-padded to two digits. Rounds to the nearest centisecond with correct carry
 * (59.999... seconds carries into minutes, 60 minutes carries into degrees).
 */
fun Double.toDmscs(): String {
    if (this.isNaN() || this.isInfinite()) return "-"
    val negative = this < 0
    val absDeg = kotlin.math.abs(this)

    // Round to the nearest centisecond of arc first, entirely in integer centiseconds, so the
    // subsequent degree/minute/second/centisecond split can't reintroduce floating-point error
    // at the boundary (e.g. 59.999999 seconds silently failing to carry into minutes).
    val totalCentiseconds = BigDecimal(absDeg * 360000.0).setScale(0, RoundingMode.HALF_UP).toLong()

    var remaining = totalCentiseconds
    val centiseconds = remaining % 100
    remaining /= 100
    val seconds = remaining % 60
    remaining /= 60
    val minutes = remaining % 60
    val degrees = remaining / 60

    val sign = if (negative) "-" else ""
    return "$sign$degrees° ${minutes.toString().padStart(2, '0')}′ ${seconds.toString().padStart(2, '0')}″ ${centiseconds.toString().padStart(2, '0')}"
}
