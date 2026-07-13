package com.example.features.hod.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.AppRoutes


import com.example.core.theme.CamsTextPrimary
import com.example.core.theme.CamsTextSecondary
import com.example.features.hod.widgets.HODBaseScreen

@Composable
fun HODClassAdvisorManagementScreen(onNavigate: (String) -> Unit) {
    HODBaseScreen(
        title = "Class Advisor Configuration",
        subtitle = "Assign faculty members to manage class student registers, attendance and leaves.",
        currentRoute = AppRoutes.HOD_CLASS_ADVISOR,
        onNavigate = onNavigate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEDE9FE), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text("Class Advisor Configuration", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                        Text("Assign faculty members to manage class student registers, attendance and leaves.", fontSize = 12.sp, color = CamsTextSecondary)
                    }
                }
            }

            // Data Table
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text("Active Department Classes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary)
                            Text("Manage advisor roles for current batches", fontSize = 12.sp, color = CamsTextSecondary)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("BATCH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary, modifier = Modifier.weight(1f))
                        Text("SECTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary, modifier = Modifier.weight(1f))
                        Text("ASSIGNED CLASS ADVISOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary, modifier = Modifier.weight(2f))
                        Text("ACTIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextSecondary, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
                    }

                    LazyColumn {
                        items(4) { i ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("202${6-i}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CamsTextPrimary, modifier = Modifier.weight(1f))
                                Box(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Section ${'A' + i}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6D28D9),
                                        modifier = Modifier
                                            .background(Color(0xFFEDE9FE), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Box(modifier = Modifier.weight(2f)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 16.dp)
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("-- Assign Faculty --", fontSize = 12.sp, color = CamsTextPrimary)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CamsTextSecondary)
                                    }
                                }
                                Box(modifier = Modifier.weight(1.5f), contentAlignment = Alignment.CenterEnd) {
                                    Button(
                                        onClick = { },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Save Assignment", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
