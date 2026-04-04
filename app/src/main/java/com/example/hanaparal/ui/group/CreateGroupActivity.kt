package com.example.hanaparal.ui.group

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hanaparal.data.model.StudyGroup
import com.example.hanaparal.ui.theme.HanapAralTheme
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
    val groups by viewModel.groups.collectAsState()
    val groupToEdit = remember(editGroupId, groups) { groups.find { it.id == editGroupId } }

    var name by remember { mutableStateOf(groupToEdit?.name ?: "") }
    var subject by remember { mutableStateOf(groupToEdit?.subject ?: "") }
    var description by remember { mutableStateOf(groupToEdit?.description ?: "") }
    var schedule by remember { mutableStateOf(groupToEdit?.schedule ?: "") }
    var maxMembers by remember { mutableStateOf(groupToEdit?.maxMembers?.toString() ?: "20") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Date/Time Picker Logic
    val showDateTimePicker = {
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            TimePickerDialog(context, { _, hourOfDay, minute ->
                val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
                schedule = "$dayOfMonth/${month + 1}/$year at $formattedTime"
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editGroupId == null) "Create Study Group" else "Edit Group") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Group Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject (e.g., CS CORE)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = schedule,
                onValueChange = { schedule = it },
                label = { Text("Schedule") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = showDateTimePicker) {
                        Icon(Icons.Default.DateRange, contentDescription = "Pick Date/Time")
                    }
                }
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedTextField(
                value = maxMembers,
                onValueChange = { if (it.all { char -> char.isDigit() }) maxMembers = it },
                label = { Text("Member Limit") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    if (editGroupId == null) {
                        viewModel.createGroup(name, subject, description, schedule, maxMembers.toIntOrNull() ?: 20)
                    } else {
                        groupToEdit?.let {
                            viewModel.updateGroup(it.copy(
                                name = name,
                                subject = subject,
                                description = description,
                                schedule = schedule,
                                maxMembers = maxMembers.toIntOrNull() ?: 20
                            ))
                        }
                    }
                    onBack() 
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && subject.isNotBlank() && schedule.isNotBlank()
            ) {
                Text(if (editGroupId == null) "Create Group" else "Save Changes")
            }
        }
    }
}
