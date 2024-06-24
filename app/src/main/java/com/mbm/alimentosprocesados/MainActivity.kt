package com.mbm.alimentosprocesados

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mbm.alimentosprocesados.database.PreferencesHelper
import com.mbm.alimentosprocesados.databinding.ActivityMainBinding
import com.mbm.alimentosprocesados.viewmodels.PrototypeControlState
import com.mbm.alimentosprocesados.viewmodels.PrototypeControlViewModel
import com.mbm.alimentosprocesados.viewmodels.ServerStatus
import com.mbm.alimentosprocesados.workers.CHANNEL_ID
import com.mbm.alimentosprocesados.workers.DetectionWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private val viewModel: PrototypeControlViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        applyWindowInsets()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_NOTIFICATIONS
                )
            }
        }

        val detectionWorkRequest = PeriodicWorkRequestBuilder<DetectionWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "detectionWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            detectionWorkRequest
        )

        createNotificationChannel()

        auth = Firebase.auth
        setupNavigation()

        val loadingDialog = createLoadingDialog()
        observeViewModel(loadingDialog)

        setupTopAppBar(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        handleDestinationChanges()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_NOTIFICATIONS) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Notificaciones habilitadas", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notificaciones deshabilitadas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupNavigation() {
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
    }

    private fun createLoadingDialog(): AlertDialog {
        return MaterialAlertDialogBuilder(this)
            .setView(R.layout.dialog_progress)
            .setTitle("Conectando al prototipo")
            .setCancelable(false)
            .create()
    }

    private fun observeViewModel(loadingDialog: AlertDialog) {
        viewModel.currentStatus.observe(this) { state ->
            when (state.serverStatus) {
                ServerStatus.PROTOTYPE_STARTED -> handlePrototypeStarted(state, loadingDialog)
                ServerStatus.ERROR_WHEN_TURNING_ON_PROTOTYPE -> handleError(
                    state,
                    loadingDialog,
                    "Error al encender el prototipo"
                )

                ServerStatus.PROTOTYPE_ALREADY_RUNNING -> handlePrototypeRunning(
                    state,
                    loadingDialog
                )

                ServerStatus.PROTOTYPE_OFF -> handlePrototypeOff(state, loadingDialog)
                ServerStatus.ERROR_TURNING_OFF_PROTOTYPE -> handleError(
                    state,
                    loadingDialog,
                    "Error al apagar el prototipo"
                )

                ServerStatus.PROTOTYPE_NOT_RUNNING -> handlePrototypeNotRunning(
                    state,
                    loadingDialog
                )

                ServerStatus.PROTOTYPE_RESTARTED -> handlePrototypeRestarted(state, loadingDialog)
                ServerStatus.ERROR_CONNECTION -> handleError(
                    state,
                    loadingDialog,
                    "Problema de conexión"
                )

                ServerStatus.LOADING -> handleLoading(state, loadingDialog)
            }
        }
    }

    private fun handlePrototypeRestarted(state: PrototypeControlState, loadingDialog: AlertDialog) {
        loadingDialog.dismiss()
        PreferencesHelper(this).isPrototypeOn = true
        binding.topAppBar.menu.findItem(R.id.encender_apagar_prototipo).title =
            getString(R.string.prototype_off)
        if (!state.isShowMessage) {
            showMessage("Prototipo reiniciado", "El prototipo se ha reiniciado correctamente")
            viewModel.setIsShowMessage()
        }
    }

    private fun handlePrototypeStarted(state: PrototypeControlState, loadingDialog: AlertDialog) {
        loadingDialog.dismiss()
        PreferencesHelper(this).isPrototypeOn = true
        binding.topAppBar.menu.findItem(R.id.encender_apagar_prototipo).title =
            getString(R.string.prototype_off)
        if (!state.isShowMessage) {
            showMessage("Prototipo encendido", "El prototipo se ha encendido correctamente")
            viewModel.setIsShowMessage()
        }
    }

    private fun handlePrototypeRunning(state: PrototypeControlState, loadingDialog: AlertDialog) {
        loadingDialog.dismiss()
        PreferencesHelper(this).isPrototypeOn = true
        binding.topAppBar.menu.findItem(R.id.encender_apagar_prototipo).title =
            getString(R.string.prototype_off)
        if (!state.isShowMessage) {
            showMessage("Prototipo encendido", "El prototipo esta encendido")
            viewModel.setIsShowMessage()
        }
    }

    private fun handlePrototypeOff(state: PrototypeControlState, loadingDialog: AlertDialog) {
        loadingDialog.dismiss()
        PreferencesHelper(this).isPrototypeOn = false
        binding.topAppBar.menu.findItem(R.id.encender_apagar_prototipo).title =
            getString(R.string.prototype_on)
        if (!state.isShowMessage) {
            showMessage("Prototipo apagado", "El prototipo se encuentra apagado en este momento")
            viewModel.setIsShowMessage()
        }
    }

    private fun handlePrototypeNotRunning(
        state: PrototypeControlState,
        loadingDialog: AlertDialog
    ) {
        loadingDialog.dismiss()
        PreferencesHelper(this).isPrototypeOn = false
        binding.topAppBar.menu.findItem(R.id.encender_apagar_prototipo).title =
            getString(R.string.prototype_on)
        if (!state.isShowMessage) {
            showMessage("Prototipo apagado", "El prototipo se encuentra apagado en este momento")
            viewModel.setIsShowMessage()
        }
    }

    private fun handleError(
        state: PrototypeControlState,
        loadingDialog: AlertDialog,
        title: String
    ) {
        loadingDialog.dismiss()
        if (!state.isShowMessage) {
            MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(state.message)
                .setPositiveButton("Aceptar", null)
                .show()
            viewModel.setIsShowMessage()
        }
    }

    private fun handleLoading(state: PrototypeControlState, loadingDialog: AlertDialog) {
        loadingDialog.setTitle(state.message)
        loadingDialog.show()
    }

    private fun showMessage(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setPositiveButton("Aceptar", null)
            .setMessage(message)
            .show()
    }

    private fun handleDestinationChanges() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.monthlyFoodOctagonsReportFragment,
                R.id.monthlyDetailedNutritionalFoodReportFragment,
                R.id.monthlyReportForDiscardedProcessedFoodPackagesWithOctagonsByBrandFragment -> {
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
        binding.topAppBar.menu.findItem(R.id.encender_apagar_prototipo).title =
            if (PreferencesHelper(this).isPrototypeOn) getText(R.string.prototype_off) else getText(
                R.string.prototype_on
            )
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.cerrar_sesion -> {
                    menuItem.isEnabled = false
                    auth.signOut()
                    navController.navigate(R.id.authFragment)
                    Handler(Looper.getMainLooper()).postDelayed({
                        menuItem.isEnabled = true
                    }, 1000)
                    true
                }

                R.id.encender_apagar_prototipo -> {
                    menuItem.isEnabled = false
                    if (PreferencesHelper(this).isPrototypeOn)
                        viewModel.turnOffPrototype()
                    else {
                        viewModel.turnOnPrototype()
                    }
                    Handler(Looper.getMainLooper()).postDelayed({
                        menuItem.isEnabled = true
                    }, 1000)
                    true
                }

                R.id.reiniciar_prototipo -> {
                    menuItem.isEnabled = false
                    viewModel.restartPrototype()
                    Handler(Looper.getMainLooper()).postDelayed({
                        menuItem.isEnabled = true
                    }, 1000)
                    true
                }

                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun createNotificationChannel() {
        val name = "Low Battery Channel"
        val descriptionText = "Channel for low battery notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val REQUEST_CODE_NOTIFICATIONS = 100
    }
}