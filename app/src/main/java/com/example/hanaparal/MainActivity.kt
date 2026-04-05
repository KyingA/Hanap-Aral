package com.example.hanaparal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.NotificationRepository
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.ui.admin.SuperAdminActivity
import com.example.hanaparal.ui.group.CreateGroupActivity
import com.example.hanaparal.ui.group.GroupDetailActivity
import com.example.hanaparal.ui.group.GroupListActivity
import com.example.hanaparal.ui.group.GroupViewModel
import com.example.hanaparal.ui.profile.ProfileActivity
import com.example.hanaparal.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM_TOKEN", token ?: "")
                
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null && token != null) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .update("fcmToken", token)
                        .addOnFailureListener { e ->
                            Log.w("FCM", "Error updating token in Firestore", e)
                        }
                }
            } else {
                Log.w("FCM", "Fetching token failed", task.exception)
            }
        }

        setContent {
            HanapAralTheme(dynamicColor = false) {
                DashboardScreen(
                    onCreateClick = {
                        startActivity(Intent(this, CreateGroupActivity::class.java))
                    },
                    onFindGroupClick = {
                        startActivity(Intent(this, GroupListActivity::class.java))
                    },
                    onProfileClick = {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    },
                    onAdminClick = {
                        startActivity(Intent(this, SuperAdminActivity::class.java))
                    },
                    onGroupClick = { groupId ->
                        val intent = Intent(this, GroupDetailActivity::class.java)
                        intent.putExtra("GROUP_ID", groupId)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: GroupViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel(),
    onCreateClick: () -> Unit = {},
    onFindGroupClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAdminClick: () -> Unit = {},
    onGroupClick: (String) -> Unit = {}
) {
    val groups by viewModel.groups.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showNotifications by remember { mutableStateOf(false) }

    // Notification State
    val notifications by NotificationRepository.notifications.collectAsState()
    val hasUnread by NotificationRepository.hasUnread.collectAsState(initial = false)
    
    val myGroups = groups.filter { it.memberIds.contains(viewModel.currentUserId) }
    
    val filteredGroups = groups.filter { group ->
        val matchesSearch = group.name.contains(searchQuery, ignoreCase = true) || 
                          group.subject.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || group.subject.contains(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    if (showNotifications) {
        ModernNotificationDialog(
            notifications = notifications,
            onDismiss = { showNotifications = false },
            onMarkAllRead = { NotificationRepository.markAllAsRead() },
            onMarkItemRead = { id -> NotificationRepository.markAsRead(id) }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (hasUnread) Color(0xFF0A0A14) else DashboardBg) // Darker background if unread
    ) {
        BackgroundOrbs()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeaderSection(
                greeting = greeting, 
                onProfileClick = onProfileClick, 
                onAdminClick = onAdminClick,
                hasUnread = hasUnread,
                onNotificationsClick = { showNotifications = true }
            )

            Spacer(modifier = Modifier.height(20.dp))
            
            MotivationalQuoteCard()

            Spacer(modifier = Modifier.height(24.dp))

            SearchBox(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            DailyGoalCard()

            Spacer(modifier = Modifier.height(28.dp))

            StatsPanel(
                groupCount = myGroups.size.toString(),
                memberCount = myGroups.sumOf { it.memberIds.size }.toString()
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (myGroups.isNotEmpty()) {
                SectionHeader(title = "My Group", actionText = "See all", onActionClick = onFindGroupClick)
                Spacer(modifier = Modifier.height(16.dp))
                
                val firstGroup = myGroups.first()
                MyGroupHighlightCard(
                    title = firstGroup.name,
                    subject = firstGroup.subject,
                    schedule = firstGroup.schedule,
                    memberCount = "${firstGroup.memberIds.size}/${firstGroup.maxMembers}",
                    onClick = { onGroupClick(firstGroup.id) }
                )
            } else {
                SectionHeader(title = "My Group", actionText = "See all", onActionClick = onFindGroupClick)
                Spacer(modifier = Modifier.height(16.dp))
                EmptyMyGroupCard(onCreateClick)
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            SectionHeader(title = "Upcoming Sessions")
            Spacer(modifier = Modifier.height(16.dp))
            UpcomingSessionsRow(myGroups, onGroupClick)

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(title = "New Group", icon = Icons.Default.Add, color = Color(0xFF6366F1), onClick = onCreateClick, modifier = Modifier.weight(1f))
                ActionCard(title = "Explore", icon = Icons.Default.Search, color = Color(0xFFEC4899), onClick = onFindGroupClick, modifier = Modifier.weight(1f))
                ActionCard(title = "Alerts", icon = Icons.Default.Campaign, color = Color(0xFFF59E0B), onClick = { showNotifications = true }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(36.dp))

            SectionHeader(title = "Discover Communities")
            Spacer(modifier = Modifier.height(16.dp))
            
            CategoryChips(selectedCategory = selectedCategory, onCategorySelected = { selectedCategory = it })
            
            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = filteredGroups,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "GroupList"
            ) { targetGroups ->
                Column {
                    if (targetGroups.isEmpty()) {
                        EmptyStateListItem(if (searchQuery.isNotEmpty()) "No results for \"$searchQuery\"" else "No groups available")
                    } else {
                        targetGroups.take(5).forEach { group ->
                            StudyGroupListItem(group = group, onClick = { onGroupClick(group.id) })
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }

        FloatingBottomNav()
    }
}

@Composable
fun HeaderSection(
    greeting: String, 
    onProfileClick: () -> Unit, 
    onAdminClick: () -> Unit,
    hasUnread: Boolean,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp).clickable { onProfileClick() },
                shape = CircleShape,
                color = Color(0xFF6366F1).copy(alpha = 0.2f),
                border = BorderStroke(2.dp, Color(0xFF6366F1))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "J", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$greeting, John", 
                        color = Color.White, 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFF59E0B).copy(alpha = 0.15f), 
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onAdminClick() }
                    ) {
                        Text(text = "🔥 5", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Level 12", color = TextGray.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
        
        Box {
            IconButton(onClick = onNotificationsClick) {
                Icon(imageVector = Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color.White)
            }
            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                        .border(1.5.dp, DashboardBg, CircleShape)
                )
            }
        }
    }
}

@Composable
fun ModernNotificationDialog(
    notifications: List<com.example.hanaparal.data.NotificationItem>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onMarkItemRead: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Content
                if (notifications.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "All caught up!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "You don't have any notifications.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        notifications.forEach { item ->
                            ModernNotificationItem(item, onMarkRead = { onMarkItemRead(item.id) })
                        }
                    }
                }

                // Footer
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onMarkAllRead() }
                        .padding(top = 8.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        text = "Mark all as read",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernNotificationItem(
    item: com.example.hanaparal.data.NotificationItem,
    onMarkRead: () -> Unit
) {
    val timeAgo = remember(item.timestamp) {
        val diff = System.currentTimeMillis() - item.timestamp
        when {
            diff < 60000 -> "Just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(item.timestamp))
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (item.isRead) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.03f))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Checkbox to mark as read
        Checkbox(
            checked = item.isRead,
            onCheckedChange = { if (it) onMarkRead() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.size(24.dp).padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Medium
            )
        }
        
        IconButton(onClick = { /* Item options */ }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun MotivationalQuoteCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        color = Color(0xFF6366F1).copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = Color(0xFF6366F1),
                modifier = Modifier.size(32.dp).graphicsLayer(scaleX = -1f)
            )
            Text(
                text = "The beautiful thing about learning is that no one can take it away from you.",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "- B.B. King",
                color = Color(0xFF6366F1),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun SearchBox(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.4f)),
        placeholder = { Text("Search subjects, groups...", color = TextGray.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFF6366F1)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextGray)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark,
            focusedBorderColor = Color(0xFF6366F1),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

@Composable
fun DailyGoalCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 0.7f },
                    modifier = Modifier.size(56.dp),
                    color = Color(0xFF10B981),
                    strokeWidth = 6.dp,
                    trackColor = Color(0xFF10B981).copy(alpha = 0.1f)
                )
                Text(text = "70%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(text = "Daily Goal", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "3h 30m / 5h study time", color = TextGray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun StatsPanel(groupCount: String, memberCount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatItem(label = "My Groups", value = groupCount, icon = Icons.AutoMirrored.Filled.List, color = Color(0xFF6366F1), modifier = Modifier.weight(1f))
        StatItem(label = "Total Peers", value = memberCount, icon = Icons.Default.People, color = Color(0xFFEC4899), modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatItem(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = label, color = TextGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun MyGroupHighlightCard(title: String, subject: String, schedule: String, memberCount: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF6366F1),
        shadowElevation = 8.dp
    ) {
        Box {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.size(150.dp).align(Alignment.BottomEnd).offset(x = 30.dp, y = 30.dp)
            )
            
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.School, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = subject, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = schedule, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = memberCount, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMyGroupCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = SurfaceDark,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AddCircle, null, tint = Color(0xFF6366F1), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "No group yet", color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "Create or join a group to start", color = TextGray, fontSize = 14.sp)
        }
    }
}

@Composable
fun UpcomingSessionsRow(groups: List<StudyGroup>, onGroupClick: (String) -> Unit) {
    if (groups.isEmpty()) {
        Text(text = "No sessions scheduled", color = TextGray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 8.dp))
    } else {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groups.take(3).forEach { group ->
                SessionChip(
                    name = group.name,
                    time = group.schedule.split(",").firstOrNull() ?: "TBA",
                    onClick = { onGroupClick(group.id) }
                )
            }
        }
    }
}

@Composable
fun SessionChip(name: String, time: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF10B981), CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = time, color = TextGray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ActionCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null, onActionClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText, 
                color = Color(0xFF6366F1), 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
fun CategoryChips(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All", "Math", "Science", "History", "IT", "Arts")
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        categories.forEach { category ->
            val isSelected = selectedCategory == category
            Surface(
                modifier = Modifier.clickable { onCategorySelected(category) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF6366F1) else SurfaceLight,
                border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = category,
                    color = if (isSelected) Color.White else TextGray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StudyGroupListItem(group: StudyGroup, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.05f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = group.subject.take(1).uppercase(), color = Color(0xFF6366F1), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = group.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = group.subject, color = TextGray, fontSize = 13.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${group.memberIds.size}/${group.maxMembers}", color = TextGray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = "Join", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyStateListItem(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = TextGray, fontSize = 15.sp)
    }
}

@Composable
fun FloatingBottomNav() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .width(280.dp)
                .height(64.dp)
                .shadow(24.dp, CircleShape, spotColor = Color.Black),
            shape = CircleShape,
            color = SurfaceDark.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(Icons.Default.Home, "Home", true)
                NavItem(Icons.Default.Explore, "Explore", false)
                NavItem(Icons.Default.ChatBubble, "Messages", false)
                NavItem(Icons.Default.Settings, "Settings", false)
            }
        }
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(if (isSelected) 1f else 0.4f)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = if (isSelected) Color(0xFF6366F1) else Color.White, modifier = Modifier.size(24.dp))
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(4.dp).background(Color(0xFF6366F1), CircleShape))
        }
    }
}

@Composable
fun BackgroundOrbs() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-50).dp)
                .size(250.dp)
                .background(Brush.radialGradient(listOf(Color(0xFF6366F1).copy(alpha = 0.15f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .size(300.dp)
                .background(Brush.radialGradient(listOf(Color(0xFFEC4899).copy(alpha = 0.1f), Color.Transparent)))
        )
    }
}
