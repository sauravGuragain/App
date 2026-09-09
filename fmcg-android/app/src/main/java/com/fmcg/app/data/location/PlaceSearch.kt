package com.fmcg.app.data.location

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class PlaceResult(val label: String, val lat: Double, val lng: Double)

/**
 * Address lookup via OpenStreetMap's Nominatim — free, no API key, which keeps
 * the pilot on zero-cost infrastructure.
 *
 * Nominatim's usage policy requires an identifying User-Agent and caps callers
 * at roughly one request per second, so the picker debounces typing rather
 * than searching on every keystroke.
 */
@Singleton
class PlaceSearch @Inject constructor(
    private val io: CoroutineDispatcher,
) {
    private val client = OkHttpClient.Builder()
        .callTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun search(query: String, limit: Int = 6): List<PlaceResult> = withContext(io) {
        if (query.isBlank()) return@withContext emptyList()
        val url = "https://nominatim.openstreetmap.org/search" +
            "?format=json&limit=$limit&q=" + java.net.URLEncoder.encode(query, "UTF-8")
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "OMServices-Android/1.0 (field sales app)")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                val out = mutableListOf<PlaceResult>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val lat = o.optString("lat").toDoubleOrNull()
                    val lng = o.optString("lon").toDoubleOrNull()
                    if (lat != null && lng != null) {
                        out.add(PlaceResult(o.optString("display_name"), lat, lng))
                    }
                }
                out
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
