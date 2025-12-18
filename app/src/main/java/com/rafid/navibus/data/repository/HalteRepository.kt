package com.rafid.navibus.data.repository

import com.google.android.gms.maps.model.LatLng
import com.rafid.navibus.data.model.Halte

object HalteRepository {
    fun getAllHalte(): List<Halte> {
        return listOf(
            // --- KORIDOR 1 (Prambanan - Bandara - Malioboro - JEC) ---
            Halte("h1_01", "Terminal Prambanan", LatLng(-7.7555640482787, 110.48979227796498), "1A"),
            Halte("h1_02", "Halte Kalasan", LatLng(-7.769858254713181, 110.46897494595864), "1A"),
            Halte("h1_03", "Halte Maguwoharjo (Ringroad)", LatLng(-7.760723280537349, 110.4090074128295), "1A"),
            Halte("h1_04", "Halte Bandara Adisucipto", LatLng(-7.784533826750278, 110.43570770440982), "1A • 1B • 3A • 3B"),
            Halte("h1_05", "Halte Janti (Selatan)", LatLng(-7.78577533891867, 110.41047599353352), "1A • 1B"),
            Halte("h1_06", "Halte Janti (Utara)", LatLng(-7.783200544212492, 110.41169820616405), "1A • 1B"),
            Halte("h1_07", "Halte Mall Galeria", LatLng(-7.782329069250647, 110.37927862795962), "1A • 2A • 3A"),
            Halte("h1_08", "Halte Malioboro 1 (Malioboro Mall)", LatLng(-7.790844118951577, 110.36615103225522), "1A • 2A • 3A • 8"),
            Halte("h1_09", "Halte Malioboro 2 (Kepatihan)", LatLng(-7.795267093821031, 110.36557198596722), "1A • 2A • 3A • 8"),
            Halte("h1_10", "Halte Titik Nol (Senopati)", LatLng(-7.8015400760025795, 110.36704649162976), "1A • 2A • 3A • 8"), // previously Halte Malioboro 3
            Halte("h1_11", "Halte Taman Pintar", LatLng(-7.801388974099236, 110.36715956653481), "1A • 4A"),
            Halte("h1_12", "Terminal Condongcatur", LatLng(-7.756714265008597, 110.39588744812865), "1B • 2A • 2B • 3A • 3B • 5A"),
            Halte("h1_13", "Halte UIN Sunan Kalijaga", LatLng(-7.785888208590202, 110.39475004987467), "1B • 4A • 4B"),
            Halte("h1_14", "Halte JEC (Jogja Expo Center)", LatLng(-7.798535565546978, 110.40290034551577), "1B • 3A • 3B"),

            // --- KORIDOR 2 (Jombor - Malioboro - Basen - UGM) ---
            Halte("h2_01", "Terminal Jombor", LatLng(-7.747399158891106, 110.36120808657327), "2A • 2B • 5A • 5B • 8 • 9"),
            Halte("h2_02", "Halte Monjali", LatLng(-7.750526047201322, 110.36754779063686), "2A • 2B"),
            Halte("h2_03", "Halte Karangjati", LatLng(-7.764420133022753, 110.36907949268829), "2A • 2B"),
            Halte("h2_04", "Halte Sudirman 2 (AM Sangaji)", LatLng(-7.783068107787821, 110.36953517893434), "2A • 2B"),
            Halte("h2_05", "Halte Mangkubumi 1", LatLng(-7.784762712770573, 110.36689953474568), "2A • 2B"),
            Halte("h2_06", "Halte Basen", LatLng(-7.8145, 110.3952), "2A • 2B"),
            Halte("h2_07", "Halte Rejowinangun", LatLng(-7.8105, 110.4035), "2A • 2B"),
            Halte("h2_08", "Halte Gembira Loka", LatLng(-7.802296742063348, 110.39879759501096), "2A • 2B"),

            // --- KORIDOR 3 (Giwangan - Jokteng - UGM - Condongcatur) ---
            Halte("h3_01", "Terminal Giwangan", LatLng(-7.834097585191045, 110.39169891280697), "3A • 3B • 4A • 4B • 7 • 9 • 10 • 11"),
            Halte("h3_02", "Halte Tegalgendu 1", LatLng(-7.826021294790589, 110.39178554375881), "3A • 3B"),
            Halte("h3_03", "Halte Jokteng Wetan", LatLng(-7.814826537588837, 110.36998109581302), "3A • 3B"),
            Halte("h3_04", "Halte Jokteng Kulon", LatLng(-7.813175970430675, 110.35734640852822), "3A • 3B"),
            Halte("h3_05", "Halte Plengkung Gading", LatLng(-7.814102860952396, 110.36388855160112), "3A • 3B"),
            Halte("h3_06", "Halte Ngabean", LatLng(-7.803686241118326, 110.35629873840429), "3A • 3B • 6A • 6B • 8 • 9 • 11"),
            Halte("h3_07", "Halte Samsat", LatLng(-7.787172181151456, 110.3598103822651), "3A • 3B • 8"),
            Halte("h3_08", "Halte Pingit", LatLng(-7.7829105390849405, 110.36253808270966), "3A • 3B • 8"),
            Halte("h3_09", "Halte Fakultas Kedokteran Hewan UGM", LatLng(-7.767036341015763, 110.38484670399468), "3A • 3B • 4A • 4B"), // Bundaran UGM
            Halte("h3_10", "Halte Colombo (UNY)", LatLng(-7.776165186741804, 110.37860689820423), "3A • 3B • 4A • 4B"),
            Halte("h3_11", "Halte Gejayan", LatLng(-7.7689, 110.3893), "3A • 3B"),
            Halte("h3_12", "Halte Ringroad Utara (UPN)", LatLng(-7.7599, 110.4072), "3A • 3B • 5A • 5B"),
            Halte("h3_13", "Halte Kentungan", LatLng(-7.7533, 110.3854), "3A • 3B"),

            // --- KORIDOR 4 (Giwangan - UIN - UGM - Lempuyangan) ---
            Halte("h4_01", "Halte UAD Kampus 4", LatLng(-7.83485283158781, 110.3832299589587), "4A • 4B"),
            Halte("h4_02", "Halte XT Square", LatLng(-7.8159, 110.3850), "4A • 4B"),
            Halte("h4_03", "Halte Kusumanegara 1", LatLng(-7.8028, 110.3830), "4A • 4B • 1A • 1B"),
            Halte("h4_04", "Halte Stasiun Lempuyangan", LatLng(-7.7907, 110.3756), "4A • 4B • 10"),
            Halte("h4_05", "Halte Kridosono", LatLng(-7.7884, 110.3734), "4A • 4B • 2A • 10"),
            Halte("h4_06", "Halte APMD", LatLng(-7.7942, 110.3922), "4A • 4B"),

            // --- KORIDOR 5 (Jombor - Babarsari - Janti) ---
            Halte("h5_01", "Halte Babarsari", LatLng(-7.7781, 110.4150), "5A • 5B"),
            Halte("h5_02", "Halte Seturan", LatLng(-7.7699, 110.4103), "5A • 5B"),
            Halte("h5_03", "Halte UPN Veteran", LatLng(-7.7600, 110.4080), "5A • 5B"),

            // --- KORIDOR 6 (Ngabean - Bugisan - Patangpuluhan) ---
            Halte("h6_01", "Halte Bugisan", LatLng(-7.8153, 110.3501), "6A • 6B"),
            Halte("h6_02", "Halte Patangpuluhan", LatLng(-7.8048, 110.3477), "6A • 6B"),
            Halte("h6_03", "Halte Tamansari", LatLng(-7.8105, 110.3592), "6A • 6B"),

            // --- KORIDOR 8 (Jombor - Demak Ijo - Malioboro - Janti) ---
            Halte("h8_01", "Halte Demak Ijo", LatLng(-7.7805, 110.3402), "8"),
            Halte("h8_02", "Halte Mirota Godean", LatLng(-7.7831, 110.3548), "8"),

            // --- KORIDOR 9 (Giwangan - Jombor) ---
            Halte("h9_01", "Halte Gamping (Ambarketawang)", LatLng(-7.7962, 110.3297), "9 • 10"),
            Halte("h9_02", "Halte Pelem Gurih", LatLng(-7.8000, 110.3350), "9 • 10"),

            // --- KORIDOR 10 (Gamping - Kusumanegara) ---
            Halte("h10_01", "Halte RS PKU Gamping", LatLng(-7.7965, 110.3320), "10"),
            Halte("h10_02", "Halte Wirobrajan", LatLng(-7.8010, 110.3520), "10"),

            // --- KORIDOR 11 (Giwangan - Condongcatur) ---
            Halte("h11_01", "Halte Pasty", LatLng(-7.8209, 110.3581), "11"),
            Halte("h11_02", "Halte Dongkelan", LatLng(-7.8300, 110.3550), "11"),

            // --- AREA UTARA (Jalan Kaliurang) ---
            Halte("hu_01", "Halte Jakal Km 5 (UGM)", LatLng(-7.7597, 110.3779), "3B • 5A"),
            Halte("hu_02", "Halte MM UGM", LatLng(-7.7654, 110.3752), "3B • 5A"),

            // --- AREA SELATAN (Jalan Parangtritis) ---
            Halte("hs_01", "Halte Piramid (Teman Bus)", LatLng(-7.8447, 110.3630), "Teman Bus"),
            Halte("hs_02", "Halte ISI Yogyakarta", LatLng(-7.8548, 110.3632), "Teman Bus")
        )
    }

    fun getRoute3APolyline(): List<LatLng> {
        return listOf(
            LatLng(-7.7501, 110.4020),
            LatLng(-7.7533, 110.3854),
            LatLng(-7.7719, 110.3745),
            LatLng(-7.7845, 110.3598),
            LatLng(-7.8038, 110.3583),
            LatLng(-7.8341, 110.3897)
        )
    }
}