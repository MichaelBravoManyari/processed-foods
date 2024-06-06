package com.mbm.alimentosprocesados

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mbm.alimentosprocesados.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.monthlyFoodOctagonsReportFragment,
                R.id.monthlyDetailedNutritionalFoodReportFragment,
                R.id.monthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment
            ), binding.main
        )

        setupTopAppBar(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.monthlyFoodOctagonsReportFragment, R.id.monthlyDetailedNutritionalFoodReportFragment, R.id.monthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment -> {
                    binding.appBarLayout.visibility = View.VISIBLE
                }

                else -> {
                    binding.appBarLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun setupTopAppBar(
        navController: NavController,
        appBarConfiguration: AppBarConfiguration
    ) {
        binding.topAppBar.setupWithNavController(navController, appBarConfiguration)
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.cerrar_sesion -> {
                    auth.signOut()
                    navController.navigate(R.id.action_monthlyFoodOctagonsReportFragment_to_authFragment)
                    true
                }

                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}