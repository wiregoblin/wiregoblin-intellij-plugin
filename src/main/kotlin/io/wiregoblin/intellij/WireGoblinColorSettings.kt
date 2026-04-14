package io.wiregoblin.intellij

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import org.jetbrains.yaml.psi.YAMLKeyValue

internal object WireGoblinColorSettings {
    val structureKey = createKey("STRUCTURE_KEY", DefaultLanguageHighlighterColors.KEYWORD)
    val metadataKey = createKey("METADATA_KEY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
    val fieldKey = createKey("FIELD_KEY", DefaultLanguageHighlighterColors.STATIC_FIELD)
    val enumValue = createKey("ENUM_VALUE", DefaultLanguageHighlighterColors.CONSTANT)
    val identifierValue = createKey("IDENTIFIER_VALUE", DefaultLanguageHighlighterColors.LABEL)
    val envPlaceholder = createKey("ENV_PLACEHOLDER", DefaultLanguageHighlighterColors.METADATA)
    val sqlPlaceholder = createKey("SQL_PLACEHOLDER", DefaultLanguageHighlighterColors.NUMBER)
    val constantReference = createKey("CONSTANT_REFERENCE", DefaultLanguageHighlighterColors.CLASS_NAME)
    val variableReference = createKey("VARIABLE_REFERENCE", DefaultLanguageHighlighterColors.PARAMETER)
    val expressionReference = createKey("EXPRESSION_REFERENCE", DefaultLanguageHighlighterColors.MARKUP_ENTITY)
    val constantEntryKey = createKey("CONSTANT_ENTRY_KEY", constantReference)
    val secretEntryKey = createKey("SECRET_ENTRY_KEY", constantReference)
    val variableEntryKey = createKey("VARIABLE_ENTRY_KEY", variableReference)
    val secretVariableEntryKey = createKey("SECRET_VARIABLE_ENTRY_KEY", variableReference)
    val variableDefinitionKey = createKey("VARIABLE_DEFINITION_KEY", variableReference)

    val remoteCall = createKey("REMOTE_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL)
    val dataAccess = createKey("DATA_ACCESS", DefaultLanguageHighlighterColors.INSTANCE_METHOD)
    val messaging = createKey("MESSAGING", DefaultLanguageHighlighterColors.CLASS_NAME)
    val flowControl = createKey("FLOW_CONTROL", DefaultLanguageHighlighterColors.KEYWORD)
    val stateMutation = createKey("STATE_MUTATION", DefaultLanguageHighlighterColors.STATIC_METHOD)
    val runtime = createKey("RUNTIME", DefaultLanguageHighlighterColors.MARKUP_ENTITY)
    val retryRule = createKey("RETRY_RULE", DefaultLanguageHighlighterColors.CONSTANT)

    private val structureKeys = setOf(
        WireGoblinKeys.WORKFLOWS,
        WireGoblinKeys.BLOCKS,
        WireGoblinKeys.CATCH_ERROR_BLOCKS,
        WireGoblinKeys.BLOCK,
        WireGoblinKeys.RETRY_ON,
        WireGoblinKeys.RULES,
        WireGoblinKeys.CONDITION,
        WireGoblinKeys.AI,
        WireGoblinKeys.CONSTANTS,
        WireGoblinKeys.SECRETS,
        WireGoblinKeys.VARIABLES,
        WireGoblinKeys.SECRET_VARIABLES,
        WireGoblinKeys.OUTPUTS,
        WireGoblinKeys.ASSIGN,
        WireGoblinKeys.COLLECT,
    )

    private val variableDefinitionOwnerKeys = setOf(
        WireGoblinKeys.ASSIGN,
        "set",
        WireGoblinKeys.COLLECT,
        WireGoblinKeys.OUTPUTS,
    )

    private val metadataKeys = setOf(
        WireGoblinKeys.ID,
        WireGoblinKeys.NAME,
        WireGoblinKeys.TYPE,
        WireGoblinKeys.VERSION,
    )

    private val knownFieldKeys = buildSet {
        addAll(WireGoblinSchema.workflowKeys)
        addAll(WireGoblinSchema.blockKeys)
        addAll(WireGoblinSchema.conditionKeys)
        addAll(WireGoblinSchema.aiKeys)
        WireGoblinSchema.blockSpecs.forEach { spec ->
            addAll(spec.fields.map { it.name })
        }
    }

    fun typeValueAttributes(type: String, kind: WireGoblinTypeResolver.AllowedTypeKind): TextAttributesKey? {
        return when (kind) {
            WireGoblinTypeResolver.AllowedTypeKind.BLOCK -> blockTypeAttributes(type)
            WireGoblinTypeResolver.AllowedTypeKind.RETRY_RULE -> retryRule
        }
    }

    fun keyAttributes(keyValue: YAMLKeyValue): TextAttributesKey? {
        val key = keyValue.keyText
        val parentSection = (keyValue.parent as? org.jetbrains.yaml.psi.YAMLMapping)?.parent as? YAMLKeyValue
        return when {
            parentSection?.keyText == WireGoblinKeys.CONSTANTS -> constantEntryKey
            parentSection?.keyText == WireGoblinKeys.SECRETS -> secretEntryKey
            parentSection?.keyText == WireGoblinKeys.VARIABLES -> variableEntryKey
            parentSection?.keyText == WireGoblinKeys.SECRET_VARIABLES -> secretVariableEntryKey
            parentSection?.keyText in variableDefinitionOwnerKeys &&
                WireGoblinReferenceSupport.parseReferenceToken(key) != null -> null
            parentSection?.keyText in variableDefinitionOwnerKeys -> variableDefinitionKey
            key in structureKeys -> structureKey
            key in metadataKeys -> metadataKey
            key in knownFieldKeys -> fieldKey
            else -> null
        }
    }

    fun constrainedValueAttributes(keyValue: YAMLKeyValue, value: String): TextAttributesKey? {
        val kind = WireGoblinTypeResolver.allowedTypeValues(keyValue)?.takeIf { keyValue.keyText == WireGoblinKeys.TYPE }?.kind
        if (kind != null) {
            return typeValueAttributes(value, kind)
        }

        val constraint = WireGoblinTypeResolver.allowedFieldValues(keyValue) ?: return null
        return if (value in constraint.values) enumValue else null
    }

    fun referenceAttributes(kind: WireGoblinReferenceKind): TextAttributesKey {
        return when (kind) {
            WireGoblinReferenceKind.CONSTANT_OR_SECRET -> constantReference
            WireGoblinReferenceKind.VARIABLE -> variableReference
            WireGoblinReferenceKind.EXPRESSION -> expressionReference
        }
    }

    fun scalarValueAttributes(keyValue: YAMLKeyValue): TextAttributesKey? {
        return when (keyValue.keyText) {
            WireGoblinKeys.ID, "target_step_id", "target_workflow_id" -> identifierValue
            else -> null
        }
    }

    private fun blockTypeAttributes(type: String): TextAttributesKey? {
        return when (type) {
            "http", "grpc", "openai" -> remoteCall
            "postgres", "redis", "transform" -> dataAccess
            "smtp", "imap", "slack", "telegram" -> messaging
            "retry", "foreach", "parallel", "workflow", "goto", "delay" -> flowControl
            "setvars", "assert", "log" -> stateMutation
            "container" -> runtime
            else -> null
        }
    }

    private fun createKey(suffix: String, fallback: TextAttributesKey): TextAttributesKey {
        return TextAttributesKey.createTextAttributesKey("WIREGOBLIN_$suffix", fallback)
    }
}
