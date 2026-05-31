package com.timetask.pro.v2.core.network.openai

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class OpenAiResponseTextExtractorTest {
    private val extractor = OpenAiResponseTextExtractor()

    @Test
    fun `extracts top-level output text`() {
        val result = extractor.extract(
            """
            {
              "id": "resp_123",
              "output_text": "{\"title\":\"Plan sprint\"}"
            }
            """.trimIndent()
        )

        assertEquals("{\"title\":\"Plan sprint\"}", result.outputText)
        assertNull(result.refusal)
    }

    @Test
    fun `extracts nested output text parts`() {
        val result = extractor.extract(
            """
            {
              "output": [
                {
                  "type": "message",
                  "content": [
                    { "type": "output_text", "text": "{\"title\":\"Draft roadmap\"}" },
                    { "type": "output_text", "text": "{\"confidence\":0.8}" }
                  ]
                }
              ]
            }
            """.trimIndent()
        )

        assertEquals("{\"title\":\"Draft roadmap\"}\n{\"confidence\":0.8}", result.outputText)
        assertNull(result.refusal)
    }

    @Test
    fun `extracts refusal message`() {
        val result = extractor.extract(
            """
            {
              "output": [
                {
                  "type": "message",
                  "content": [
                    { "type": "refusal", "refusal": "I cannot help with that request." }
                  ]
                }
              ]
            }
            """.trimIndent()
        )

        assertNull(result.outputText)
        assertEquals("I cannot help with that request.", result.refusal)
    }
}
