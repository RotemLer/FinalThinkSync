package com.example.finalthinksync

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.finalthinksync.utils.NotificationManager


class SummaryAdapter(private val showSaveButton: Boolean = true) :
    ListAdapter<Summary, SummaryAdapter.SummaryViewHolder>(SummaryDiffCallback()) {

    inner class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.item_summary_TV_Title)
        val courseText: TextView = itemView.findViewById(R.id.item_summary_TV_Course)
        val lecturerText: TextView = itemView.findViewById(R.id.item_summary_TV_Lecturer)
        val reviewsText: TextView = itemView.findViewById(R.id.item_summary_TV_Reviews)
        val saveButton: Button = itemView.findViewById(R.id.item_summary_BTN_Save)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_summary_row, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val summary = getItem(position)
        holder.titleText.text = summary.title
        holder.courseText.text = summary.course
        holder.lecturerText.text = summary.lecturer

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, SummaryDetailsActivity::class.java).apply {
                putExtra("summaryId", summary.id)
                putExtra("uploaderUid", summary.uploaderUid)
                putExtra("title", summary.title)
                putExtra("course", summary.course)
                putExtra("lecturer", summary.lecturer)
                putExtra("pdfUrl", summary.pdfUrl)
            }
            context.startActivity(intent)
        }

        if (summary.reviews.isNotEmpty()) {
            val reviewText = summary.reviews.joinToString("\n\n") {
                "By ${it.username} (${it.rating}⭐):\n${it.text}"
            }
            holder.reviewsText.text = reviewText
            holder.reviewsText.visibility = View.VISIBLE
        } else {
            holder.reviewsText.text = ""
            holder.reviewsText.visibility = View.GONE
        }

        if (showSaveButton) {
            holder.saveButton.visibility = View.VISIBLE

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userRef = FirebaseFirestore.getInstance()
                    .collection("users").document(currentUser.uid)

                userRef.get().addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        userRef.set(mapOf("savedSummaries" to listOf<String>()))
                            .addOnSuccessListener {
                                holder.saveButton.performClick()
                            }
                            .addOnFailureListener {
                                Toast.makeText(holder.itemView.context, "Error initializing user", Toast.LENGTH_SHORT).show()
                            }
                        return@addOnSuccessListener
                    }

                    val savedList = doc.get("savedSummaries") as? List<*> ?: listOf<Any>()
                    val alreadySaved = savedList.contains(summary.id)

                    if (alreadySaved) {
                        holder.saveButton.text = "✔️ Saved"
                        holder.saveButton.isEnabled = true
                        holder.saveButton.setOnClickListener {
                            userRef.update("savedSummaries", FieldValue.arrayRemove(summary.id))
                                .addOnSuccessListener {
                                    Toast.makeText(holder.itemView.context, "Removed from saved", Toast.LENGTH_SHORT).show()
                                    holder.saveButton.text = "💾 Save"
                                }
                                .addOnFailureListener {
                                    Toast.makeText(holder.itemView.context, "Error removing", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        holder.saveButton.text = "💾 Save"
                        holder.saveButton.isEnabled = true
                        holder.saveButton.setOnClickListener {
                            userRef.update("savedSummaries", FieldValue.arrayUnion(summary.id))
                                .addOnSuccessListener {
                                    Toast.makeText(holder.itemView.context, "Saved successfully", Toast.LENGTH_SHORT).show()
                                    holder.saveButton.text = "✔️ Saved"

                                    val currentUser = FirebaseAuth.getInstance().currentUser

                                    if (summary.uploaderUid != currentUser?.uid) {
                                        val notificationData = mapOf(
                                            "action" to "saved your summary",
                                            "actorName" to (currentUser?.email ?: "user"),
                                            "summaryTitle" to summary.title,
                                            "summaryId" to summary.id,
                                            "route" to "summary/${summary.id}",
                                            "type" to "save",
                                            "message" to "${currentUser?.email ?: "someone"} saved summary '${summary.title}'"
                                        )
                                        Log.d("NOTIF_DEBUG", "🔔 Attempting to notify ${summary.uploaderUid}")

                                        NotificationManager.createNotification(
                                            toUserId = summary.uploaderUid,
                                            data = notificationData
                                        ) { success ->
                                            if (success) {
                                                Log.d("NOTIF_DEBUG", "SAVE notification sent.")
                                            } else {
                                                Log.e("NOTIF_DEBUG", "SAVE notification failed.")
                                            }
                                        }
                                    }

                                }
                                .addOnFailureListener {
                                    Toast.makeText(holder.itemView.context, "Error saving", Toast.LENGTH_SHORT).show()
                                }
                        }

                    }
                }
            }
        } else {
            holder.saveButton.visibility = View.GONE
        }
    }

}

class SummaryDiffCallback : DiffUtil.ItemCallback<Summary>() {
    override fun areItemsTheSame(oldItem: Summary, newItem: Summary): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Summary, newItem: Summary): Boolean = oldItem == newItem
}
