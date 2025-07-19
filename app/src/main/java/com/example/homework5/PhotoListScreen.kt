package com.example.homework5

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun PhotoListScreen() {
    val context = LocalContext.current
    val db = remember { DatabaseHelper(context) }
    var photos by remember { mutableStateOf(db.getAll()) }
    var showDialog by remember { mutableStateOf(false) }
    var pickedImage by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pickedImage = uriToBitmap(context, it)
            showDialog = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) imagePickerLauncher.launch("image/*")
    }

    Column {
        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    imagePickerLauncher.launch("image/*")
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Get Started")
        }

        LazyColumn {
            itemsIndexed(photos) { index, photo ->
                Box(modifier = Modifier.pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        if (dragAmount > 50) {
                            db.delete(photo.id)
                            photos = db.getAll()
                        }
                    }
                }) {
                    PhotoCard(photo)
                }
            }
        }
    }

    if (showDialog && pickedImage != null) {
        PhotoDialog(
            bitmap = pickedImage!!,
            onSave = { title, desc ->
                db.insert(PhotoEntity(image = pickedImage!!, title = title, description = desc))
                photos = db.getAll()
            },
            onDismiss = {
                showDialog = false
            }
        )
    }
}
