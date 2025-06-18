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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a1st.ui.theme._1STTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            _1STTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TodoApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TodoApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var taskText by remember { mutableStateOf("") }
    val taskList = remember { mutableStateListOf<Task>() }

    // Load saved tasks once on first launch
    LaunchedEffect(Unit) {
        TaskDataStore.getTasks(context).collect {
            taskList.clear()
            taskList.addAll(it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = taskText,
            onValueChange = { taskText = it },
            label = { Text("Enter task") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (taskText.isNotBlank()) {
                    taskList.add(Task(taskText))
                    scope.launch {
                        TaskDataStore.saveTasks(context, taskList)
                    }
                    taskText = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Your Tasks:", style = MaterialTheme.typography.headlineSmall)

        if (taskList.isEmpty()) {
            Text("No tasks yet!", modifier = Modifier.padding(8.dp))
        } else {
            LazyColumn {
                items(taskList, key = { it.text }) { task ->
                    var visible by remember { mutableStateOf(true) }

                    AnimatedVisibility(
                        visible = visible,
                        exit = fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
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
                            }

                            TextButton(
                                onClick = {
                                    visible = false
                                    scope.launch {
                                        delay(300) // Wait for fade-out animation
                                        taskList.remove(task)
                                        TaskDataStore.saveTasks(context, taskList)
                                    }
                                }
                            ) {
                                Text("❌")
                            }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoListPreview() {
    _1STTheme {
        TodoApp()
    }
}
