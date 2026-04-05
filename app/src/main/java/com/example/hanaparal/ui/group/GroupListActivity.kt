package com.example.hanaparal.ui.group

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.ui.theme.BackgroundLight
import com.example.hanaparal.ui.theme.DarkNavy
import com.example.hanaparal.ui.theme.HanapAralTheme
import com.example.hanaparal.ui.theme.SubtitleGray

class GroupListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HanapAralTheme {
                GroupListScreen(
                    onBack = { finish() },
                    onGroupClick = { groupId ->
                        val intent = Intent(this, GroupDetailActivity::class.java)
                        intent.putExtra("GROUP_ID", groupId)
                        startActivity(intent)
                    },
                    onCreateGroupClick = {
                        startActivity(Intent(this, CreateGroupActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupListScreen(
    viewModel: GroupViewModel = viewModel(),
    onBack: () -> Unit,
    onGroupClick: (String) -> Unit,
    onCreateGroupClick: () -> Unit
) {
    val groups by viewModel.studyGroups.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredGroups = groups.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || 
        it.subject.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text("Available Groups", fontWeight = FontWeight.Bold, color = DarkNavy) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkNavy)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateGroupClick,
                containerColor = Color(0xFF8B5CF6),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search groups...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SubtitleGray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkNavy,
                    unfocusedTextColor = DarkNavy,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.05f),
                    focusedBorderColor = Color(0xFF8B5CF6)
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredGroups.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No groups found", color = SubtitleGray)
                        }
                    }
                } else {
                    items(filteredGroups) { group ->
                        GroupListItem(
                            group = group,
                            onClick = { onGroupClick(group.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupListItem(
    group: StudyGroup,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5CF6),
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavy
                )
                Text(
                    text = group.subject,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B5CF6),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = SubtitleGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${group.memberIds.size} / ${group.maxMembers} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtitleGray
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = SubtitleGray
            )
        }
    }
}
