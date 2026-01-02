package com.rafid.navibus

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rafid.navibus.data.model.HistoryTrip

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        rvHistory = findViewById(R.id.rvHistory)

        rvHistory.layoutManager = LinearLayoutManager(this)

        loadHistory()
    }

    private fun loadHistory() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Gagal memuat riwayat, pengguna tidak ditemukan.", Toast.LENGTH_LONG).show()
            return
        }

        val historyRef = database.reference.child("trip_history").child(user.uid)

        historyRef.orderByChild("timestamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val trips = mutableListOf<HistoryTrip>()
                for (tripSnapshot in snapshot.children) {
                    val trip = tripSnapshot.getValue(HistoryTrip::class.java)
                    trip?.let { trips.add(it) }
                }
                trips.reverse()
                rvHistory.adapter = HistoryAdapter(trips)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HistoryActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
