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
    private val routes: MutableList<RouteObj>,
): RecyclerView.Adapter<RouteFrameAdapter.RouteObjHolder>() {

    class RouteObjHolder(view: View) : RecyclerView.ViewHolder(view){
        var imageView: ImageView = itemView.findViewById(R.id.route_image)
        val routeItemClickable: View = view.findViewById(R.id.route_item_clickable)

        val routeNameText: TextView = view.findViewById(R.id.route_name_text)
        val routeLengthText: TextView = view.findViewById(R.id.route_length_text)
        val routeDurationText: TextView = view.findViewById(R.id.route_duration)
        val routeDifficulty: TextView = view.findViewById(R.id.route_difficulty)
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
        if (position<routes.size) {
//            println(position)
//            println(routes.size)

            val route = routes[position]

            holder.routeNameText.text = route.route_name_text
            holder.routeLengthText.text = route.route_length_text
            holder.routeDurationText.text = route.route_duration
            holder.routeDifficulty.text = route.difficulty.toString()
            holder.imageView.setImageBitmap(route.snapshot)

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
                holder.itemView.context.startActivity(i)
            }
        }


//        val storageRef = Firebase.storage.reference
//        var dataRef = storageRef.child(route.geoJsonurl.toString())
//        dataRef.downloadUrl.addOnSuccessListener {
//            holder.imageView.getMapboxMap().loadStyle(
//                (
//                        style(styleUri = Style.MAPBOX_STREETS) {
//                            +geoJsonSource("line") {
//                                url(it.toString())
//                            }
//                            +lineLayer("linelayer", "line") {
//                                lineCap(LineCap.ROUND)
//                                lineJoin(LineJoin.ROUND)
//                                lineOpacity(0.9)
//                                lineWidth(8.0)
//                                lineColor("#F55C5C")
//                            }
//
//                            val cameraOptions = CameraOptions.Builder()
//                                .zoom(3.0)
//                                .center(Point.fromLngLat(-118.2437, 34.0522))
//                                .build()
//
//                            mapView.getMapboxMap().setCamera(cameraOptions)
//                        }
//                        )
//            )
//        }.addOnFailureListener {
//
//        }


    }


    override fun getItemCount(): Int {
        return routes.size
    }

}