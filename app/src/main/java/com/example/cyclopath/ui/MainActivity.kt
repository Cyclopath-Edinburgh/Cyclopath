package com.example.cyclopath.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.cyclopath.R
import com.example.cyclopath.databinding.ActivityMainBinding
import com.example.cyclopath.ui.library.Testa
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var recordButton : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        Mapbox.getInstance(this, "sk.eyJ1Ijoia2FuZ2NoZW5neXU1MjkiLCJhIjoiY2xleDZmbnJjMmg2NTNzcnY0b2YzdHR2dSJ9.L5DcbA65cohPI3X1jHRptQ")
        Mapbox.getInstance(this,R.string.matoken.toString())
//        if (Mapbox.getAccessToken() == null) {
//            // Mapbox instance has not been initialized
//            println("kkkkkkkkkkk")
//        } else {
//            // Mapbox instance has been initialized
//            println("ppppppppppp")
//        }


        setContentView(R.layout.activity_main)

//        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
//        supportActionBar?.setDisplayShowCustomEnabled(true)
//        supportActionBar?.setCustomView(R.layout.action_bar)



        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment?
        val navController: NavController
        navController = navHostFragment?.navController
                ?: findNavController(this, R.id.nav_host_fragment_activity_main)
        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavigationUI.setupWithNavController(navView, navController)

        val bottom_nav = findViewById<BottomNavigationView>(R.id.nav_view)
        bottom_nav.itemIconTintList = null


//        recordButton = findViewById<FloatingActionButton>(R.id.main_centralButton)
//        recordButton.setOnClickListener {
//            val intent = Intent(this, RecordActivity::class.java)
//            startActivity(intent)
//        }


        val southwest = LatLng(55.942617, -3.361678)
        val northeast = LatLng(55.985612, -3.176283)
        val edinburghBounds = LatLngBounds.Builder().include(southwest).include(northeast).build()


        val options = com.mapbox.mapboxsdk.snapshotter.MapSnapshotter.Options(500, 500)
        options.withRegion(edinburghBounds)
//
////
//        val mapSnapshotter = com.mapbox.mapboxsdk.snapshotter.MapSnapshotter(this,options)
//


    }
}