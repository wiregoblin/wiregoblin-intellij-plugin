package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object TransformBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "transform",
        requiredFields = listOf(
            WireGoblinFieldSpec("value", required = true, schemaType = WireGoblinSchemaType.OBJECT),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("casts"),
            WireGoblinFieldSpec("regex"),
        ),
        templateLines = listOf(
            "value:",
            """  field: "${'$'}value"""",
        ),
    )
}
