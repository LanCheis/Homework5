package com.homework.photoapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.homework.photoapp.database.DatabaseHelper
import com.homework.photoapp.database.PhotoEntity
import com.homework.photoapp.ui.theme.PhotoAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper = DatabaseHelper(this)

        setContent {
            PhotoAppTheme {
                PhotoApp(databaseHelper)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoApp(databaseHelper: DatabaseHelper) {
    var photos by remember { mutableStateOf(listOf<PhotoEntity>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var currentPhoto by remember { mutableStateOf<PhotoEntity?>(null) }
    var photoTitle by remember { mutableStateOf("") }
    var photoDescription by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Load photos from database
    LaunchedEffect(Unit) {
        photos = databaseHelper.getAllPhotos()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denied
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            currentPhoto = PhotoEntity(uri = it.toString())
            photoTitle = ""
            photoDescription = ""
            showAddDialog = true
        }
    }

    fun checkAndRequestPermission() {
        when (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )) {
            PackageManager.PERMISSION_GRANTED -> {
                imagePickerLauncher.launch("image/*")
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    // Main UI matching PDF design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header bar with purple background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF9C27B0))
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "Photo Gallery",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            if (photos.isEmpty()) {
                // Empty state with icon
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Phone with photo icon (placeholder)
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Photo",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "No photos yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    Text(
                        text = "Tap + to add your first photo",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Photo list - scrollable
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(photos) { photo ->
                        PhotoCard(
                            photo = photo,
                            onEdit = {
                                currentPhoto = photo
                                photoTitle = photo.title
                                photoDescription = photo.description
                                showAddDialog = true
                            },
                            onDelete = {
                                databaseHelper.deletePhoto(photo.id)
                                photos = databaseHelper.getAllPhotos()
                            }
                        )
                    }
                }
            }
        }

        // Purple floating action button
        FloatingActionButton(
            onClick = { checkAndRequestPermission() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            containerColor = Color(0xFF9C27B0)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add Photo",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }

    // Add/Edit Photo Dialog
    if (showAddDialog && currentPhoto != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            modifier = Modifier.fillMaxWidth(0.9f),
            title = {
                Text(
                    text = "Add Photo Details",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Photo preview
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(currentPhoto!!.uri)),
                        contentDescription = "Photo preview",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title input
                    OutlinedTextField(
                        value = photoTitle,
                        onValueChange = { photoTitle = it },
                        label = { Text("Add Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description input
                    OutlinedTextField(
                        value = photoDescription,
                        onValueChange = { photoDescription = it },
                        label = { Text("Add Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val photoToSave = currentPhoto!!.copy(
                            title = photoTitle,
                            description = photoDescription
                        )

                        if (currentPhoto!!.id == 0L) {
                            // New photo
                            databaseHelper.insertPhoto(photoToSave)
                        } else {
                            // Update existing photo
                            databaseHelper.updatePhoto(photoToSave)
                        }

                        photos = databaseHelper.getAllPhotos()
                        showAddDialog = false
                        currentPhoto = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        currentPhoto = null
                    }
                ) {
                    Text("Cancel", color = Color(0xFF9C27B0))
                }
            }
        )
    }
}

@Composable
fun PhotoCard(
    photo: PhotoEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF9C27B0)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo thumbnail
            Image(
                painter = rememberAsyncImagePainter(Uri.parse(photo.uri)),
                contentDescription = photo.title,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Photo info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = photo.title.ifEmpty { "Untitled" },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = photo.description.ifEmpty { "No description" },
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    maxLines = 2
                )
            }

            // Edit button
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Add, // Using Add icon as edit placeholder
                    contentDescription = "Edit",
                    tint = Color.White
                )
            }
        }
    }
}
