package com.example.features.parent.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.parent.providers.ContactCollegeViewModel
import com.example.features.parent.widgets.ParentDrawer
import kotlinx.coroutines.launch

@Composable
fun ContactCollegeScreen(viewModel: ContactCollegeViewModel, onNavigate: (String) -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(uiState.successMsg) {
        if (uiState.successMsg != null) {
            name = ""; email = ""; subject = ""; message = ""
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ParentDrawer(
                currentRoute = "/parent/contact",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        CamsScreen(
            title = "Contact College",
            subtitle = "Support & Institutional Communications",
            navigationIcon = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.White)
                }
            },
            actions = {
                IconButton(onClick = { onNavigate("LOGOUT") }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                }
            },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Emergency Contacts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            fun iconForRole(role: String): ImageVector = when (role) {
                "Principal" -> Icons.Filled.Person
                "Registrar" -> Icons.Filled.Badge
                "Exam Cell" -> Icons.Filled.Description
                "Admissions" -> Icons.Filled.School
                else -> Icons.Filled.Phone
            }

            BoxWithConstraints {
                val isTablet = maxWidth > 600.dp
                if (isTablet) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        uiState.contacts.forEach { contact ->
                            ContactSmallCard(contact.role, contact.phone, iconForRole(contact.role), Modifier.weight(1f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.contacts.chunked(2).forEach { rowContacts ->
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                rowContacts.forEach { contact ->
                                    ContactSmallCard(contact.role, contact.phone, iconForRole(contact.role), Modifier.weight(1f))
                                }
                                if (rowContacts.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Text("Submit an Inquiry", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            
            CamsCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.submitInquiry(name, email, subject, message)
                        },
                        enabled = !uiState.isSubmitting && name.isNotBlank() && email.isNotBlank() && subject.isNotBlank() && message.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (uiState.isSubmitting) "Sending..." else "Send Message", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("College Location", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            CamsCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(color = CamsNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(uiState.campusName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Text(uiState.campusAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }

    if (uiState.successMsg != null || uiState.errorMsg != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearMessages() },
            title = { Text(if (uiState.errorMsg != null) "Error" else "Success") },
            text = { Text(uiState.errorMsg ?: uiState.successMsg ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearMessages() }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun ContactSmallCard(role: String, phone: String, icon: ImageVector, modifier: Modifier) {
    val context = androidx.compose.ui.platform.LocalContext.current
    CamsCard(
        modifier = modifier,
        onClick = {
            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:$phone")
            }
            context.startActivity(intent)
        }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(40.dp).background(CamsNavy.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(role, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(phone, fontSize = 13.sp, color = CamsNavy, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}
