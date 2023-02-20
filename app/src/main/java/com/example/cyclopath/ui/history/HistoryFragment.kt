package com.example.cyclopath.ui.history

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cyclopath.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2

/**
 * A simple [Fragment] subclass.
 * Use the [HistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HistoryFragment : Fragment() {

    // List to store the name of the activity
    var historyList: ArrayList<String> = arrayListOf()

    // Connect to the Firebase storage
    var storage = Firebase.storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val root = inflater.inflate(R.layout.fragment_history, container, false)

        var sp = context?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val name = sp!!.getString("username", "user")

        val storageRef = storage.reference
        var dataRef = storageRef.child("history/$name")

        dataRef.listAll()
                .addOnSuccessListener { (items, prefixes) ->
                    items.forEach { item ->
                        if (item.name.endsWith(".geojson")) {
                            historyList.add(item.name.dropLast(8))
                        }
                    }
                    val llm = LinearLayoutManager(context)
                    val view = root.findViewById<RecyclerView>(R.id.h_view)
                    val adapter = HistoryAdapter(historyList)
                    view.layoutManager = llm
                    view.adapter = adapter
                }
                .addOnFailureListener {
                }


        // Retrieve the historic data from the Firebase storage
//        dataRef.getBytes(ONE_MEGABYTE).addOnSuccessListener { it ->
//            val data = String(it).lines()
//            data.forEach {
//                if (it != "") {
//                    val strs = it.split(",").toTypedArray()
//                    timeList.add(strs[0])
//                    activityList.add(strs[1])
//                }
//            }
//            timeList.reverse()
//            activityList.reverse()
//            val llm = LinearLayoutManager(applicationContext)
//            val view = findViewById<RecyclerView>(R.id.hd_view)
//            val adapter = ListAdapter(timeList, activityList)
//            view.layoutManager = llm
//            view.adapter = adapter
//        }.addOnFailureListener {
//
//        }
        return root
    }
}