package com.timetask.pro.v2.domain.usecase.ai

import com.timetask.pro.v2.domain.model.ai.AiTaskDraft
import com.timetask.pro.v2.domain.model.ai.AiTaskParseRequest
import com.timetask.pro.v2.domain.model.ai.AiTaskParseResult
import com.timetask.pro.v2.domain.model.ai.AiTaskPriority
import com.timetask.pro.v2.domain.repository.AiTaskParserRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParseTaskInputWithAiUseCaseTest {
    @Test
    fun `returns unavailable before repository call for blank input`() = runTest {
        val repository = RecordingRepository()
        val useCase = ParseTaskInputWithAiUseCase(repository)

        val result = useCase(request(" \n\t "))

        assertEquals(AiTaskParseResult.Unavailable("Task input is empty."), result)
        assertNull(repository.lastRequest)
    }

    @Test
    fun `normalizes input and known labels before repository call`() = runTest {
        val repository = RecordingRepository()
        val useCase = ParseTaskInputWithAiUseCase(repository)

        val result = useCase(
            request(
                input = "  Plan   release\nnotes  ",
                knownTags = listOf(" Work ", "work", "", "Deep   Focus"),
                knownFolders = listOf(" Inbox ", "inbox"),
                knownCategories = listOf(" Study ", "study"),
            )
        )

        assertTrue(result is AiTaskParseResult.Success)
        assertEquals("Plan release notes", repository.lastRequest?.input)
        assertEquals(listOf("Work", "Deep Focus"), repository.lastRequest?.knownTags)
        assertEquals(listOf("Inbox"), repository.lastRequest?.knownFolders)
        assertEquals(listOf("Study"), repository.lastRequest?.knownCategories)
    }

    @Test
    fun `rejects oversized input to avoid sending excessive context`() = runTest {
        val repository = RecordingRepository()
        val useCase = ParseTaskInputWithAiUseCase(repository)

        val result = useCase(request("x".repeat(4_001)))

        assertEquals(AiTaskParseResult.Unavailable("Task input is too long for AI parsing."), result)
        assertNull(repository.lastRequest)
    }

    private fun request(
        input: String,
        knownTags: List<String> = emptyList(),
        knownFolders: List<String> = emptyList(),
        knownCategories: List<String> = emptyList(),
    ) = AiTaskParseRequest(
        input = input,
        referenceDateIso = "2026-05-31",
        timeZoneId = "Europe/Kiev",
        knownTags = knownTags,
        knownFolders = knownFolders,
        knownCategories = knownCategories,
    )

    private class RecordingRepository : AiTaskParserRepository {
        var lastRequest: AiTaskParseRequest? = null

        override suspend fun parseTask(request: AiTaskParseRequest): AiTaskParseResult {
            lastRequest = request
            return AiTaskParseResult.Success(
                AiTaskDraft(
                    title = request.input,
                    description = "",
                    dueDateIso = null,
                    dueTime = null,
                    priority = AiTaskPriority.NONE,
                    tags = emptyList(),
                    folderHint = null,
                    categoryHint = null,
                    estimatedMinutes = null,
                    confidence = 0.5,
                    needsReview = true,
                    warnings = emptyList(),
                )
            )
        }
    }
}
