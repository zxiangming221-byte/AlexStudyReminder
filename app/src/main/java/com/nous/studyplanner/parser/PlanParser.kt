package com.nous.studyplanner.parser

import java.time.LocalDate
import java.time.Year

class PlanParser {

    // ── Date patterns ──
    private val datePatterns = listOf(
        Regex("""^((?:20\d{2})?\s*年\s*)?(\d{1,2})\s*月\s*(\d{1,2})\s*[日号]\s*$"""),
        Regex("""^()(\d{1,2})\s*/\s*(\d{1,2})\s*$"""),
        Regex("""^()(\d{1,2})\s*\.\s*(\d{1,2})\s*$"""),
    )

    // ── Standard time: "08:00-10:00 数学" or "8:00~10:00 数学" ──
    private val stdTimePattern = Regex(
        """^(\d{1,2})[:：](\d{2})\s*[-~～至到]\s*(\d{1,2})[:：](\d{2})\s+(.+)$"""
    )

    // ── Chinese time: "上午8:00-10:00 数学", "下午三点-五点 语文" ──
    // Period: 凌晨/早上/早晨/上午/中午/下午/晚上/傍晚
    // Hour: digits or Chinese numbers (一~十)
    // Minute: digits or 半(30) or absent(00)
    // Separator: -~～至到
    private val cnPeriods = mapOf(
        "凌晨" to 0, "早上" to 0, "早晨" to 0,
        "上午" to 0, "中午" to 0,
        "下午" to 12, "傍晚" to 12, "晚上" to 12,
    )

    private val cnDigits = mapOf(
        '零' to 0, '〇' to 0, '一' to 1, '二' to 2, '两' to 2,
        '三' to 3, '四' to 4, '五' to 5, '六' to 6,
        '七' to 7, '八' to 8, '九' to 9, '十' to 10,
    )

    // Matches: "上午8:00" "下午三点" "晚上9点半"
    private val cnSingleTime = Regex(
        """^(凌晨|早上|早晨|上午|中午|下午|傍晚|晚上)?\s*(\d{1,2}|[零〇一二两三四五六七八九十]+)\s*[:：点時]\s*(\d{1,2}|半)?\s*$"""
    )

    // Matches full task: "上午8:00-10:00 数学" "下午三点-五点 语文"
    private val cnTimePattern = Regex(
        """^(凌晨|早上|早晨|上午|中午|下午|傍晚|晚上)?\s*(\d{1,2}|[零〇一二两三四五六七八九十]+)\s*[:：点時]?\s*(\d{1,2}|半)?\s*[-~～至到]\s*(凌晨|早上|早晨|上午|中午|下午|傍晚|晚上)?\s*(\d{1,2}|[零〇一二两三四五六七八九十]+)\s*[:：点時]?\s*(\d{1,2}|半)?\s+(.+)$"""
    )

    // ── Public API ──

    fun parse(text: String, year: Int = Year.now().value): ParseResult {
        val entries = mutableListOf<ParsedEntry>()
        val errors = mutableListOf<String>()
        val lines = text.trim().lines()

        var currentDate: String? = null
        var lineNum = 0
        val today = LocalDate.now().toString() // "2026-06-08"

        for (line in lines) {
            lineNum++
            val trimmed = line.trim()

            if (trimmed.isEmpty() || trimmed.matches(Regex("""^[-=*#_]{3,}$"""))) continue

            // ── Try date header ──
            val dateResult = matchDate(trimmed, year)
            if (dateResult != null) {
                currentDate = dateResult
                continue
            }

            // ── Try standard time: "08:00-10:00 数学" ──
            val stdMatch = stdTimePattern.find(trimmed)
            if (stdMatch != null) {
                val date = currentDate ?: today  // ⬅ default to today
                if (currentDate == null) {
                    errors.add("第${lineNum}行: 未指定日期，默认使用今天($today) — \"$trimmed\"")
                }
                entries.add(ParsedEntry(
                    date = date,
                    startTime = "${stdMatch.groupValues[1].padStart(2, '0')}:${stdMatch.groupValues[2]}",
                    endTime = "${stdMatch.groupValues[3].padStart(2, '0')}:${stdMatch.groupValues[4]}",
                    subject = stdMatch.groupValues[5].trim()
                ))
                continue
            }

            // ── Try Chinese time: "上午8:00-10:00 数学" "下午三点-五点 语文" ──
            val cnMatch = cnTimePattern.find(trimmed)
            if (cnMatch != null) {
                val date = currentDate ?: today
                if (currentDate == null) {
                    errors.add("第${lineNum}行: 未指定日期，默认使用今天($today) — \"$trimmed\"")
                }
                val startPeriod = cnMatch.groupValues[1]
                val startHourRaw = cnMatch.groupValues[2]
                val startMinRaw = cnMatch.groupValues[3]
                val endPeriod = cnMatch.groupValues[4]
                val endHourRaw = cnMatch.groupValues[5]
                val endMinRaw = cnMatch.groupValues[6]
                val subject = cnMatch.groupValues[7].trim()

                val start = resolveCnTime(startPeriod, startHourRaw, startMinRaw)
                val end = resolveCnTime(endPeriod, endHourRaw, endMinRaw)

                if (start != null && end != null) {
                    entries.add(ParsedEntry(
                        date = date,
                        startTime = start,
                        endTime = end,
                        subject = subject
                    ))
                    continue
                }
            }

            // ── Unrecognized ──
            if (currentDate != null) {
                errors.add("第${lineNum}行: 格式无法识别 — \"$trimmed\"")
            } else {
                errors.add("第${lineNum}行: 无法识别 — \"$trimmed\"")
            }
        }

        if (entries.isEmpty() && errors.isEmpty() && currentDate != null) {
            errors.add("已识别日期但未找到任何任务行")
        }

        return ParseResult(entries, errors)
    }

    // ── Helpers ──

    private fun matchDate(text: String, year: Int): String? {
        for (pattern in datePatterns) {
            val match = pattern.find(text) ?: continue
            return when {
                match.groupValues[1].isNotBlank() -> {
                    val y = match.groupValues[1].replace("年", "").trim()
                    val m = match.groupValues[2].padStart(2, '0')
                    val d = match.groupValues[3].padStart(2, '0')
                    "$y-$m-$d"
                }
                else -> {
                    val m = match.groupValues[2].padStart(2, '0')
                    val d = match.groupValues[3].padStart(2, '0')
                    "$year-$m-$d"
                }
            }
        }
        return null
    }

    private fun resolveCnTime(period: String, hourRaw: String, minRaw: String): String? {
        if (hourRaw.isEmpty()) return null

        val offset = cnPeriods[period] ?: 0
        val hour = parseCnNumber(hourRaw) ?: hourRaw.toIntOrNull() ?: return null
        val minute = when {
            minRaw.isEmpty() || minRaw == "整" -> 0
            minRaw == "半" -> 30
            else -> minRaw.toIntOrNull() ?: return null
        }

        // Apply period offset (e.g., 下午 3 → 15)
        val finalHour = if (offset > 0 && hour <= 12) hour + offset else hour

        return "${finalHour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }

    private fun parseCnNumber(s: String): Int? {
        if (s.length == 1) return cnDigits[s[0]]
        if (s.length == 2) {
            val a = cnDigits[s[0]] ?: return null
            val b = cnDigits[s[1]] ?: return null
            // "二十" → 20, "十二" → 12, "十五" → 15
            return when {
                s[1] == '十' -> a * 10           // 二十 → 20
                s[0] == '十' -> 10 + b            // 十二 → 12
                else -> a * 10 + b               // fallback
            }
        }
        if (s.length == 3) {
            // "二十三" → first*10 + third
            val a = cnDigits[s[0]] ?: return null
            val c = cnDigits[s[2]] ?: return null
            return a * 10 + c
        }
        return null
    }
}
