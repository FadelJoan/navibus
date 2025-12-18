// data/model/Halte.kt
package com.rafid.navibus.data.model

import com.google.android.gms.maps.model.LatLng

data class Halte(
    val id: String,
    val name: String,
    val latLng: LatLng,
    val koridor: String
)