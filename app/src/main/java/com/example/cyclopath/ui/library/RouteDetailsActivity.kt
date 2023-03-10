package com.example.cyclopath.ui.library

import RouteObj
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.G
import com.example.cyclopath.G.user
import com.example.cyclopath.R
import com.facebook.AccessTokenManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mapbox.api.directions.v5.models.DirectionsRoute
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
import java.io.File
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
        var save : FloatingActionButton = findViewById(R.id.save_route)
        var comment : TextView = findViewById(R.id.etTodoTitle)

        var detail_name: TextView = findViewById(R.id.rd_name)
        var detail_description: TextView = findViewById(R.id.rd_des)
        var detail_duration: TextView = findViewById(R.id.rd_dur)
        var detai_distance: TextView = findViewById(R.id.rd_dis)
        var detail_rate: TextView = findViewById(R.id.rating)

        //
        detail_name.setText(intent.getStringExtra("route"))
        detail_description.setText(intent.getStringExtra("description"))
        detail_duration.setText(intent.getStringExtra("duration"))
        detai_distance.setText(intent.getStringExtra("distance"))



        // get the mapview
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
                                .zoom(13.0)
                                .center(Point.fromLngLat(-3.195851,55.947145 ))
                                .build()

                            routelane.getMapboxMap().setCamera(cameraOptions)
                        }
                        )
            )
        }.addOnFailureListener {
        }



        // get the comment recyclerview
        var routeCmtRef = storageRef.child("route_comment/${name}")

        // Create a Gson instance with the custom TypeAdapter registered
        val gson = GsonBuilder()
            .registerTypeAdapter(GeoJson::class.java, LibraryFragment.GeoJsonTypeAdapter())
            .create()

        var comments =arrayListOf<String>()
        val adapter = CommentAdapter(comments)
        val llm = LinearLayoutManager(this)
        val view = findViewById<RecyclerView>(R.id.c_view)
        view.layoutManager = llm
        view.adapter = adapter
        println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        routeCmtRef.listAll()
            .addOnSuccessListener { listResult ->
                println("ccccccccccccccccccccccccccccccccc")
                // Iterate over each item in the list and download its contents
                listResult.items.forEach { item ->
                    item.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        // Convert the downloaded bytes to a String
                        val jsonString = String(bytes)

                        // Parse the JSON String into a String object
                        val comment = gson.fromJson(jsonString, String::class.java)

                        // Add the Route object to the list
                        comments.add(comment)
                        println("33333333333333333333333333333333333333")
                        println(comment)
                        // Update the RecyclerView on the main thread
                        view.post {
                            adapter.notifyItemInserted(comments.size - 1)
                        }
                    }.addOnFailureListener {
                        // Handle any errors that occur while downloading the file
                        Log.e(AccessTokenManager.TAG, "Failed to download route: ${item.name}", it)
                    }
                }
            }
            .addOnFailureListener {
                Log.e(AccessTokenManager.TAG, "Failed to list routes", it)
            }


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
                Toast.makeText(this, "Successfully comment!", Toast.LENGTH_SHORT).show()

                comments.add(cmt)
                view.post {
                    adapter.notifyItemInserted(comments.size - 1)
                }
                comment.setText("")

            }
        }

        save.setOnClickListener {
            val temp = RouteObj()

            temp.route_name_text = intent.getStringExtra("route")
            temp.route_description_text = intent.getStringExtra("description")
            temp.route_duration = intent.getStringExtra("duration")
            temp.difficulty = intent.getDoubleExtra("difficulty",0.0)
            temp.route_length_text = intent.getStringExtra("distance")
            temp.geoJsonurl = url

            var sp = this.baseContext?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
            val name = sp!!.getString("username","empty")

            // store routeObj.json
            val gson = Gson()
            val routeObjJson = gson.toJson(temp)
            val storageRef = Firebase.storage.reference
            val routeRef = storageRef.child("savedroutes/$name/${temp.route_name_text}.json")
            routeRef.putBytes(routeObjJson.toByteArray())

            Toast.makeText(this, "You have bookmarked this route, you can find it from your favorite routes", Toast.LENGTH_SHORT).show()


        }
    }
}