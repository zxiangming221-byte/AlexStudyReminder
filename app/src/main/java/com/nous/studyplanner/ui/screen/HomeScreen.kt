package com.nous.studyplanner.ui.screen

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nous.studyplanner.data.entity.StudyPlan
import com.nous.studyplanner.di.AppEntryPoint
import com.nous.studyplanner.ui.theme.*
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onCreatePlan: () -> Unit, onPlanClick: (Long) -> Unit, onSettings: () -> Unit) {
    val app = LocalContext.current.applicationContext as Application
    val entryPoint = remember { EntryPointAccessors.fromApplication(app, AppEntryPoint::class.java) }
    val planDao = remember { entryPoint.studyPlanDao() }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Notification permission (Android 13+)
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not, app still works */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    var plans by remember { mutableStateOf<List<StudyPlan>>(emptyList()) }
    LaunchedEffect(Unit) {
        planDao.getAllPlans().collect { plans = it }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(greet(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, "设置", tint = MaterialTheme.colorScheme.onSurfaceVariant) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
        floatingActionButton = { RainbowFab(onClick = onCreatePlan) }
    ) { padding ->
        if (plans.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 48.sp); Spacer(Modifier.height(12.dp))
                    Text("还没有学习计划", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text("点击 + 创建", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                }
            }
        } else {
            LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(plans, key = { it.id }) { plan ->
                    PlanCard(plan, onClick = { onPlanClick(plan.id) },
                        onDelete = { scope.launch { planDao.deleteById(plan.id) } }, index = plans.indexOf(plan))
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

private fun greet(): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (h) { in 0..5 -> "夜深了 🌙"; in 6..11 -> "早上好 ☀️"; in 12..13 -> "中午好 🌤️"; in 14..17 -> "下午好 🌈"; in 18..21 -> "晚上好 🌆"; else -> "夜深了 🌙" }
}

@Composable
private fun RainbowFab(onClick: () -> Unit) {
    val angle by rememberInfiniteTransition(label = "f").animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "a")
    FloatingActionButton(onClick = onClick, shape = CircleShape, containerColor = Color.Transparent, contentColor = Color.White,
        modifier = Modifier.size(60.dp).clip(CircleShape).background(Brush.sweepGradient(RainbowGradient, center = Offset(0.5f, 0.5f)))) {
        Icon(Icons.Default.Add, "新建", tint = Color.White)
    }
}

@Composable
private fun PlanCard(plan: StudyPlan, onClick: () -> Unit, onDelete: () -> Unit, index: Int) {
    var visible by remember { mutableStateOf(false) }
    val s by animateFloatAsState(if (visible) 1f else 0.9f, spring(dampingRatio = 0.5f, stiffness = 200f), label = "sc")
    LaunchedEffect(Unit) { kotlinx.coroutines.delay((index * 40).toLong()); visible = true }
    val fmt = remember { SimpleDateFormat("M月d日 HH:mm", Locale.CHINESE) }
    Card(Modifier.fillMaxWidth().scale(s).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(44.dp).clip(RoundedCornerShape(2.dp)).background(Brush.verticalGradient(listOf(RainbowGradient[index % 6], RainbowGradient[(index + 1) % 6]))))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(plan.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(fmt.format(Date(plan.createdAt)), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.DeleteOutline, "删除", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)) }
        }
    }
}
