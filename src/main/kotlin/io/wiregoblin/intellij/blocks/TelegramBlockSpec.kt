package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object TelegramBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "telegram",
        requiredFields = listOf(
            WireGoblinFieldSpec("token", required = true),
            WireGoblinFieldSpec("chat_id", required = true),
            WireGoblinFieldSpec("message", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("parse_mode", allowedValues = listOf("HTML", "Markdown", "MarkdownV2")),
            WireGoblinFieldSpec("base_url"),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            """token: "@telegram_token"""",
            """chat_id: "@chat_id"""",
            """message: """"",
        ),
    )
}
