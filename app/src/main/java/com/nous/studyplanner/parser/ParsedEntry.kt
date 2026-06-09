package com.nous.studyplanner.parser

data class ParsedEntry(
    val date: String,       // "2026-06-07"
    val startTime: String,  // "08:00"
    val endTime: String,    // "10:00"
    val subject: String     // "数学刷题"
)

data class ParseResult(
    val entries: List<ParsedEntry>,
    val errors: List<String>
)
