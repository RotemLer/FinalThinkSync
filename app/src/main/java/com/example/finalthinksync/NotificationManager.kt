package com.example.finalthinksync.utils

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object NotificationManager {

    fun createNotification(
        toUserId: String,
        data: Map<String, Any?>,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val fullData = mutableMapOf<String, Any>(
            "timestamp" to FieldValue.serverTimestamp(),
            "isRead" to false
        )

        data.forEach { (key, value) ->
            if (value != null) fullData[key] = value
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(toUserId)
            .collection("notifications")
            .add(fullData)
            .addOnSuccessListener {
                onComplete?.invoke(true)
            }
            .addOnFailureListener {
                it.printStackTrace()
                Log.e("NOTIF_DEBUG", "🔥 ERROR: ${it.message}")
                onComplete?.invoke(false)
            }
    }
}
