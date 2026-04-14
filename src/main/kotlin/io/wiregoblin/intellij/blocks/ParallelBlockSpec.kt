package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinKeys
import io.wiregoblin.intellij.WireGoblinSchemaType

object ParallelBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "parallel",
        requiredFields = listOf(
            WireGoblinFieldSpec(WireGoblinKeys.BLOCKS, required = true, schemaType = WireGoblinSchemaType.BLOCK_LIST),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec(WireGoblinKeys.COLLECT),
        ),
        templateLines = listOf(
            "${WireGoblinKeys.BLOCKS}:",
            """  - id: "branch_1"""",
            """    type: "http"""",
            """    method: "GET"""",
            """    url: "@base_url"""",
        ),
    )
}
