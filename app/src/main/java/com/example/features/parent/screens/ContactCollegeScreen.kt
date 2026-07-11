package com.example.features.parent.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.core.ui.CamsCard
import com.example.core.ui.CamsScreen
import com.example.features.parent.widgets.ParentDrawer
import kotlinx.coroutines.launch

@Composable
fun ContactCollegeScreen(onNavigate: (String) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

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
                    Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                }
            },
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Emergency Contacts", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ContactSmallCard("Principal", "+91 98765 43210", Icons.Filled.Person, Modifier.weight(1f))
                ContactSmallCard("Registrar", "+91 98765 01234", Icons.Filled.Badge, Modifier.weight(1f))
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ContactSmallCard("Exam Cell", "+91 98765 99999", Icons.Filled.Description, Modifier.weight(1f))
                ContactSmallCard("Admissions", "+91 98765 88888", Icons.Filled.School, Modifier.weight(1f))
            }

            Text("Submit an Inquiry", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
            
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
                        onClick = { /* Submit */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CamsNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Send Message", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text("College Location", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
            CamsCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(color = CamsNavy.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.LocationOn, null, tint = CamsNavy)
                        }
                    }
                    Column {
                        Text("LexNova University Campus", fontWeight = FontWeight.Bold, color = CamsTextPrimary)
                        Text("Sector 44, Academic District, New Delhi - 110001", fontSize = 12.sp, color = CamsTextSecondary)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ContactSmallCard(role: String, phone: String, icon: ImageVector, modifier: Modifier) {
    CamsCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(40.dp).background(CamsNavy.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = CamsNavy, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(role, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
            Text(phone, fontSize = 13.sp, color = CamsNavy, fontWeight = FontWeight.Medium)
        }
    }
}
