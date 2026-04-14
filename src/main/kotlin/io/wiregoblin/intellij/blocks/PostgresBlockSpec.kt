package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object PostgresBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "postgres",
        requiredFields = listOf(
            WireGoblinFieldSpec("dsn", required = true),
            WireGoblinFieldSpec("query", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("params"),
            WireGoblinFieldSpec("transaction", schemaType = WireGoblinSchemaType.BOOLEAN),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            """dsn: "@postgres_dsn"""",
            """query: "SELECT 1"""",
        ),
    )
}
