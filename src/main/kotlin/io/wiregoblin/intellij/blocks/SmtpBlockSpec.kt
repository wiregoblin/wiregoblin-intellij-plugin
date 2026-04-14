package io.wiregoblin.intellij.blocks

import io.wiregoblin.intellij.WireGoblinBlockSpec
import io.wiregoblin.intellij.WireGoblinFieldSpec
import io.wiregoblin.intellij.WireGoblinSchemaType

object SmtpBlockSpec {
    val spec = WireGoblinBlockSpec(
        type = "smtp",
        requiredFields = listOf(
            WireGoblinFieldSpec("host", required = true),
            WireGoblinFieldSpec("port", required = true, schemaType = WireGoblinSchemaType.INTEGER),
            WireGoblinFieldSpec("username", required = true),
            WireGoblinFieldSpec("password", required = true),
            WireGoblinFieldSpec("from", required = true),
            WireGoblinFieldSpec("to", required = true, schemaType = WireGoblinSchemaType.STRING_LIST),
            WireGoblinFieldSpec("subject", required = true),
            WireGoblinFieldSpec("text", required = true),
        ),
        optionalFields = listOf(
            WireGoblinFieldSpec("starttls", schemaType = WireGoblinSchemaType.BOOLEAN),
            WireGoblinFieldSpec("cc", schemaType = WireGoblinSchemaType.STRING_LIST),
            WireGoblinFieldSpec("bcc", schemaType = WireGoblinSchemaType.STRING_LIST),
            WireGoblinFieldSpec("html"),
            WireGoblinFieldSpec("timeout_seconds", schemaType = WireGoblinSchemaType.INTEGER),
        ),
        templateLines = listOf(
            """host: "@smtp_host"""",
            "port: 587",
            """username: "@smtp_user"""",
            """password: "@smtp_pass"""",
            """from: "noreply@example.com"""",
            """to: ["${'$'}test_email"]""",
            """subject: """"",
            """text: """"",
        ),
    )
}
