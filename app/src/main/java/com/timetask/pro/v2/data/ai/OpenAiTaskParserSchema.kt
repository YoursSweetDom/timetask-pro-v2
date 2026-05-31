package com.timetask.pro.v2.data.ai

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * JSON Schema для OpenAI Structured Outputs.
 * Схема держится рядом с DTO, чтобы контракт не расходился с Kotlin-моделью.
 */
object OpenAiTaskParserSchema {
    const val NAME = "task_draft"

    val schema: JsonObject = buildJsonObject {
        put("type", "object")
        put("additionalProperties", false)
        put(
            "required",
            JsonArray(
                listOf(
                    "title",
                    "description",
                    "due_date",
                    "due_time",
                    "priority",
                    "tags",
                    "folder_hint",
                    "category_hint",
                    "estimated_minutes",
                    "confidence",
                    "needs_review",
                    "warnings",
                ).map(::JsonPrimitive)
            )
        )
        put(
            "properties",
            buildJsonObject {
                put("title", stringProperty("Short task title without date/time fragments."))
                put("description", stringProperty("Optional extra details, empty string if none."))
                put("due_date", nullableStringProperty("ISO date in YYYY-MM-DD format, or null."))
                put("due_time", nullableStringProperty("24-hour time in HH:MM format, or null."))
                put("priority", enumProperty("none", "low", "medium", "high", "urgent"))
                put(
                    "tags",
                    buildJsonObject {
                        put("type", "array")
                        put("items", stringProperty("Tag name without #."))
                    }
                )
                put("folder_hint", nullableStringProperty("Existing or suggested folder name, or null."))
                put("category_hint", nullableStringProperty("Existing or suggested category name, or null."))
                put(
                    "estimated_minutes",
                    nullableIntegerProperty(
                        description = "Estimated task duration in minutes, or null.",
                        minimum = 1,
                        maximum = 1440,
                    )
                )
                put(
                    "confidence",
                    buildJsonObject {
                        put("type", "number")
                        put("minimum", 0)
                        put("maximum", 1)
                    }
                )
                put("needs_review", buildJsonObject { put("type", "boolean") })
                put(
                    "warnings",
                    buildJsonObject {
                        put("type", "array")
                        put("items", stringProperty("Short warning for ambiguous fields."))
                    }
                )
            }
        )
    }

    private fun stringProperty(description: String): JsonObject {
        return buildJsonObject {
            put("type", "string")
            put("description", description)
        }
    }

    private fun nullableStringProperty(description: String): JsonObject {
        return buildJsonObject {
            put(
                "anyOf",
                JsonArray(
                    listOf(
                        stringProperty(description),
                        buildJsonObject { put("type", "null") },
                    )
                )
            )
        }
    }

    private fun nullableIntegerProperty(
        description: String,
        minimum: Int,
        maximum: Int,
    ): JsonObject {
        return buildJsonObject {
            put(
                "anyOf",
                JsonArray(
                    listOf(
                        buildJsonObject {
                            put("type", "integer")
                            put("description", description)
                            put("minimum", minimum)
                            put("maximum", maximum)
                        },
                        buildJsonObject { put("type", "null") },
                    )
                )
            )
        }
    }

    private fun enumProperty(vararg values: String): JsonObject {
        return buildJsonObject {
            put("type", "string")
            put("enum", JsonArray(values.map(::JsonPrimitive)))
        }
    }
}

