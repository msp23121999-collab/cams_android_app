package com.example.features.faculty.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
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
import com.example.features.faculty.widgets.FacultyBaseScreen

@Composable
fun FacultySmartClassroomScreen(
    onNavigate: (String) -> Unit
) {
    var acStatus by remember { mutableStateOf(true) }
    var lightsStatus by remember { mutableStateOf(true) }
    var projectorStatus by remember { mutableStateOf(false) }
    var speakerStatus by remember { mutableStateOf(false) }
    
    var temperature by remember { mutableStateOf(24f) }

    FacultyBaseScreen(
        title = "Smart Classroom",
        subtitle = "Classroom IoT Control Center - Room 402",
        currentRoute = "/faculty/smart-classroom",
        onNavigate = onNavigate
    ) {
        // 1. Environmental Status
        CamsCard {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnvironmentStat("Temperature", "${temperature.toInt()}°C", Icons.Filled.Thermostat, Color(0xFFF97316))
                EnvironmentStat("Humidity", "45%", Icons.Filled.WaterDrop, Color(0xFF3B82F6))
                EnvironmentStat("Occupancy", "42/60", Icons.Filled.Groups, Color(0xFF10B981))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Control Grid
        Text("Device Controls", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ControlTile(
                "Main Lights", 
                Icons.Filled.Lightbulb, 
                lightsStatus, 
                modifier = Modifier.weight(1f)
            ) { lightsStatus = !lightsStatus }
            
            ControlTile(
                "Air Conditioner", 
                Icons.Filled.AcUnit, 
                acStatus, 
                modifier = Modifier.weight(1f)
            ) { acStatus = !acStatus }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ControlTile(
                "Projector", 
                Icons.Filled.SettingsInputComponent, 
                projectorStatus, 
                modifier = Modifier.weight(1f)
            ) { projectorStatus = !projectorStatus }
            
            ControlTile(
                "Audio System", 
                Icons.Filled.Speaker, 
                speakerStatus, 
                modifier = Modifier.weight(1f)
            ) { speakerStatus = !speakerStatus }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Temperature Slider
        if (acStatus) {
            CamsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AC Temperature", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("${temperature.toInt()}°C", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = CamsNavy)
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 16f..30f,
                        colors = SliderDefaults.colors(
                            thumbColor = CamsNavy,
                            activeTrackColor = CamsNavy,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun EnvironmentStat(label: String, value: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ControlTile(
    label: String,
    icon: ImageVector,
    isOn: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        modifier = modifier.height(120.dp),
        color = if (isOn) CamsNavy else Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isOn) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, 
                    null, 
                    tint = if (isOn) Color.White else CamsTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Column {
                Text(
                    label, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = if (isOn) Color.White else CamsTextPrimary
                )
                Text(
                    if (isOn) "ON" else "OFF", 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Medium,
                    color = if (isOn) Color.White.copy(alpha = 0.7f) else CamsTextSecondary
                )
            }
        }
    }
}
