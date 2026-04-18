package io.wiregoblin.intellij

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLValue

class WireGoblinWorkflowTargetUsageSearcher : QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
    override fun execute(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>): Boolean {
        val targetKeyValue = WireGoblinWorkflowTargetReferences.workflowIdKeyValue(queryParameters.elementToSearch)
            ?: return true
        val file = targetKeyValue.containingFile as? YAMLFile ?: return true

        WireGoblinWorkflowTargetReferences.referencesTo(file, targetKeyValue).forEach { reference ->
            if (!consumer.process(reference)) {
                return false
            }
        }

        return true
    }
}

internal object WireGoblinWorkflowTargetReferences {
    fun workflowIdKeyValue(element: PsiElement): YAMLKeyValue? {
        return when (element) {
            is YAMLKeyValue -> element.takeIf { it.keyText == WireGoblinKeys.ID }
            is YAMLValue -> (element.parent as? YAMLKeyValue)?.takeIf { it.keyText == WireGoblinKeys.ID }
            else -> null
        }
    }

    fun referencesTo(file: YAMLFile, targetKeyValue: YAMLKeyValue): List<PsiReference> {
        if (!WireGoblinFileSupport.isWireGoblinFile(file)) {
            return emptyList()
        }
        if (WireGoblinYamlReferenceScopeHelper.workflowIdEntries(file).none { it == targetKeyValue }) {
            return emptyList()
        }

        val workflowId = targetKeyValue.valueText
        if (workflowId.isBlank()) {
            return emptyList()
        }

        return PsiTreeUtil.findChildrenOfType(file, YAMLKeyValue::class.java)
            .filter { it.keyText == WireGoblinKeys.TARGET_WORKFLOW_ID && it.valueText == workflowId }
            .flatMap { keyValue ->
                keyValue.value?.references.orEmpty()
                    .filter { it.isReferenceTo(targetKeyValue) }
            }
    }
}
