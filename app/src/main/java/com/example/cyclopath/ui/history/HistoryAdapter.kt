package com.example.cyclopath.ui.history

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R

/**
 * Create the data to be shown in the Recycler View in the historic data activity
 */
class HistoryAdapter(val alist: ArrayList<String>) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) :
            RecyclerView.ViewHolder(view) {
        val activity: TextView

        init {
            activity = view.findViewById<View>(R.id.activity) as TextView
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.history_row, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.activity.text = alist[position]
        viewHolder.itemView.setOnClickListener {
            var intent = Intent(viewHolder.itemView.context, ViewHistoryActivity::class.java)
            intent.putExtra("name",alist[position])
            viewHolder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return alist.size
    }

}