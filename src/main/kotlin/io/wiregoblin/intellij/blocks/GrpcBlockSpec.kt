package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object GrpcBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "grpc",
        requiredFields = listOf(
            WireGoblinFieldSpec("address", required = true),
            WireGoblinFieldSpec("method", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("request"),
            WireGoblinFieldSpec("metadata"),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            """address: "@grpc_host"""",
            """method: "/package.Service/Method"""",
        ),
    )
}
