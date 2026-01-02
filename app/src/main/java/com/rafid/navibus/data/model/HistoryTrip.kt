package com.rafid.navibus.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class HistoryTrip(
    val id: Int = 0, // ID dikembalikan dengan nilai default
    val startHalte: String = "",
    val destinationHalte: String = "",
    val busCode: String = "", 
    val timestamp: String = ""
)