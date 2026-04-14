package io.wiregoblin.intellij.contract

import io.wiregoblin.intellij.WireGoblinKeys
import io.wiregoblin.intellij.WireGoblinSchema

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WireGoblinBlockSpecsContractTest {
    @Test
    fun `every block spec matches expected contract`() {
        val expected = mapOf(
            "http" to expectedBlock(
                required = listOf("method", "url"),
                optional = listOf("headers", "body", "timeout_seconds"),
                enums = mapOf("method" to listOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")),
            ),
            "grpc" to expectedBlock(
                required = listOf("address", "method"),
                optional = listOf("request", "metadata", "timeout_seconds"),
            ),
            "postgres" to expectedBlock(
                required = listOf("dsn", "query"),
                optional = listOf("params", "transaction", "timeout_seconds"),
            ),
            "redis" to expectedBlock(
                required = listOf("address", "command"),
                optional = listOf("args", "password", "db", "timeout_seconds"),
            ),
            "openai" to expectedBlock(
                required = listOf("token", "model", "user_prompt"),
                optional = listOf("base_url", "system_prompt", "temperature", "max_tokens", "headers", "timeout_seconds"),
            ),
            "smtp" to expectedBlock(
                required = listOf("host", "port", "username", "password", "from", "to", "subject", "text"),
                optional = listOf("starttls", "cc", "bcc", "html", "timeout_seconds"),
            ),
            "imap" to expectedBlock(
                required = listOf("host", "port", "username", "password"),
                optional = listOf("tls", "mailbox", "criteria", "wait", "select_mode", "mark_as_seen", "delete", "timeout_seconds"),
                enums = mapOf("select_mode" to listOf("first", "all", "count")),
            ),
            "slack" to expectedBlock(
                required = listOf("webhook_url", "text"),
                optional = listOf(WireGoblinKeys.BLOCKS, "channel", "username", "icon_emoji", "timeout_seconds"),
            ),
            "telegram" to expectedBlock(
                required = listOf("token", "chat_id", "message"),
                optional = listOf("parse_mode", "base_url", "timeout_seconds"),
                enums = mapOf("parse_mode" to listOf("HTML", "Markdown", "MarkdownV2")),
            ),
            "container" to expectedBlock(
                required = listOf("image", "command"),
                optional = listOf("env", "workdir", "mount_source", "timeout_seconds", "docker_path"),
            ),
            "delay" to expectedBlock(
                required = listOf("milliseconds"),
            ),
            "log" to expectedBlock(
                required = listOf("message"),
                optional = listOf("level"),
                enums = mapOf("level" to listOf("debug", "info", "warn", "error")),
            ),
            "setvars" to expectedBlock(
                required = listOf("set"),
            ),
            "assert" to expectedBlock(
                required = listOf(WireGoblinKeys.VARIABLE, WireGoblinKeys.OPERATOR, WireGoblinKeys.EXPECTED),
                optional = listOf("error_message"),
                enums = mapOf(WireGoblinKeys.OPERATOR to operators),
            ),
            "goto" to expectedBlock(
                required = listOf(WireGoblinKeys.VARIABLE, WireGoblinKeys.OPERATOR, WireGoblinKeys.EXPECTED, "target_step_id"),
                optional = listOf("wait_seconds"),
                enums = mapOf(WireGoblinKeys.OPERATOR to operators),
            ),
            "transform" to expectedBlock(
                required = listOf("value"),
                optional = listOf("casts", "regex"),
            ),
            "retry" to expectedBlock(
                required = listOf(WireGoblinKeys.BLOCK, "max_attempts", "delay_ms"),
                optional = listOf(WireGoblinKeys.RETRY_ON),
            ),
            "foreach" to expectedBlock(
                required = listOf("items", WireGoblinKeys.BLOCK),
                optional = listOf("concurrency", WireGoblinKeys.COLLECT),
            ),
            "parallel" to expectedBlock(
                required = listOf(WireGoblinKeys.BLOCKS),
                optional = listOf(WireGoblinKeys.COLLECT),
            ),
            "workflow" to expectedBlock(
                required = listOf("target_workflow_id"),
                optional = listOf("inputs"),
            ),
        )

        assertEquals(expected.keys, WireGoblinSchema.blockSpecsByType.keys)

        expected.forEach { (type, contract) ->
            val spec = requireNotNull(WireGoblinSchema.blockSpecsByType[type])
            assertEquals(contract.required, spec.requiredFields.map { it.name }, "required fields mismatch for '$type'")
            assertEquals(contract.optional, spec.optionalFields.map { it.name }, "optional fields mismatch for '$type'")

            contract.enums.forEach { (fieldName, values) ->
                assertEquals(values, spec.field(fieldName)?.allowedValues, "enum values mismatch for '$type.$fieldName'")
            }

            val declaredFields = spec.fields.map { it.name }.toSet()
            val templateKeys = spec.templateLines
                .asSequence()
                .filter { it.isNotBlank() && !it.startsWith(" ") && !it.startsWith("\t") }
                .map { it.substringBefore(":").trim() }
                .filter { it.isNotEmpty() }
                .toSet()

            assertTrue(
                templateKeys.all { it in declaredFields },
                "template keys for '$type' must be declared fields: $templateKeys vs $declaredFields",
            )
        }
    }

    private fun expectedBlock(
        required: List<String>,
        optional: List<String> = emptyList(),
        enums: Map<String, List<String>> = emptyMap(),
    ) = ExpectedBlockContract(required, optional, enums)

    private data class ExpectedBlockContract(
        val required: List<String>,
        val optional: List<String>,
        val enums: Map<String, List<String>>,
    )

    companion object {
        private val operators = listOf("=", "!=", ">", ">=", "<", "<=", "contains", "not_contains", "matches", "not_matches")
    }
}
