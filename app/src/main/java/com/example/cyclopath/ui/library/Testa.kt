package com.example.cyclopath.ui.library

import RouteObj
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class Testa {

    fun uploadObj() {
        val newRoute = RouteObj()

        newRoute.route_name_text = "New Route1"
        newRoute.route_length_text = "20 km"
        newRoute.route_elevation_text = "2000 m"
        newRoute.route_popularity_text = "Moderate"

        val storageRef = Firebase.storage.reference
        val dataRef = storageRef.child("TestObj/sad")
        val baos = ByteArrayOutputStream()

        val d = newRoute.route_name_text
        val s = newRoute.route_elevation_text
        val c = newRoute.route_length_text
        val g= newRoute.route_popularity_text
        baos.write("$newRoute".toByteArray())
        val data = baos.toByteArray()
        dataRef.putBytes(data)
    }
}

