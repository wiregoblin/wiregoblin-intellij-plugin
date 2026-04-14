package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object DelayBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "delay",
        requiredFields = listOf(
            WireGoblinFieldSpec("milliseconds", required = true, schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            "milliseconds: 1000",
        ),
    )
}
