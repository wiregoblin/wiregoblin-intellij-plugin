package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object ContainerBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "container",
        requiredFields = listOf(
            WireGoblinFieldSpec("image", required = true),
            WireGoblinFieldSpec("command", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("env", schemaType = WireGoblinSchemaType.STRING_MAP),
            WireGoblinFieldSpec("workdir"),
            WireGoblinFieldSpec("mount_source"),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
            WireGoblinFieldSpec("docker_path"),
        ),
        templateLines = listOf(
            """image: "alpine:latest"""",
            """command: "echo hello"""",
        ),
    )
}
