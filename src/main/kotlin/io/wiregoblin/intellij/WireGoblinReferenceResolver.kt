package io.wiregoblin.intellij

import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue

internal object WireGoblinReferenceResolver {
    fun suggestions(kind: WireGoblinReferenceKind, file: YAMLFile?, position: PsiElement): List<String> {
        return when (kind) {
            WireGoblinReferenceKind.CONSTANT_OR_SECRET ->
                scopedReferences(kind, file, position, WireGoblinKeys.CONSTANTS, WireGoblinKeys.SECRETS)
            WireGoblinReferenceKind.VARIABLE -> buildList {
                addAll(scopedReferences(kind, file, position, WireGoblinKeys.VARIABLES, WireGoblinKeys.SECRET_VARIABLES))
                addAll(
                    WireGoblinYamlReferenceScopeHelper.assignVariables(
                        WireGoblinYamlContextLocator.currentWorkflowMapping(position),
                        position,
                    ),
                )
            }
            WireGoblinReferenceKind.EXPRESSION -> WireGoblinReferenceCatalog.bangReferences
        }
    }

    fun exists(kind: WireGoblinReferenceKind, raw: String, file: YAMLFile?, position: PsiElement): Boolean {
        return suggestions(kind, file, position).contains(raw)
    }

    fun resolve(kind: WireGoblinReferenceKind, raw: String, file: YAMLFile?, position: PsiElement): YAMLKeyValue? {
        return when (kind) {
            WireGoblinReferenceKind.CONSTANT_OR_SECRET ->
                scopedReferenceTarget(raw, file, position, WireGoblinKeys.CONSTANTS, WireGoblinKeys.SECRETS)
            WireGoblinReferenceKind.VARIABLE ->
                scopedReferenceTarget(raw, file, position, WireGoblinKeys.VARIABLES, WireGoblinKeys.SECRET_VARIABLES)
                ?: assignReferenceTarget(raw, position)
            WireGoblinReferenceKind.EXPRESSION -> null
        }
    }

    private fun scopedReferences(
        kind: WireGoblinReferenceKind,
        file: YAMLFile?,
        position: PsiElement,
        vararg sectionNames: String,
    ): List<String> {
        val topLevel = WireGoblinYamlContextLocator.topLevelMapping(file)
        val workflow = WireGoblinYamlContextLocator.currentWorkflowMapping(position)
        return buildList {
            sectionNames.forEach { sectionName ->
                addAll(WireGoblinYamlReferenceScopeHelper.sectionKeys(topLevel, sectionName).map { "${kind.prefix}$it" })
                addAll(WireGoblinYamlReferenceScopeHelper.sectionKeys(workflow, sectionName).map { "${kind.prefix}$it" })
            }
        }
    }

    private fun assignReferenceTarget(raw: String, position: PsiElement): YAMLKeyValue? {
        val workflow = WireGoblinYamlContextLocator.currentWorkflowMapping(position)
        return WireGoblinYamlReferenceScopeHelper.assignVariableEntries(workflow, position)
            .firstOrNull { it.keyText == raw }
    }

    private fun scopedReferenceTarget(
        raw: String,
        file: YAMLFile?,
        position: PsiElement,
        vararg sectionNames: String,
    ): YAMLKeyValue? {
        val key = raw.drop(1)
        if (key.isBlank()) {
            return null
        }

        val workflow = WireGoblinYamlContextLocator.currentWorkflowMapping(position)
        sectionNames.forEach { sectionName ->
            WireGoblinYamlReferenceScopeHelper.sectionEntries(workflow, sectionName)
                .firstOrNull { it.keyText == key }
                ?.let { return it }
        }

        val topLevel = WireGoblinYamlContextLocator.topLevelMapping(file)
        sectionNames.forEach { sectionName ->
            WireGoblinYamlReferenceScopeHelper.sectionEntries(topLevel, sectionName)
                .firstOrNull { it.keyText == key }
                ?.let { return it }
        }

        return null
    }
}
