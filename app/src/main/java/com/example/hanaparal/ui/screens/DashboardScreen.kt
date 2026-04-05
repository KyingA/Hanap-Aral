package com.example.hanaparal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.R
import com.example.hanaparal.ui.theme.DarkNavy
import com.example.hanaparal.ui.theme.HanapAralTheme
import androidx.compose.runtime.*
import com.example.hanaparal.ui.auth.LoginActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.model.UserProfile
import com.example.hanaparal.ui.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(profileViewModel: ProfileViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val userProfile by profileViewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile()
    }
    
    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        bottomBar = { DashboardBottomNav(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DashboardContent(paddingValues, userProfile, onProfileClick = { selectedTab = 2 })
            // 1 -> GroupsScreen(paddingValues)
            2 -> ProfileScreen(
                paddingValues = paddingValues,
                userProfile = userProfile,
                onEditProfileClick = {
                    val intent = android.content.Intent(context, com.example.hanaparal.ui.profile.ProfileActivity::class.java)
                    context.startActivity(intent)
                },
                onLogoutClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    val intent = android.content.Intent(context, com.example.hanaparal.ui.auth.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }
            )
            else -> DashboardContent(paddingValues, userProfile, onProfileClick = { selectedTab = 2 })
        }
    }
}

@Composable
fun DashboardContent(paddingValues: PaddingValues, userProfile: UserProfile?, onProfileClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
            // Top Bar Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_book),
                        contentDescription = "Logo",
                        modifier = Modifier.size(24.dp),
                        tint = DarkNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HanapAral",
                        color = DarkNavy,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB6E3FA))
                            .clickable { onProfileClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        val profileImage = userProfile?.profileImage
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val resId = if (!profileImage.isNullOrEmpty()) {
                            context.resources.getIdentifier(profileImage, "drawable", context.packageName)
                        } else 0

                        if (resId != 0) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = DarkNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Greeting Section
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                val currentTime = System.currentTimeMillis()
                val createdAt = userProfile?.createdAt ?: currentTime
                val isNewUser = (currentTime - createdAt) < (24 * 60 * 60 * 1000L) // 24 hours
                
                Text(
                    text = if (isNewUser) "WELCOME TO HANAPARAL" else "WELCOME BACK",
                    color = Color(0xFF4C705B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                val firstName = userProfile?.fullname?.split(" ")?.get(0) ?: "User"
                Text(
                    text = "Hello, $firstName!",
                    color = DarkNavy,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildAnnotatedString {
                        append("You have ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = DarkNavy)) {
                            append("3 study sessions")
                        }
                        append(" scheduled for this ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = DarkNavy)) {
                            append("week")
                        }
                        append(". Keep up the momentum!")
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Find Group", fontSize = 15.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = androidx.compose.ui.text.style.TextAlign.Left)
                        }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, tint = DarkNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create", color = DarkNavy, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // My Groups Section
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("My Groups", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFE5E7EB),
                            shape = CircleShape,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("4", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                            }
                        }
                    }
                    Text("View All", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    GroupCard(
                        iconString = "{ }",
                        iconBg = Color(0xFFCDEAC3),
                        status = "ACTIVE",
                        statusBg = Color(0xFFFFEDD5),
                        statusColor = Color(0xFFC2410C),
                        title = "Data Structures 101",
                        members = "12",
                        time = "Wed, 4PM",
                        progress = 0.7f
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    GroupCard(
                        iconString = "C",
                        iconBg = Color(0xFFBFDBFE),
                        status = "NEW",
                        statusBg = Color(0xFFE0E7FF),
                        statusColor = Color(0xFF4338CA),
                        title = "C++ Study Circle",
                        members = "8",
                        time = "Fri, 2PM",
                        progress = 0.3f
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    GroupCard(
                        iconString = "PY",
                        iconBg = Color(0xFFFFEBEE),
                        status = "CLOSED",
                        statusBg = Color(0xFFFFCDD2),
                        statusColor = Color(0xFFB71C1C),
                        title = "Python Programming",
                        members = "4",
                        time = "Sat, 10AM",
                        progress = 0.9f
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    GroupCard(
                        iconString = "DSA",
                        iconBg = Color(0xFFE8F5E9),
                        status = "CLOSED",
                        statusBg = Color(0xFFC8E6C9),
                        statusColor = Color(0xFF2E7D32),
                        title = "Data Structures 101",
                        members = "12",
                        time = "Wed, 4PM",
                        progress = 0.7f
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Upcoming Sessions
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Upcoming Sessions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SessionRow("24", "OCT", "Algorithm Review", "4:30 PM • Library Room 4", Icons.Default.KeyboardArrowRight)
                        HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 12.dp))
                        SessionRow("26", "OCT", "Calculus Mock Exam", "10:00 AM • Zoom", Icons.Default.Videocam)
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8F9FA),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable { }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("View Calendar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Suggested for You
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Suggested for You", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Text(
                    "Based on your Program in Computer Science",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                SuggestedCard(
                    bgGradientTop = Color(0xFF469B82),
                    bgGradientBottom = Color(0xFF327A65),
                    tagText = "CS CORE",
                    title = "Algorithms Mastery",
                    desc = "Weekly deep dives into Big O notation and sorting optimizations. Perfect for juniors.",
                    logoContent = {
                        Icon(
                            imageVector = Icons.Default.Create, // Placeholder for poly icon
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                SuggestedCard(
                    bgGradientTop = Color(0xFF1F4045),
                    bgGradientBottom = Color(0xFF0F262A),
                    tagText = "ADVANCED",
                    tagColor = Color(0xFFFFEDD5),
                    tagTextColor = Color(0xFFC2410C),
                    title = "SQL & Database Systems",
                    desc = "Hands-on practice with relational schema design and complex queries.",
                    logoContent = {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color(0xFF86D9E9)
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

@Composable
fun GroupCard(
    iconString: String,
    iconBg: Color,
    status: String,
    statusBg: Color,
    statusColor: Color,
    title: String,
    members: String,
    time: String,
    progress: Float
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.width(260.dp).clickable { },
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(color = iconBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(iconString, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkNavy)
                    }
                }
                Surface(color = statusBg, shape = RoundedCornerShape(4.dp)) {
                    Text(
                        status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6B7280))
                Spacer(modifier = Modifier.width(4.dp))
                Text(members, fontSize = 12.sp, color = Color(0xFF6B7280))
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF6B7280))
                Spacer(modifier = Modifier.width(4.dp))
                Text(time, fontSize = 12.sp, color = Color(0xFF6B7280))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color(0xFFF3F4F6), CircleShape)) {
                Box(modifier = Modifier.fillMaxWidth(progress).height(4.dp).background(Color(0xFF4C705B), CircleShape))
            }
        }
    }
}

@Composable
fun SessionRow(day: String, month: String, title: String, subtitle: String, trailingIcon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(color = Color.White, shape = RoundedCornerShape(12.dp), border = BorderStroke(2.dp, Color(0xFF4C705B)), modifier = Modifier.width(50.dp).height(54.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(day, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DarkNavy, lineHeight = 20.sp)
                Text(month, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = Color(0xFF6B7280))
        }
        Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(24.dp), tint = DarkNavy)
    }
}

@Composable
fun SuggestedCard(
    bgGradientTop: Color,
    bgGradientBottom: Color, // Simplified to single color for background if brush not used
    tagText: String,
    tagColor: Color = Color(0xFFCDEAC3),
    tagTextColor: Color = Color(0xFF4C705B),
    title: String,
    desc: String,
    logoContent: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 5.dp,
        modifier = Modifier.fillMaxWidth().clickable { }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(bgGradientTop, bgGradientBottom))),
                contentAlignment = Alignment.Center
            ) {
                logoContent()
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = tagColor, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = tagText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF4B5563))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trusted", fontSize = 11.sp, color = Color(0xFF4B5563))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Spacer(modifier = Modifier.height(8.dp))
                Text(desc, fontSize = 13.sp, color = Color(0xFF6B7280), lineHeight = 18.sp)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small Avatar Stack Placeholder
                    Row {
                        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFFEADBCE)).border(2.dp, Color.White, CircleShape))
                        Box(modifier = Modifier.size(28.dp).offset(x = (-8).dp).clip(CircleShape).background(Color(0xFF4B5563)).border(2.dp, Color.White, CircleShape))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE5E7EB),
                            modifier = Modifier.size(28.dp).offset(x = (-16).dp).border(2.dp, Color.White, CircleShape)
                        ) { Box(contentAlignment = Alignment.Center) { Text("+16", fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
                    }
                    
                    Surface(
                        color = Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { }
                    ) {
                        Text(
                            "Join Group",
                            color = Color(0xFF1E3A8A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Home,
                label = "HOME",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            NavItem(
                icon = Icons.Default.Person,
                label = "GROUPS",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            NavItem(
                icon = Icons.Default.AccountCircle,
                label = "PROFILE",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
        }
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    if (isSelected) {
        Surface(
            color = Color(0xFFDBEAFE),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(icon, contentDescription = label, tint = Color(0xFF1E3A8A), modifier = Modifier.size(24.dp))
                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clickable { onClick() }
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFF6B7280), modifier = Modifier.size(24.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF6B7280))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDashboard() {
    HanapAralTheme {
        DashboardScreen()
    }
}
