package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinKeys
import io.wiregoblin.intellij.WireGoblinSchemaType

object RetryBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "retry",
        requiredFields = listOf(
            WireGoblinFieldSpec(WireGoblinKeys.BLOCK, required = true, schemaType = WireGoblinSchemaType.BLOCK),
            WireGoblinFieldSpec("max_attempts", required = true, schemaType = WireGoblinSchemaType.INTEGER),
            WireGoblinFieldSpec("delay_ms", required = true, schemaType = WireGoblinSchemaType.INTEGER),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec(WireGoblinKeys.RETRY_ON, schemaType = WireGoblinSchemaType.OBJECT),
        ),
        templateLines = listOf(
            "max_attempts: 3",
            "delay_ms: 500",
            "${WireGoblinKeys.BLOCK}:",
            """  type: "http"""",
            """  method: "GET"""",
            """  url: "@base_url/health"""",
        ),
    )
}
