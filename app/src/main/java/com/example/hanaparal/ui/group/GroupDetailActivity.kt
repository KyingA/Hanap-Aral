package com.example.hanaparal.ui.group

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.repository.ChatMessage
import com.example.hanaparal.ui.theme.*
import com.example.hanaparal.utils.GroupNotificationTopics
import com.google.firebase.messaging.FirebaseMessaging
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GroupDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val groupId = intent.getStringExtra("GROUP_ID") ?: ""
        setContent {
            HanapAralTheme {
                GroupDetailScreen(groupId = groupId, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
    viewModel: GroupViewModel = viewModel()
) {
    val groups by viewModel.studyGroups.collectAsState()
    val group = groups.find { it.id == groupId }
    val error by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Tab Selection State
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Discussion", "Announcements", "Files", "Members")

    // Observe error message
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete this group? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGroup(groupId)
                    showDeleteDialog = false
                    onBack()
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DashboardScreenBg
    ) {
        if (group != null) {
            val profileImages by viewModel.memberProfileImages.collectAsState()
            val isMember = group.memberIds.contains(viewModel.currentUserId)

            LaunchedEffect(group.id, group.memberIds) {
                viewModel.loadMemberProfileImages(group.memberIds)
            }

            if (!isMember) {
                PreviewJoinUI(group, viewModel, onBack)
            } else {
                LaunchedEffect(group.id) {
                    viewModel.loadAnnouncements(group.id)
                    FirebaseMessaging.getInstance()
                        .subscribeToTopic(GroupNotificationTopics.topicForGroupId(group.id))
                }
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            DarkNavy.copy(alpha = 0.1f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .statusBarsPadding()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkNavy)
                                }
                                Row {
                                    if (group.adminId == viewModel.currentUserId) {
                                        IconButton(onClick = {
                                            val intent = Intent(context, CreateGroupActivity::class.java)
                                            intent.putExtra("EDIT_GROUP_ID", group.id)
                                            context.startActivity(intent)
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DarkNavy)
                                        }
                                        IconButton(onClick = { showDeleteDialog = true }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DarkNavy)
                                        }
                                    }
                                    
                                    var showMenu by remember { mutableStateOf(false) }
                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = DarkNavy)
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                            modifier = Modifier.background(Color.White)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Leave Group", color = Color.Red) },
                                                onClick = {
                                                    showMenu = false
                                                    viewModel.leaveGroup(group.id, onBack)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                                Surface(
                                    color = DarkNavy.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = group.subject,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DarkNavy,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = group.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavy
                                )
                                
                                val timeUntil = remember(group.timeStart) {
                                    try {
                                        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                                        val startTime = sdf.parse(group.timeStart)
                                        val now = Calendar.getInstance()
                                        val startCal = Calendar.getInstance().apply {
                                            startTime?.let { time = it }
                                            set(Calendar.YEAR, now.get(Calendar.YEAR))
                                            set(Calendar.MONTH, now.get(Calendar.MONTH))
                                            set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                                        }
                                        if (startCal.before(now)) startCal.add(Calendar.DAY_OF_MONTH, 1)
                                        val diff = startCal.timeInMillis - now.timeInMillis
                                        val hours = diff / (1000 * 60 * 60)
                                        val minutes = (diff / (1000 * 60)) % 60
                                        if (hours > 0) "Starts in ${hours}h ${minutes}m"
                                        else "Starts in ${minutes}m"
                                    } catch (e: Exception) { "Upcoming Session" }
                                }

                                Text(
                                    text = timeUntil,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkNavy,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SubtitleGray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = group.scheduleDays.joinToString(", "), style = MaterialTheme.typography.bodyMedium, color = SubtitleGray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = SubtitleGray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "${group.timeStart} - ${group.timeEnd}", style = MaterialTheme.typography.bodyMedium, color = SubtitleGray)
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MemberAvatarStack(group.memberIds, profileImages)
                                    Text(text = "${group.memberIds.size}/${group.maxMembers} members", style = MaterialTheme.typography.bodyMedium, color = SubtitleGray)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            GroupDetailBalancedTabs(
                                tabs = tabs,
                                selectedIndex = selectedTab,
                                onTabSelected = { selectedTab = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    bottomBar = {
                        if (selectedTab == 0) {
                            ChatInput(groupId = group.id, viewModel = viewModel)
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (selectedTab) {
                            0 -> DiscussionView(groupId = group.id, viewModel = viewModel)
                            1 -> AnnouncementsView(group = group, viewModel = viewModel)
                            2 -> FilesView()
                            3 -> MembersView(group, viewModel.currentUserId, profileImages)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Four equal-width segments so short and long labels (e.g. "Announcements") stay visually balanced.
 * Styling matches the dashboard bottom nav (light blue selection, navy/gray text).
 */
@Composable
private fun GroupDetailBalancedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF3F4F6),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = selectedIndex == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) Color(0xFFDBEAFE) else Color.Transparent)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 10.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (selected) Color(0xFF1E3A8A) else SubtitleGray
                    )
                }
            }
        }
    }
}

@Composable
fun DiscussionView(groupId: String, viewModel: GroupViewModel) {
    val messages by viewModel.messages.collectAsState()
    
    LaunchedEffect(groupId) {
        viewModel.loadMessages(groupId)
    }

    if (messages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No messages yet. Say hi!", color = SubtitleGray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                MessageBubble(message = message, isCurrentUser = message.senderId == viewModel.currentUserId)
            }
        }
    }
}

@Composable
fun AnnouncementsView(group: StudyGroup, viewModel: GroupViewModel) {
    val announcements by viewModel.announcements.collectAsState()
    val context = LocalContext.current
    var showPostDialog by remember { mutableStateOf(false) }
    var titleDraft by remember { mutableStateOf("") }
    var bodyDraft by remember { mutableStateOf("") }
    val dateFormat = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }
    val isAdmin = group.adminId == viewModel.currentUserId

    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("Post announcement", color = DarkNavy, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = titleDraft,
                        onValueChange = { titleDraft = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkNavy,
                            unfocusedTextColor = DarkNavy
                        )
                    )
                    OutlinedTextField(
                        value = bodyDraft,
                        onValueChange = { bodyDraft = it },
                        label = { Text("Message") },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DarkNavy,
                            unfocusedTextColor = DarkNavy
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.postAnnouncement(group.id, titleDraft, bodyDraft) { ok ->
                            if (ok) {
                                titleDraft = ""
                                bodyDraft = ""
                                showPostDialog = false
                                Toast.makeText(context, "Announcement published", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) { Text("Publish", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showPostDialog = false }) { Text("Cancel") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (announcements.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Campaign,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = DarkNavy.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No announcements yet.", color = SubtitleGray, fontSize = 16.sp)
                Text(
                    "Admins can post updates for all members. Push notifications require deploying the Cloud Function in the /functions folder to your Firebase project.",
                    color = SubtitleGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showPostDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
                    ) {
                        Text("Post announcement", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(announcements, key = { it.id }) { ann ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.06f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                ann.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )
                            Text(
                                dateFormat.format(java.util.Date(ann.createdAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = SubtitleGray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                ann.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkNavy,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        if (isAdmin && announcements.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showPostDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = DarkNavy,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Post announcement")
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isCurrentUser: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (!isCurrentUser) {
            Text(text = "Member ${message.senderId}", style = MaterialTheme.typography.labelSmall, color = SubtitleGray, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
        }
        Surface(
            color = if (isCurrentUser) DarkNavy else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 16.dp
            ),
            border = if (isCurrentUser) null else BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
            shadowElevation = 1.dp
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (isCurrentUser) Color.White else DarkNavy,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ChatInput(groupId: String, viewModel: GroupViewModel) {
    var text by remember { mutableStateOf("") }
    
    Surface(
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Type a message...", color = SubtitleGray) },
                modifier = Modifier.weight(1f),
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkNavy,
                    unfocusedTextColor = DarkNavy,
                    unfocusedContainerColor = IconBgLight,
                    focusedContainerColor = IconBgLight,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = DarkNavy
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = DarkNavy
            ) {
                IconButton(onClick = {
                    if (text.isNotBlank()) {
                        viewModel.sendMessage(groupId, text)
                        text = ""
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewJoinUI(group: StudyGroup, viewModel: GroupViewModel, onBack: () -> Unit) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Group Details", color = DarkNavy) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkNavy)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(100.dp).background(DarkNavy.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = group.name.take(1).uppercase(), style = MaterialTheme.typography.displayMedium, color = DarkNavy)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = group.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DarkNavy)
            Text(text = group.subject, style = MaterialTheme.typography.titleMedium, color = DarkNavy)
            Text(text = group.formattedSchedule, style = MaterialTheme.typography.bodyMedium, color = SubtitleGray)
            Text(text = "${group.memberIds.size}/${group.maxMembers} members", style = MaterialTheme.typography.bodyMedium, color = SubtitleGray)
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp), tint = DarkNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("About", fontWeight = FontWeight.Bold, color = DarkNavy)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = group.description, color = SubtitleGray)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = { viewModel.joinGroup(group.id) },
                modifier = Modifier.fillMaxWidth(),
                enabled = group.memberIds.size < group.maxMembers,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy)
            ) {
                Text(text = if (group.memberIds.size >= group.maxMembers) "Group Full" else "Join Study Group", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MemberAvatarStack(
    memberIds: List<String>,
    profileImageByUserId: Map<String, String>
) {
    val context = LocalContext.current
    Row {
        memberIds.take(5).forEachIndexed { i, userId ->
            val key = profileImageByUserId[userId].orEmpty()
            val resId = if (key.isNotBlank()) {
                context.resources.getIdentifier(key, "drawable", context.packageName)
            } else {
                0
            }
            Surface(
                modifier = Modifier.size(32.dp).offset(x = (i * -8).dp),
                shape = CircleShape,
                color = if (resId != 0) Color.White else DarkNavy.copy(alpha = 0.5f),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (resId != 0) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = userId.take(1).uppercase(),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilesView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(64.dp), tint = SubtitleGray.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
        Text("No files shared yet", color = SubtitleGray)
    }
}

@Composable
fun MembersView(
    group: StudyGroup,
    currentUserId: String,
    profileImageByUserId: Map<String, String>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Admin", style = MaterialTheme.typography.titleSmall, color = SubtitleGray) }
        item {
            MemberItem(
                name = if (group.adminId == currentUserId) "You (Admin)" else "Admin (${group.adminId})",
                isAdmin = true,
                userId = group.adminId,
                profileImageKey = profileImageByUserId[group.adminId]
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("All Members (${group.memberIds.size})", style = MaterialTheme.typography.titleSmall, color = SubtitleGray)
        }
        items(group.memberIds.filter { it != group.adminId }) { memberId ->
            MemberItem(
                name = if (memberId == currentUserId) "You" else "Member $memberId",
                isAdmin = false,
                userId = memberId,
                profileImageKey = profileImageByUserId[memberId]
            )
        }
    }
}

@Composable
fun MemberItem(
    name: String,
    isAdmin: Boolean,
    userId: String? = null,
    profileImageKey: String? = null
) {
    val context = LocalContext.current
    val resId = remember(profileImageKey, context) {
        val key = profileImageKey.orEmpty()
        if (key.isNotBlank()) {
            context.resources.getIdentifier(key, "drawable", context.packageName)
        } else {
            0
        }
    }
    val fallbackInitial = (userId?.take(1) ?: name.firstOrNull()?.toString() ?: "?").uppercase()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (resId != 0) Color.White else DarkNavy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(fallbackInitial, fontWeight = FontWeight.Bold, color = DarkNavy)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, fontWeight = FontWeight.SemiBold, color = DarkNavy)
            if (isAdmin) {
                Spacer(modifier = Modifier.weight(1f))
                Surface(color = DarkNavy.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                    Text("Admin", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = DarkNavy)
                }
            }
        }
    }
}
