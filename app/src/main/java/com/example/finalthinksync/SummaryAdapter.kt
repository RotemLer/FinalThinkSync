package com.example.finalthinksync

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SummaryAdapter(
    private val summaries: List<Summary>
) : RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    inner class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.item_summary_TV_Title)
        val courseText: TextView = itemView.findViewById(R.id.item_summary_TV_Course)
        val lecturerText: TextView = itemView.findViewById(R.id.item_summary_TV_Lecturer)

        init {
            itemView.setOnClickListener {
                val summary = summaries[adapterPosition]
                val context = itemView.context
                val intent = Intent(context, ItemSummaryActivity::class.java).apply {
                    putExtra("title", summary.title)
                    putExtra("course", summary.course)
                    putExtra("lecturer", summary.lecturer)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_summary, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val summary = summaries[position]
        holder.titleText.text = summary.title
        holder.courseText.text = summary.course
        holder.lecturerText.text = summary.lecturer
    }

    override fun getItemCount(): Int = summaries.size
}
