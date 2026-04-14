package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object HttpBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "http",
        requiredFields = listOf(
            WireGoblinFieldSpec("method", required = true, allowedValues = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")),
            WireGoblinFieldSpec("url", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("headers"),
            WireGoblinFieldSpec("body"),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            """method: "GET"""",
            """url: "@base_url"""",
        ),
    )
}
