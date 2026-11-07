package com.mahin.azu

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mahin.azu.data.receiver.PackageChangeReceiver
import com.mahin.azu.ui.screens.AppDrawerScreen
import com.mahin.azu.ui.screens.HomeScreen
import com.mahin.azu.ui.theme.AzuTheme
import com.mahin.azu.ui.viewmodel.LauncherViewModel

/**
 * Single-activity launcher. Navigation: home -> drawer.
 * Set as the default launcher via the HOME intent filter in the manifest.
 */
class MainActivity : ComponentActivity() {

    private val packageReceiver = PackageChangeReceiver()
    private lateinit var launcherViewModel: LauncherViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as AzuApp

        launcherViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LauncherViewModel(app.appRepository) as T
        })[LauncherViewModel::class.java]

        val packageFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(packageReceiver, packageFilter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(packageReceiver, packageFilter)
        }

        setContent {
            AzuTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                viewModel = launcherViewModel,
                                onOpenDrawer = { navController.navigate("drawer") },
                            )
                        }
                        composable("drawer") {
                            AppDrawerScreen(
                                viewModel = launcherViewModel,
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        try { unregisterReceiver(packageReceiver) } catch (_: IllegalArgumentException) { /* already unregistered */ }
        super.onDestroy()
    }
}
