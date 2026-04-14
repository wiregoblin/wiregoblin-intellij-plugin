package io.wiregoblin.intellij

import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

internal object WireGoblinYamlContext {
    fun contextKeys(position: PsiElement): List<String> = WireGoblinYamlContextLocator.contextKeys(position)

    fun supportsEnvPlaceholder(keyValue: YAMLKeyValue): Boolean =
        WireGoblinYamlContextLocator.supportsEnvPlaceholder(keyValue)

    fun shouldIgnoreReferenceToken(
        keyValue: YAMLKeyValue,
        token: WireGoblinReferenceSupport.ReferenceOccurrence,
    ): Boolean = WireGoblinYamlReferenceScopeHelper.shouldIgnoreReferenceToken(keyValue, token)

    fun isMisnamedWorkflowBlockKey(keyValue: YAMLKeyValue): Boolean =
        WireGoblinYamlContextLocator.isMisnamedWorkflowBlockKey(keyValue)

    fun blockKeysFor(mapping: YAMLMapping?): List<String> = WireGoblinYamlContextLocator.blockKeysFor(mapping)

    fun currentBlockSpec(mapping: YAMLMapping?): WireGoblinBlockSpec? =
        WireGoblinYamlContextLocator.currentBlockSpec(mapping)

    fun currentBlockType(mapping: YAMLMapping?): String? = WireGoblinYamlContextLocator.currentBlockType(mapping)

    fun sequenceOwnerKey(mapping: YAMLMapping): String? = WireGoblinYamlContextLocator.sequenceOwnerKey(mapping)

    fun nearestSequenceItemMapping(position: PsiElement): YAMLMapping? =
        WireGoblinYamlContextLocator.nearestSequenceItemMapping(position)

    fun nearestSequenceOwnerKey(position: PsiElement): String? =
        WireGoblinYamlContextLocator.nearestSequenceOwnerKey(position)

    fun currentWorkflowMapping(position: PsiElement): YAMLMapping? =
        WireGoblinYamlContextLocator.currentWorkflowMapping(position)

    fun topLevelMapping(file: YAMLFile?): YAMLMapping? = WireGoblinYamlContextLocator.topLevelMapping(file)

    fun workflowRunTarget(keyValue: YAMLKeyValue): WireGoblinWorkflowRunTarget? =
        WireGoblinWorkflowRunHelper.workflowRunTarget(keyValue)

    fun sectionKeys(mapping: YAMLMapping?, sectionName: String): List<String> =
        WireGoblinYamlReferenceScopeHelper.sectionKeys(mapping, sectionName)

    fun sectionEntries(mapping: YAMLMapping?, sectionName: String): List<YAMLKeyValue> =
        WireGoblinYamlReferenceScopeHelper.sectionEntries(mapping, sectionName)

    fun assignVariables(workflowMapping: YAMLMapping?, position: PsiElement? = null): List<String> =
        WireGoblinYamlReferenceScopeHelper.assignVariables(workflowMapping, position)

    fun assignVariableEntries(workflowMapping: YAMLMapping?, position: PsiElement? = null): List<YAMLKeyValue> =
        WireGoblinYamlReferenceScopeHelper.assignVariableEntries(workflowMapping, position)
}

internal data class WireGoblinWorkflowRunTarget(
    val workflowId: String,
    val disableRun: Boolean,
)
