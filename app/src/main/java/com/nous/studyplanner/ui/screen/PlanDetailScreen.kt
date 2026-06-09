package com.nous.studyplanner.ui.screen

import android.app.Application
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Block

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nous.studyplanner.data.entity.StudyPlan
import com.nous.studyplanner.data.entity.StudyTask
import com.nous.studyplanner.di.AppEntryPoint
import com.nous.studyplanner.ui.theme.*
import com.nous.studyplanner.worker.ReminderScheduler
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanDetailScreen(planId: Long, onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as Application
    val ep = remember { EntryPointAccessors.fromApplication(app, AppEntryPoint::class.java) }
    val planDao = remember { ep.studyPlanDao() }
    val taskDao = remember { ep.studyTaskDao() }
    val scope = rememberCoroutineScope()

    var plan by remember { mutableStateOf<StudyPlan?>(null) }
    var tasks by remember { mutableStateOf<List<StudyTask>>(emptyList()) }
    var showDelete by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    LaunchedEffect(planId) {
        plan = planDao.getPlanById(planId)
        taskDao.getTasksByPlanId(planId).collect { tasks = it }
    }

    if (showDelete) AlertDialog(onDismissRequest = { showDelete = false },
        title = { Text("删除计划") }, text = { Text("确定删除「${plan?.title ?: ""}」吗？") },
        confirmButton = { TextButton(onClick = {
            scope.launch { planDao.deleteById(planId) }; showDelete = false; onBack()
        }) { Text("删除", color = SystemRed) } },
        dismissButton = { TextButton(onClick = { showDelete = false }) { Text("取消") } })

    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopAppBar(
            title = { Column { Text(plan?.title ?: "", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                if (tasks.isNotEmpty()) Text("${tasks.size}个 · ${tasks.count { it.isCompleted }}完成", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } },
            actions = {
                IconButton(onClick = { editMode = !editMode }) {
                    Icon(Icons.Filled.Edit, if (editMode) "阅读" else "编辑", tint = if (editMode) SystemBlue else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showDelete = true }) { Icon(Icons.Default.Delete, "删除", tint = SystemRed.copy(alpha = 0.6f)) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) }
    ) { padding ->
        if (tasks.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("暂无任务", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        else {
            val grouped = tasks.groupBy { it.date }
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                grouped.forEach { (date, ts) ->
                    item(key = "h_$date") { DateHeader(date, ts.size) }
                    items(ts, key = { it.id }) { task ->
                        TaskCard(task = task, editMode = editMode,
                            onToggle = { scope.launch { taskDao.setCompleted(task.id, !task.isCompleted) } },
                            onUpdate = { updated ->
                                scope.launch { taskDao.update(updated) }
                            },
                            index = ts.indexOf(task),
                            taskDao = taskDao)
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun DateHeader(date: String, count: Int) {
    val d = remember(date) {
        try { val p = date.split("-"); val cal = java.util.Calendar.getInstance(); cal.set(p[0].toInt(), p[1].toInt() - 1, p[2].toInt()); "${p[1].toInt()}月${p[2].toInt()}日 ${listOf("周一","周二","周三","周四","周五","周六","周日")[cal.get(java.util.Calendar.DAY_OF_WEEK) - 2]}" } catch (_: Exception) { date }
    }
    Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(3.dp).height(18.dp).clip(RoundedCornerShape(1.5.dp)).background(SystemBlue)); Spacer(Modifier.width(10.dp))
        Text(d, fontWeight = FontWeight.Bold, fontSize = 16.sp); Spacer(Modifier.width(6.dp))
        Text("${count}项", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TaskCard(task: StudyTask, editMode: Boolean, onToggle: () -> Unit, onUpdate: (StudyTask) -> Unit, index: Int, taskDao: com.nous.studyplanner.data.dao.StudyTaskDao) {
    var visible by remember { mutableStateOf(false) }
    val s by animateFloatAsState(if (visible) 1f else 0.92f, spring(dampingRatio = 0.5f, stiffness = 200f), label = "ts")
    LaunchedEffect(Unit) { kotlinx.coroutines.delay((index * 30).toLong()); visible = true }
    val c = RainbowGradient[index % 6]
    val scope = rememberCoroutineScope()

    var editSubject by remember(task.id) { mutableStateOf(task.subject) }
    var editContent by remember(task.id) { mutableStateOf(task.content) }
    var editTags by remember(task.id) { mutableStateOf(task.tags) }

    // Tag history from prefs
    val ctx = LocalContext.current
    val tagHistory = remember { ctx.getSharedPreferences("alex_settings", 0).getString("tag_history", "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList() }

    Card(Modifier.fillMaxWidth().scale(s), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column {
            // ── Header row (always visible) ──
            Row(Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(if (task.isCompleted) SystemGray5 else c.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("${task.startTime}-${task.endTime}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (task.isCompleted) SystemGray else c.copy(alpha = 0.85f))
                }
                Spacer(Modifier.width(10.dp))
                if (editMode) {
                    OutlinedTextField(editSubject, { editSubject = it }, Modifier.weight(1f).height(48.dp),
                        singleLine = true, textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        shape = RoundedCornerShape(8.dp))
                } else {
                    Text(task.subject, Modifier.weight(1f), fontSize = 15.sp,
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface)
                }
                if (task.isCompleted) Icon(Icons.Filled.CheckCircle, "完成", tint = SystemGreen, modifier = Modifier.size(22.dp))
                else Spacer(Modifier.size(22.dp))
            }

            // ── Content & Tags section ──
            if (task.content.isNotBlank() || task.tags.isNotBlank() || editMode) {
                if (editMode) {
                    // Edit content
                    OutlinedTextField(editContent, { editContent = it }, Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp).heightIn(min = 60.dp),
                        label = { Text("内容") }, shape = RoundedCornerShape(8.dp),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp))
                    Spacer(Modifier.height(4.dp))
                    // Edit tags
                    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(editTags, { editTags = it }, Modifier.weight(1f).height(48.dp),
                            singleLine = true, label = { Text("标签") }, placeholder = { Text("学习,数学,重点") },
                            shape = RoundedCornerShape(8.dp), textStyle = LocalTextStyle.current.copy(fontSize = 13.sp))
                        Spacer(Modifier.width(6.dp))
                        // Save edits button
                        FilledTonalButton(onClick = {
                            val updated = task.copy(subject = editSubject, content = editContent, tags = editTags)
                            onUpdate(updated)
                            // Save tags to history
                            val allTags = (tagHistory + editTags.split(",", "，").map { it.trim() }.filter { it.isNotBlank() }).distinct().take(20)
                            ctx.getSharedPreferences("alex_settings", 0).edit().putString("tag_history", allTags.joinToString(",")).apply()
                        }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                            Text("保存", fontSize = 12.sp)
                        }
                    }
                    // Tag suggestions
                    if (tagHistory.isNotEmpty()) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            tagHistory.take(8).forEach { tag ->
                                SuggestionChip(onClick = {
                                    val current = editTags.split(",", "，").map { it.trim() }.filter { it.isNotBlank() }
                                    if (tag !in current) editTags = (current + tag).joinToString(", ")
                                }, label = { Text(tag, fontSize = 11.sp) })
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                } else {
                    // Read mode: show content and tags
                    if (task.content.isNotBlank()) {
                        Text(task.content, Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp,
                            maxLines = 3)
                    }
                    if (task.tags.isNotBlank()) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            task.tags.split(",", "，").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                                SuggestionChip(onClick = {}, label = { Text(tag, fontSize = 10.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = c.copy(alpha = 0.08f)))
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }

            // ── Reminder toggle row ──
            Row(Modifier.fillMaxWidth().clickable { scope.launch { taskDao.setReminderEnabled(task.id, !task.reminderEnabled) } }
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (task.reminderEnabled) Icons.Filled.Notifications else Icons.Filled.Block,
                    "提醒", tint = if (task.reminderEnabled) SystemBlue.copy(alpha = 0.7f) else SystemGray.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (task.reminderEnabled) "已开启提醒" else "已关闭提醒", fontSize = 11.sp,
                    color = if (task.reminderEnabled) SystemBlue.copy(alpha = 0.7f) else SystemGray)
            }
        }
    }
}
