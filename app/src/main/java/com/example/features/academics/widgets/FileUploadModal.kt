package com.example.features.academics.widgets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.core.theme.LexNovaPurple

@Composable
fun FileUploadModal(
    onDismiss: () -> Unit,
    onUpload: (Uri, String) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedUri = uri
        fileName = uri?.lastPathSegment ?: "document.pdf"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudUpload,
                    contentDescription = null,
                    tint = LexNovaPurple,
                    modifier = Modifier.size(48.dp)
                )
                
                Text(
                    text = "Upload Assignment",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                
                if (selectedUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Filled.InsertDriveFile, contentDescription = null, tint = Color(0xFF64748B))
                        Text(text = fileName ?: "File selected", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
                
                Button(
                    onClick = { launcher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B).copy(alpha = 0.2f), contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (selectedUri == null) "Select File" else "Change File")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            selectedUri?.let { onUpload(it, fileName ?: "document.pdf") }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedUri != null,
                        colors = ButtonDefaults.buttonColors(containerColor = LexNovaPurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}
