package com.rafid.navibus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.rafid.navibus.Constants.KEY_EMAIL
import com.rafid.navibus.Constants.PREFS_NAME

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()

        val btnHistory = findViewById<Button>(R.id.btnHistory)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        val userEmail = sharedPreferences.getString(KEY_EMAIL, "user@navibus.com")
        tvUserName.text = userEmail?.split("@")?.get(0) ?: "Pengguna"

        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            // Sign out from Firebase
            auth.signOut()

            // Clear login state from SharedPreferences
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            // Navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
