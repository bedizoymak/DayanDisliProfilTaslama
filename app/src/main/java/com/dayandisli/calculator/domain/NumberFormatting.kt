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

/**
 * Normalizes the forgiving decimal notation used by shop-floor data entry. A decimal separator
 * may be a comma or a dot and whitespace around it is ignored. When two numeric groups are
 * separated only by whitespace (for example "2 5"), that space is treated as the intended
 * decimal separator so the field immediately becomes "2.5" instead of reporting an error.
 */
fun normalizeDecimalInput(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ""

    val groups = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (!trimmed.contains('.') && !trimmed.contains(',') && groups.all { it.all(Char::isDigit) }) {
        return when {
            groups.size >= 2 -> "${groups.first()}.${groups.drop(1).joinToString("")}" 
            raw.lastOrNull()?.isWhitespace() == true -> "${groups.first()}."
            else -> groups.first()
        }
    }

    // Convert comma to dot, remove whitespace, and collapse accidental repeated separators:
    // 2..6, 2,,6 and 2 . . 6 all become 2.6.
    return trimmed
        .replace(Regex("\\s+"), "")
        .replace(',', '.')
        .replace(Regex("\\.+"), ".")
}

/** Normalizes an integer field such as tooth count or quantity without inventing a decimal. */
fun normalizeIntegerInput(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ""
    return if (trimmed.all { it.isDigit() || it.isWhitespace() || it == '.' || it == ',' }) {
        trimmed.filter(Char::isDigit)
    } else {
        trimmed
    }
}

fun parseLocaleInt(raw: String): Int? {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return null
    return trimmed.toIntOrNull() ?: parseLocaleDouble(trimmed)?.toInt()
}

/**
 * Splits an unsigned decimal-degree magnitude into its D/M/S/CS components (degree, minute,
 * second, centisecond), rounding to the nearest centisecond with correct carry (59.999... seconds
 * carries into minutes, 60 minutes carries into degrees, etc.) so no component ever lands on an
 * out-of-range value like "60" or "100". Rounding happens entirely in integer centiseconds first
 * so the split itself can't reintroduce floating-point error at a carry boundary.
 */
private data class Dmscs(val degrees: Long, val minutes: Long, val seconds: Long, val centiseconds: Long) {
    override fun toString(): String =
        "$degrees° ${minutes.toString().padStart(2, '0')}′ ${seconds.toString().padStart(2, '0')}″ ${centiseconds.toString().padStart(2, '0')}"
}

private fun dmscsOf(magnitudeDegrees: Double): Dmscs {
    val totalCentiseconds = BigDecimal(kotlin.math.abs(magnitudeDegrees) * 360000.0).setScale(0, RoundingMode.HALF_UP).toLong()
    var remaining = totalCentiseconds
    val centiseconds = remaining % 100
    remaining /= 100
    val seconds = remaining % 60
    remaining /= 60
    val minutes = remaining % 60
    val degrees = remaining / 60
    return Dmscs(degrees, minutes, seconds, centiseconds)
}

/**
 * Formats a decimal-degree value as an unsigned D° M′ S″ CS magnitude (degree, minute, second,
 * centisecond), e.g. `19° 59′ 59″ 95`. Minute, second and centisecond are zero-padded to two
 * digits. Any sign on the input is ignored — use [toDmscsSigned] where a +/- sign must be shown
 * (e.g. a beta difference), and this formatter for an angle that is always a magnitude (e.g. a
 * realized helix angle).
 */
fun Double.toDmscs(): String {
    if (this.isNaN() || this.isInfinite()) return "-"
    return dmscsOf(this).toString()
}

/**
 * Formats a decimal-degree value as a D° M′ S″ CS magnitude with a mandatory leading sign, e.g.
 * `+0° 00′ 00″ 15` or `-0° 00′ 00″ 15`. Intended for signed differences (e.g. Fark (β)) where the
 * direction of the deviation matters, unlike [toDmscs].
 */
fun Double.toDmscsSigned(): String {
    if (this.isNaN() || this.isInfinite()) return "-"
    val sign = if (this < 0) "-" else "+"
    return "$sign${dmscsOf(this)}"
}
