package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object RedisBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "redis",
        requiredFields = listOf(
            WireGoblinFieldSpec("address", required = true),
            WireGoblinFieldSpec("command", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("args"),
            WireGoblinFieldSpec("password"),
            WireGoblinFieldSpec("db", schemaType = WireGoblinSchemaType.INTEGER),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            """address: "@redis_address"""",
        ),
    )
}
