package com.dayandisli.calculator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.dayandisli.calculator.ui.screens.HelicalGearFormScreen
import com.dayandisli.calculator.ui.screens.HelicalGearReceiptScreen
import com.dayandisli.calculator.ui.screens.HelicalGearViewModel
import com.dayandisli.calculator.ui.screens.HomeScreen
import com.dayandisli.calculator.ui.screens.MachineDetailScreen
import com.dayandisli.calculator.ui.screens.MachinesScreen
import com.dayandisli.calculator.ui.screens.ReishauerFormScreen
import com.dayandisli.calculator.ui.screens.ReishauerReceiptScreen
import com.dayandisli.calculator.ui.screens.ReishauerViewModel
import com.dayandisli.calculator.ui.screens.SpurGearFormScreen
import com.dayandisli.calculator.ui.screens.SpurGearReceiptScreen
import com.dayandisli.calculator.ui.screens.SpurGearViewModel
import com.dayandisli.calculator.ui.screens.WeightScreen
import androidx.lifecycle.viewmodel.compose.viewModel

private val machineIdArg: List<NamedNavArgument> = listOf(navArgument("machineId") { type = NavType.StringType })

@Composable
fun CalculatorNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Destinations.HOME) {
        composable(Destinations.HOME) {
            HomeScreen(onNavigate = { route -> navController.navigate(route) })
        }

        composable(Destinations.MACHINES) {
            MachinesScreen(
                onMachineSelected = { machineId ->
                    navController.navigate(Destinations.machineDetail(machineId))
                },
            )
        }

        composable(Destinations.MACHINE_DETAIL, arguments = machineIdArg) { entry ->
            val machineId = entry.arguments?.getString("machineId").orEmpty()
            MachineDetailScreen(
                machineId = machineId,
                onSpur = { navController.navigate(if (machineId in setOf(Destinations.REISHAUER_MACHINE_ID, "azdirma-genel", "gleason")) Destinations.reishauerSpurForm(machineId) else Destinations.spurForm(machineId)) },
                onHelical = {
                    if (machineId in setOf(Destinations.REISHAUER_MACHINE_ID, "azdirma-genel", "gleason")) {
                        navController.navigate(Destinations.reishauerForm(machineId))
                    } else {
                        navController.navigate(Destinations.helicalForm(machineId))
                    }
                },
                spurAvailable = true,
            )
        }

        composable(Destinations.WEIGHT) {
            WeightScreen()
        }

        // Spur flow: form and receipt share one SpurGearViewModel scoped to this nested graph's
        // own back stack entry, so the result survives navigating to the receipt and back
        // without re-serializing it through nav arguments.
        navigation(startDestination = Destinations.SPUR_FORM, route = Destinations.SPUR_FLOW, arguments = machineIdArg) {
            composable(Destinations.SPUR_FORM) { entry ->
                val machineId = entry.arguments?.getString("machineId").orEmpty()
                val parentEntry = remember(entry, navController) { navController.getBackStackEntry(Destinations.SPUR_FLOW) }
                val vm: SpurGearViewModel = viewModel(parentEntry)
                SpurGearFormScreen(
                    machineId = machineId,
                    viewModel = vm,
                    onViewReceipt = { navController.navigate(Destinations.spurReceipt(machineId)) },
                )
            }
            composable(Destinations.SPUR_RECEIPT) { entry ->
                val machineId = entry.arguments?.getString("machineId").orEmpty()
                val parentEntry = remember(entry, navController) { navController.getBackStackEntry(Destinations.SPUR_FLOW) }
                val vm: SpurGearViewModel = viewModel(parentEntry)
                SpurGearReceiptScreen(machineId = machineId, viewModel = vm)
            }
        }

        navigation(startDestination = Destinations.HELICAL_FORM, route = Destinations.HELICAL_FLOW, arguments = machineIdArg) {
            composable(Destinations.HELICAL_FORM) { entry ->
                val machineId = entry.arguments?.getString("machineId").orEmpty()
                val parentEntry = remember(entry, navController) { navController.getBackStackEntry(Destinations.HELICAL_FLOW) }
                val vm: HelicalGearViewModel = viewModel(parentEntry)
                HelicalGearFormScreen(
                    machineId = machineId,
                    viewModel = vm,
                    onViewReceipt = { navController.navigate(Destinations.helicalReceipt(machineId)) },
                )
            }
            composable(Destinations.HELICAL_RECEIPT) { entry ->
                val machineId = entry.arguments?.getString("machineId").orEmpty()
                val parentEntry = remember(entry, navController) { navController.getBackStackEntry(Destinations.HELICAL_FLOW) }
                val vm: HelicalGearViewModel = viewModel(parentEntry)
                HelicalGearReceiptScreen(machineId = machineId, viewModel = vm)
            }
        }

        navigation(startDestination = Destinations.REISHAUER_FORM, route = Destinations.REISHAUER_FLOW, arguments = machineIdArg) {
            composable(Destinations.REISHAUER_FORM) { entry ->
                val machineId = entry.arguments?.getString("machineId").orEmpty()
                val parentEntry = remember(entry, navController) { navController.getBackStackEntry(Destinations.REISHAUER_FLOW) }
                val vm: ReishauerViewModel = viewModel(parentEntry)
                ReishauerFormScreen(
                    machineId = machineId,
                    viewModel = vm,
                    onViewReceipt = { navController.navigate(Destinations.reishauerReceipt(machineId)) },
                )
            }
            composable(Destinations.REISHAUER_RECEIPT) { entry ->
                val machineId = entry.arguments?.getString("machineId").orEmpty()
                val parentEntry = remember(entry, navController) { navController.getBackStackEntry(Destinations.REISHAUER_FLOW) }
                val vm: ReishauerViewModel = viewModel(parentEntry)
                ReishauerReceiptScreen(machineId = machineId, viewModel = vm)
            }
        }

        navigation(startDestination = Destinations.REISHAUER_SPUR_FORM, route = Destinations.REISHAUER_SPUR_FLOW, arguments = machineIdArg) {
            composable(Destinations.REISHAUER_SPUR_FORM) { entry ->
                val machineId = entry.arguments?.getString("machineId").orEmpty()
                val parentEntry = remember(entry, navController) { navController.getBackStackEntry(Destinations.REISHAUER_SPUR_FLOW) }
                val vm: ReishauerViewModel = viewModel(parentEntry)
                ReishauerFormScreen(machineId, vm, { navController.navigate(Destinations.reishauerSpurReceipt(machineId)) }, isSpur = true)
            }
            composable(Destinations.REISHAUER_SPUR_RECEIPT) { entry ->
                val machineId = entry.arguments?.getString("machineId").orEmpty()
                val parentEntry = remember(entry, navController) { navController.getBackStackEntry(Destinations.REISHAUER_SPUR_FLOW) }
                val vm: ReishauerViewModel = viewModel(parentEntry)
                ReishauerReceiptScreen(machineId, vm, isSpur = true)
            }
        }
    }
}
