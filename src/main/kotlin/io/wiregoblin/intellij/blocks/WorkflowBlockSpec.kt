package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec

object WorkflowBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "workflow",
        requiredFields = listOf(
            WireGoblinFieldSpec("target_workflow_id", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("inputs"),
        ),
        templateLines = listOf(
            """target_workflow_id: "other_workflow_id"""",
        ),
    )
}
