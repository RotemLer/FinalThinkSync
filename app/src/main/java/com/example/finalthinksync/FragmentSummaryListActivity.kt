package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FragmentSummaryListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SummaryAdapter
    private lateinit var filterTypeSpinner: Spinner
    private lateinit var filterValueInput: AutoCompleteTextView

    private var fullSummaryList: List<Summary> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_summary_list)

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: "User"

        val welcomeTextView = findViewById<TextView>(R.id.summary_list_TV_Welcome)
        welcomeTextView.text = "Hello, $email!"

        filterTypeSpinner = findViewById(R.id.spinner_filter_type)
        filterValueInput = findViewById(R.id.summary_list_ET_FilterValue)
        recyclerView = findViewById(R.id.summary_list_RV_Summaries)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SummaryAdapter()
        recyclerView.adapter = adapter

        setupFilterTypeSpinner()
        loadSummariesFromFirestore()

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
                        val timestamp = doc.getTimestamp("timestamp")?.seconds ?: 0L
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
                refreshSuggestions()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load summaries", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupFilterTypeSpinner() {
        val filterOptions = resources.getStringArray(R.array.filter_types)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterTypeSpinner.adapter = spinnerAdapter

        filterTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                refreshSuggestions()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        filterValueInput.setOnClickListener {
            filterValueInput.showDropDown()
        }

        filterValueInput.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = filterValueInput.adapter.getItem(position).toString()
            applyFilter(filterTypeSpinner.selectedItem.toString(), selectedValue)
        }

        filterValueInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val typed = s.toString()
                applyFilter(filterTypeSpinner.selectedItem.toString(), typed)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun refreshSuggestions() {
        if (fullSummaryList.isEmpty()) return

        val selected = filterTypeSpinner.selectedItem.toString()
        val suggestions = when (selected) {
            "Course" -> fullSummaryList.map { it.course }.distinct().sorted()
            "Year" -> fullSummaryList.map { it.year.toString() }.distinct().sorted()
            "Lecturer" -> fullSummaryList.map { it.lecturer }.distinct().sorted()
            else -> listOf()
        }

        val adapterSuggestions = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
        filterValueInput.setAdapter(adapterSuggestions)
        filterValueInput.setText("")
    }

    private fun applyFilter(filterType: String, value: String) {
        val filteredList = when (filterType) {
            "Course" -> fullSummaryList.filter { it.course.contains(value, ignoreCase = true) }
            "Year" -> fullSummaryList.filter { it.year.toString().contains(value) }
            "Lecturer" -> fullSummaryList.filter { it.lecturer.contains(value, ignoreCase = true) }
            else -> fullSummaryList
        }
        adapter.submitList(filteredList)
    }
}
