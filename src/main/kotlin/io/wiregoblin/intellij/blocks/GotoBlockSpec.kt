package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec

object GotoBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "goto",
        requiredFields = listOf(
            WireGoblinFieldSpec("variable", required = true),
            WireGoblinFieldSpec("operator", required = true, allowedValues = listOf("=", "!=", ">", ">=", "<", "<=", "contains", "not_contains", "matches", "not_matches")),
            WireGoblinFieldSpec("expected", required = true),
            WireGoblinFieldSpec("target_step_id", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("wait_seconds"),
        ),
        templateLines = listOf(
            """variable: "${'$'}status"""",
            """operator: "!="""",
            """expected: "done"""",
            """target_step_id: "target_step"""",
        ),
    )
}
