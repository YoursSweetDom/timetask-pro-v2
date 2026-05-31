package com.timetask.pro.v2.data.ai

import com.timetask.pro.v2.domain.model.ai.AiTaskParseRequest
import com.timetask.pro.v2.domain.model.ai.AiTaskParseResult
import com.timetask.pro.v2.domain.model.ai.AiTaskPriority
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LocalRuleBasedTaskParserRepositoryTest {
    private val repository = LocalRuleBasedTaskParserRepository()

    @Test
    fun `extracts time tags and Ukrainian priority from multiline input`() = runTest {
        val result = repository.parseTask(
            request(
                """
                Терміново підготувати демо
                завтра о 14:30 #work #Demo
                """.trimIndent()
            )
        )

        val draft = (result as AiTaskParseResult.Success).draft
        assertEquals("підготувати демо завтра о 14:30", draft.title)
        assertEquals("14:30", draft.dueTime)
        assertEquals(AiTaskPriority.URGENT, draft.priority)
        assertEquals(listOf("work", "Demo"), draft.tags)
        assertTrue(draft.needsReview)
    }

    @Test
    fun `deduplicates tags case insensitively`() = runTest {
        val result = repository.parseTask(request("Review notes #Work #work #deep-work"))

        val draft = (result as AiTaskParseResult.Success).draft
        assertEquals(listOf("Work", "deep-work"), draft.tags)
    }

    @Test
    fun `returns unavailable for blank input`() = runTest {
        val result = repository.parseTask(request("   "))

        assertEquals(AiTaskParseResult.Unavailable("Task input is empty."), result)
    }

    private fun request(input: String) = AiTaskParseRequest(
        input = input,
        referenceDateIso = "2026-05-31",
        timeZoneId = "Europe/Kiev",
    )
}
