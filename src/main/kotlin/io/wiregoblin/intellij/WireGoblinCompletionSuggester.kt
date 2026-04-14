package io.wiregoblin.intellij

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.AutoCompletionPolicy
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequenceItem

internal object WireGoblinCompletionSuggester {
    fun addCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        val file = parameters.originalFile
        if (!WireGoblinFileSupport.isWireGoblinFile(file)) {
            return
        }

        if (addBlockTypeCompletions(parameters, result)) {
            result.stopHere()
            return
        }

        if (addFieldValueCompletions(parameters, result)) {
            result.stopHere()
            return
        }

        val valueContext = WireGoblinCompletionContextHelper.valueCompletionContext(parameters)

        if (addReferenceCompletions(parameters, result, valueContext)) {
            result.stopHere()
            return
        }

        if (addEnvCompletions(parameters, result, valueContext)) {
            result.stopHere()
            return
        }

        if (WireGoblinCompletionContextHelper.isLikelyValueContext(
                parameters,
                WireGoblinCompletionContextHelper.keyValueAtCaret(parameters),
            )
        ) {
            result.stopHere()
            return
        }

        addKeyCompletions(parameters, result)
    }

    private fun addKeyCompletions(parameters: CompletionParameters, result: CompletionResultSet) {
        val contextPosition = WireGoblinCompletionContextHelper.contextPosition(parameters) ?: return
        val keys = WireGoblinYamlContextLocator.contextKeys(contextPosition)
        if (keys.isEmpty()) {
            return
        }

        val currentMapping = PsiTreeUtil.getParentOfType(contextPosition, YAMLMapping::class.java)
        val currentSequenceItem = PsiTreeUtil.getParentOfType(contextPosition, YAMLSequenceItem::class.java)
        val currentSequenceOwner = currentSequenceItem
            ?.parent
            ?.parent as? YAMLKeyValue
        val insideExistingSequenceItem =
            currentSequenceItem != null &&
                (currentSequenceOwner?.keyText == WireGoblinKeys.BLOCKS ||
                    currentSequenceOwner?.keyText == WireGoblinKeys.CATCH_ERROR_BLOCKS)
        val existingKeys = if (insideExistingSequenceItem) {
            currentMapping?.keyValues?.map { it.keyText }?.toSet().orEmpty()
        } else {
            emptySet()
        }
        val availableKeys = keys.filter { it !in existingKeys }

        availableKeys.forEach { key ->
                result.addElement(
                    LookupElementBuilder.create(key)
                        .withPresentableText("$key:")
                        .withTypeText("WireGoblin", true)
                        .withInsertHandler(WireGoblinCompletionInsertHandlers.keyInsertHandler(key)),
                )
            }

        result.stopHere()
    }

    private fun addReferenceCompletions(
        parameters: CompletionParameters,
        result: CompletionResultSet,
        valueContext: WireGoblinCompletionContextHelper.ValueCompletionContext?,
    ): Boolean {
        valueContext ?: return false
        val anchor = WireGoblinCompletionContextHelper.contextPosition(parameters)
            ?: valueContext.keyValue.value
            ?: valueContext.keyValue
        val token = WireGoblinCompletionContextHelper.currentReferenceToken(parameters, valueContext.keyValue.value) ?: return false
        val suggestions = WireGoblinReferenceResolver.suggestions(
            token.kind,
            parameters.originalFile as? YAMLFile,
            anchor,
        )
            .filter { it.startsWith(token.raw) }
            .distinct()

        if (suggestions.isEmpty()) {
            return false
        }

        val prefixedResult = result.withPrefixMatcher(token.raw)
        suggestions.forEach { suggestion ->
            prefixedResult.addElement(
                AutoCompletionPolicy.NEVER_AUTOCOMPLETE.applyPolicy(
                    LookupElementBuilder.create(suggestion)
                        .withTypeText("WireGoblin reference", true)
                        .withInsertHandler(WireGoblinCompletionInsertHandlers.referenceInsertHandler(token, suggestion)),
                ),
            )
        }
        return true
    }

    private fun addEnvCompletions(
        parameters: CompletionParameters,
        result: CompletionResultSet,
        valueContext: WireGoblinCompletionContextHelper.ValueCompletionContext?,
    ): Boolean {
        valueContext ?: return false
        if (WireGoblinCompletionContextHelper.currentReferenceToken(parameters, valueContext.keyValue.value) != null) {
            return false
        }

        WireGoblinReferenceCatalog.helperTemplates.forEach { helper ->
            result.addElement(
                LookupElementBuilder.create(helper.template)
                    .withPresentableText(helper.template)
                    .withTypeText(helper.description, true)
                    .withInsertHandler(
                        WireGoblinCompletionInsertHandlers.envInsertHandler(
                            helper.template,
                            helper.selectionStartOffset,
                            helper.selectionEndOffset,
                        ),
                    ),
            )
        }
        return true
    }

    private fun addFieldValueCompletions(
        parameters: CompletionParameters,
        result: CompletionResultSet,
    ): Boolean {
        val keyValue = WireGoblinCompletionContextHelper.keyValueAtCaret(parameters) ?: return false
        if (!WireGoblinCompletionContextHelper.isLikelyValueContext(parameters, keyValue)) {
            return false
        }

        val constraint = WireGoblinTypeResolver.allowedFieldValues(keyValue) ?: return false
        val token = WireGoblinCompletionContextHelper.currentScalarToken(parameters, keyValue.value)
        val prefix = token?.raw.orEmpty()
        val replacementStart = token?.startOffset ?: (keyValue.value?.textRange?.startOffset ?: parameters.offset)
        val suggestions = constraint.values.filter { it.startsWith(prefix) }
        if (suggestions.isEmpty()) {
            return false
        }

        suggestions.forEach { value ->
            val description = WireGoblinValueHelp.descriptionFor(keyValue.keyText, value)
            result.addElement(
                LookupElementBuilder.create(value)
                    .let { builder ->
                        if (description != null) {
                            builder.withTailText("  $description", true)
                        } else {
                            builder
                        }
                    }
                    .withTypeText(constraint.description, true)
                    .withInsertHandler(WireGoblinCompletionInsertHandlers.scalarValueInsertHandler(replacementStart, value)),
            )
        }
        return true
    }

    private fun addBlockTypeCompletions(
        parameters: CompletionParameters,
        result: CompletionResultSet,
    ): Boolean {
        val keyValue = WireGoblinCompletionContextHelper.keyValueAtCaret(parameters) ?: return false
        if (keyValue.keyText != WireGoblinKeys.TYPE) {
            return false
        }

        val constraint = WireGoblinTypeResolver.allowedTypeValues(keyValue) ?: return false
        constraint.values.forEach { type ->
            result.addElement(
                LookupElementBuilder.create(type)
                    .withTypeText(constraint.description, true)
                    .withInsertHandler(WireGoblinCompletionInsertHandlers.blockTypeInsertHandler(type)),
            )
        }
        return true
    }
}
