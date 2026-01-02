package com.rafid.navibus.data.repository

import com.google.android.gms.maps.model.LatLng
import com.rafid.navibus.data.model.Halte

object HalteRepository {

    fun getAllHalte(): List<Halte> {
        // Expanded list with 15 stops for better testing and realism
        return listOf(
            Halte("h1_08", "Halte Malioboro 1 (Malioboro Mall)", LatLng(-7.790844, 110.366151), "1A • 2A • 3A"),
            Halte("h1_07", "Halte Mall Galeria", LatLng(-7.782329, 110.379278), "1A • 2A"),
            Halte("h1_14", "Halte JEC (Jogja Expo Center)", LatLng(-7.798535, 110.402900), "1B • 3A"),
            Halte("h2_01", "Terminal Jombor", LatLng(-7.747399, 110.361208), "2A • 5A • 8"),
            Halte("h4_01", "Halte UAD Kampus 4", LatLng(-7.834852, 110.383229), "4A"),
            Halte("h1_12", "Terminal Condongcatur", LatLng(-7.756714, 110.395887), "1B • 2B • 3B"),
            Halte("h4_02", "Halte Kusumanegara", LatLng(-7.8028, 110.3830), "4A"),
            Halte("h2_08", "Halte Gembira Loka", LatLng(-7.8022, 110.3987), "2A • 4A"),

            // -- Added to reach 15 stops --
            Halte("h1_11", "Halte Taman Pintar", LatLng(-7.801388, 110.367159), "1A • 4A"),
            Halte("h1_04", "Halte Bandara Adisucipto", LatLng(-7.784533, 110.435707), "1A • 1B • 3A"),
            Halte("h2_05", "Halte Mangkubumi 1", LatLng(-7.784762, 110.366899), "2A"),
            Halte("h2_02", "Halte Monjali", LatLng(-7.750526, 110.367547), "2A"),
            Halte("h3_01", "Terminal Giwangan", LatLng(-7.834097, 110.391698), "3A • 3B • 4A"),
            Halte("h3_03", "Halte Jokteng Wetan", LatLng(-7.814826, 110.369981), "3A • 3B"),
            Halte("h1_13", "Halte UIN Sunan Kalijaga", LatLng(-7.785888, 110.394750), "1B • 4A")
        )
    }
}