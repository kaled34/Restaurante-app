package com.example.viagourmet.Presentacion.screens.admin

import java.time.LocalDateTime
import java.util.Calendar

// ── Helpers de fecha compatibles con minSdk 24 ───────────────────────────────
// Usados en PedidoAdminCard.kt y PedidoDetallesSheet.kt

fun LocalDateTime.toHoraString(): String {
    val cal = localDateTimeToCalendar(this)
    return "%02d:%02d".format(
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE)
    )
}

fun LocalDateTime.toFechaHoraString(): String {
    val cal = localDateTimeToCalendar(this)
    return "%02d/%02d/%04d %02d:%02d".format(
        cal.get(Calendar.DAY_OF_MONTH),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.YEAR),
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE)
    )
}

// Convierte LocalDateTime a Calendar
fun localDateTimeToCalendar(ldt: LocalDateTime): Calendar {
    val cal = Calendar.getInstance()

    val str = ldt.toString()
    val parts = str.split("T")
    val dateParts = parts[0].split("-")
    val timeParts = parts[1].split(":")
    cal.set(
        dateParts[0].toInt(),           // year
        dateParts[1].toInt() - 1,       // month (0-based)
        dateParts[2].toInt(),           // day
        timeParts[0].toInt(),           // hour
        timeParts[1].toInt(),           // minute
        timeParts[2].substringBefore(".").toIntOrNull() ?: 0  // second
    )
    return cal
}