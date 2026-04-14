package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinKeys
import io.wiregoblin.intellij.WireGoblinSchemaType

object SlackBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "slack",
        requiredFields = listOf(
            WireGoblinFieldSpec("webhook_url", required = true),
            WireGoblinFieldSpec("text", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec(WireGoblinKeys.BLOCKS),
            WireGoblinFieldSpec("channel"),
            WireGoblinFieldSpec("username"),
            WireGoblinFieldSpec("icon_emoji"),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            """webhook_url: "@slack_webhook_url"""",
            """text: """"",
        ),
    )
}
