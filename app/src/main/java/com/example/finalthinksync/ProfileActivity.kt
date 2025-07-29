package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var uploadedRecyclerView: RecyclerView
    private lateinit var savedRecyclerView: RecyclerView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email ?: "Unknown user"
        val userUid = currentUser?.uid

        val emailText = findViewById<TextView>(R.id.profile_TEXT_Email)
        val logoutButton = findViewById<Button>(R.id.profile_BTN_Logout)
        uploadedRecyclerView = findViewById(R.id.profile_RV_UploadedSummaries)
        savedRecyclerView = findViewById(R.id.profile_RV_SavedSummaries)

        emailText.text = "🔐 Logged in as: $userEmail"

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        uploadedRecyclerView.layoutManager = LinearLayoutManager(this)
        uploadedRecyclerView.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        )

        savedRecyclerView.layoutManager = LinearLayoutManager(this)
        savedRecyclerView.addItemDecoration(
            DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        )

        if (userUid != null) {
            loadUploadedSummaries(userUid)
            loadSavedSummaries(userUid)

            val bellIcon = findViewById<ImageView>(R.id.profile_IMG_Bell)
            bellIcon.setOnClickListener {
                startActivity(Intent(this, NotificationsActivity::class.java))
            }

            checkUnreadNotifications()
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_profile
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
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkUnreadNotifications()
    }

    private fun checkUnreadNotifications() {
        val badgeText = findViewById<TextView>(R.id.profile_TEXT_Badge)
        val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userUid)
            .collection("notifications")
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { result ->
                val count = result.size()
                if (count > 0) {
                    badgeText.text = count.toString()
                    badgeText.visibility = View.VISIBLE
                } else {
                    badgeText.visibility = View.GONE
                }
            }
    }

    private fun loadUploadedSummaries(uid: String) {
        db.collection("summaries")
            .whereEqualTo("uploaderUid", uid)
            .get()
            .addOnSuccessListener { result ->
                val summaries = mutableListOf<Summary>()
                for (doc in result) {
                    val timestampObj = doc.getTimestamp("timestamp")
                    val timestampLong = timestampObj?.seconds ?: 0L

                    val summary = Summary(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        course = doc.getString("course") ?: "",
                        lecturer = doc.getString("lecturer") ?: "",
                        year = (doc.getLong("year") ?: 0L).toInt(),
                        pdfUrl = doc.getString("pdfUrl") ?: "",
                        timestamp = timestampLong,
                        uploaderUid = doc.getString("uploaderUid") ?: ""
                    )

                    db.collection("summaries")
                        .document(summary.id)
                        .collection("reviews")
                        .get()
                        .addOnSuccessListener { reviewDocs ->
                            val reviewList = reviewDocs.mapNotNull { it.toObject(Review::class.java) }
                            summary.reviews = reviewList
                            summaries.add(summary)
                            if (summaries.size == result.size()) {
                                val adapter = SummaryAdapter(showSaveButton = false)
                                uploadedRecyclerView.adapter = adapter
                                adapter.submitList(summaries)
                            }
                        }
                        .addOnFailureListener {
                            summaries.add(summary)
                            if (summaries.size == result.size()) {
                                val adapter = SummaryAdapter(showSaveButton = false)
                                uploadedRecyclerView.adapter = adapter
                                adapter.submitList(summaries)
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load uploaded summaries", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSavedSummaries(uid: String) {
        val userRef = db.collection("users").document(uid)
        userRef.get().addOnSuccessListener { document ->
            val savedIds = document.get("savedSummaries") as? List<String> ?: emptyList()

            if (savedIds.isEmpty()) {
                val adapter = SummaryAdapter(showSaveButton = false)
                savedRecyclerView.adapter = adapter
                adapter.submitList(emptyList())
                return@addOnSuccessListener
            }

            db.collection("summaries")
                .whereIn(FieldPath.documentId(), savedIds)
                .get()
                .addOnSuccessListener { result ->
                    val savedSummaries = result.map { doc ->
                        val timestampObj = doc.getTimestamp("timestamp")
                        val timestampLong = timestampObj?.seconds ?: 0L

                        Summary(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            course = doc.getString("course") ?: "",
                            lecturer = doc.getString("lecturer") ?: "",
                            year = (doc.getLong("year") ?: 0L).toInt(),
                            pdfUrl = doc.getString("pdfUrl") ?: "",
                            timestamp = timestampLong,
                            uploaderUid = doc.getString("uploaderUid") ?: ""
                        )
                    }

                    val adapter = SummaryAdapter(showSaveButton = false)
                    savedRecyclerView.adapter = adapter
                    adapter.submitList(savedSummaries)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load saved summaries", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show()
        }
    }
}
