package com.example.cyclopath.ui.library

import RouteObj
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R
import com.example.cyclopath.ui.history.ViewHistoryActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style


class RouteFrameAdapter(
    private var routes: MutableList<RouteObj>,
): RecyclerView.Adapter<RouteFrameAdapter.RouteObjHolder>() {
    private var routesTemp: MutableList<RouteObj> = routes

    class RouteObjHolder(view: View) : RecyclerView.ViewHolder(view){
        var imageView: ImageView = itemView.findViewById(R.id.route_image)
        val routeItemClickable: View = view.findViewById(R.id.route_item_clickable)

        val routeNameText: TextView = view.findViewById(R.id.route_name_text)
        val routeLengthText: TextView = view.findViewById(R.id.route_length_text)
        val routeDurationText: TextView = view.findViewById(R.id.route_duration)
        val routeDifficulty: TextView = view.findViewById(R.id.route_difficulty)
        val routeUp: TextView = view.findViewById(R.id.up_ele)
        val routeDown: TextView = view.findViewById(R.id.down_ele)
        val routeAddress: TextView = view.findViewById(R.id.route_address)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteObjHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.route_item, parent, false)
        return RouteObjHolder(view)
    }

    override fun onBindViewHolder(holder: RouteObjHolder, position: Int) {
        if (position<routes.size) {
//            println(position)
//            println(routes.size)

            val route = routes[position]

            holder.routeNameText.text = route.route_name_text
            holder.routeLengthText.text = route.route_length_text
            holder.routeDurationText.text = route.route_duration
            holder.routeDifficulty.text = route.difficulty_level
            holder.imageView.setImageBitmap(route.staticimage)
            holder.routeUp.text = route.route_up.toString() + "m"
            holder.routeDown.text = route.route_down.toString() + "m"
            holder.routeAddress.text = route.route_start

            // Set item click listener
            holder.routeItemClickable.setOnClickListener {
                // Handle item click event
                val i = Intent(holder.itemView.context, RouteDetailsActivity::class.java)
                i.putExtra("route", routes[position].route_name_text)
                i.putExtra("url", routes[position].geoJsonurl)
                i.putExtra("distance", routes[position].route_length_text)
                i.putExtra("description", routes[position].route_description_text)
                i.putExtra("difficulty", routes[position].difficulty)
                i.putExtra("popularity", routes[position].route_popularity_text)
                i.putExtra("elevation", routes[position].route_elevation_text)
                i.putExtra("duration", routes[position].route_duration)
                i.putExtra("up",routes[position].route_up)
                i.putExtra("down",routes[position].route_down)
                i.putExtra("zoom",routes[position].zoomlevel)
                i.putExtra("focusLng",routes[position].focusLng)
                i.putExtra("focusLat",routes[position].focusLat)
                i.putExtra("difficultyLevel",routes[position].difficulty_level)
                holder.itemView.context.startActivity(i)
            }
        }


    }

    fun sortByDistance() {
        routes.sortBy { it.route_distance }
        notifyDataSetChanged()
    }

    fun sortByDifficulty() {
        routes.sortByDescending { it.difficulty }
        notifyDataSetChanged()
    }

    fun sortByRating() {
        routes.sortByDescending { it.route_popularity_text }
        notifyDataSetChanged()
        println("iiiooo")
        println(routesTemp.toString())
    }

    fun sortByNearest() {
        filter()
        routes.sortBy { it.near }
        notifyDataSetChanged()
    }

    fun filter(){
        routes = routesTemp
        routes = routes.filter { item ->
            item.near < 2.0
        } as MutableList<RouteObj>
    }


    override fun getItemCount(): Int {
        return routes.size
    }

}