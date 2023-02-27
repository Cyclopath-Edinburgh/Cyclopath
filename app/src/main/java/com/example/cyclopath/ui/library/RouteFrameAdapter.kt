package com.example.cyclopath.ui.library

import RouteObj
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R

class RouteFrameAdapter(
    private val routes: MutableList<RouteObj>,
): RecyclerView.Adapter<RouteFrameAdapter.RouteObjHolder>() {

    class RouteObjHolder(view: View) : RecyclerView.ViewHolder(view){
        val imageView: ImageView = itemView.findViewById(R.id.route_image)
        val routeItemClickable: View = view.findViewById(R.id.route_item_clickable)

        val routeNameText: TextView = view.findViewById(R.id.route_name_text)
        val routeLengthText: TextView = view.findViewById(R.id.route_length_text)
        val routeElevationText: TextView = view.findViewById(R.id.route_elevation_text)
        val routePopularityText: TextView = view.findViewById(R.id.route_popularity_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteObjHolder {
        return RouteObjHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.route_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RouteObjHolder, position: Int) {
        val route = routes[position]

        holder.routeNameText.text = route.route_name_text
        holder.routeLengthText.text = route.route_length_text
        holder.routeElevationText.text = route.route_elevation_text
        holder.routePopularityText.text = route.route_popularity_text

        // Set item click listener
        holder.routeItemClickable.setOnClickListener() {
            // Handle item click event
        }

    }

    override fun getItemCount(): Int {
        return routes.size
    }

}