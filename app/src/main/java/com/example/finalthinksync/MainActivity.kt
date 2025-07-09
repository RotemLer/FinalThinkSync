package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🛡️ אם המשתמש כבר מחובר – נעביר אותו ישירות למסך הסיכומים
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            startActivity(Intent(this, FragmentSummaryListActivity::class.java))
            finish()
            return
        }

        // ממשיכים רק אם המשתמש לא מחובר
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.main_BTN_Login)
        val registerButton = findViewById<Button>(R.id.main_BTN_Register)

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }
}
