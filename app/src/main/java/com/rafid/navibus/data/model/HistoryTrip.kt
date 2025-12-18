package com.rafid.navibus.data.model

data class HistoryTrip(
    val id: Int,
    val startHalte: String,
    val destinationHalte: String,
    val busCode: String, 
    val timestamp: String
)