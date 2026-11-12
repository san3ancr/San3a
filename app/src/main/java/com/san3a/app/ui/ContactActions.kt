package com.san3a.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

object ContactActions {

    fun call(context: Context, phone: String) {
        val clean = phone.filter { it.isDigit() || it == '+' }
        if (clean.isBlank()) return
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean")))
        }
    }

    fun whatsapp(context: Context, phone: String) {
        val digits = phone.filter { it.isDigit() }
        if (digits.isBlank()) return
        val url = "https://wa.me/$digits"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
