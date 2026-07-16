package com.example.features.career.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.theme.LexNovaPurple
import com.example.features.career.providers.CareerViewModel
import com.example.features.student.widgets.StudentDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificationsScreen(
    viewModel: CareerViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            StudentDrawer(
                currentRoute = "/career/certifications",
                onNavigate = {
                    scope.launch { drawerState.close() }
                    onNavigate(it)
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Certifications") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            BoxWithConstraints(modifier = Modifier.padding(paddingValues)) {
                val isTablet = maxWidth > 600.dp
                
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LexNovaPurple)
                    }
                } else {
                    if (isTablet) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) {
                                CertificationsList(uiState.certifications)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CertificationAnalytics()
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                CertificationAnalytics()
                            }
                            items(uiState.certifications) { cert ->
                                CertificationCard(cert)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CertificationsList(certifications: List<com.example.features.career.models.StudentCertification>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(certifications) { cert ->
            CertificationCard(cert)
        }
    }
}

@Composable
fun CertificationCard(cert: com.example.features.career.models.StudentCertification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (cert.isVerified) Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFF64748B).copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (cert.isVerified) Color(0xFF10B981) else Color(0xFF64748B),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cert.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = cert.issuer, style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF64748B)))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "Issued: ${cert.issueDate}", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                    if (cert.expiryDate != null) {
                        Text(text = "Exp: ${cert.expiryDate}", style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)))
                    }
                }
            }
        }
    }
}

@Composable
fun CertificationAnalytics() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Certification Progress", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(24.dp))
            
            // Simple Area Chart representation using Canvas
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val path = Path()
                val points = listOf(
                    Offset(0f, size.height),
                    Offset(size.width * 0.2f, size.height * 0.8f),
                    Offset(size.width * 0.4f, size.height * 0.6f),
                    Offset(size.width * 0.6f, size.height * 0.7f),
                    Offset(size.width * 0.8f, size.height * 0.3f),
                    Offset(size.width, size.height * 0.2f)
                )
                
                path.moveTo(points.first().x, points.first().y)
                points.forEach { point ->
                    path.lineTo(point.x, point.y)
                }
                
                // Draw line
                drawPath(
                    path = path,
                    color = LexNovaPurple,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Draw filled area
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    color = LexNovaPurple.copy(alpha = 0.2f)
                )
                
                // Draw points
                points.forEach { point ->
                    drawCircle(
                        color = LexNovaPurple,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }
    }
}
