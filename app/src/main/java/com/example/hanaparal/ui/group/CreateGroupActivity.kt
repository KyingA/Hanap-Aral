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
import com.example.hanaparal.ui.theme.BackgroundLight
import com.example.hanaparal.ui.theme.DarkNavy
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
    var maxMembers by remember { mutableStateOf(groupToEdit?.maxMembers?.toString() ?: "20") }
    
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    var selectedDays by remember { mutableStateOf(groupToEdit?.scheduleDays?.toSet() ?: emptySet()) }
    var timeStart by remember { mutableStateOf(groupToEdit?.timeStart ?: "9:00 AM") }
    var timeEnd by remember { mutableStateOf(groupToEdit?.timeEnd ?: "10:00 AM") }

    val context = LocalContext.current

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
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = { Text(if (editGroupId == null) "Create Study Group" else "Edit Group", fontWeight = FontWeight.Bold, color = DarkNavy) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundLight),
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
                    focusedBorderColor = Color(0xFF8B5CF6),
                    focusedLabelColor = Color(0xFF8B5CF6),
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
                    focusedBorderColor = Color(0xFF8B5CF6),
                    focusedLabelColor = Color(0xFF8B5CF6),
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
                    focusedBorderColor = Color(0xFF8B5CF6),
                    focusedLabelColor = Color(0xFF8B5CF6),
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
                            selectedContainerColor = Color(0xFF8B5CF6),
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
                    focusedBorderColor = Color(0xFF8B5CF6),
                    focusedLabelColor = Color(0xFF8B5CF6),
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
                                maxMembers = maxMembers.toIntOrNull() ?: 20,
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
                                maxMembers = maxMembers.toIntOrNull() ?: 20
                            )) { onBack() }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && subject.isNotBlank() && selectedDays.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (editGroupId == null) "Create Group" else "Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}
