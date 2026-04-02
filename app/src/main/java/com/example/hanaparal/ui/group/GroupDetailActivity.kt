package com.example.hanaparal.ui.group

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.ui.theme.HanapAralTheme

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
    val groups by viewModel.groups.collectAsState()
    val group = groups.find { it.id == groupId }
    val error by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    // Observe error message
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize()
        ) {
            if (group != null) {
                Text(
                    text = group.name, 
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = group.subject, 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(text = group.description, style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(24.dp))

                // Member Count Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Members", fontWeight = FontWeight.Bold)
                            Text(text = "${group.memberIds.size} / ${group.maxMembers}")
                        }
                        
                        LinearProgressIndicator(
                            progress = { group.memberIds.size.toFloat() / group.maxMembers.toFloat() },
                            modifier = Modifier.width(100.dp),
                        )
                    }
                }
                
                if (group.adminId == viewModel.currentUserId) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You are the Admin of this group",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Join Button Logic
                val isMember = group.memberIds.contains(viewModel.currentUserId)

                Button(
                    onClick = { 
                        if (!isMember) {
                            viewModel.joinGroup(group.id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isMember && group.memberIds.size < group.maxMembers,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMember) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = when {
                            isMember -> "Already a Member"
                            group.memberIds.size >= group.maxMembers -> "Group Full"
                            else -> "Join Study Group"
                        }
                    )
                }
            } else {
                Text("Loading group details...")
            }
        }
    }
}
