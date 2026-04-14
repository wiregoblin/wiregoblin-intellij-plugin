package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinKeys
import io.wiregoblin.intellij.WireGoblinSchemaType

object ForeachBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "foreach",
        requiredFields = listOf(
            WireGoblinFieldSpec("items", required = true),
            WireGoblinFieldSpec(WireGoblinKeys.BLOCK, required = true, schemaType = WireGoblinSchemaType.BLOCK),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("concurrency", schemaType = WireGoblinSchemaType.INTEGER),
            WireGoblinFieldSpec(WireGoblinKeys.COLLECT),
        ),
        templateLines = listOf(
            """items: "${'$'}items"""",
            "${WireGoblinKeys.BLOCK}:",
            """  type: "log"""",
            """  message: "Item !Each.Item"""",
        ),
    )
}
