package com.example.features.student.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.theme.*
import com.example.core.ui.CamsCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileHeaderCard(
    fullName: String,
    courseName: String,
    profilePhotoUrl: String? = null,
    rollNo: String? = null,
    semester: Int? = null,
    department: String? = null,
    section: String? = null,
    mentorName: String? = null,
    mentorEmail: String? = null,
    advisorName: String? = null,
    advisorEmail: String? = null
) {
    CamsCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Avatar and Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Avatar Frame
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(CamsNavy.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CamsNavy.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profilePhotoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profilePhotoUrl,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                )
                            } else {
                                Text(
                                    text = fullName.take(1).uppercase(),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = CamsNavy
                                    )
                                )
                            }
                        }
                        
                        // Verified Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .size(24.dp)
                                .background(CamsNavy, CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }

                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = CamsTextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "Scholar ID: ${rollNo ?: "N/A"}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = CamsTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Badges
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailBadge(Icons.Filled.School, courseName)
                    DetailBadge(Icons.Filled.Badge, department ?: "N/A")
                    DetailBadge(Icons.Filled.CalendarMonth, "Semester ${semester ?: "N/A"}")
                    DetailBadge(Icons.Filled.Group, "Section ${section ?: "N/A"}")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mentor and Advisor Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MentorAdvisorSmallCard(
                        title = "Faculty Mentor",
                        name = mentorName ?: "N/A",
                        email = mentorEmail ?: "N/A",
                        modifier = Modifier.weight(1f)
                    )
                    MentorAdvisorSmallCard(
                        title = "Class Advisor",
                        name = advisorName ?: "N/A",
                        email = advisorEmail ?: "N/A",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MentorAdvisorSmallCard(title: String, name: String, email: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CamsBackground),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title.uppercase(), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = CamsTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CamsTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(email, fontSize = 13.sp, color = CamsTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailBadge(icon: ImageVector, text: String) {
    Surface(
        color = CamsNavy.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CamsNavy.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = CamsNavy, modifier = Modifier.size(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = CamsNavy
                )
            )
        }
    }
}
