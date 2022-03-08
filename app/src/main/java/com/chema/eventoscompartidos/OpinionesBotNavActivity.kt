package com.chema.eventoscompartidos

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.chema.eventoscompartidos.databinding.ActivityOpinionesBotNavBinding
import com.chema.eventoscompartidos.utils.VariablesCompartidas

class OpinionesBotNavActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpinionesBotNavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOpinionesBotNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_opiniones_bot_nav)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_fotos, R.id.navigation_dashboard, R.id.navigation_arrives
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }



    override fun onBackPressed() {
        super.onBackPressed()
        VariablesCompartidas.checkMode = false
    }
}