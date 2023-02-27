package com.example.cyclopath.ui.library

import RouteObj
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R
import com.example.cyclopath.ui.history.HistoryAdapter
import com.facebook.AccessTokenManager.Companion.TAG
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson

/**
 * A simple [Fragment] subclass.
 * Use the [LibraryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LibraryFragment : Fragment() {

    // List to store the name of the activity
    var routeList: ArrayList<RouteObj> = arrayListOf()

    // Connect to the Firebase storage
    var storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_library, container, false)

        val storageRef = storage.reference
        var routeRef = storageRef.child("library")

        val routeObjList = mutableListOf<RouteObj>()

        routeRef.listAll()
            .addOnSuccessListener { listResult ->
                // Iterate over each item in the list and download its contents
                listResult.items.forEach { item ->
                        item.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                            // Convert the downloaded bytes to a String
                            val jsonString = String(bytes)

                            // Parse the JSON String into a Route object
                            val route = Gson().fromJson(jsonString, RouteObj::class.java)

                            // Add the Route object to the list
                            routeObjList.add(route)
                        }.addOnFailureListener {
                            // Handle any errors that occur while downloading the file
                            Log.e(TAG, "Failed to download route: ${item.name}", it)
                        }
                val llm = LinearLayoutManager(context)
                val view = root.findViewById<RecyclerView>(R.id.r_view)
                val adapter = RouteFrameAdapter(routeList)
                view.layoutManager = llm
                view.adapter = adapter
            }}
            .addOnFailureListener {
                Log.e(TAG, "Failed to list routes", it)
            }
        return root
    }



}