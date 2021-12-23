package com.srmstudios.commentsold.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.databinding.ActivityMainBinding
import com.srmstudios.commentsold.util.CommentSoldPrefsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    @Inject
    lateinit var commentSoldPrefsManager: CommentSoldPrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController

        setupViews()
        setupListeners()
    }

    private fun setupViews() {

        // Top Level Destinations in the Nav Graph
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment,
                R.id.productsFragment,
                R.id.inventoryFragment
            )
        )

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.bottomNavigationView,navController)
    }

    private fun setupListeners() {
        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.splashFragment -> {
                    // no need to show the top bar on the Splash Screen
                    showHideTopBar(false)
                    showHideBottomNavigation(false)
                }
                R.id.loginFragment -> {
                    showHideTopBar(true)
                    showHideBottomNavigation(false)
                }
                R.id.productsFragment,R.id.inventoryFragment -> {
                    // Bug in Navigation Component
                    // In bottom navigation view, navigation component creates new fragments on each selection
                    // and pushes to the NavHostFragment if none of the fragment is a Start Destination
                    // So we change the Start Destination here from Splash Fragment to Products Fragment
                    // to stop the creation of new fragments and use the existing ones in the back stack
                    navController.graph.setStartDestination(R.id.productsFragment)

                    showHideTopBar(true)
                    showHideBottomNavigation(true)
                }
                else -> {
                    showHideTopBar(true)
                    showHideBottomNavigation(true)
                }
            }
        }
    }

    private fun showHideTopBar(show: Boolean){
        if(show){
            supportActionBar?.show()
        }else{
            supportActionBar?.hide()
        }
    }

    private fun showHideBottomNavigation(show: Boolean){
        binding.bottomNavigationView.isVisible = show
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}