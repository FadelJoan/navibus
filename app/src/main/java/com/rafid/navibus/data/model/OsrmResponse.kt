package com.rafid.navibus.data.model

import com.google.gson.annotations.SerializedName

data class OsrmResponse(
    @SerializedName("routes") val routes: List<OsrmRoute>
)

data class OsrmRoute(
    @SerializedName("geometry") val geometry: String
)
