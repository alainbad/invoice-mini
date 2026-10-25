package com.alanbadrain.invoicemini

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alanbadrain.invoicemini.ui.screens.BusinessProfileScreen
import com.alanbadrain.invoicemini.ui.screens.HomeScreen
import com.alanbadrain.invoicemini.ui.screens.InvoiceFormScreen
import com.alanbadrain.invoicemini.ui.screens.InvoiceListScreen
import com.alanbadrain.invoicemini.ui.screens.InvoicePreviewScreen

@Composable
fun InvoiceMiniNavHost(viewModel: InvoiceViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onCreateInvoice = { navController.navigate("invoice_form") },
                onOpenInvoice = { id -> navController.navigate("invoice_form?invoiceId=$id") },
                onOpenList = { navController.navigate("invoice_list") },
                onOpenProfile = { navController.navigate("business_profile") }
            )
        }
        composable(
            route = "invoice_form?invoiceId={invoiceId}",
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId")
            InvoiceFormScreen(
                invoiceId = invoiceId,
                viewModel = viewModel,
                onPreview = { id -> navController.navigate("invoice_preview/$id") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "invoice_preview/{invoiceId}",
            arguments = listOf(navArgument("invoiceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getString("invoiceId") ?: return@composable
            InvoicePreviewScreen(
                invoiceId = invoiceId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("invoice_list") {
            InvoiceListScreen(
                viewModel = viewModel,
                onOpenInvoice = { id -> navController.navigate("invoice_form?invoiceId=$id") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("business_profile") {
            BusinessProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}