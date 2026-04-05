package com.example.hanaparal.ui.group

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.repository.ChatMessage
import com.example.hanaparal.ui.theme.*
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
    val tabs = listOf("Discussion", "Files", "Members")

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
        color = BackgroundLight
    ) {
        if (group != null) {
            val isMember = group.memberIds.contains(viewModel.currentUserId)

            if (!isMember) {
                PreviewJoinUI(group, viewModel, onBack)
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF8B5CF6).copy(alpha = 0.1f),
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
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = group.subject,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF8B5CF6),
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
                                    color = Color(0xFF8B5CF6),
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
                                    MemberAvatarStack(group.memberIds)
                                    Text(text = "${group.memberIds.size}/${group.maxMembers} members", style = MaterialTheme.typography.bodyMedium, color = SubtitleGray)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            TabRow(
                                selectedTabIndex = selectedTab,
                                containerColor = Color.Transparent,
                                contentColor = Color(0xFF8B5CF6),
                                divider = { HorizontalDivider(color = Color.Black.copy(alpha = 0.05f)) },
                                indicator = { tabPositions ->
                                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Color(0xFF8B5CF6))
                                }
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedTab == index,
                                        onClick = { selectedTab = index },
                                        text = { Text(title, style = MaterialTheme.typography.titleSmall, color = if (selectedTab == index) Color(0xFF8B5CF6) else SubtitleGray) }
                                    )
                                }
                            }
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
                            1 -> FilesView()
                            2 -> MembersView(group, viewModel.currentUserId)
                        }
                    }
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
fun MessageBubble(message: ChatMessage, isCurrentUser: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (!isCurrentUser) {
            Text(text = "Member ${message.senderId}", style = MaterialTheme.typography.labelSmall, color = SubtitleGray, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
        }
        Surface(
            color = if (isCurrentUser) Color(0xFF8B5CF6) else Color.White,
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
                    focusedBorderColor = Color(0xFF8B5CF6)
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(0xFF8B5CF6)
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
                modifier = Modifier.size(100.dp).background(Color(0xFF8B5CF6).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = group.name.take(1).uppercase(), style = MaterialTheme.typography.displayMedium, color = Color(0xFF8B5CF6))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = group.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DarkNavy)
            Text(text = group.subject, style = MaterialTheme.typography.titleMedium, color = Color(0xFF8B5CF6))
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
            ) {
                Text(text = if (group.memberIds.size >= group.maxMembers) "Group Full" else "Join Study Group", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MemberAvatarStack(memberIds: List<String>) {
    Row { 
        memberIds.take(5).forEachIndexed { i, userId ->
            Surface(
                modifier = Modifier.size(32.dp).offset(x = (i * -8).dp),
                shape = CircleShape,
                color = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = userId.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
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
fun MembersView(group: StudyGroup, currentUserId: String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Admin", style = MaterialTheme.typography.titleSmall, color = SubtitleGray) }
        item { MemberItem(name = if (group.adminId == currentUserId) "You (Admin)" else "Admin (${group.adminId})", isAdmin = true) }
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("All Members (${group.memberIds.size})", style = MaterialTheme.typography.titleSmall, color = SubtitleGray)
        }
        items(group.memberIds.filter { it != group.adminId }) { memberId ->
            MemberItem(name = if (memberId == currentUserId) "You" else "Member $memberId", isAdmin = false)
        }
    }
}

@Composable
fun MemberItem(name: String, isAdmin: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color(0xFF8B5CF6).copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Text(name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF8B5CF6))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = name, fontWeight = FontWeight.SemiBold, color = DarkNavy)
            if (isAdmin) {
                Spacer(modifier = Modifier.weight(1f))
                Surface(color = Color(0xFF8B5CF6).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                    Text("Admin", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF8B5CF6))
                }
            }
        }
    }
}
