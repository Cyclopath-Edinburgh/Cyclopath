package com.example.cyclopath.ui.library

import RouteObj
import android.util.Log
import com.facebook.AccessTokenManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.mapbox.api.directions.v5.models.DirectionsRoute
import java.lang.reflect.Type

class getRouteAdv {
    val routeObjList = mutableListOf<RouteObj>()
    // List to store the name of the activity

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
                        Log.e(AccessTokenManager.TAG, "Failed to download route: ${item.name}", it)
                    }
                }
                println("111")
                println("success")
            }
            .addOnFailureListener {
                Log.e(AccessTokenManager.TAG, "Failed to list routes", it)
            }
        return routeObjList

    }
}