package io.wiregoblin.intellij

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence

internal object WireGoblinCompletionContextHelper {
    fun contextPosition(parameters: CompletionParameters): PsiElement? {
        val caretOffset = parameters.offset
        return listOfNotNull(
            parameters.originalPosition,
            parameters.originalFile.findElementAt((caretOffset - 1).coerceAtLeast(0)),
            parameters.originalFile.findElementAt(caretOffset),
            parameters.position,
        ).firstOrNull()
    }

    fun currentReferenceToken(
        parameters: CompletionParameters,
        value: PsiElement?,
    ): WireGoblinReferenceSupport.ReferenceTokenWithOffset? {
        val startOffset = value?.textRange?.startOffset ?: valueStartOffset(parameters)
        val prefixText = originalText(parameters, startOffset, parameters.offset)
        val token = WireGoblinReferenceSupport.parseReferenceToken(prefixText) ?: return null
        return WireGoblinReferenceSupport.ReferenceTokenWithOffset(token.kind, token.raw, parameters.offset - token.raw.length)
    }

    fun currentScalarToken(parameters: CompletionParameters, value: PsiElement?): ScalarToken? {
        val startOffset = value?.textRange?.startOffset ?: valueStartOffset(parameters)
        val rawPrefix = originalText(parameters, startOffset, parameters.offset)
        val trimmedPrefix = rawPrefix.trimStart('"', '\'')
        val skipped = rawPrefix.length - trimmedPrefix.length
        return ScalarToken(startOffset + skipped, trimmedPrefix)
    }

    fun valueCompletionContext(parameters: CompletionParameters): ValueCompletionContext? {
        val keyValue = keyValueAtCaret(parameters) ?: return null
        if (!isLikelyValueContext(parameters, keyValue)) return null
        if (!WireGoblinYamlContextLocator.supportsEnvPlaceholder(keyValue)) return null
        return ValueCompletionContext(keyValue)
    }

    fun isLikelyValueContext(parameters: CompletionParameters, keyValue: YAMLKeyValue?): Boolean {
        keyValue ?: return false
        return isLikelyValueContextAtOffset(parameters.originalFile.text, parameters.offset, keyValue)
    }

    fun keyValueAtCaret(parameters: CompletionParameters): YAMLKeyValue? {
        lineKeyValueAtCaret(parameters)?.let { return it }

        val contextPosition = contextPosition(parameters)
        val neighborCandidates = listOfNotNull(
            contextPosition,
            contextPosition?.let { PsiTreeUtil.prevVisibleLeaf(it) },
            contextPosition?.let { PsiTreeUtil.nextVisibleLeaf(it) },
            parameters.position,
            parameters.position.let { PsiTreeUtil.prevVisibleLeaf(it) },
            parameters.position.let { PsiTreeUtil.nextVisibleLeaf(it) },
        )

        return neighborCandidates.firstNotNullOfOrNull { candidate ->
            PsiTreeUtil.getParentOfType(candidate, YAMLKeyValue::class.java, false)
                ?: (candidate as? YAMLKeyValue)
        }
    }

    private fun lineKeyValueAtCaret(parameters: CompletionParameters): YAMLKeyValue? {
        val text = parameters.originalFile.text
        val offset = parameters.offset.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', (offset - 1).coerceAtLeast(0)).let { it + 1 }
        val linePrefix = text.substring(lineStart, offset)
        val key = Regex("""^\s*(?:-\s*)?([A-Za-z0-9_]+)\s*:\s*.*$""").matchEntire(linePrefix)?.groupValues?.get(1) ?: return null
        val keyOffsetInLine = linePrefix.indexOf(key)
        if (keyOffsetInLine < 0) {
            return null
        }
        val keyElement = parameters.originalFile.findElementAt(lineStart + keyOffsetInLine) ?: return null
        return PsiTreeUtil.getParentOfType(keyElement, YAMLKeyValue::class.java, false)
    }

    private fun valueStartOffset(parameters: CompletionParameters): Int {
        val lineStart = parameters.originalFile.text.lastIndexOf('\n', (parameters.offset - 1).coerceAtLeast(0)).let { it + 1 }
        val prefix = originalText(parameters, lineStart, parameters.offset)
        return lineStart + WireGoblinReferenceSupport.valueStartOffset(prefix)
    }

    private fun currentLineLooksLikeKeyValue(parameters: CompletionParameters): Boolean {
        return currentLineLooksLikeKeyValue(parameters.originalFile.text, parameters.offset)
    }

    fun isLikelyValueContextAtOffset(text: String, offset: Int, keyValue: YAMLKeyValue): Boolean {
        if (keyValue.value is YAMLMapping || keyValue.value is YAMLSequence) {
            return false
        }
        if (!currentLineLooksLikeKeyValue(text, offset)) {
            return false
        }
        val keyEndOffset = keyValue.textRange.startOffset + keyValue.keyText.length
        return offset > keyEndOffset
    }

    private fun currentLineLooksLikeKeyValue(text: String, offset: Int): Boolean {
        val safeOffset = offset.coerceIn(0, text.length)
        val lineStart = text.lastIndexOf('\n', (safeOffset - 1).coerceAtLeast(0)).let { it + 1 }
        val linePrefix = text.substring(lineStart, safeOffset)
        return Regex("""^\s*(?:-\s*)?[A-Za-z0-9_]+\s*:\s*.*$""").matches(linePrefix)
    }

    private fun originalText(parameters: CompletionParameters, startOffset: Int, endOffset: Int): String {
        val text = parameters.originalFile.text
        val safeStart = startOffset.coerceIn(0, text.length)
        val safeEnd = endOffset.coerceIn(safeStart, text.length)
        return text.substring(safeStart, safeEnd)
    }

    internal data class ValueCompletionContext(
        val keyValue: YAMLKeyValue,
    )

    internal data class ScalarToken(
        val startOffset: Int,
        val raw: String,
    )
}
