package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.register_TEXT_Email)
        val passwordEditText = findViewById<EditText>(R.id.register_TEXT_Password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.register_TEXT_ConfirmPassword)
        val registerButton = findViewById<Button>(R.id.register_BTN_Register)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                    // ✅ מעבירים ישר למסך הסיכומים
                    startActivity(Intent(this, FragmentSummaryListActivity::class.java))
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Registration failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
