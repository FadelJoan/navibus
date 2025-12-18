// utils/MapUtils.kt
package com.rafid.navibus.utils

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

object MapUtils {

    fun drawRoute(
        map: GoogleMap,
        points: List<LatLng>,
        color: Int = 0xE91E63,
        width: Float = 16f
    ) {
        map.addPolyline(
            PolylineOptions()
                .addAll(points)
                .color(color)
                .width(width)
                .geodesic(true)
        )
    }

    fun addHalteMarkers(map: GoogleMap, halteList: List<com.rafid.navibus.data.model.Halte>) {
        halteList.forEach { halte ->
            map.addMarker(
                MarkerOptions()
                    .position(halte.latLng)
                    .title(halte.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))  // fallback biru standar
                // .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_navigation))  // comment dulu yang ini
            )?.tag = halte.id
        }
    }
}