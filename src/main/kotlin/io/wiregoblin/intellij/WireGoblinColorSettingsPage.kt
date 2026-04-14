package io.wiregoblin.intellij

import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.jetbrains.yaml.YAMLFileType
import javax.swing.Icon

class WireGoblinColorSettingsPage : ColorSettingsPage {
    override fun getDisplayName(): String = WireGoblinPlugin.DISPLAY_NAME

    override fun getIcon(): Icon? = null

    override fun getHighlighter(): SyntaxHighlighter {
        return requireNotNull(SyntaxHighlighterFactory.getSyntaxHighlighter(YAMLFileType.YML, null, null)) {
            "YAML syntax highlighter is not available"
        }
    }

    override fun getDemoText(): String {
        return """
            <structure>constants</structure>:
              <constantKey>openai_base_url</constantKey>: "https://api.openai.com/v1"
            <structure>secrets</structure>:
              <secretKey>openai_token</secretKey>: "<env>${'$'}{OPENAI_API_KEY}</env>"
            <structure>variables</structure>:
              <variableKey>cached_user_name</variableKey>: "Alice"
            <structure>secret_variables</structure>:
              <secretVariableKey>chat_token</secretVariableKey>: "<refConst>@telegram_token</refConst>"
            <structure>workflows</structure>:
              - <meta>id</meta>: "demo"
                <structure>blocks</structure>:
                  - <meta>id</meta>: "fetch"
                    <meta>type</meta>: <remote>http</remote>
                    <field>method</field>: <enum>GET</enum>
                    <field>url</field>: "<env>${'$'}{HOST:=https://api.example.com}</env>/users/<refVar>${'$'}user_id</refVar>"
                    <structure>assign</structure>:
                      <varKey>${'$'}response_id</varKey>: "body.id"
                  - <meta>id</meta>: "cache"
                    <meta>type</meta>: <data>redis</data>
                  - <meta>id</meta>: "notify"
                    <meta>type</meta>: <message>slack</message>
                    <field>text</field>: "Run <refExpr>!RunID</refExpr>"
                  - <meta>id</meta>: "retry_fetch"
                    <meta>type</meta>: <flow>retry</flow>
                    <structure>retry_on</structure>:
                      <structure>rules</structure>:
                        - <meta>type</meta>: <retryRule>status_code</retryRule>
                  - <meta>id</meta>: "vars"
                    <meta>type</meta>: <state>setvars</state>
                  - <meta>id</meta>: "job"
                    <meta>type</meta>: <runtime>container</runtime>
                  - <meta>id</meta>: "db"
                    <meta>type</meta>: <data>postgres</data>
                    <field>query</field>: "select * from runs where id = <sql>${'$'}1</sql>"
                  - <meta>id</meta>: "jump"
                    <meta>type</meta>: <flow>goto</flow>
                    <field>target_step_id</field>: <idValue>"notify"</idValue>
        """.trimIndent()
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, com.intellij.openapi.editor.colors.TextAttributesKey> {
        return mapOf(
            "structure" to WireGoblinColorSettings.structureKey,
            "meta" to WireGoblinColorSettings.metadataKey,
            "constantKey" to WireGoblinColorSettings.constantEntryKey,
            "secretKey" to WireGoblinColorSettings.secretEntryKey,
            "variableKey" to WireGoblinColorSettings.variableEntryKey,
            "secretVariableKey" to WireGoblinColorSettings.secretVariableEntryKey,
            "field" to WireGoblinColorSettings.fieldKey,
            "varKey" to WireGoblinColorSettings.variableDefinitionKey,
            "enum" to WireGoblinColorSettings.enumValue,
            "idValue" to WireGoblinColorSettings.identifierValue,
            "env" to WireGoblinColorSettings.envPlaceholder,
            "sql" to WireGoblinColorSettings.sqlPlaceholder,
            "refConst" to WireGoblinColorSettings.constantReference,
            "refVar" to WireGoblinColorSettings.variableReference,
            "refExpr" to WireGoblinColorSettings.expressionReference,
            "remote" to WireGoblinColorSettings.remoteCall,
            "data" to WireGoblinColorSettings.dataAccess,
            "message" to WireGoblinColorSettings.messaging,
            "flow" to WireGoblinColorSettings.flowControl,
            "state" to WireGoblinColorSettings.stateMutation,
            "runtime" to WireGoblinColorSettings.runtime,
            "retryRule" to WireGoblinColorSettings.retryRule,
        )
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return arrayOf(
            AttributesDescriptor("WireGoblin Structure Key", WireGoblinColorSettings.structureKey),
            AttributesDescriptor("WireGoblin Metadata Key", WireGoblinColorSettings.metadataKey),
            AttributesDescriptor("WireGoblin Constant Key", WireGoblinColorSettings.constantEntryKey),
            AttributesDescriptor("WireGoblin Secret Key", WireGoblinColorSettings.secretEntryKey),
            AttributesDescriptor("WireGoblin Variable Key", WireGoblinColorSettings.variableEntryKey),
            AttributesDescriptor("WireGoblin Secret Variable Key", WireGoblinColorSettings.secretVariableEntryKey),
            AttributesDescriptor("WireGoblin Field Key", WireGoblinColorSettings.fieldKey),
            AttributesDescriptor("WireGoblin Variable Definition Key", WireGoblinColorSettings.variableDefinitionKey),
            AttributesDescriptor("WireGoblin Enum Value", WireGoblinColorSettings.enumValue),
            AttributesDescriptor("WireGoblin Identifier Value", WireGoblinColorSettings.identifierValue),
            AttributesDescriptor("WireGoblin Environment Placeholder", WireGoblinColorSettings.envPlaceholder),
            AttributesDescriptor("WireGoblin SQL Placeholder", WireGoblinColorSettings.sqlPlaceholder),
            AttributesDescriptor("WireGoblin Constant/Secret Reference", WireGoblinColorSettings.constantReference),
            AttributesDescriptor("WireGoblin Variable Reference", WireGoblinColorSettings.variableReference),
            AttributesDescriptor("WireGoblin Expression Reference", WireGoblinColorSettings.expressionReference),
            AttributesDescriptor("WireGoblin Remote Call Type", WireGoblinColorSettings.remoteCall),
            AttributesDescriptor("WireGoblin Data Access Type", WireGoblinColorSettings.dataAccess),
            AttributesDescriptor("WireGoblin Messaging Type", WireGoblinColorSettings.messaging),
            AttributesDescriptor("WireGoblin Flow Control Type", WireGoblinColorSettings.flowControl),
            AttributesDescriptor("WireGoblin State/Utility Type", WireGoblinColorSettings.stateMutation),
            AttributesDescriptor("WireGoblin Runtime Type", WireGoblinColorSettings.runtime),
            AttributesDescriptor("WireGoblin Retry Rule Type", WireGoblinColorSettings.retryRule),
        )
    }

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY
}
