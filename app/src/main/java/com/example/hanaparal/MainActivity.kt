package com.example.hanaparal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.ui.group.CreateGroupActivity
import com.example.hanaparal.ui.group.GroupDetailActivity
import com.example.hanaparal.ui.group.GroupListActivity
import com.example.hanaparal.ui.group.GroupViewModel
import com.example.hanaparal.ui.profile.ProfileActivity
import com.example.hanaparal.ui.theme.*

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
    
    val myGroups = groups.filter { it.memberIds.contains(viewModel.currentUserId) }
    val suggestedGroups = groups.filter { !it.memberIds.contains(viewModel.currentUserId) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightGrayBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Top Bar ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HanapAral",
                        style = TextStyle(
                            color = DarkNavy,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "Connect. Study. Excel.",
                        color = SubtitleGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(IconBgLight)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = DarkNavy, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(PremiumPurple, Color(0xFF818CF8))))
                            .clickable { onProfileClick() }
                            .shadow(4.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Greeting ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = DarkNavy,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Welcome back, Anthony! 👋",
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (myGroups.isEmpty()) "Ready to ace your subjects? Join a group now!" else "You're currently in ${myGroups.size} active study groups. Keep going!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Action Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    title = "Find Group",
                    subtitle = "Search by subject",
                    icon = Icons.Default.Search,
                    containerColor = SoftPurple,
                    contentColor = PremiumPurple,
                    onClick = onFindGroupClick,
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    title = "Create",
                    subtitle = "Start a group",
                    icon = Icons.Default.Add,
                    containerColor = LightGreen,
                    contentColor = PrimaryGreen,
                    onClick = onCreateClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── My Groups ──
            SectionHeader(title = "My Groups", count = myGroups.size, onSeeAll = onFindGroupClick)

            Spacer(modifier = Modifier.height(16.dp))

            if (myGroups.isEmpty()) {
                EmptyStateCard(message = "You haven't joined any groups yet.")
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    myGroups.forEach { group ->
                        MyGroupCard(group = group, onClick = { onGroupClick(group.id) })
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Suggested for You ──
            SectionHeader(title = "Suggested for You")
            Text(
                text = "Personalized for your academic success",
                color = SubtitleGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            suggestedGroups.forEach { group ->
                SuggestedGroupCard(
                    group = group,
                    onJoinClick = { 
                        viewModel.joinGroup(group.id)
                        Toast.makeText(context, "Joining ${group.name}...", Toast.LENGTH_SHORT).show()
                    },
                    onCardClick = { onGroupClick(group.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.height(120.dp))
        }

        // ── Bottom Navigation ──
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .shadow(24.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(icon = Icons.Default.Home, label = "Home", isSelected = true, onClick = {})
                NavItem(icon = Icons.Default.Search, label = "Explore", onClick = onFindGroupClick)
                NavItem(icon = Icons.Default.Person, label = "Profile", onClick = onProfileClick)
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, color = contentColor.copy(alpha = 0.6f), fontSize = 11.sp)
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int? = null, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = DarkNavy,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(10.dp))
                Surface(
                    shape = CircleShape,
                    color = SoftPurple,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = count.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PremiumPurple)
                    }
                }
            }
        }
        if (onSeeAll != null) {
            Text(
                text = "View All",
                modifier = Modifier.clickable { onSeeAll() },
                color = PremiumPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = IconBgLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = SubtitleGray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean = false, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PremiumPurple else SubtitleGray,
            modifier = Modifier.size(26.dp)
        )
        if (isSelected) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PremiumPurple
            )
        }
    }
}

@Composable
fun MyGroupCard(group: StudyGroup, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SoftPurple
                ) {
                    Text(
                        text = group.subject,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PremiumPurple
                    )
                }
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = SubtitleGray)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = group.name,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = DarkNavy,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(20.dp), tint = SubtitleGray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${group.memberIds.size}/${group.maxMembers} Students",
                    fontSize = 14.sp,
                    color = SubtitleGray,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            val progress = group.memberIds.size.toFloat() / group.maxMembers.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = PremiumPurple,
                trackColor = SoftPurple
            )
        }
    }
}

@Composable
fun SuggestedGroupCard(group: StudyGroup, onJoinClick: () -> Unit, onCardClick: () -> Unit) {
    Surface(
        onClick = onCardClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SoftPurple),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.take(1).uppercase(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PremiumPurple
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DarkNavy
                )
                Text(
                    text = group.subject,
                    fontSize = 13.sp,
                    color = SubtitleGray,
                    fontWeight = FontWeight.Medium
                )
            }
            Button(
                onClick = onJoinClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumPurple,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Join", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    HanapAralTheme(dynamicColor = false) {
        DashboardScreen()
    }
}
