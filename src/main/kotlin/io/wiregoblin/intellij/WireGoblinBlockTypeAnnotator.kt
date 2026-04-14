package io.wiregoblin.intellij

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

class WireGoblinBlockTypeAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val keyValue = element as? YAMLKeyValue ?: return
        if (!WireGoblinFileSupport.isWireGoblinFile(keyValue.containingFile)) {
            return
        }
        annotateKey(keyValue, holder)
        annotateKeyReferences(keyValue, holder)
        annotateScalarValue(keyValue, holder)
        if (WireGoblinYamlContextLocator.isMisnamedWorkflowBlockKey(keyValue)) {
            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Use 'blocks' here, not 'block'.",
            ).range(keyValue).create()
            return
        }
        if (keyValue.keyText != WireGoblinKeys.TYPE) {
            annotateFieldValue(keyValue, holder)
            annotateReferences(keyValue, holder)
            annotateEnvPlaceholders(keyValue, holder)
            annotateSqlPlaceholders(keyValue, holder)
            return
        }

        val value = keyValue.valueText.trim().removeSurrounding("\"").removeSurrounding("'")
        if (value.isEmpty()) {
            return
        }

        val constraint = WireGoblinTypeResolver.allowedTypeValues(keyValue) ?: return
        val allowedValuesSet = if (constraint.kind == WireGoblinTypeResolver.AllowedTypeKind.RETRY_RULE) {
            WireGoblinSchema.retryRuleTypesSet
        } else {
            WireGoblinSchema.blockTypesSet
        }
        if (value in allowedValuesSet) {
            annotateTypeValue(keyValue, holder, value, constraint.kind)
            return
        }

        val valueElement = keyValue.value ?: keyValue
        holder.newAnnotation(
            HighlightSeverity.ERROR,
            "Unknown ${constraint.description} '$value'. Expected one of: ${constraint.values.joinToString(", ")}",
        ).range(valueElement).create()
    }

    private fun annotateKey(keyValue: YAMLKeyValue, holder: AnnotationHolder) {
        val attributes = WireGoblinColorSettings.keyAttributes(keyValue) ?: return
        val range = TextRange(keyValue.textRange.startOffset, keyValue.textRange.startOffset + keyValue.keyText.length)
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(range)
            .textAttributes(attributes)
            .create()
    }

    private fun annotateKeyReferences(keyValue: YAMLKeyValue, holder: AnnotationHolder) {
        WireGoblinReferenceSupport.findReferenceTokens(keyValue.keyText).forEach { token ->
            val range = TextRange(
                keyValue.textRange.startOffset + token.startOffset,
                keyValue.textRange.startOffset + token.endOffset,
            )
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(range)
                .textAttributes(WireGoblinColorSettings.referenceAttributes(token.kind))
                .create()
        }
    }

    private fun annotateTypeValue(
        keyValue: YAMLKeyValue,
        holder: AnnotationHolder,
        value: String,
        kind: WireGoblinTypeResolver.AllowedTypeKind,
    ) {
        val attributes = WireGoblinColorSettings.typeValueAttributes(value, kind) ?: return
        val valueElement = keyValue.value ?: return
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(valueElement)
            .textAttributes(attributes)
            .create()
    }

    private fun annotateFieldValue(keyValue: YAMLKeyValue, holder: AnnotationHolder) {
        val constraint = WireGoblinTypeResolver.allowedFieldValues(keyValue) ?: return
        val value = keyValue.valueText.trim().removeSurrounding("\"").removeSurrounding("'")
        if (value.isEmpty()) {
            return
        }

        if (value in constraint.values) {
            annotateConstrainedValue(keyValue, holder, value)
            return
        }

        val valueElement = keyValue.value ?: keyValue
        holder.newAnnotation(
            HighlightSeverity.ERROR,
            "Unknown ${constraint.description.lowercase()} '$value'. Expected one of: ${constraint.values.joinToString(", ")}",
        ).range(valueElement).create()
    }

    private fun annotateConstrainedValue(keyValue: YAMLKeyValue, holder: AnnotationHolder, value: String) {
        val attributes = WireGoblinColorSettings.constrainedValueAttributes(keyValue, value) ?: return
        val valueElement = keyValue.value ?: return
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(valueElement)
            .textAttributes(attributes)
            .create()
    }

    private fun annotateReferences(keyValue: YAMLKeyValue, holder: AnnotationHolder) {
        if (!WireGoblinYamlContextLocator.supportsEnvPlaceholder(keyValue)) {
            return
        }

        val valueElement = keyValue.value ?: return
        if (valueElement is YAMLMapping) {
            return
        }
        val file = keyValue.containingFile as? YAMLFile ?: return
        val position = valueElement.firstChild ?: valueElement

        WireGoblinReferenceSupport.findReferenceTokens(valueElement.text).forEach { token ->
            if (WireGoblinYamlReferenceScopeHelper.shouldIgnoreReferenceToken(keyValue, token)) {
                return@forEach
            }

            val range = TextRange(
                valueElement.textRange.startOffset + token.startOffset,
                valueElement.textRange.startOffset + token.endOffset,
            )
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(range)
            .textAttributes(WireGoblinColorSettings.referenceAttributes(token.kind))
            .create()

            if (WireGoblinReferenceResolver.exists(token.kind, token.raw, file, position)) {
                return@forEach
            }

            holder.newAnnotation(
                HighlightSeverity.ERROR,
                "Unknown WireGoblin reference '${token.raw}'.",
            ).range(range).create()
        }
    }

    private fun annotateScalarValue(keyValue: YAMLKeyValue, holder: AnnotationHolder) {
        val attributes = WireGoblinColorSettings.scalarValueAttributes(keyValue) ?: return
        val valueElement = keyValue.value ?: return
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(valueElement)
            .textAttributes(attributes)
            .create()
    }

    private fun annotateEnvPlaceholders(keyValue: YAMLKeyValue, holder: AnnotationHolder) {
        if (!WireGoblinYamlContextLocator.supportsEnvPlaceholder(keyValue)) {
            return
        }
        val valueElement = keyValue.value ?: return
        WireGoblinHighlightSupport.findEnvironmentPlaceholders(valueElement.text).forEach { range ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(
                    TextRange(
                        valueElement.textRange.startOffset + range.first,
                        valueElement.textRange.startOffset + range.last + 1,
                    ),
                )
                .textAttributes(WireGoblinColorSettings.envPlaceholder)
                .create()
        }
    }

    private fun annotateSqlPlaceholders(keyValue: YAMLKeyValue, holder: AnnotationHolder) {
        if (keyValue.keyText != "query" || WireGoblinYamlContextLocator.enclosingBlockType(keyValue) != "postgres") {
            return
        }
        val valueElement = keyValue.value ?: return
        WireGoblinHighlightSupport.findSqlPlaceholders(valueElement.text).forEach { range ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(
                    TextRange(
                        valueElement.textRange.startOffset + range.first,
                        valueElement.textRange.startOffset + range.last + 1,
                    ),
                )
                .textAttributes(WireGoblinColorSettings.sqlPlaceholder)
                .create()
        }
    }
}
