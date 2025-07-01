package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ItemSummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_summary)

        val titleText = findViewById<TextView>(R.id.item_summary_TV_Title)
        val courseText = findViewById<TextView>(R.id.item_summary_TV_Course)
        val lecturerText = findViewById<TextView>(R.id.item_summary_TV_Lecturer)

        // אם תרצה לקבל ערכים מ־Intent
        val title = intent.getStringExtra("title") ?: "Unknown Title"
        val course = intent.getStringExtra("course") ?: "Unknown Course"
        val lecturer = intent.getStringExtra("lecturer") ?: "Unknown Lecturer"

        titleText.text = title
        courseText.text = course
        lecturerText.text = lecturer

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
