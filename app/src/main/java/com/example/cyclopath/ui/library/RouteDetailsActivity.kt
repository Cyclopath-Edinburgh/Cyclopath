package com.example.cyclopath.ui.library

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R
import com.facebook.AccessTokenManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mapbox.geojson.GeoJson
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.style.expressions.dsl.generated.zoom
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RouteDetailsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.route_details)
        val name = intent.getStringExtra("route")
        val url = intent.getStringExtra("url")
        var routelane : MapView = findViewById(R.id.routelane)
        var btn : Button = findViewById(R.id.btnAddComment)
        var comment : TextView = findViewById(R.id.etTodoTitle)

        btn.setOnClickListener {
            val cmt = comment.text.toString()
            if(cmt.isNotEmpty()) {
                val gson = Gson()
                val routeObjJson = gson.toJson(cmt)
                val storageRef = Firebase.storage.reference
                val curr = LocalDateTime.now()
                val time = DateTimeFormatter.ofPattern("HH:mm:ss")
                val randomName = name+" "+curr.format(time)
                val routeRef = storageRef.child("route_comment/${name}/${randomName}.json")
                routeRef.putBytes(routeObjJson.toByteArray())
//                comment.text.clear()
            }
        }



        val storageRef = Firebase.storage.reference
        var dataRef = url?.let { storageRef.child(it) }
        dataRef!!.downloadUrl.addOnSuccessListener {
            routelane.getMapboxMap().loadStyle(
                (
                        style(styleUri = Style.MAPBOX_STREETS) {
                            +geoJsonSource("line") {
                                url(it.toString())
                            }
                            +lineLayer("linelayer", "line") {
                                lineCap(LineCap.ROUND)
                                lineJoin(LineJoin.ROUND)
                                lineOpacity(0.9)
                                lineWidth(8.0)
                                lineColor("#F55C5C")
                            }

                            val cameraOptions = CameraOptions.Builder()
                                .zoom(10.0)
                                .center(Point.fromLngLat(-3.195851,55.947145 ))
                                .build()

                            routelane.getMapboxMap().setCamera(cameraOptions)
                        }
                        )
            )
        }.addOnFailureListener {
        }



//        val root = inflater.inflate(R.layout.fragment_library, container, false)
//        val storageRef = storage.reference
//        var routeRef = storageRef.child("routes")
//
//        // Create a Gson instance with the custom TypeAdapter registered
//        val gson = GsonBuilder()
//            .registerTypeAdapter(GeoJson::class.java, LibraryFragment.GeoJsonTypeAdapter())
//            .create()
//
//        var commentList: ArrayList<String> = {"sad"}
//        val adapter = CommentAdapter(commentList)
//        val llm = LinearLayoutManager(context)
//        val view = root.findViewById<RecyclerView>(R.id.r_view)
//        view.layoutManager = llm
//        view.adapter = adapter
//        routeRef.listAll()
//            .addOnSuccessListener { listResult ->
//                // Iterate over each item in the list and download its contents
//                listResult.items.forEach { item ->
//                    item.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
//                        // Convert the downloaded bytes to a String
//                        val jsonString = String(bytes)
//
//                        // Parse the JSON String into a Route object
//                        val route = gson.fromJson(jsonString, RouteObj::class.java)
//
//                        // Add the Route object to the list
//                        routeObjList.add(route)
//                        println(route)
//                        // Update the RecyclerView on the main thread
//                        view.post {
//                            adapter.notifyItemInserted(routeObjList.size - 1)
//                        }
//                    }.addOnFailureListener {
//                        // Handle any errors that occur while downloading the file
//                        Log.e(AccessTokenManager.TAG, "Failed to download route: ${item.name}", it)
//                    }
//                }
//            }
//            .addOnFailureListener {
//                Log.e(AccessTokenManager.TAG, "Failed to list routes", it)
//            }
    }
}