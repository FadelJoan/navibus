package com.rafid.navibus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPreferences = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)

        val btnHistory = findViewById<Button>(R.id.btnHistory)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvUserName = findViewById<TextView>(R.id.tvUserName)

        // Display logged-in user's email
        tvUserName.text = sharedPreferences.getString(LoginActivity.KEY_EMAIL, "user@navibus.com")

        btnHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            // Clear login state
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
