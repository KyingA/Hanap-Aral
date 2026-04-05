package com.example.hanaparal.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hanaparal.R
import com.example.hanaparal.data.repository.NotificationRepository
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.ui.group.CreateGroupActivity
import com.example.hanaparal.ui.group.GroupDetailActivity
import com.example.hanaparal.ui.group.GroupListActivity
import com.example.hanaparal.ui.group.GroupViewModel
import com.example.hanaparal.ui.theme.DarkNavy
import com.example.hanaparal.ui.theme.DashboardScreenBg
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.ui.theme.SubtitleGray
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hanaparal.ui.auth.LoginActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.UserProfile
import com.example.hanaparal.MainViewModel
import com.example.hanaparal.ui.admin.SuperAdminActivity
import com.example.hanaparal.ui.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    mainViewModel: MainViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val myGroups by groupViewModel.studyGroups.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    val isGroupCreationEnabled by mainViewModel.isGroupCreationEnabled
    val announcementHeader by mainViewModel.announcementHeader
    val isSuperAdmin by mainViewModel.isSuperAdmin

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile()
    }
    
    Scaffold(
        containerColor = DashboardScreenBg,
        bottomBar = { DashboardBottomNav(selectedTab = selectedTab, onTabSelected = { selectedTab = it }) }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DashboardContent(
                paddingValues = paddingValues,
                userProfile = userProfile,
                allGroups = myGroups,
                groupViewModel = groupViewModel,
                isGroupCreationEnabled = isGroupCreationEnabled,
                announcementHeader = announcementHeader,
                isSuperAdmin = isSuperAdmin,
                onProfileClick = { selectedTab = 1 }
            )
            1 -> ProfileScreen(
                paddingValues = paddingValues,
                userProfile = userProfile,
                onEditProfileClick = {
                    val intent = android.content.Intent(context, com.example.hanaparal.ui.profile.ProfileActivity::class.java)
                    context.startActivity(intent)
                },
                onLogoutClick = {
                    val activity = context as? ComponentActivity
                    if (activity != null) {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(activity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra(LoginActivity.EXTRA_AFTER_LOGOUT, true)
                        }
                        // Do not call finish(); CLEAR_TASK already tears down this activity.
                        // Calling finish() here can crash on some devices during the transition.
                        activity.startActivity(intent)
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardContent(
    paddingValues: PaddingValues,
    userProfile: UserProfile?,
    allGroups: List<StudyGroup>,
    groupViewModel: GroupViewModel,
    isGroupCreationEnabled: Boolean = true,
    announcementHeader: String = "",
    isSuperAdmin: Boolean = false,
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val notifications by NotificationRepository.notifications.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    var showNotificationsDialog by remember { mutableStateOf(false) }
    val currentUserId = groupViewModel.currentUserId
    val myGroups = allGroups.filter { it.memberIds.contains(currentUserId) }
    Box(modifier = Modifier.fillMaxSize()) {
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
                    if (isSuperAdmin) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Superuser controls",
                            tint = DarkNavy,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    context.startActivity(Intent(context, SuperAdminActivity::class.java))
                                }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { showNotificationsDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (notifications.isNotEmpty()) {
                                    val unread = notifications.count { !it.isRead }
                                    Badge(
                                        containerColor = if (unread > 0) Color(0xFFDC2626) else Color(0xFF6B7280)
                                    ) {
                                        Text(
                                            text = if (notifications.size > 99) "99+" else notifications.size.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = if (notifications.any { !it.isRead }) DarkNavy else Color(0xFF6B7280),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
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
                        val profileContext = LocalContext.current
                        val resId = if (!profileImage.isNullOrEmpty()) {
                            profileContext.resources.getIdentifier(profileImage, "drawable", profileContext.packageName)
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
                val showRemoteAnnouncement = announcementHeader.isNotBlank() &&
                    !announcementHeader.trim().equals("Welcome to Hanap-Aral!", ignoreCase = true)
                if (showRemoteAnnouncement) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEEF2FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = announcementHeader,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = DarkNavy,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (myGroups.isEmpty()) {
                        buildAnnotatedString {
                            append("Join or create a ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = DarkNavy)) {
                                append("study group")
                            }
                            append(" to get started!")
                        }
                    } else {
                        buildAnnotatedString {
                            append("You're in ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = DarkNavy)) {
                                append("${myGroups.size} study group${if (myGroups.size != 1) "s" else ""}")
                            }
                            append(". Keep up the momentum!")
                        }
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { context.startActivity(Intent(context, GroupListActivity::class.java)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Find Group", fontSize = 15.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Left)
                        }
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        onClick = {
                            if (isGroupCreationEnabled) {
                                context.startActivity(Intent(context, CreateGroupActivity::class.java))
                            } else {
                                Toast.makeText(
                                    context,
                                    "Group creation is temporarily disabled.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isGroupCreationEnabled) Color.White else Color(0xFFF3F4F6),
                        shadowElevation = if (isGroupCreationEnabled) 2.dp else 0.dp
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = if (isGroupCreationEnabled) DarkNavy else Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Create",
                                color = if (isGroupCreationEnabled) DarkNavy else Color(0xFF9CA3AF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
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
                                Text("${myGroups.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563))
                            }
                        }
                    }
                    if (myGroups.isNotEmpty()) {
                        Text(
                            "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavy,
                            modifier = Modifier.clickable { context.startActivity(Intent(context, GroupListActivity::class.java)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (myGroups.isEmpty()) {
                    // Empty state for no groups
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFD1D5DB)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No groups yet", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Find or create a study group to get started!", fontSize = 13.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                            .height(IntrinsicSize.Min)
                    ) {
                        val groupColors = listOf(
                            Color(0xFFCDEAC3), Color(0xFFBFDBFE), Color(0xFFFFEBEE),
                            Color(0xFFE8F5E9), Color(0xFFFFF3E0), Color(0xFFE1BEE7)
                        )
                        myGroups.forEachIndexed { index, group ->
                            GroupCard(
                                iconString = group.name.take(2).uppercase(),
                                iconBg = groupColors[index % groupColors.size],
                                status = group.status,
                                statusBg = if (group.status == "ACTIVE") Color(0xFFFFEDD5) else Color(0xFFE5E7EB),
                                statusColor = if (group.status == "ACTIVE") Color(0xFFC2410C) else Color(0xFF4B5563),
                                title = group.name,
                                members = "${group.memberIds.size}",
                                time = group.formattedSchedule,
                                progress = group.memberIds.size.toFloat() / group.maxMembers.toFloat(),
                                onClick = {
                                    val intent = Intent(context, GroupDetailActivity::class.java)
                                    intent.putExtra("GROUP_ID", group.id)
                                    context.startActivity(intent)
                                }
                            )
                            if (index < myGroups.lastIndex) {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Upcoming Sessions
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Upcoming Sessions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Spacer(modifier = Modifier.height(16.dp))

                if (myGroups.isEmpty()) {
                    // Empty state for no sessions
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFD1D5DB)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No upcoming sessions", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sessions will appear here when you join a group.", fontSize = 13.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 0.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            myGroups.take(3).forEachIndexed { index, group ->
                                SessionRow(
                                    day = group.scheduleDays.firstOrNull()?.take(3) ?: "--",
                                    month = group.subject.take(3).uppercase(),
                                    title = group.name,
                                    subtitle = "${group.timeStart} • ${group.scheduleDays.joinToString(", ")}",
                                    trailingIcon = Icons.Default.ChevronRight
                                )
                                if (index < minOf(2, myGroups.lastIndex)) {
                                    HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(vertical = 12.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DashboardScreenBg,
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clickable { context.startActivity(Intent(context, GroupListActivity::class.java)) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("View All Groups", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Suggested for You - Dynamic
            val suggestedGroups = allGroups.filter { !it.memberIds.contains(currentUserId) }
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Suggested for You", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Text(
                    if (userProfile?.program?.isNotBlank() == true)
                        "Based on your Program in ${userProfile.program}"
                    else
                        "Groups you might be interested in",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (suggestedGroups.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color(0xFFD1D5DB)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No suggestions right now", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkNavy)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Check back later for new groups!", fontSize = 13.sp, color = Color(0xFF6B7280), textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    val gradientColors = listOf(
                        Pair(Color(0xFF469B82), Color(0xFF327A65)),
                        Pair(Color(0xFF1F4045), Color(0xFF0F262A)),
                        Pair(Color(0xFF5B4A9E), Color(0xFF3D2E7C)),
                        Pair(Color(0xFF2D5F8A), Color(0xFF1A3D5C))
                    )
                    suggestedGroups.forEachIndexed { index, group ->
                        val colors = gradientColors[index % gradientColors.size]
                        DynamicSuggestedCard(
                            group = group,
                            bgGradientTop = colors.first,
                            bgGradientBottom = colors.second,
                            onJoinClick = { groupViewModel.joinGroup(group.id) },
                            onCardClick = {
                                val intent = Intent(context, GroupDetailActivity::class.java)
                                intent.putExtra("GROUP_ID", group.id)
                                context.startActivity(intent)
                            }
                        )
                        if (index < suggestedGroups.lastIndex) {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                title = { Text("Notifications") },
                text = {
                    if (notifications.isEmpty()) {
                        Text(
                            "No messages yet. Send a test from Firebase Console → Messaging (Cloud Messaging). " +
                                "On Android 13+, allow notifications when the app asks.",
                            fontSize = 14.sp
                        )
                    } else {
                        Column(
                            modifier = Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())
                        ) {
                            notifications.forEach { n ->
                                Text(n.title, fontWeight = FontWeight.Bold)
                                Text(n.body, fontSize = 13.sp, color = Color(0xFF6B7280))
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            NotificationRepository.markAllAsRead()
                            showNotificationsDialog = false
                        }
                    ) { Text("Mark all read") }
                },
                dismissButton = {
                    TextButton(onClick = { showNotificationsDialog = false }) { Text("Close") }
                }
            )
        }
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
    progress: Float,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.width(260.dp).fillMaxHeight().clickable { onClick() },
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
fun DynamicSuggestedCard(
    group: StudyGroup,
    bgGradientTop: Color,
    bgGradientBottom: Color,
    onJoinClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val memberAvatars = listOf(
        R.drawable.bugssy, R.drawable.coolchik, R.drawable.dock,
        R.drawable.doggy, R.drawable.qthams, R.drawable.rizcat
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 5.dp,
        modifier = Modifier.fillMaxWidth().clickable { onCardClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(bgGradientTop, bgGradientBottom))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.take(2).uppercase(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.3f)
                )
                Text(
                    text = group.subject,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFCDEAC3), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text = group.subject.uppercase().take(8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4C705B),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${group.memberIds.size}/${group.maxMembers} members",
                        fontSize = 11.sp,
                        color = Color(0xFF4B5563)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(group.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkNavy)
                Spacer(modifier = Modifier.height(4.dp))
                Text(group.description.ifEmpty { group.formattedSchedule }, fontSize = 13.sp, color = Color(0xFF6B7280), lineHeight = 18.sp, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Member avatar stack using drawable images
                    Row {
                        group.memberIds.take(3).forEachIndexed { i, _ ->
                            val avatarRes = memberAvatars[i % memberAvatars.size]
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .offset(x = (i * -8).dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = avatarRes),
                                    contentDescription = "Member",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        if (group.memberIds.size > 3) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE5E7EB),
                                modifier = Modifier.size(28.dp).offset(x = (3 * -8).dp).border(2.dp, Color.White, CircleShape)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("+${group.memberIds.size - 3}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    if (group.memberIds.size < group.maxMembers) {
                        Surface(
                            color = Color(0xFFDBEAFE),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { onJoinClick() }
                        ) {
                            Text(
                                "Join Group",
                                color = Color(0xFF1E3A8A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Full",
                                color = Color(0xFF9CA3AF),
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
                icon = Icons.Default.AccountCircle,
                label = "PROFILE",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
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
