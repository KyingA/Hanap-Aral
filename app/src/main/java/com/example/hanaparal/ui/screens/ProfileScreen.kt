package com.example.hanaparal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.model.UserProfile
import com.example.hanaparal.ui.group.GroupViewModel
import com.example.hanaparal.ui.theme.DarkNavy
import com.example.hanaparal.ui.theme.DashboardScreenBg

@Composable
fun ProfileScreen(
    paddingValues: PaddingValues,
    userProfile: UserProfile?,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    groupViewModel: GroupViewModel = viewModel()
) {
    val context = LocalContext.current
    val allGroups by groupViewModel.studyGroups.collectAsState()
    val myGroups = allGroups.filter { it.memberIds.contains(groupViewModel.currentUserId) }
    
    // Find the next upcoming session from user's groups
    val nextSession = myGroups.firstOrNull { it.scheduleDays.isNotEmpty() && it.timeStart.isNotEmpty() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(DashboardScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Profile",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = DarkNavy
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Avatar and Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB6E3FA)),
                contentAlignment = Alignment.Center
            ) {
                val profileImage = userProfile?.profileImage
                val resId = if (!profileImage.isNullOrEmpty()) {
                    context.resources.getIdentifier(profileImage, "drawable", context.packageName)
                } else 0

                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Profile Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = DarkNavy,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = userProfile?.fullname?.takeIf { it.isNotBlank() } ?: "User",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkNavy
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = userProfile?.program?.takeIf { it.isNotBlank() } ?: "Program",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF6B7280)
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = userProfile?.email?.takeIf { it.isNotBlank() } ?: "email@university.edu",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stat Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Groups Card - Dynamic
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF2E7D32))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Groups", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${myGroups.size}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = DarkNavy)
                    }
                }
            }
            
            // Class Reminder Card - Dynamic
            Surface(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxHeight()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFE65100))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Next Class", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (nextSession != null) {
                        Text(
                            nextSession.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${nextSession.timeStart}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            nextSession.scheduleDays.joinToString(", "),
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text("None", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD1D5DB))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("No classes yet", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Settings & Preferences",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Settings Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsRow(
                    icon = Icons.Default.Person,
                    iconBgColor = Color(0xFFF3F4F6),
                    iconTintColor = DarkNavy,
                    text = "Edit Profile",
                    onClick = { onEditProfileClick() }
                )
                
                Divider(color = Color(0xFFF3F4F6), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                
                SettingsRow(
                    icon = Icons.Default.Settings,
                    iconBgColor = Color(0xFFF3F4F6),
                    iconTintColor = DarkNavy,
                    text = "App Settings",
                    onClick = { /* TODO: Navigate to App Settings */ }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Logout Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFF3F4F6)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth().clickable { onLogoutClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Log Out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = iconBgColor,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkNavy
            )
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(24.dp)
        )
    }
}
