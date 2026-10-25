package com.alanbadrain.invoicemini.ui.components

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

@Composable
fun SearchSelection(title: String, value: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var isOpen by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    val selectedLabel = options.firstOrNull { it.first == value }?.second ?: value.ifBlank { "Select" }
    OutlinedButton(onClick = { search = ""; isOpen = true }, modifier = Modifier.fillMaxWidth()) {
        Text("$title: $selectedLabel ▾")
    }
    if (isOpen) AlertDialog(
        onDismissRequest = { isOpen = false }, title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(search, { search = it }, label = { Text("Search") })
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(options.filter { it.first.contains(search, true) || it.second.contains(search, true) }, key = { it.first }) { option ->
                        TextButton(onClick = { onSelect(option.first); isOpen = false }) { Text(option.second) }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { isOpen = false }) { Text("Close") } }
    )
}

@Composable
fun InvoiceImageField(title: String, path: String, onChange: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            try {
                val saved = withContext(Dispatchers.IO) {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                    require(bounds.outWidth > 0 && bounds.outHeight > 0)
                    var sample = 1
                    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1600) sample *= 2
                    val bitmap = context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
                    } ?: throw IllegalArgumentException("Invalid image")
                    val directory = File(context.filesDir, "invoice_images").apply { mkdirs() }
                    val output = File(directory, "${UUID.randomUUID()}.png")
                    try {
                        output.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                    } finally { bitmap.recycle() }
                    output.absolutePath
                }
                onChange(saved); error = null
            } catch (e: Exception) { error = "Unable to load this image. Please select another image." }
            finally { busy = false }
        }
    }
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        val bitmap = remember(path) { if (path.isBlank()) null else BitmapFactory.decodeFile(path) }
        if (bitmap != null) Image(bitmap.asImageBitmap(), contentDescription = title, modifier = Modifier.height(100.dp))
        Row {
            OutlinedButton(enabled = !busy, onClick = { picker.launch("image/*") }) { Text(if (path.isBlank()) "Upload" else "Replace") }
            if (path.isNotBlank()) TextButton(enabled = !busy, onClick = { onChange("") }) { Text("Remove") }
        }
        if (busy) Text("Loading image…")
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
