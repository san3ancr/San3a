package com.san3a.app.data

fun normalizePhone(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ""
    val digits = trimmed.filter { it.isDigit() }
    return if (trimmed.startsWith("+")) "+$digits" else digits
}
