package com.example.cyclopath.ui.library

import RouteObj
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R
import com.facebook.AccessTokenManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import com.mapbox.geojson.GeoJson

class myRoutesActivity: AppCompatActivity() {
    val routeObjList = mutableListOf<RouteObj>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.my_routes)

        var sp = this.baseContext?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val name = sp!!.getString("username","empty")

        val storageRef = Firebase.storage.reference
        var routeRef = storageRef.child("savedroutes/$name")
        var routeGeoRef = storageRef.child("routegeojson")

        // Create a Gson instance with the custom TypeAdapter registered
        val gson = GsonBuilder()
            .registerTypeAdapter(GeoJson::class.java, LibraryFragment.GeoJsonTypeAdapter())
            .create()



        val adapter = RouteFrameAdapter(routeObjList)
        val llm = LinearLayoutManager(this)
        val view = findViewById<RecyclerView>(R.id.r_view2)
        view.layoutManager = llm
        view.adapter = adapter
        routeRef.listAll()
            .addOnSuccessListener { listResult ->
                // Iterate over each item in the list and download its contents
                listResult.items.forEach { item ->
                    item.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        // Convert the downloaded bytes to a String
                        val jsonString = String(bytes)

                        // Parse the JSON String into a Route object
                        val route = gson.fromJson(jsonString, RouteObj::class.java)

                        // Create a reference to the image with the same name as the RouteObj
                        val imageRef = storageRef.child("images/${route.route_name_text}.png")

                        // Download the image as a byte array
                        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { imageBytes ->
                            // Convert the downloaded bytes to a Bitmap
                            val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                            // Set the Bitmap to the Route object
                            route.staticimage = imageBitmap

                            // Add the Route object to the list
                            routeObjList.add(route)

                            // Update the RecyclerView on the main thread
                            view.post {
                                adapter.notifyItemChanged(routeObjList.size - 1)
                            }
                        }.addOnFailureListener {
                            // Handle any errors that occur while downloading the image
                            Log.e(AccessTokenManager.TAG, "Failed to download image for route: ${route.route_name_text}", it)
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
    }
}
