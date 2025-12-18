package com.rafid.navibus

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREFS_NAME = "NavibusPrefs"
        const val KEY_EMAIL = "user_email"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        if (isLoggedIn()) {
            goToMainActivity()
            return
        }

        setContentView(R.layout.activity_login)

        databaseHelper = DatabaseHelper.getInstance(this)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (databaseHelper.checkUser(email, password)) {
                saveLoginState(email)
                goToMainActivity()
            } else {
                Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getString(KEY_EMAIL, null) != null
    }

    private fun saveLoginState(email: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_EMAIL, email)
        editor.apply()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
