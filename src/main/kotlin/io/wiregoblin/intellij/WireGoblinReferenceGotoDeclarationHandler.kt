package io.wiregoblin.intellij

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue

class WireGoblinReferenceGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor,
    ): Array<PsiElement>? {
        sourceElement ?: return null
        val file = sourceElement.containingFile as? YAMLFile ?: return null
        if (!WireGoblinFileSupport.isWireGoblinFile(file)) {
            return null
        }

        val keyValue = PsiTreeUtil.getParentOfType(sourceElement, YAMLKeyValue::class.java, false) ?: return null
        workflowIdUsageTargets(keyValue, file, offset)?.let { return it }

        if (!WireGoblinYamlContextLocator.supportsEnvPlaceholder(keyValue)) {
            return null
        }

        val valueElement = keyValue.value ?: return null
        val caretInValue = offset - valueElement.textRange.startOffset
        if (caretInValue < 0 || caretInValue > valueElement.textLength) {
            return null
        }

        val token = WireGoblinReferenceSupport.findReferenceTokens(valueElement.text)
            .firstOrNull { caretInValue in it.startOffset until it.endOffset }
            ?: return null

        if (token.kind == WireGoblinReferenceKind.EXPRESSION) {
            val doc = WireGoblinBuiltInReferenceDocs.find(token.raw) ?: return null
            return arrayOf(WireGoblinBuiltInReferenceTarget(sourceElement.project, sourceElement, doc.name))
        }

        val target = WireGoblinReferenceResolver.resolve(token.kind, token.raw, file, valueElement.firstChild ?: valueElement)
            ?: return null
        return arrayOf(target)
    }

    override fun getActionText(context: com.intellij.openapi.actionSystem.DataContext): String? = null

    private fun workflowIdUsageTargets(keyValue: YAMLKeyValue, file: YAMLFile, offset: Int): Array<PsiElement>? {
        val valueElement = keyValue.value ?: return null
        if (offset !in valueElement.textRange.startOffset..valueElement.textRange.endOffset) {
            return null
        }

        val workflowIdKeyValue = WireGoblinWorkflowTargetReferences.workflowIdKeyValue(valueElement) ?: return null
        val usages = WireGoblinWorkflowTargetReferences.referencesTo(file, workflowIdKeyValue)
            .map { it.element }
            .toTypedArray()

        return usages.takeIf { it.isNotEmpty() }
    }
}
