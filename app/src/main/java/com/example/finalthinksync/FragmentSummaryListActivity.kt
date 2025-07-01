package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import androidx.recyclerview.widget.DividerItemDecoration


class FragmentSummaryListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_summary_list)

        // הצגת אימייל המשתמש
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "User"

        val welcomeTextView = findViewById<TextView>(R.id.summary_list_TV_Welcome)
        welcomeTextView.text = "Hello, $email!"

        // הגדרת RecyclerView
        val summaries = listOf(
            Summary("מבוא למדעי המחשב", "מדעי המחשב", "ד\"ר לוי", 2023),
            Summary("מבוא לכלכלה", "כלכלה", "פרופ' כהן", 2024)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.summary_list_RV_Summaries)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        )
        recyclerView.adapter = SummaryAdapter(summaries)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, FragmentSummaryListActivity::class.java))
                    true
                }
                R.id.nav_upload -> {
                    startActivity(Intent(this, FragmentSummaryUploadActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java)) // אופציונלי
                    true
                }
                else -> false
            }
        }

    }
}
