package com.alanbadrain.invoicemini.ui.screens

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.alanbadrain.invoicemini.ui.components.SearchSelection
import com.alanbadrain.invoicemini.util.InternationalOptions
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alanbadrain.invoicemini.InvoiceViewModel
import com.alanbadrain.invoicemini.data.BusinessProfileEntity
import java.io.File
import java.io.FileOutputStream

@Composable
fun BusinessProfileScreen(viewModel: InvoiceViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val profile by viewModel.businessProfile.collectAsState()

    var businessName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("") }
    var callingCode by remember { mutableStateOf("") }
    var taxNumber by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var logoPath by remember { mutableStateOf<String?>(null) }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val stored = viewModel.getBusinessProfileOnce()
        stored?.let {
            businessName = it.businessName
            email = it.email ?: ""
            address = it.address ?: ""
            phone = it.phone ?: ""
            logoPath = it.logoUri
            countryCode = it.countryCode ?: ""
            callingCode = it.callingCode ?: ""
            taxNumber = it.taxNumber ?: ""
        }
        loaded = true
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                val destFile = File(context.filesDir, "business_logo.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
                logoPath = destFile.absolutePath
            } catch (e: Exception) {
                // ignore; logoPath stays unchanged
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("Business Profile", style = MaterialTheme.typography.titleLarge)
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))

        Text("Logo", style = MaterialTheme.typography.titleMedium)
        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))

        if (logoPath != null) {
            val bitmap = remember(logoPath) { BitmapFactory.decodeFile(logoPath) }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Business logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(60.dp).clip(RoundedCornerShape(8.dp))
                )
            }
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            OutlinedButton(onClick = { logoPath = null }) { Text("Remove Logo") }
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        }

        OutlinedButton(onClick = {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Icon(Icons.Filled.Image, contentDescription = null)
            androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
            Text(if (logoPath == null) "Add Logo" else "Change Logo")
        }

        androidx.compose.foundation.layout.Spacer(Modifier.size(20.dp))

        OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth())
        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
        SearchSelection("Country", countryCode, InternationalOptions.countries) {
            countryCode = it
            callingCode = InternationalOptions.callingCodes[it] ?: ""
        }
        OutlinedTextField(callingCode, { callingCode = it }, label = { Text("Calling Code") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(taxNumber, { taxNumber = it }, label = { Text("Tax Registration Number (optional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())

        androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))

        savedMessage?.let { Text(it, color = androidx.compose.ui.graphics.Color(0xFF2E9E4A)) }

        Button(enabled = loaded, onClick = {
            viewModel.saveBusinessProfile(
                BusinessProfileEntity(
                    countryCode = countryCode.ifBlank { null },
                    callingCode = callingCode.ifBlank { null },
                    taxNumber = taxNumber.ifBlank { null },
                    id = 1,
                    businessName = businessName,
                    email = email.ifBlank { null },
                    address = address.ifBlank { null },
                    phone = phone.ifBlank { null },
                    logoUri = logoPath
                )
            )
            savedMessage = "Saved"
        }) {
            Text("Save")
        }
    }
}
