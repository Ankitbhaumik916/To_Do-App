package com.example.a1st

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.a1st.ui.theme._1STTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.a1st.SettingsDataStore
import com.example.a1st.OpenAISuggestions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = this
            val scope = rememberCoroutineScope()

            val isDarkMode by produceState(initialValue = false) {
                SettingsDataStore.isDarkModeEnabled(context).collect {
                    value = it
                }
            }

            _1STTheme(darkTheme = isDarkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoApp(
                        modifier = Modifier.padding(innerPadding),
                        isDark = isDarkMode,
                        onToggleTheme = { enabled ->
                            scope.launch {
                                SettingsDataStore.setDarkMode(context, enabled)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var taskText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val taskList = remember { mutableStateListOf<Task>() }
    var planText by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    var suggestedTasks by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        TaskDataStore.getTasks(context).collect {
            taskList.clear()
            taskList.addAll(it)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(if (isDark) "🌙" else "☀️")
                Switch(checked = isDark, onCheckedChange = onToggleTheme)
            }

            Text(
                text = "📝 Your Tasks",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    TextField(
                        value = taskText,
                        onValueChange = { taskText = it },
                        label = { Text("What's on your mind?") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(if (selectedDate.isBlank()) "📅 Pick Due Date" else "Due: $selectedDate")
                        }
                        Button(
                            onClick = {
                                if (taskText.isNotBlank()) {
                                    taskList.add(Task(taskText, dueDate = selectedDate))
                                    scope.launch {
                                        TaskDataStore.saveTasks(context, taskList)
                                    }
                                    taskText = ""
                                    selectedDate = ""
                                }
                            }
                        ) {
                            Text("Add ➕")
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = planText,
                onValueChange = { planText = it },
                label = { Text("Paste your weekly plan here") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                showSuggestions = true
                OpenAISuggestions.suggestTasksFromPlan(planText,
                    onResult = { tasks -> suggestedTasks = tasks },
                    onError = { error -> suggestedTasks = listOf("Error: $error") }
                )
            }, modifier = Modifier.padding(top = 8.dp)) {
                Text("🧠 Suggest Tasks")
            }
        }

        if (showSuggestions && suggestedTasks.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("🔮 Suggestions:", style = MaterialTheme.typography.titleMedium)
                    suggestedTasks.forEach { suggestion ->
                        Text("• $suggestion")
                    }
                }
            }
        }

        if (showDatePicker) {
            item {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val millis = datePickerState.selectedDateMillis
                                if (millis != null) {
                                    selectedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                        .format(Date(millis))
                                }
                                showDatePicker = false
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }

        items(taskList, key = { it.text }) { task ->
            var visible by remember { mutableStateOf(true) }

            AnimatedVisibility(
                visible = visible,
                exit = fadeOut(tween(300))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Checkbox(
                                checked = task.isDone,
                                onCheckedChange = {
                                    task.isDone = it
                                    scope.launch {
                                        TaskDataStore.saveTasks(context, taskList)
                                    }
                                }
                            )
                            Column {
                                Crossfade(targetState = task.isDone) { done ->
                                    Text(
                                        text = task.text,
                                        style = if (done)
                                            MaterialTheme.typography.bodyLarge.copy(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            )
                                        else
                                            MaterialTheme.typography.bodyLarge
                                    )
                                }
                                if (task.dueDate.isNotBlank()) {
                                    Text(
                                        text = "📅 Due: ${task.dueDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        TextButton(
                            onClick = {
                                visible = false
                                scope.launch {
                                    delay(300)
                                    taskList.remove(task)
                                    TaskDataStore.saveTasks(context, taskList)
                                }
                            }
                        ) { Text("❌") }
                    }
                }
            }
        }
    }
}
