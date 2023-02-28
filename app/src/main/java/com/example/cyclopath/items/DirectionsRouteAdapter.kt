package com.example.cyclopath.items

import android.util.JsonReader
import android.util.JsonWriter
import com.google.gson.TypeAdapter
import com.mapbox.api.directions.v5.models.DirectionsRoute

// Define a custom TypeAdapter for DirectionsRoute
class DirectionsRouteAdapter : TypeAdapter<DirectionsRoute>() {
    override fun write(out: com.google.gson.stream.JsonWriter?, value: DirectionsRoute?) {
        // Not needed for parsing
    }

    override fun read(`in`: com.google.gson.stream.JsonReader?): DirectionsRoute? {
        val route = DirectionsRoute.fromJson(`in`.toString())
        // Add any additional logic to parse the DirectionsRoute object here
        return route
    }
}