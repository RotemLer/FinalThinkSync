package com.example.finalthinksync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SummaryDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary_details)

        val titleText = findViewById<TextView>(R.id.item_summary_TV_Title)
        val courseText = findViewById<TextView>(R.id.item_summary_TV_Course)
        val lecturerText = findViewById<TextView>(R.id.item_summary_TV_Lecturer)
        val openPdfButton = findViewById<Button>(R.id.item_summary_BTN_Open)
        val pdfContainer = findViewById<FrameLayout>(R.id.pdfContainer)
        val btnReview = findViewById<Button>(R.id.btn_write_review)


        val summaryId = intent.getStringExtra("summaryId") ?: ""
        val uploaderUid = intent.getStringExtra("uploaderUid") ?: ""
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val title = intent.getStringExtra("title") ?: "Unknown Title"
        val course = intent.getStringExtra("course") ?: "Unknown Course"
        val lecturer = intent.getStringExtra("lecturer") ?: "Unknown Lecturer"
        val pdfUrl = intent.getStringExtra("pdfUrl")

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val savedRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("saved_summaries")
            .document(summaryId)


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

        btnReview.setOnClickListener {
            showReviewDialog(summaryId)
        }


        // ✅ Load all reviews dynamically
        val reviewContainer = findViewById<LinearLayout>(R.id.reviewContainer)

        FirebaseFirestore.getInstance()
            .collection("summaries")
            .document(summaryId)
            .collection("reviews")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val review = doc.toObject(Review::class.java)
                    val reviewId = doc.id

                    val reviewLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(0, 0, 0, 32)
                    }

                    val reviewText = TextView(this).apply {
                        text = "By ${review.username} (${review.rating}⭐):\n${review.text}"
                        textSize = 16f
                    }
                    reviewLayout.addView(reviewText)

                    if (!review.reply.isNullOrEmpty()) {
                        val replyText = TextView(this).apply {
                            text = "Uploader's reply:\n${review.reply}"
                            textSize = 14f
                        }
                        reviewLayout.addView(replyText)
                    }

                    if (currentUid == uploaderUid && review.reply.isNullOrEmpty()) {
                        val replyInput = EditText(this).apply {
                            hint = "Write a reply..."
                        }

                        val sendReplyBtn = Button(this).apply {
                            text = "Send Reply"
                            setOnClickListener {
                                val replyText = replyInput.text.toString()
                                if (replyText.isNotBlank()) {
                                    FirebaseFirestore.getInstance()
                                        .collection("summaries")
                                        .document(summaryId)
                                        .collection("reviews")
                                        .document(reviewId)
                                        .update("reply", replyText)
                                        .addOnSuccessListener {
                                            Toast.makeText(this@SummaryDetailsActivity, "Reply sent", Toast.LENGTH_SHORT).show()
                                            recreate()
                                        }
                                }
                            }
                        }

                        reviewLayout.addView(replyInput)
                        reviewLayout.addView(sendReplyBtn)
                    }

                    reviewContainer.addView(reviewLayout)
                }
            }
    }

    private fun showReviewDialog(summaryId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_write_review, null)
        val editText = dialogView.findViewById<EditText>(R.id.reviewEditText)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

        AlertDialog.Builder(this)
            .setTitle("Write review")
            .setView(dialogView)
            .setPositiveButton("Send") { _, _ ->
                val reviewText = editText.text.toString()
                val rating = ratingBar.rating.toInt()

                val user = FirebaseAuth.getInstance().currentUser
                val review = Review(
                    userId = user?.uid ?: "",
                    username = user?.email ?: "anonymous user",
                    rating = rating,
                    text = reviewText
                )

                FirebaseFirestore.getInstance()
                    .collection("summaries")
                    .document(summaryId)
                    .collection("reviews")
                    .add(review)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Review saved", Toast.LENGTH_SHORT).show()
                        recreate()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error sending review", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
