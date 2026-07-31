package com.fmcg.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Launch turn-by-turn navigation in whatever maps app the user has installed
 * via a standard geo: intent (no paid API). Falls back to OpenStreetMap in a
 * browser if no navigation app is present.
 */
fun openExternalNavigation(context: Context, lat: Double, lng: Double, label: String) {
    val geo = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
    val intent = Intent(Intent.ACTION_VIEW, geo).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val web = Uri.parse("https://www.openstreetmap.org/?mlat=$lat&mlon=$lng#map=18/$lat/$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, web).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
