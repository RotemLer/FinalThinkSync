package com.example.finalthinksync

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.finalthinksync.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificationsActivity : AppCompatActivity() {

    private lateinit var notificationContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        notificationContainer = findViewById(R.id.notificationContainer)
        val backBtn = findViewById<Button>(R.id.notification_BTN_BackToProfile)
        backBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("notifications")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val data = doc.data
                    val action = data["action"] as? String ?: "עשה משהו"
                    val actor = data["actorName"] as? String ?: "מישהו"
                    val summary = data["summaryTitle"] as? String ?: ""
                    val time = data["timestamp"]

                    val display = "$actor $action\n📄 $summary"

                    val timeString = (time as? com.google.firebase.Timestamp)?.let {
                        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        sdf.format(it.toDate())
                    } ?: ""

                    val item = TextView(this).apply {
                        text = "$display\n🕓 $timeString"
                        textSize = 16f
                        setPadding(0, 0, 0, 32)
                    }

                    notificationContainer.addView(item)
                }
            }
    }
}
