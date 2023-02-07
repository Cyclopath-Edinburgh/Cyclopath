package com.example.cyclopath

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.example.cyclopath.databinding.ActivityMainBinding
import com.example.cyclopath.ui.record.RecordActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var recordButton : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        recordButton = findViewById<FloatingActionButton>(R.id.main_centralButton)
        recordButton.setOnClickListener {
            val intent = Intent(this, RecordActivity::class.java)
            startActivity(intent)
        }

    }
}