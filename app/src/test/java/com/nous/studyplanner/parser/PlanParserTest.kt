package com.nous.studyplanner.parser

import org.junit.Assert.*
import org.junit.Test

class PlanParserTest {

    private val parser = PlanParser()

    @Test
    fun `parse standard format`() {
        val input = """
            6月7日
            08:00-10:00 数学刷题
            10:30-12:00 英语背单词
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertEquals(2, result.entries.size)
        assertTrue(result.errors.isEmpty())

        assertEquals("2026-06-07", result.entries[0].date)
        assertEquals("08:00", result.entries[0].startTime)
        assertEquals("10:00", result.entries[0].endTime)
        assertEquals("数学刷题", result.entries[0].subject)

        assertEquals("10:30", result.entries[1].startTime)
        assertEquals("英语背单词", result.entries[1].subject)
    }

    @Test
    fun `parse multiple dates`() {
        val input = """
            6月7日
            08:00-10:00 数学
            14:00-16:00 物理
            
            6月8日
            09:00-11:00 英语
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertEquals(3, result.entries.size)
        assertEquals("2026-06-07", result.entries[0].date)
        assertEquals("2026-06-07", result.entries[1].date)
        assertEquals("2026-06-08", result.entries[2].date)
    }

    @Test
    fun `parse short time format`() {
        val input = """
            6月7日
            8:00-10:00 语文
            8:30-9:30 历史
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertEquals(2, result.entries.size)
        assertEquals("08:00", result.entries[0].startTime)
        assertEquals("08:30", result.entries[1].startTime)
    }

    @Test
    fun `parse alternative separators`() {
        val input = """
            6月7日
            08:00~10:00 数学~符号
            08:00至10:00 至符号
            08:00到10:00 到符号
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertEquals(3, result.entries.size)
        assertEquals("数学~符号", result.entries[0].subject)
        assertEquals("至符号", result.entries[1].subject)
        assertEquals("到符号", result.entries[2].subject)
    }

    @Test
    fun `parse full year format`() {
        val input = """
            2026年6月7日
            08:00-10:00 考试复习
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertEquals(1, result.entries.size)
        assertEquals("2026-06-07", result.entries[0].date)
    }

    @Test
    fun `parse slash date format`() {
        val input = """
            6/7
            08:00-10:00 数学
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertEquals(1, result.entries.size)
        assertEquals("2026-06-07", result.entries[0].date)
    }

    @Test
    fun `parse dot date format`() {
        val input = """
            6.7
            08:00-10:00 数学
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertEquals(1, result.entries.size)
        assertEquals("2026-06-07", result.entries[0].date)
    }

    @Test
    fun `error on task without date`() {
        val input = """
            08:00-10:00 没有日期的任务
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertTrue(result.entries.isEmpty())
        assertTrue(result.errors.isNotEmpty())
        assertTrue(result.errors[0].contains("日期"))
    }

    @Test
    fun `error on unrecognized format`() {
        val input = """
            6月7日
            这是一行无法识别的文字
            08:00-10:00 数学
            另外一行乱码
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertEquals(1, result.entries.size)
        assertEquals(2, result.errors.size)
    }

    @Test
    fun `parse Chinese colon time`() {
        val input = """
            6月7日
            08：00-10：00 全角冒号测试
        """.trimIndent()

        val result = parser.parse(input, 2026)

        assertEquals(1, result.entries.size)
        assertEquals("08:00", result.entries[0].startTime)
        assertEquals("10:00", result.entries[0].endTime)
        assertEquals("全角冒号测试", result.entries[0].subject)
    }
}
