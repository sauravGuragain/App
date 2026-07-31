package com.fmcg.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

/** Epoch millis -> ISO-8601 UTC string the backend's Pydantic datetime parses. */
fun Long.toIso8601(): String = synchronized(iso) { iso.format(Date(this)) }

private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

/** Today's date (UTC) as yyyy-MM-dd — matches how the backend stores recorded_at. */
fun todayUtc(): String = synchronized(dateFmt) { dateFmt.format(Date()) }
