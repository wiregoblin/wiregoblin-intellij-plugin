package io.wiregoblin.intellij

import io.wiregoblin.intellij.blocks.AssertBlockSpec
import io.wiregoblin.intellij.blocks.ContainerBlockSpec
import io.wiregoblin.intellij.blocks.DelayBlockSpec
import io.wiregoblin.intellij.blocks.ForeachBlockSpec
import io.wiregoblin.intellij.blocks.GotoBlockSpec
import io.wiregoblin.intellij.blocks.GrpcBlockSpec
import io.wiregoblin.intellij.blocks.HttpBlockSpec
import io.wiregoblin.intellij.blocks.ImapBlockSpec
import io.wiregoblin.intellij.blocks.LogBlockSpec
import io.wiregoblin.intellij.blocks.OpenAiBlockSpec
import io.wiregoblin.intellij.blocks.ParallelBlockSpec
import io.wiregoblin.intellij.blocks.PostgresBlockSpec
import io.wiregoblin.intellij.blocks.RedisBlockSpec
import io.wiregoblin.intellij.blocks.RetryBlockSpec
import io.wiregoblin.intellij.blocks.SetVarsBlockSpec
import io.wiregoblin.intellij.blocks.SlackBlockSpec
import io.wiregoblin.intellij.blocks.SmtpBlockSpec
import io.wiregoblin.intellij.blocks.TelegramBlockSpec
import io.wiregoblin.intellij.blocks.TransformBlockSpec
import io.wiregoblin.intellij.blocks.WorkflowBlockSpec

data class WireGoblinFieldSpec(
    val name: String,
    val required: Boolean = false,
    val allowedValues: List<String> = emptyList(),
    val schemaType: WireGoblinSchemaType = WireGoblinSchemaType.STRING,
)

enum class WireGoblinSchemaType {
    STRING,
    INTEGER,
    BOOLEAN,
    STRING_OR_INTEGER,
    STRING_MAP,
    STRING_LIST,
    BLOCK,
    BLOCK_LIST,
    OBJECT,
}

data class WireGoblinBlockSpec(
    val type: String,
    val requiredFields: List<WireGoblinFieldSpec>,
    val optionalFields: List<WireGoblinFieldSpec> = emptyList(),
    val templateLines: List<String> = emptyList(),
) {
    val fields: List<WireGoblinFieldSpec> = requiredFields + optionalFields

    fun field(name: String): WireGoblinFieldSpec? = fields.firstOrNull { it.name == name }
}

internal object WireGoblinSchema {
    val topLevelKeys = listOf(
        WireGoblinKeys.ID,
        WireGoblinKeys.NAME,
        WireGoblinKeys.VERSION,
        WireGoblinKeys.AI,
        WireGoblinKeys.CONSTANTS,
        WireGoblinKeys.SECRETS,
        WireGoblinKeys.VARIABLES,
        WireGoblinKeys.SECRET_VARIABLES,
        WireGoblinKeys.WORKFLOWS,
    )

    val workflowKeys = listOf(
        WireGoblinKeys.ID,
        WireGoblinKeys.NAME,
        WireGoblinKeys.DISABLE_RUN,
        WireGoblinKeys.TIMEOUT_SECONDS,
        WireGoblinKeys.CONSTANTS,
        WireGoblinKeys.SECRETS,
        WireGoblinKeys.VARIABLES,
        WireGoblinKeys.SECRET_VARIABLES,
        WireGoblinKeys.OUTPUTS,
        WireGoblinKeys.CATCH_ERROR_BLOCKS,
        WireGoblinKeys.BLOCKS,
    )

    val commonBlockFields = listOf(
        WireGoblinFieldSpec(WireGoblinKeys.ID, required = true),
        WireGoblinFieldSpec(WireGoblinKeys.NAME),
        WireGoblinFieldSpec(WireGoblinKeys.TYPE, required = true),
        WireGoblinFieldSpec(WireGoblinKeys.CONDITION, schemaType = WireGoblinSchemaType.OBJECT),
        WireGoblinFieldSpec(WireGoblinKeys.CONTINUE_ON_ERROR, schemaType = WireGoblinSchemaType.BOOLEAN),
        WireGoblinFieldSpec(WireGoblinKeys.ASSIGN, schemaType = WireGoblinSchemaType.STRING_MAP),
    )

    val conditionFields = listOf(
        WireGoblinFieldSpec(WireGoblinKeys.VARIABLE, required = true),
        WireGoblinFieldSpec(WireGoblinKeys.OPERATOR, required = true, allowedValues = listOf("=", "!=", ">", ">=", "<", "<=", "contains", "not_contains", "matches", "not_matches")),
        WireGoblinFieldSpec(WireGoblinKeys.EXPECTED, required = true),
    )
    val conditionKeys = conditionFields.map { it.name }
    val conditionFieldsByName = conditionFields.associateBy { it.name }

    val aiFields = listOf(
        WireGoblinFieldSpec("enabled", schemaType = WireGoblinSchemaType.BOOLEAN),
        WireGoblinFieldSpec(WireGoblinKeys.PROVIDER, allowedValues = WireGoblinAiProvider.valuesList()),
        WireGoblinFieldSpec(WireGoblinKeys.BASE_URL),
        WireGoblinFieldSpec(WireGoblinKeys.MODEL),
        WireGoblinFieldSpec(WireGoblinKeys.TIMEOUT_SECONDS, schemaType = WireGoblinSchemaType.INTEGER),
        WireGoblinFieldSpec("redact_secrets", schemaType = WireGoblinSchemaType.BOOLEAN),
    )
    val aiKeys = aiFields.map { it.name }
    val aiFieldsByName = aiFields.associateBy { it.name }

    val retryRuleTypes = listOf("transport_error", "status_code", "path")
    val retryRuleTypesSet = retryRuleTypes.toSet()

    val blockSpecs = listOf(
        HttpBlockSpec.spec,
        GrpcBlockSpec.spec,
        PostgresBlockSpec.spec,
        RedisBlockSpec.spec,
        OpenAiBlockSpec.spec,
        SmtpBlockSpec.spec,
        ImapBlockSpec.spec,
        SlackBlockSpec.spec,
        TelegramBlockSpec.spec,
        ContainerBlockSpec.spec,
        DelayBlockSpec.spec,
        LogBlockSpec.spec,
        SetVarsBlockSpec.spec,
        AssertBlockSpec.spec,
        GotoBlockSpec.spec,
        TransformBlockSpec.spec,
        RetryBlockSpec.spec,
        ForeachBlockSpec.spec,
        ParallelBlockSpec.spec,
        WorkflowBlockSpec.spec,
    )

    val blockTypes = blockSpecs.map { it.type }
    val blockTypesSet = blockTypes.toSet()
    val blockSpecsByType = blockSpecs.associateBy { it.type }
    val blockKeys = commonBlockFields.map { it.name }
    val blockTypeTemplates = blockSpecsByType.mapValues { it.value.templateLines }
}
