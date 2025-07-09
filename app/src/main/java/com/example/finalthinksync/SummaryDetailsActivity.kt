package com.example.finalthinksync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class SummaryDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary_details)

        val titleText = findViewById<TextView>(R.id.item_summary_TV_Title)
        val courseText = findViewById<TextView>(R.id.item_summary_TV_Course)
        val lecturerText = findViewById<TextView>(R.id.item_summary_TV_Lecturer)
        val openPdfButton = findViewById<Button>(R.id.item_summary_BTN_Open)
        val pdfContainer = findViewById<FrameLayout>(R.id.pdfContainer)

        val title = intent.getStringExtra("title") ?: "Unknown Title"
        val course = intent.getStringExtra("course") ?: "Unknown Course"
        val lecturer = intent.getStringExtra("lecturer") ?: "Unknown Lecturer"
        val pdfUrl = intent.getStringExtra("pdfUrl")

        titleText.text = title
        courseText.text = course
        lecturerText.text = lecturer

        openPdfButton.setOnClickListener {
            if (!pdfUrl.isNullOrEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(Uri.parse(pdfUrl), "application/pdf")
                        flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("PDF_OPEN_ERROR", "Error opening PDF: ${e.message}")
                    Toast.makeText(this, "No app found to open PDF", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "PDF not available", Toast.LENGTH_SHORT).show()
            }
        }

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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
