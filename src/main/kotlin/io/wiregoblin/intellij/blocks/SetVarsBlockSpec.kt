package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object SetVarsBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "setvars",
        requiredFields = listOf(
            WireGoblinFieldSpec("set", required = true, schemaType = WireGoblinSchemaType.STRING_MAP),
        ),
        templateLines = listOf(
            "set:",
            """  ${'$'}name: "value"""",
        ),
    )
}
