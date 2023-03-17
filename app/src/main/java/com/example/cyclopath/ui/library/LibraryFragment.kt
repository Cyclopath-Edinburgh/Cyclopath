package com.example.cyclopath.ui.library

import RouteObj
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R
import com.example.cyclopath.items.DirectionsRouteAdapter
import com.example.cyclopath.ui.history.HistoryAdapter
import com.facebook.AccessTokenManager.Companion.TAG
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.gson.*
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.*
import com.mapbox.maps.MapView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Type
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.launch
import java.io.File

/**
 * A simple [Fragment] subclass.
 * Use the [LibraryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LibraryFragment : Fragment() {
    val routeObjList = mutableListOf<RouteObj>()
    // List to store the name of the activity
    var routeList: ArrayList<RouteObj> = arrayListOf()

    // Connect to the Firebase storage
    var storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    class GeoJsonTypeAdapter : JsonDeserializer<GeoJson>, JsonSerializer<GeoJson> {
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): GeoJson? {
            json?.let {
                val jsonObject = it.asJsonObject
                val type = jsonObject.get("type").asString

                return when (type) {
                    "Feature" -> context?.deserialize<Feature>(jsonObject, Feature::class.java)
                    "Point" -> context?.deserialize<Point>(jsonObject, Point::class.java)
                    "LineString" -> context?.deserialize<LineString>(jsonObject, LineString::class.java)
                    "Polygon" -> context?.deserialize<Polygon>(jsonObject, Polygon::class.java)
                    // Add support for other GeoJson types as needed
                    else -> throw IllegalArgumentException("Unsupported GeoJson type: $type")
                }
            }
            return null
        }

        override fun serialize(
            src: GeoJson?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement {
            return context!!.serialize(src)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_library, container, false)

        val storageRef = storage.reference
        var routeRef = storageRef.child("routes")
        var routeGeoRef = storageRef.child("routegeojson")


        // Create a Gson instance with the custom TypeAdapter registered
        val gson = GsonBuilder()
            .registerTypeAdapter(GeoJson::class.java, GeoJsonTypeAdapter())
            .create()



        val adapter = RouteFrameAdapter(routeObjList)
        val llm = LinearLayoutManager(context)
        val view = root.findViewById<RecyclerView>(R.id.r_view)
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

//                            var specificFileRef = routeGeoRef.child(route.route_name_text+".geojson")
//
//                            // Download the file to a local file
//                            val localFile = File.createTempFile("filename", "json")
//                            specificFileRef.getFile(localFile).addOnSuccessListener {
//                                // Handle success case
//                            }.addOnFailureListener {
//                                // Handle failure case
//                            }

                            // Add the Route object to the list
                            routeObjList.add(route)
                            println(route)
                            // Update the RecyclerView on the main thread
                            view.post {
                                adapter.notifyItemInserted(routeObjList.size - 1)
                            }
                        }.addOnFailureListener {
                            // Handle any errors that occur while downloading the file
                            Log.e(TAG, "Failed to download route: ${item.name}", it)
                        }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to list routes", it)
            }

        val myrouteBTN : FloatingActionButton = root.findViewById<FloatingActionButton>(R.id.myroutes)
        myrouteBTN.setOnClickListener{
            val intent = Intent(context, myRoutesActivity::class.java)
            startActivity(intent)
        }

        val sortSpinner = root.findViewById<Spinner>(R.id.sort_spinner)
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedOption = parent?.getItemAtPosition(position).toString()
                when (selectedOption) {
                    "Distance" -> {
                        // Sort by distance
                        adapter.sortByDistance()
                    }
                    "Difficulty" -> {
                        // Sort by difficulty
                        adapter.sortByDifficulty()
                    }
                    "Ratings" -> {
                        // Sort by ratings
                        adapter.sortByRating()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }




        return root
    }



}