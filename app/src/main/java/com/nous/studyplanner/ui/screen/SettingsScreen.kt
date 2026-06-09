package com.nous.studyplanner.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nous.studyplanner.ui.theme.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// ── Simple prefs helper ──
private object Prefs {
    private const val NAME = "alex_settings"
    fun get(ctx: Context, key: String, def: String): String =
        ctx.getSharedPreferences(NAME, 0).getString(key, def) ?: def
    fun set(ctx: Context, key: String, value: String) =
        ctx.getSharedPreferences(NAME, 0).edit().putString(key, value).apply()
}

// ── Language ──
enum class Lang(val code: String, val label: String) {
    ZH_CN("zh_CN", "简体中文"), ZH_TW("zh_TW", "繁體中文"), EN("en", "English")
}

private val strings = mapOf(
    "settings" to mapOf("zh_CN" to "设置", "zh_TW" to "設定", "en" to "Settings"),
    "lang" to mapOf("zh_CN" to "语言", "zh_TW" to "語言", "en" to "Language"),
    "theme" to mapOf("zh_CN" to "深色模式", "zh_TW" to "深色模式", "en" to "Dark Mode"),
    "accent" to mapOf("zh_CN" to "强调色", "zh_TW" to "強調色", "en" to "Accent Color"),
    "remind" to mapOf("zh_CN" to "提醒设置", "zh_TW" to "提醒設定", "en" to "Reminders"),
    "defaultRemind" to mapOf("zh_CN" to "默认提醒时间", "zh_TW" to "預設提醒時間", "en" to "Default Reminder"),
    "about" to mapOf("zh_CN" to "关于", "zh_TW" to "關於", "en" to "About"),
    "version" to mapOf("zh_CN" to "版本", "zh_TW" to "版本", "en" to "Version"),
    "developer" to mapOf("zh_CN" to "开发", "zh_TW" to "開發", "en" to "Developer"),
    "onTime" to mapOf("zh_CN" to "准时", "zh_TW" to "準時", "en" to "On time"),
    "before1" to mapOf("zh_CN" to "1分钟前", "zh_TW" to "1分鐘前", "en" to "1 min before"),
    "before5" to mapOf("zh_CN" to "5分钟前", "zh_TW" to "5分鐘前", "en" to "5 min before"),
    "before10" to mapOf("zh_CN" to "10分钟前", "zh_TW" to "10分鐘前", "en" to "10 min before"),
    "before15" to mapOf("zh_CN" to "15分钟前", "zh_TW" to "15分鐘前", "en" to "15 min before"),
    "light" to mapOf("zh_CN" to "浅色", "zh_TW" to "淺色", "en" to "Light"),
    "dark" to mapOf("zh_CN" to "深色", "zh_TW" to "深色", "en" to "Dark"),
    "system" to mapOf("zh_CN" to "跟随系统", "zh_TW" to "跟隨系統", "en" to "System"),
    "rmode" to mapOf("zh_CN" to "提醒模式", "zh_TW" to "提醒模式", "en" to "Reminder Mode"),
    "rmSound" to mapOf("zh_CN" to "铃声+振动", "zh_TW" to "鈴聲+振動", "en" to "Sound + Vibrate"),
    "rmVibrate" to mapOf("zh_CN" to "仅振动", "zh_TW" to "僅振動", "en" to "Vibrate Only"),
    "rmSilent" to mapOf("zh_CN" to "静音通知", "zh_TW" to "靜音通知", "en" to "Silent"),
    "footer" to mapOf("zh_CN" to "让每一分钟都有价值 ✨", "zh_TW" to "讓每一分鐘都有價值 ✨", "en" to "Make every minute count ✨"),
)

fun str(lang: String, key: String): String = strings[key]?.get(lang) ?: strings[key]?.get("en") ?: key

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // State
    var langCode by remember { mutableStateOf(Prefs.get(ctx, "lang", "zh_CN")) }
    var themeMode by remember { mutableStateOf(Prefs.get(ctx, "theme", "system")) }
    var accentColor by remember { mutableStateOf(Prefs.get(ctx, "accent", "blue")) }
    var remindMin by remember { mutableIntStateOf(Prefs.get(ctx, "remind", "5").toIntOrNull() ?: 5) }
    var remindMode by remember { mutableStateOf(Prefs.get(ctx, "reminder_mode", "sound+vibrate")) }

    val t = { key: String -> str(langCode, key) }
    val remindOptions = listOf(0 to "onTime", 1 to "before1", 5 to "before5", 10 to "before10", 15 to "before15")
    val themeOptions = listOf("light" to "light", "dark" to "dark", "system" to "system")
    val accentColors = listOf(
        "blue" to SystemBlue, "green" to SystemGreen, "orange" to SystemOrange,
        "red" to SystemRed, "purple" to Color(0xFF9B59B6), "pink" to Color(0xFFFF6B6B)
    )

    fun saveAndApply() {
        Prefs.set(ctx, "lang", langCode)
        Prefs.set(ctx, "theme", themeMode)
        Prefs.set(ctx, "accent", accentColor)
        Prefs.set(ctx, "remind", remindMin.toString())
        Prefs.set(ctx, "reminder_mode", remindMode)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(t("settings"), fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = { saveAndApply(); onBack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {

            // ── Language ──
            SectionLabel(t("lang"))
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Lang.entries.forEachIndexed { i, lang ->
                        Row(Modifier.fillMaxWidth().clickable { langCode = lang.code; saveAndApply() }.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(lang.label, fontSize = 15.sp)
                            if (lang.code == langCode) Icon(Icons.Filled.CheckCircle, null, tint = SystemBlue, modifier = Modifier.size(20.dp))
                        }
                        if (i < Lang.entries.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Theme mode ──
            SectionLabel(t("theme"))
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    themeOptions.forEachIndexed { i, (value, key) ->
                        Row(Modifier.fillMaxWidth().clickable { themeMode = value; saveAndApply() }.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(t(key), fontSize = 15.sp)
                            if (value == themeMode) Icon(Icons.Filled.CheckCircle, null, tint = SystemBlue, modifier = Modifier.size(20.dp))
                        }
                        if (i < themeOptions.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Accent color ──
            SectionLabel(t("accent"))
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    accentColors.forEach { (name, color) ->
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color).clickable { accentColor = name; saveAndApply() },
                            contentAlignment = Alignment.Center) {
                            if (name == accentColor) Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Reminder ──
            SectionLabel(t("remind"))
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    remindOptions.forEachIndexed { i, (value, key) ->
                        Row(Modifier.fillMaxWidth().clickable { remindMin = value; saveAndApply() }.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(t(key), fontSize = 15.sp)
                            if (value == remindMin) Icon(Icons.Filled.CheckCircle, null, tint = SystemBlue, modifier = Modifier.size(20.dp))
                        }
                        if (i < remindOptions.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Reminder Mode ──
            SectionLabel(t("rmode"))
            val modeOptions = listOf("sound+vibrate" to "rmSound", "vibrate" to "rmVibrate", "silent" to "rmSilent")
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    modeOptions.forEachIndexed { i, (value, key) ->
                        Row(Modifier.fillMaxWidth().clickable { remindMode = value; saveAndApply() }.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(t(key), fontSize = 15.sp)
                            if (value == remindMode) Icon(Icons.Filled.CheckCircle, null, tint = SystemBlue, modifier = Modifier.size(20.dp))
                        }
                        if (i < modeOptions.size - 1) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── About ──
            SectionLabel(t("about"))
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t("version"), fontSize = 15.sp); Text(com.nous.studyplanner.BuildConfig.VERSION_NAME, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(t("developer"), fontSize = 15.sp); Text("Alex", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(t("footer"), Modifier.fillMaxWidth(), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
}
