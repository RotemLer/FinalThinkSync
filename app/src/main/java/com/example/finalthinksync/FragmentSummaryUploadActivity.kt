package com.example.finalthinksync

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.Query


class FragmentSummaryUploadActivity : AppCompatActivity() {

    private val PDF_REQUEST_CODE = 100
    private var selectedPdfUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_summary_upload)

        val titleEditText = findViewById<EditText>(R.id.summary_upload_TEXT_Title)
        val courseEditText = findViewById<EditText>(R.id.summary_upload_TEXT_Course)
        val lecturerEditText = findViewById<EditText>(R.id.summary_upload_TEXT_Lecturer)
        val yearEditText = findViewById<EditText>(R.id.summary_upload_TEXT_Year)
        val uploadButton = findViewById<Button>(R.id.summary_upload_BTN_Upload)
        val choosePdfButton = findViewById<Button>(R.id.summary_upload_BTN_PDF)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uploaderUid = currentUser?.uid ?: ""

        choosePdfButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(intent, PDF_REQUEST_CODE)
        }


        uploadButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val course = courseEditText.text.toString().trim()
            val lecturer = lecturerEditText.text.toString().trim()
            val year = yearEditText.text.toString().trim()

            if (title.isBlank() || course.isBlank() || lecturer.isBlank() || year.isBlank() || selectedPdfUri == null) {
                Toast.makeText(this, "Please fill in all fields and select a PDF file.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uploaderUid = currentUser.uid
            val fileName = "summaries/${System.currentTimeMillis()}_${title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")}.pdf"
            val fileRef = storage.reference.child(fileName)

            try {
                val inputStream = contentResolver.openInputStream(selectedPdfUri!!)
                if (inputStream == null) {
                    Toast.makeText(this, "Could not open the file.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val fileBytes = inputStream.readBytes()
                inputStream.close()

                Toast.makeText(this, "Uploading the file...", Toast.LENGTH_SHORT).show()
                Log.d("UploadDebug", "Uploading ${fileBytes.size} bytes to $fileName")

                fileRef.putBytes(fileBytes)
                    .addOnSuccessListener {
                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            val summaryData = hashMapOf(
                                "title" to title,
                                "course" to course,
                                "lecturer" to lecturer,
                                "year" to year.toInt(),
                                "pdfUrl" to uri.toString(),
                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                "uploaderUid" to uploaderUid
                            )

                            db.collection("summaries")
                                .add(summaryData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "The summary was uploaded successfully!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("UploadDebug", "Data upload failed: ${e.message}")
                                    Toast.makeText(this, "Data upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("UploadDebug", "File upload failed: ${e.message}")
                        Toast.makeText(this, "File upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

            } catch (e: Exception) {
                Log.e("UploadDebug", "Error reading file: ${e.message}")
                Toast.makeText(this, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }




        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_upload
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, FragmentSummaryListActivity::class.java))
                    true
                }
                R.id.nav_upload -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d("UploadDebug", "onActivityResult called")

        if (requestCode == PDF_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            data.data?.let { uri ->
                selectedPdfUri = uri

                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d("UploadDebug", "Permission granted for uri: $uri")
                    Log.d("UploadDebug", "Uri: $uri")
                    Log.d("UploadDebug", "Authority: ${uri.authority}")
                    Log.d("UploadDebug", "Path: ${uri.path}")
                    Log.d("UploadDebug", "Scheme: ${uri.scheme}")

                } catch (e: SecurityException) {
                    Log.e("UploadDebug", "Permission failed: ${e.message}")
                    Toast.makeText(this, "Permission failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }

                Log.d("UploadDebug", "File selected: $uri")
                Toast.makeText(this, "File selected successfully", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("UploadDebug", "File not selected or cancelled")
        }
    }



}
