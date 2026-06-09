package com.nous.studyplanner.parser

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PlanParserTest {

    private val parser = PlanParser()

    // ── Original tests ──

    @Test fun `parse standard format`() {
        val result = parser.parse("6月7日\n08:00-10:00 数学刷题\n10:30-12:00 英语背单词", 2026)
        assertEquals(2, result.entries.size)
        assertEquals("2026-06-07", result.entries[0].date)
        assertEquals("08:00", result.entries[0].startTime)
        assertEquals("10:00", result.entries[0].endTime)
        assertEquals("数学刷题", result.entries[0].subject)
    }

    @Test fun `parse multiple dates`() {
        val result = parser.parse("6月7日\n08:00-10:00 数学\n14:00-16:00 物理\n\n6月8日\n09:00-11:00 英语", 2026)
        assertEquals(3, result.entries.size)
        assertEquals("2026-06-07", result.entries[0].date)
        assertEquals("2026-06-08", result.entries[2].date)
    }

    @Test fun `parse short time format`() {
        val result = parser.parse("6月7日\n8:00-10:00 语文\n8:30-9:30 历史", 2026)
        assertEquals(2, result.entries.size)
        assertEquals("08:00", result.entries[0].startTime)
    }

    @Test fun `parse alternative separators`() {
        val result = parser.parse("6月7日\n08:00~10:00 数学\n08:00至10:00 物理\n08:00到10:00 化学", 2026)
        assertEquals(3, result.entries.size)
    }

    @Test fun `parse full year format`() {
        val result = parser.parse("2026年6月7日\n08:00-10:00 考试", 2026)
        assertEquals("2026-06-07", result.entries[0].date)
    }

    @Test fun `parse slash and dot date`() {
        assertEquals(1, parser.parse("6/7\n08:00-10:00 数学", 2026).entries.size)
        assertEquals(1, parser.parse("6.7\n08:00-10:00 数学", 2026).entries.size)
    }

    @Test fun `chinese colon time`() {
        val result = parser.parse("6月7日\n08：00-10：00 全角冒号", 2026)
        assertEquals(1, result.entries.size)
        assertEquals("08:00", result.entries[0].startTime)
    }

    // ── New: No date → default today ──

    @Test fun `no date defaults to today`() {
        val result = parser.parse("08:00-10:00 数学\n14:00-16:00 英语")
        val today = LocalDate.now().toString()
        assertEquals(2, result.entries.size)
        assertEquals(today, result.entries[0].date)
        assertEquals(today, result.entries[1].date)
    }

    @Test fun `some tasks with date some without`() {
        val result = parser.parse("6月7日\n08:00-10:00 数学\n\n08:00-10:00 英语", 2026)
        assertEquals(2, result.entries.size)
        assertEquals("2026-06-07", result.entries[0].date)
        // Second task inherits previous date header
        assertEquals("2026-06-07", result.entries[1].date)
    }

    // ── New: Chinese time recognition ──

    @Test fun `chinese time with digits`() {
        val result = parser.parse("6月7日\n上午8:00-10:00 数学\n下午3:00-5:00 物理")
        assertEquals(2, result.entries.size)
        assertEquals("08:00", result.entries[0].startTime)
        assertEquals("15:00", result.entries[1].startTime)
    }

    @Test fun `chinese time with chinese numbers`() {
        val result = parser.parse("6月7日\n上午八点-十点 语文\n下午三点-五点 英语")
        assertEquals(2, result.entries.size)
        assertEquals("08:00", result.entries[0].startTime)
        assertEquals("10:00", result.entries[0].endTime)
        assertEquals("15:00", result.entries[1].startTime)
    }

    @Test fun `chinese time half hour`() {
        val result = parser.parse("6月7日\n上午八点半-十点 数学")
        assertEquals(1, result.entries.size)
        assertEquals("08:30", result.entries[0].startTime)
    }

    @Test fun `single date header with chinese time`() {
        val result = parser.parse("6月7日\n上午8:00-10:00 数学\n下午2:30-4:00 英语\n晚上7:00-9:00 自习", 2026)
        assertEquals(3, result.entries.size)
        assertEquals("08:00", result.entries[0].startTime)
        assertEquals("14:30", result.entries[1].startTime)
        assertEquals("19:00", result.entries[2].startTime)
    }

    @Test fun `chinese time afternoon twelve`() {
        // 下午十二点 = 12:00 noon (12+12=24→fix: should stay 12)
        val result = parser.parse("6月7日\n中午十二点-下午一点 午休")
        assertEquals(1, result.entries.size)
        assertEquals("12:00", result.entries[0].startTime)
        assertEquals("13:00", result.entries[0].endTime)
    }

    @Test fun `mixed standard and chinese time`() {
        val result = parser.parse("6月7日\n08:00-10:00 数学\n下午三点-五点 英语")
        assertEquals(2, result.entries.size)
        assertEquals("08:00", result.entries[0].startTime)
        assertEquals("15:00", result.entries[1].startTime)
    }
}
