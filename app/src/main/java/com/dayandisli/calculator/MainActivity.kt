package com.dayandisli.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dayandisli.calculator.ui.components.AppScaffold
import com.dayandisli.calculator.ui.navigation.CalculatorNavHost
import com.dayandisli.calculator.ui.navigation.Destinations
import com.dayandisli.calculator.ui.theme.DayanCalculatorTheme

/** Single-activity host for the DAYAN calculator: opens directly to the calculator home,
 * fully offline, with native Compose navigation (no WebView, no ERP chrome). */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DayanCalculatorTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                AppScaffold(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        // The shared primary navigation always returns to its root before opening
                        // a destination. This also exits any nested Spur, Helical, or Reishauer
                        // flow, so the sidebar behaves identically on every screen.
                        navController.popBackStack(Destinations.HOME, inclusive = false)
                        if (route != Destinations.HOME) {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    },
                ) { modifier ->
                    CalculatorNavHostContainer(modifier, navController)
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun CalculatorNavHostContainer(modifier: Modifier, navController: androidx.navigation.NavHostController) {
    androidx.compose.foundation.layout.Box(modifier = modifier.fillMaxSize()) {
        CalculatorNavHost(navController = navController)
    }
}
