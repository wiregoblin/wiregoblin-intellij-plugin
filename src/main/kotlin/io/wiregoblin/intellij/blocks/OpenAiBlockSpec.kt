package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object OpenAiBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "openai",
        requiredFields = listOf(
            WireGoblinFieldSpec("token", required = true),
            WireGoblinFieldSpec("model", required = true),
            WireGoblinFieldSpec("user_prompt", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("base_url"),
            WireGoblinFieldSpec("system_prompt"),
            WireGoblinFieldSpec("temperature", schemaType = WireGoblinSchemaType.STRING_OR_INTEGER),
            WireGoblinFieldSpec("max_tokens", schemaType = WireGoblinSchemaType.INTEGER),
            WireGoblinFieldSpec("headers"),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            """token: "@openai_key"""",
            """model: "gpt-4o"""",
            """user_prompt: """"",
        ),
    )
}
