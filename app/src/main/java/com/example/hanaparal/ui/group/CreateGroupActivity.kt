package com.example.hanaparal.ui.group

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.data.config.RemoteConfigManager
import com.example.hanaparal.ui.theme.DarkNavy
import com.example.hanaparal.ui.theme.DashboardScreenBg
import com.example.hanaparal.ui.theme.HanapAralTheme
import java.text.SimpleDateFormat
import java.util.*

class CreateGroupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val editGroupId = intent.getStringExtra("EDIT_GROUP_ID")
        setContent {
            HanapAralTheme {
                CreateGroupScreen(
                    editGroupId = editGroupId,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    editGroupId: String? = null,
    onBack: () -> Unit,
    viewModel: GroupViewModel = viewModel()
) {
    val groups by viewModel.studyGroups.collectAsState()
    val groupToEdit = remember(editGroupId, groups) { groups.find { it.id == editGroupId } }

    var name by remember { mutableStateOf(groupToEdit?.name ?: "") }
    var subject by remember { mutableStateOf(groupToEdit?.subject ?: "") }
    var description by remember { mutableStateOf(groupToEdit?.description ?: "") }
    var maxMembers by remember { mutableStateOf("") }

    LaunchedEffect(editGroupId, groups) {
        val toEdit = editGroupId?.let { id -> groups.find { it.id == id } }
        when {
            toEdit != null -> maxMembers = toEdit.maxMembers.toString()
            editGroupId != null -> Unit
            else -> {
                val rc = RemoteConfigManager()
                maxMembers = rc.getMaxGroupMembers().toString()
                rc.fetchAndActivate {
                    maxMembers = rc.getMaxGroupMembers().toString()
                }
            }
        }
    }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDays by remember { mutableStateOf(groupToEdit?.scheduleDays?.toSet() ?: emptySet()) }
    var timeStart by remember { mutableStateOf(groupToEdit?.timeStart ?: "9:00 AM") }
    var timeEnd by remember { mutableStateOf(groupToEdit?.timeEnd ?: "10:00 AM") }

    val context = LocalContext.current
    val resolvedMaxMembers = maxMembers.toIntOrNull()
        ?: groupToEdit?.maxMembers
        ?: RemoteConfigManager().getMaxGroupMembers().toInt().coerceIn(2, 500)

    fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(context, { _, h, m ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
            }
            onTimeSelected(SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time))
        }, hour, minute, false).show()
    }

    Scaffold(
        containerColor = DashboardScreenBg,
        topBar = {
            TopAppBar(
                title = { Text(if (editGroupId == null) "Create Study Group" else "Edit Group", fontWeight = FontWeight.Bold, color = DarkNavy) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DashboardScreenBg),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkNavy)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkNavy,
                    unfocusedTextColor = DarkNavy,
                    focusedBorderColor = DarkNavy,
                    focusedLabelColor = DarkNavy,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject (e.g., CS CORE)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkNavy,
                    unfocusedTextColor = DarkNavy,
                    focusedBorderColor = DarkNavy,
                    focusedLabelColor = DarkNavy,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkNavy,
                    unfocusedTextColor = DarkNavy,
                    focusedBorderColor = DarkNavy,
                    focusedLabelColor = DarkNavy,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            
            Text("Schedule Days", style = MaterialTheme.typography.titleSmall, color = DarkNavy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { day ->
                    FilterChip(
                        selected = selectedDays.contains(day),
                        onClick = {
                            selectedDays = if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                        },
                        label = { Text(day.take(1)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).clickable { showTimePicker { timeStart = it } }) {
                    OutlinedTextField(
                        value = timeStart,
                        onValueChange = { },
                        label = { Text("Start Time") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = DarkNavy,
                            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledLabelColor = Color.Gray,
                            disabledTrailingIconColor = Color.Gray
                        )
                    )
                }
                Box(modifier = Modifier.weight(1f).clickable { showTimePicker { timeEnd = it } }) {
                    OutlinedTextField(
                        value = timeEnd,
                        onValueChange = { },
                        label = { Text("End Time") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = DarkNavy,
                            disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                            disabledLabelColor = Color.Gray,
                            disabledTrailingIconColor = Color.Gray
                        )
                    )
                }
            }

            OutlinedTextField(
                value = maxMembers,
                onValueChange = { if (it.all { char -> char.isDigit() }) maxMembers = it },
                label = { Text("Member Limit") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DarkNavy,
                    unfocusedTextColor = DarkNavy,
                    focusedBorderColor = DarkNavy,
                    focusedLabelColor = DarkNavy,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = { 
                    if (editGroupId == null) {
                        viewModel.createGroup(
                            StudyGroup(
                                id = "",
                                name = name,
                                subject = subject,
                                description = description,
                                adminId = viewModel.currentUserId,
                                memberIds = listOf(viewModel.currentUserId),
                                maxMembers = resolvedMaxMembers,
                                scheduleDays = selectedDays.toList(),
                                timeStart = timeStart,
                                timeEnd = timeEnd
                            ),
                            onCreated = { _ -> onBack() }
                        )
                    } else {
                        groupToEdit?.let {
                            viewModel.updateGroup(it.copy(
                                name = name,
                                subject = subject,
                                description = description,
                                scheduleDays = selectedDays.toList(),
                                timeStart = timeStart,
                                timeEnd = timeEnd,
                                maxMembers = resolvedMaxMembers
                            )) { onBack() }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && subject.isNotBlank() && selectedDays.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (editGroupId == null) "Create Group" else "Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}
