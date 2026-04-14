package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec

object LogBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "log",
        requiredFields = listOf(
            WireGoblinFieldSpec("message", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("level", allowedValues = listOf("debug", "info", "warn", "error")),
        ),
        templateLines = listOf(
            """message: """"",
        ),
    )
}
