package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

    private fun loadUploadedSummaries(uid: String) {
        db.collection("summaries")
            .whereEqualTo("uploaderUid", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val uploadedSummaries = result.map { doc ->
                    Summary(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        course = doc.getString("course") ?: "",
                        lecturer = doc.getString("lecturer") ?: "",
                        year = doc.getString("year")?.toIntOrNull() ?: 0,
                        pdfUrl = doc.getString("pdfUrl") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        uploaderUid = uid
                    )
                }
                val adapter = SummaryAdapter(showSaveButton = false)
                uploadedRecyclerView.adapter = adapter
                adapter.submitList(uploadedSummaries)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Failed to load uploaded summaries: ${e.message}")
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
                        Summary(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            course = doc.getString("course") ?: "",
                            lecturer = doc.getString("lecturer") ?: "",
                            year = doc.getString("year")?.toIntOrNull() ?: 0,
                            pdfUrl = doc.getString("pdfUrl") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            uploaderUid = doc.getString("uploaderUid") ?: ""
                        )
                    }
                    val adapter = SummaryAdapter(showSaveButton = false)
                    savedRecyclerView.adapter = adapter
                    adapter.submitList(savedSummaries)
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreError", "Failed to load saved summaries: ${e.message}")
                    Toast.makeText(this, "Failed to load saved summaries", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e ->
            Log.e("FirestoreError", "Failed to load user data: ${e.message}")
        }
    }
}
