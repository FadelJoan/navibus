package com.rafid.navibus

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Mengembalikan referensi ke LoginActivity
import com.rafid.navibus.LoginActivity

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        databaseHelper = DatabaseHelper.getInstance(this)
        rvHistory = findViewById(R.id.rvHistory)

        rvHistory.layoutManager = LinearLayoutManager(this)

        // Menggunakan konstanta dari LoginActivity seperti semula
        val sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString(LoginActivity.KEY_EMAIL, null)

        if (userEmail != null) {
            val trips = databaseHelper.getAllTripsForUser(userEmail)
            rvHistory.adapter = HistoryAdapter(trips)
        } else {
            Toast.makeText(this, "Gagal memuat riwayat, pengguna tidak ditemukan.", Toast.LENGTH_LONG).show()
        }
    }
}
