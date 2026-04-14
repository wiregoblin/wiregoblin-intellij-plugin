package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec

object AssertBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "assert",
        requiredFields = listOf(
            WireGoblinFieldSpec("variable", required = true),
            WireGoblinFieldSpec("operator", required = true, allowedValues = listOf("=", "!=", ">", ">=", "<", "<=", "contains", "not_contains", "matches", "not_matches")),
            WireGoblinFieldSpec("expected", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("error_message"),
        ),
        templateLines = listOf(
            """variable: "${'$'}status_code"""",
            """operator: "="""",
            """expected: "200"""",
        ),
    )
}
