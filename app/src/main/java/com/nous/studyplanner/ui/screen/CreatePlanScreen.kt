package com.nous.studyplanner.ui.screen

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nous.studyplanner.data.entity.StudyPlan
import com.nous.studyplanner.data.entity.StudyTask
import com.nous.studyplanner.di.AppEntryPoint
import com.nous.studyplanner.parser.ParseResult
import com.nous.studyplanner.ui.theme.*
import com.nous.studyplanner.worker.ReminderScheduler
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(onPlanCreated: () -> Unit, onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as Application
    val entryPoint = remember { EntryPointAccessors.fromApplication(app, AppEntryPoint::class.java) }
    val planDao = remember { entryPoint.studyPlanDao() }
    val taskDao = remember { entryPoint.studyTaskDao() }
    val parser = remember { entryPoint.planParser() }
    val scope = rememberCoroutineScope()

    var planText by remember { mutableStateOf("") }
    var planTitle by remember { mutableStateOf("") }
    var parseResult by remember { mutableStateOf<ParseResult?>(null) }
    var isParsing by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(saved) { if (saved) { kotlinx.coroutines.delay(400); onPlanCreated() } }

    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopAppBar(title = { Text("新建计划", fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            OutlinedTextField(value = planTitle, onValueChange = { planTitle = it }, label = { Text("计划名称（可选）") },
                placeholder = { Text("如：期末复习") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SystemBlue, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface))
            Spacer(Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SystemBlue.copy(alpha = 0.07f)), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("📋 格式示例", fontWeight = FontWeight.SemiBold, fontSize = 13.sp); Spacer(Modifier.height(4.dp))
                    Text("6月7日\n08:00-10:00 数学刷题\n10:30-12:00 英语\n\n6月8日\n14:00-16:00 物理复习", fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = planText, onValueChange = { planText = it },
                placeholder = { Text("在此粘贴学习计划...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SystemBlue, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface))
            Spacer(Modifier.height(16.dp))
            Button(onClick = { isParsing = true; parseResult = parser.parse(planText); isParsing = false; showPreview = true },
                enabled = planText.isNotBlank() && !isParsing, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SystemBlue), contentPadding = PaddingValues(vertical = 14.dp)) {
                if (isParsing) { CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)) }
                Text("解析计划", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            AnimatedVisibility(visible = showPreview && parseResult != null) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    parseResult?.let { r ->
                        if (r.errors.isNotEmpty()) Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SystemRed.copy(alpha = 0.07f)), elevation = CardDefaults.cardElevation(0.dp)) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Outlined.ErrorOutline, null, tint = SystemRed, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
                                Column { r.errors.forEach { Text(it, fontSize = 12.sp, color = SystemRed, lineHeight = 18.sp); Spacer(Modifier.height(2.dp)) } }
                            }
                        }
                        if (r.entries.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SystemGreen.copy(alpha = 0.07f)), elevation = CardDefaults.cardElevation(0.dp)) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Outlined.CheckCircle, null, tint = SystemGreen, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp))
                                    Column {
                                        Text("识别到 ${r.entries.size} 个任务", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = SystemGreen); Spacer(Modifier.height(4.dp))
                                        r.entries.forEach { Text("${it.date}  ${it.startTime}-${it.endTime}  ${it.subject}", fontSize = 11.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = {
                                scope.launch {
                                    val plan = StudyPlan(title = planTitle.ifBlank { r.entries.firstOrNull()?.date?.let { "计划 $it" } ?: "学习计划" }, rawText = planText)
                                    val planId = planDao.insert(plan)
                                    val tasks = r.entries.map { e -> StudyTask(planId = planId, date = e.date, startTime = e.startTime, endTime = e.endTime, subject = e.subject) }
                                    taskDao.insertAll(tasks)
                                    val savedTasks = taskDao.getTasksByPlanIdAsList(planId)
                                    savedTasks.forEach { t -> val rid = ReminderScheduler.schedule(app, t); if (rid.isNotEmpty()) taskDao.setWorkRequestId(t.id, rid) }
                                    saved = true
                                }
                            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SystemGreen), contentPadding = PaddingValues(vertical = 14.dp)) {
                                Text("保存计划", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }

            // ── Manual single-task entry ──
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(Modifier.height(16.dp))
            Text("或手动添加单条任务", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            var manDate by remember { mutableStateOf("") }
            var manStart by remember { mutableStateOf("") }
            var manEnd by remember { mutableStateOf("") }
            var manSubject by remember { mutableStateOf("") }
            var manReminder by remember { mutableStateOf(true) }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(manDate, { manDate = it }, label = { Text("日期") }, placeholder = { Text("6月8日") },
                    modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SystemBlue, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface))
                OutlinedTextField(manStart, { manStart = it }, label = { Text("开始") }, placeholder = { Text("08:00") },
                    modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SystemBlue, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface))
                OutlinedTextField(manEnd, { manEnd = it }, label = { Text("结束") }, placeholder = { Text("10:00") },
                    modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SystemBlue, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(manSubject, { manSubject = it }, label = { Text("科目") }, placeholder = { Text("数学刷题") },
                    modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SystemBlue, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface))
                Spacer(Modifier.width(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { manReminder = !manReminder }) {
                    Icon(if (manReminder) Icons.Filled.Notifications else Icons.Filled.Block,
                        null, tint = if (manReminder) SystemBlue else SystemGray, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (manReminder) "提醒" else "不提醒", fontSize = 12.sp,
                        color = if (manReminder) SystemBlue else SystemGray)
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                scope.launch {
                    val parsed = parser.parse("${manDate}\n${manStart}-${manEnd} ${manSubject}")
                    if (parsed.entries.isEmpty()) return@launch
                    val plan = StudyPlan(title = manSubject.ifBlank { "手动任务" }, rawText = "")
                    val planId = planDao.insert(plan)
                    val entry = parsed.entries.first()
                    val task = StudyTask(planId = planId, date = entry.date, startTime = entry.startTime,
                        endTime = entry.endTime, subject = entry.subject, reminderEnabled = manReminder)
                    taskDao.insertAll(listOf(task))
                    if (manReminder) {
                        val saved = taskDao.getTasksByPlanIdAsList(planId)
                        saved.forEach { t -> ReminderScheduler.schedule(app, t) }
                    }
                    saved = true
                }
            }, enabled = manSubject.isNotBlank() && manStart.isNotBlank() && manEnd.isNotBlank(),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SystemBlue),
                contentPadding = PaddingValues(vertical = 12.dp)) {
                Text("添加任务", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}
