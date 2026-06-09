package com.nous.studyplanner.parser

import java.time.Year

class PlanParser {

    // Matches date headers: "6月7日", "06月07日", "2026年6月7日"
    // Also handles: "6月7号", "6/7", "6.7"
    private val datePatterns = listOf(
        // "2026年6月7日" or "6月7日"
        Regex("""^((?:20\d{2})?\s*年\s*)?(\d{1,2})\s*月\s*(\d{1,2})\s*[日号]\s*$"""),
        // "6/7" or "06/07"
        Regex("""^()(\d{1,2})\s*/\s*(\d{1,2})\s*$"""),
        // "6.7" or "06.07"
        Regex("""^()(\d{1,2})\s*\.\s*(\d{1,2})\s*$"""),
    )

    // Matches task line: "08:00-10:00 数学刷题"
    // Supports separators: -, ~, ～, 至, 到
    // Supports Chinese colon: ：
    private val taskPattern = Regex(
        """^(\d{1,2})[:：](\d{2})\s*[-~～至到]\s*(\d{1,2})[:：](\d{2})\s+(.+)$"""
    )

    fun parse(text: String, year: Int = Year.now().value): ParseResult {
        val entries = mutableListOf<ParsedEntry>()
        val errors = mutableListOf<String>()
        val lines = text.trim().lines()

        var currentDate: String? = null
        var lineNum = 0

        for (line in lines) {
            lineNum++
            val trimmed = line.trim()

            // Skip empty lines and separator lines (like "---", "===")
            if (trimmed.isEmpty() || trimmed.matches(Regex("""^[-=*#_]{3,}$"""))) continue

            // Try matching a date header first
            val dateResult = matchDate(trimmed, year)
            if (dateResult != null) {
                currentDate = dateResult
                continue
            }

            // Try matching a task line
            val taskMatch = taskPattern.find(trimmed)
            if (taskMatch != null) {
                if (currentDate == null) {
                    errors.add("第${lineNum}行: 找不到对应的日期 — \"$trimmed\"")
                    continue
                }
                val startH = taskMatch.groupValues[1].padStart(2, '0')
                val startM = taskMatch.groupValues[2]
                val endH = taskMatch.groupValues[3].padStart(2, '0')
                val endM = taskMatch.groupValues[4]
                val subject = taskMatch.groupValues[5].trim()

                entries.add(
                    ParsedEntry(
                        date = currentDate,
                        startTime = "$startH:$startM",
                        endTime = "$endH:$endM",
                        subject = subject
                    )
                )
                continue
            }

            // Line not recognized
            if (currentDate != null) {
                errors.add("第${lineNum}行: 格式无法识别 — \"$trimmed\"")
            } else {
                errors.add("第${lineNum}行: 需要先指定日期（如\"6月7日\"） — \"$trimmed\"")
            }
        }

        if (entries.isEmpty() && errors.isEmpty() && currentDate != null) {
            errors.add("已识别日期但未找到任何任务行")
        }

        return ParseResult(entries, errors)
    }

    private fun matchDate(text: String, year: Int): String? {
        for (pattern in datePatterns) {
            val match = pattern.find(text) ?: continue
            return when {
                // "2026年6月7日" format
                match.groupValues[1].isNotBlank() -> {
                    val y = match.groupValues[1].replace("年", "").trim()
                    val m = match.groupValues[2].padStart(2, '0')
                    val d = match.groupValues[3].padStart(2, '0')
                    "$y-$m-$d"
                }
                // "6月7日" or "6/7" or "6.7" format
                else -> {
                    val m = match.groupValues[2].padStart(2, '0')
                    val d = match.groupValues[3].padStart(2, '0')
                    "$year-$m-$d"
                }
            }
        }
        return null
    }
}
