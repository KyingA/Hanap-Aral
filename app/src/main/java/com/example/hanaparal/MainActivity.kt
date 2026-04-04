package com.example.hanaparal

import android.content.Intent
import android.os.Bundle
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
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.ui.group.CreateGroupActivity
import com.example.hanaparal.ui.group.GroupDetailActivity
import com.example.hanaparal.ui.group.GroupListActivity
import com.example.hanaparal.ui.group.GroupViewModel
import com.example.hanaparal.ui.profile.ProfileActivity
import com.example.hanaparal.ui.theme.*
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    onCreateClick: () -> Unit = {},
    onFindGroupClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onGroupClick: (String) -> Unit = {}
) {
    val groups by viewModel.groups.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAnnouncements by remember { mutableStateOf(false) }
    
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

    if (showAnnouncements) {
        AnnouncementDialog(onDismiss = { showAnnouncements = false })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DashboardBg)
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

            // ── Header Section (With Profile Pic) ──
            HeaderSection(greeting, onProfileClick)

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

            // ── My Group Focus ──
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
            
            // ── Upcoming Sessions ──
            SectionHeader(title = "Upcoming Sessions")
            Spacer(modifier = Modifier.height(16.dp))
            UpcomingSessionsRow(myGroups, onGroupClick)

            Spacer(modifier = Modifier.height(32.dp))

            // ── Action Grid ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(title = "New Group", icon = Icons.Default.Add, color = Color(0xFF6366F1), onClick = onCreateClick, modifier = Modifier.weight(1f))
                ActionCard(title = "Explore", icon = Icons.Default.Search, color = Color(0xFFEC4899), onClick = onFindGroupClick, modifier = Modifier.weight(1f))
                ActionCard(title = "Alerts", icon = Icons.Default.Campaign, color = Color(0xFFF59E0B), onClick = { showAnnouncements = true }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Recommended Section ──
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
fun HeaderSection(greeting: String, onProfileClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Profile Image Placeholder
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
                    Text(text = "$greeting, John", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = Color(0xFFF59E0B).copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                        Text(text = "🔥 5", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Level 12", color = TextGray.copy(alpha = 0.7f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.width(40.dp).height(3.dp).clip(CircleShape), color = Color(0xFF818CF8), trackColor = Color.White.copy(alpha = 0.1f))
                }
            }
        }
        
        Box {
            Surface(
                modifier = Modifier.size(44.dp).clickable { /* Notification click */ },
                shape = CircleShape,
                color = GlassyWhite,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
            // Notification Dot
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Red).align(Alignment.TopEnd).offset(x = (-2).dp, y = (2).dp))
        }
    }
}

@Composable
fun BackgroundOrbs() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.size(450.dp).offset(x = (-180).dp, y = (-120).dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFF6366F1).copy(alpha = 0.15f), Color.Transparent))))
        Box(modifier = Modifier.size(300.dp).align(Alignment.BottomEnd).offset(x = (100).dp, y = (50).dp).clip(CircleShape).background(Brush.radialGradient(listOf(Color(0xFFEC4899).copy(alpha = 0.1f), Color.Transparent))))
    }
}

@Composable
fun MotivationalQuoteCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Color(0xFF818CF8).copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "The expert in anything was once a beginner. Keep going!",
                color = TextGray,
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun DailyGoalCard() {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = GlassyWhite.copy(alpha = 0.05f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Daily Study Goal", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "75%", color = Color(0xFF34D399), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(progress = { 0.75f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Color(0xFF34D399), trackColor = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "3h 45m / 5h completed", color = TextGray, fontSize = 11.sp)
        }
    }
}

@Composable
fun UpcomingSessionsRow(myGroups: List<StudyGroup>, onGroupClick: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (myGroups.isEmpty()) {
            // Placeholder Session Card
            Surface(
                modifier = Modifier.width(200.dp).height(80.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.1f), Color.Transparent)))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("No upcoming sessions", color = TextGray.copy(alpha = 0.5f), fontSize = 12.sp)
                }
            }
        } else {
            myGroups.forEach { group ->
                Surface(
                    onClick = { onGroupClick(group.id) },
                    modifier = Modifier.width(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = GlassyWhite.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = group.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(text = group.schedule, color = Color(0xFF818CF8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = TextGray, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "In 2 hours", color = TextGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsPanel(groupCount: String, memberCount: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = GlassyWhite.copy(alpha = 0.05f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))) {
        Row(modifier = Modifier.padding(vertical = 24.dp, horizontal = 12.dp).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            StatItem(value = groupCount, label = "Groups", icon = Icons.Default.Groups, color = Color(0xFF818CF8))
            VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp).alpha(0.1f), color = Color.White)
            StatItem(value = memberCount, label = "Members", icon = Icons.Default.Person, color = Color(0xFFF472B6))
            VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp).alpha(0.1f), color = Color.White)
            StatItem(value = "5", label = "Sessions", icon = Icons.Default.Update, color = Color(0xFF34D399))
        }
    }
}

@Composable
fun StatItem(value: String, label: String, icon: ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(text = label, color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SearchBox(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth().height(56.dp),
        placeholder = { Text(text = "Search study groups...", color = TextGray.copy(alpha = 0.6f), fontSize = 15.sp) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextGray, modifier = Modifier.size(22.dp)) },
        trailingIcon = { if (query.isNotEmpty()) { IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextGray, modifier = Modifier.size(20.dp)) } } },
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = GlassyWhite, focusedContainerColor = GlassyWhite, unfocusedBorderColor = Color.White.copy(alpha = 0.1f), focusedBorderColor = Color(0xFF6366F1), cursorColor = Color(0xFF6366F1), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
        singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

@Composable
fun CategoryChips(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All" to "🏠", "Programming" to "💻", "Math" to "📐", "Science" to "🧪", "Design" to "🎨")
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { (category, icon) ->
            val isSelected = category == selectedCategory
            Surface(onClick = { onCategorySelected(category) }, shape = RoundedCornerShape(16.dp), color = if (isSelected) Color(0xFF6366F1) else GlassyWhite.copy(alpha = 0.1f), border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
                Text(text = "$icon $category", modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp), color = if (isSelected) Color.White else TextGray, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

@Composable
fun MyGroupHighlightCard(title: String, subject: String, schedule: String, memberCount: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().graphicsLayer { shadowElevation = 8.dp.toPx(); shape = RoundedCornerShape(32.dp); clip = true }, shape = RoundedCornerShape(32.dp), color = Color.Transparent) {
        Box(modifier = Modifier.background(brush = Brush.linearGradient(colors = listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))).drawWithContent { drawContent(); drawRect(brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.15f), Color.Transparent))) }.padding(28.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(10.dp), color = Color.White.copy(alpha = 0.15f)) { Text(text = subject.uppercase(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = 1.sp) }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Pulse Indicator
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF34D399)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ACTIVE NOW", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(text = title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp)
                Text(text = "Next Session: $schedule", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    AvatarStack()
                    Surface(shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.2f)) { Text(text = "$memberCount members", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.align(Alignment.TopEnd).size(32.dp).alpha(0.6f))
        }
    }
}

@Composable
fun EmptyMyGroupCard(onCreateClick: () -> Unit) {
    Surface(onClick = onCreateClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(32.dp), color = GlassyWhite, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Groups, contentDescription = null, tint = TextGray, modifier = Modifier.size(48.dp)); Spacer(modifier = Modifier.height(16.dp)); Text(text = "You haven't joined any groups yet", color = TextGray, fontSize = 14.sp); Spacer(modifier = Modifier.height(16.dp)); Text(text = "Create or Find a Group", color = Color(0xFF818CF8), fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun AvatarStack() {
    val colors = listOf(Color(0xFFF87171), Color(0xFF60A5FA), Color(0xFF34D399), Color(0xFFFBBF24))
    val nicknames = listOf("AA", "JM", "JA", "KC")
    Row { 
        repeat(4) { i -> 
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = (i * -12).dp)
                    .clip(CircleShape)
                    .background(colors[i % colors.size])
                    .border(2.dp, Color(0xFF6366F1), CircleShape),
                contentAlignment = Alignment.Center
            ) { 
                Text(text = nicknames[i % nicknames.size], color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) 
            } 
        } 
    }
}

@Composable
fun ActionCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier.height(115.dp), shape = RoundedCornerShape(24.dp), color = GlassyWhite, border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Surface(shape = CircleShape, color = color.copy(alpha = 0.15f), modifier = Modifier.size(46.dp)) { Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) } } ; Spacer(modifier = Modifier.height(14.dp)); Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp) }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String? = null, onActionClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp)
            Box(modifier = Modifier.width(24.dp).height(3.dp).background(Color(0xFF6366F1), RoundedCornerShape(2.dp)))
        }
        if (actionText != null) {
            Text(text = actionText, color = Color(0xFF818CF8), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onActionClick() })
        }
    }
}

@Composable
fun StudyGroupListItem(group: StudyGroup, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), color = GlassyWhite.copy(alpha = 0.08f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(Color(0xFF818CF8).copy(alpha = 0.2f), Color(0xFF6366F1).copy(alpha = 0.1f)))).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) { Text(text = group.name.take(1).uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black) }
            Spacer(modifier = Modifier.width(18.dp)); Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = group.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    // NEW Badge for first 2 items
                    Surface(color = Color(0xFF34D399).copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                        Text("NEW", color = Color(0xFF34D399), fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
                Text(text = group.subject, color = TextGray.copy(alpha = 0.6f), fontSize = 13.sp)
            }
            Button(onClick = onClick, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1).copy(alpha = 0.15f)), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), modifier = Modifier.height(40.dp)) { Text(text = "Join", color = Color(0xFF818CF8), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold) }
        }
    }
}

@Composable
fun EmptyStateListItem(message: String = "No groups available") {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Groups, contentDescription = null, tint = TextGray.copy(alpha = 0.3f), modifier = Modifier.size(64.dp)); Spacer(modifier = Modifier.height(16.dp)); Text(text = message, color = TextGray.copy(alpha = 0.5f), fontSize = 15.sp) } }
}

@Composable
fun FloatingBottomNav() {
    Box(modifier = Modifier.fillMaxSize()) { Surface(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).shadow(24.dp, RoundedCornerShape(28.dp)), shape = RoundedCornerShape(28.dp), color = Color(0xFF1E1E30).copy(alpha = 0.95f), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))) { Row(modifier = Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) { BottomNavItem(icon = Icons.Default.Home, label = "Home", isSelected = true); BottomNavItem(icon = Icons.Default.Groups, label = "Groups"); BottomNavItem(icon = Icons.Default.Person, label = "Profile") } } }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(if (isSelected) 1f else 0.4f)) { Icon(icon, contentDescription = label, tint = if (isSelected) Color(0xFF818CF8) else Color.White, modifier = Modifier.size(26.dp)); if (isSelected) { Spacer(modifier = Modifier.height(6.dp)); Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(Color(0xFF818CF8))) } }
}

@Composable
fun AnnouncementDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(), shape = RoundedCornerShape(28.dp), color = Color(0xFF1E1E30), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = Color(0xFFF59E0B).copy(alpha = 0.15f)) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(32.dp)) } }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Announcements", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                AnnouncementItem(title = "Final Exams Week", body = "Good luck to everyone taking exams this week! Don't forget to take breaks.")
                Spacer(modifier = Modifier.height(16.dp))
                AnnouncementItem(title = "System Update", body = "We've added new features to help you track your study goals more effectively.")
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))) { Text(text = "Got it!", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun AnnouncementItem(title: String, body: String) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)).padding(16.dp)) { Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(4.dp)); Text(text = body, color = TextGray, fontSize = 12.sp, lineHeight = 16.sp) }
}
