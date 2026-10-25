package com.alanbadrain.invoicemini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alanbadrain.invoicemini.data.InvoiceMiniApplication
import com.alanbadrain.invoicemini.ui.theme.InvoiceMiniTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as InvoiceMiniApplication
        setContent {
            InvoiceMiniTheme {
                Surface(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    val viewModel: InvoiceViewModel = viewModel(
                        factory = InvoiceViewModel.Factory(app.repository)
                    )
                    InvoiceMiniNavHost(viewModel = viewModel)
                }
            }
        }
    }
}