package com.example.cyclopath.ui.library

import RouteObj
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R
import com.example.cyclopath.items.DirectionsRouteAdapter
import com.example.cyclopath.ui.history.HistoryAdapter
import com.facebook.AccessTokenManager.Companion.TAG
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.gson.*
import com.mapbox.api.directions.v5.models.DirectionsRoute
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.reflect.Type
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.launch

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

    fun getRoutes(): MutableList<RouteObj> {
        val storageRef = storage.reference
        var routeRef = storageRef.child("routes")

        val gson = GsonBuilder()
            .registerTypeAdapter(DirectionsRoute::class.java, object :
                JsonDeserializer<DirectionsRoute> {
                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    context: JsonDeserializationContext?
                ): DirectionsRoute {
                    return DirectionsRoute.fromJson(json?.asJsonObject.toString())
                }
            })
            .create()

        routeRef.listAll()
            .addOnSuccessListener { listResult ->
                // Iterate over each item in the list and download its contents
                listResult.items.forEach { item ->
                    item.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        // Convert the downloaded bytes to a String
                        println("winwinwin")
                        val jsonString = String(bytes)

                        // Parse the JSON String into a Route object
                        val route = gson.fromJson(jsonString, RouteObj::class.java)

                        // Add the Route object to the list
                        routeObjList.add(route)
                        println(routeObjList.size)
                        println(route.toString())
                    }.addOnFailureListener {
                        // Handle any errors that occur while downloading the file
                        Log.e(TAG, "Failed to download route: ${item.name}", it)
                    }
                }
                println("111")
                println("success")
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to list routes", it)
            }
        return routeObjList

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_library, container, false)

        val storageRef = storage.reference
        var routeRef = storageRef.child("routes")



//        // Create a Gson instance with the custom TypeAdapter registered
//        val gson = GsonBuilder()
//            .registerTypeAdapter(DirectionsRoute::class.java, DirectionsRouteAdapter())
//            .create()

        val gson = GsonBuilder()
            .registerTypeAdapter(DirectionsRoute::class.java, object :
                JsonDeserializer<DirectionsRoute> {
                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    context: JsonDeserializationContext?
                ): DirectionsRoute {
                    return DirectionsRoute.fromJson(json?.asJsonObject.toString())
                }
            })
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

                            // Add the Route object to the list
                            routeObjList.add(route)
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

        return root
    }



}