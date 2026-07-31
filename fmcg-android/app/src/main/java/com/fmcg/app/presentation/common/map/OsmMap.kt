package com.fmcg.app.presentation.common.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

data class MapMarker(val lat: Double, val lng: Double, val title: String? = null)

/** OpenStreetMap view bridged into Compose. Rebuilds its overlays whenever the
 *  markers / route / tap handler change; handles MapView's onResume/onPause. */
@Composable
fun OsmMap(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(27.7172, 85.3240),   // Kathmandu fallback
    zoom: Double = 15.0,
    markers: List<MapMarker> = emptyList(),
    routePoints: List<GeoPoint> = emptyList(),
    onMapTap: ((GeoPoint) -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(zoom)
            controller.setCenter(center)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { map ->
            map.overlays.clear()

            onMapTap?.let { handler ->
                val receiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let(handler); return true
                    }
                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                }
                map.overlays.add(MapEventsOverlay(receiver))
            }

            if (routePoints.size >= 2) {
                map.overlays.add(Polyline(map).apply { setPoints(routePoints) })
            }

            markers.forEach { m ->
                map.overlays.add(
                    Marker(map).apply {
                        position = GeoPoint(m.lat, m.lng)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = m.title
                    }
                )
            }
            map.invalidate()
        },
    )
}
