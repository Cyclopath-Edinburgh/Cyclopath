package com.example.cyclopath.ui.library

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R

class CommentAdapter (val comments: ArrayList<String>) :
    RecyclerView.Adapter<CommentAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val routeNameText: TextView = view.findViewById(R.id.user_comment)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.comment_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.routeNameText.text = comments[position]

    }

    override fun getItemCount(): Int {
        return comments.size
    }
}