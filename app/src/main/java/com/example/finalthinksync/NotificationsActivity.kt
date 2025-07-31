package com.example.finalthinksync

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        val backBtn = findViewById<ImageButton>(R.id.notification_BTN_BackToProfile)
        backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val notificationsRef = db
            .collection("users")
            .document(uid)
            .collection("notifications")
            .orderBy("timestamp")

        notificationsRef.get().addOnSuccessListener { result ->
            val batch = db.batch()

            for (doc in result) {
                val data = doc.data
                val action = data["action"] as? String ?: "did something"
                val actor = data["actorName"] as? String ?: "someone"
                val summary = data["summaryTitle"] as? String ?: ""
                val time = data["timestamp"]
                val isRead = data["isRead"] as? Boolean ?: false

                val display = "$actor $action\n📄 $summary"

                val timeString = (time as? com.google.firebase.Timestamp)?.let {
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    sdf.format(it.toDate())
                } ?: ""

                val item = TextView(this).apply {
                    text = "$display\n🕓 $timeString"
                    textSize = 16f
                    setPadding(16, 16, 16, 32)

                    setBackgroundColor(
                        if (!isRead)
                            ContextCompat.getColor(context, R.color.purple300)
                        else
                            Color.TRANSPARENT
                    )
                }

                notificationContainer.addView(item)

                if (!isRead) {
                    batch.update(doc.reference, "isRead", true)
                }
            }

            batch.commit()
        }
    }
}
