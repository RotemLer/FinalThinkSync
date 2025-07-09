package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentSummaryListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SummaryAdapter
    private lateinit var filterEditText: EditText
    private var fullSummaryList: List<Summary> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_summary_list)

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "User"

        val welcomeTextView = findViewById<TextView>(R.id.summary_list_TV_Welcome)
        welcomeTextView.text = "Hello, $email!"

        filterEditText = findViewById(R.id.summary_list_ET_FilterCourse)
        recyclerView = findViewById(R.id.summary_list_RV_Summaries)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SummaryAdapter()
        recyclerView.adapter = adapter

        loadSummariesFromFirestore()

        filterEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_home
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

    private fun loadSummariesFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("summaries")
            .get()
            .addOnSuccessListener { result ->
                val summaries = result.mapNotNull { doc ->
                    try {
                        val id = doc.id
                        val title = doc.getString("title") ?: return@mapNotNull null
                        val course = doc.getString("course") ?: return@mapNotNull null
                        val lecturer = doc.getString("lecturer") ?: return@mapNotNull null
                        val year = when (val rawYear = doc.get("year")) {
                            is Long -> rawYear.toInt()
                            is String -> rawYear.toIntOrNull() ?: 0
                            else -> 0
                        }
                        val timestamp = doc.getLong("timestamp") ?: 0L
                        val uploaderUid = doc.getString("uploaderUid") ?: ""
                        val pdfUrl = doc.getString("pdfUrl") ?: ""

                        Summary(id, title, course, lecturer, year, timestamp, uploaderUid, pdfUrl)
                    } catch (e: Exception) {
                        Log.e("FirestoreParse", "Error parsing document ${doc.id}: ${e.message}")
                        null
                    }
                }

                fullSummaryList = summaries
                adapter.submitList(fullSummaryList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load summaries", Toast.LENGTH_SHORT).show()
            }
    }



    private fun updateList(filterText: String) {
        Log.d("Filter", "Filtering by: $filterText")
        val filteredList = fullSummaryList.filter {
            it.course.contains(filterText, ignoreCase = true)
        }
        adapter.submitList(filteredList)
    }

}
