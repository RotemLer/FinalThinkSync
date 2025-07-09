package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.login_TEXT_Email)
        val passwordEditText = findViewById<EditText>(R.id.login_TEXT_Password)
        val loginButton = findViewById<Button>(R.id.login_BTN_Login)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    Log.d("DEBUG", "✅ התחברות הצליחה. UID: ${FirebaseAuth.getInstance().currentUser?.uid}")

                    startActivity(Intent(this, FragmentSummaryListActivity::class.java)) // החלף אם צריך
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Login failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
