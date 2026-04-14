package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object ImapBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "imap",
        requiredFields = listOf(
            WireGoblinFieldSpec("host", required = true),
            WireGoblinFieldSpec("port", required = true, schemaType = WireGoblinSchemaType.INTEGER),
            WireGoblinFieldSpec("username", required = true),
            WireGoblinFieldSpec("password", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("tls", schemaType = WireGoblinSchemaType.BOOLEAN),
            WireGoblinFieldSpec("mailbox"),
            WireGoblinFieldSpec("criteria"),
            WireGoblinFieldSpec("wait", schemaType = WireGoblinSchemaType.BOOLEAN),
            WireGoblinFieldSpec("select_mode", allowedValues = listOf("first", "all", "count")),
            WireGoblinFieldSpec("mark_as_seen", schemaType = WireGoblinSchemaType.BOOLEAN),
            WireGoblinFieldSpec("delete", schemaType = WireGoblinSchemaType.BOOLEAN),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            """host: "@imap_host"""",
            "port: 993",
            """username: "@imap_user"""",
            """password: "@imap_pass"""",
        ),
    )
}
