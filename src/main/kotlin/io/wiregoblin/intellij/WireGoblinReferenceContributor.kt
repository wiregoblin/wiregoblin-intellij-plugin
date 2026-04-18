package io.wiregoblin.intellij

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLValue

class WireGoblinReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: com.intellij.psi.PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            psiElement(YAMLValue::class.java),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(
                    element: PsiElement,
                    context: ProcessingContext,
                ): Array<PsiReference> {
                    val valueElement = element as? YAMLValue ?: return PsiReference.EMPTY_ARRAY
                    val keyValue = PsiTreeUtil.getParentOfType(valueElement, YAMLKeyValue::class.java, false)
                        ?: return PsiReference.EMPTY_ARRAY
                    if (!WireGoblinFileSupport.isWireGoblinFile(keyValue.containingFile)) {
                        return PsiReference.EMPTY_ARRAY
                    }
                    if (!WireGoblinYamlContextLocator.supportsEnvPlaceholder(keyValue)) {
                        return PsiReference.EMPTY_ARRAY
                    }

                    val file = keyValue.containingFile as? YAMLFile ?: return PsiReference.EMPTY_ARRAY
                    workflowTargetReference(valueElement, keyValue, file)?.let { return arrayOf(it) }

                    val anchor = valueElement.firstChild ?: valueElement

                    return WireGoblinReferenceSupport.findReferenceTokens(valueElement.text)
                        .mapNotNull { token ->
                            if (WireGoblinYamlReferenceScopeHelper.shouldIgnoreReferenceToken(keyValue, token)) {
                                return@mapNotNull null
                            }
                            when (token.kind) {
                                WireGoblinReferenceKind.CONSTANT_OR_SECRET,
                                WireGoblinReferenceKind.VARIABLE,
                                WireGoblinReferenceKind.EXPRESSION,
                                -> {
                                    val rangeInElement = TextRange(
                                        token.startOffset,
                                        token.endOffset,
                                    )
                                    WireGoblinYamlReference(valueElement, rangeInElement, token.kind, token.raw, file, anchor)
                                }
                            }
                        }
                        .toTypedArray()
                }
            },
        )
    }
}

private fun workflowTargetReference(
    valueElement: YAMLValue,
    keyValue: YAMLKeyValue,
    file: YAMLFile,
): PsiReference? {
    if (keyValue.keyText != WireGoblinKeys.TARGET_WORKFLOW_ID) {
        return null
    }

    val blockMapping = keyValue.parent as? org.jetbrains.yaml.psi.YAMLMapping ?: return null
    if (WireGoblinYamlContextLocator.currentBlockType(blockMapping) != "workflow") {
        return null
    }

    return WireGoblinWorkflowTargetReference(valueElement, scalarValueRange(valueElement), file, keyValue.valueText)
}

private fun scalarValueRange(valueElement: YAMLValue): TextRange {
    val text = valueElement.text
    return if (text.length >= 2 && text.first() == text.last() && (text.first() == '"' || text.first() == '\'')) {
        TextRange(1, text.length - 1)
    } else {
        TextRange(0, text.length)
    }
}

private class WireGoblinWorkflowTargetReference(
    element: YAMLValue,
    rangeInElement: TextRange,
    private val file: YAMLFile,
    private val targetWorkflowId: String,
) : PsiReferenceBase<YAMLValue>(element, rangeInElement, false) {
    override fun resolve(): PsiElement? {
        if (targetWorkflowId.isBlank()) {
            return null
        }

        return WireGoblinYamlReferenceScopeHelper.workflowIdEntries(file)
            .firstOrNull { it.valueText == targetWorkflowId }
            ?.value
    }

    override fun getCanonicalText(): String = targetWorkflowId

    override fun isReferenceTo(element: PsiElement): Boolean {
        val target = resolve()
        return target == element || target?.parent == element
    }
}

private class WireGoblinYamlReference(
    element: YAMLValue,
    rangeInElement: TextRange,
    private val kind: WireGoblinReferenceKind,
    private val raw: String,
    private val file: YAMLFile,
    private val anchor: PsiElement,
) : PsiReferenceBase<YAMLValue>(element, rangeInElement, false) {
    override fun resolve(): PsiElement? {
        if (kind == WireGoblinReferenceKind.EXPRESSION) {
            val doc = WireGoblinBuiltInReferenceDocs.find(raw) ?: return null
            return WireGoblinBuiltInReferenceTarget(element.project, element, doc.name)
        }
        return WireGoblinReferenceResolver.resolve(kind, raw, file, anchor)
    }
}
