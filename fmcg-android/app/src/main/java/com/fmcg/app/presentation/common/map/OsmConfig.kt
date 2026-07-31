package com.fmcg.app.presentation.common.map

import android.content.Context
import org.osmdroid.config.Configuration

/** OSMDroid requires a non-default User-Agent or the OSM tile servers reject
 *  requests. Call once from Application.onCreate before any MapView is shown. */
object OsmConfig {
    fun init(context: Context) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = context.cacheDir
            osmdroidTileCache = context.cacheDir.resolve("osm_tiles")
        }
    }
}
