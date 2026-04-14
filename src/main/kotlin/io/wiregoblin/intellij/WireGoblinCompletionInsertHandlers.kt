package io.wiregoblin.intellij

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

internal object WireGoblinCompletionInsertHandlers {
    fun keyInsertHandler(key: String): InsertHandler<LookupElement> {
        return InsertHandler { insertionContext, _ ->
            val document = insertionContext.document
            val lineNumber = document.getLineNumber(insertionContext.startOffset)
            val lineStart = document.getLineStartOffset(lineNumber)
            val lineEnd = document.getLineEndOffset(lineNumber)
            val lineText = document.getText(TextRange(lineStart, lineEnd))
            val leadingIndentLength = lineText.indexOfFirst { it != ' ' && it != '\t' }
                .let { if (it < 0) lineText.length else it }
            val leadingIndent = lineText.substring(0, leadingIndentLength)
            val trimmedLine = lineText.trim()
            val dashKeyLine = trimmedLine.startsWith("- ") && trimmedLine.length > 2 && !trimmedLine.contains(":")

            val replacementStart: Int
            val replacementEnd: Int
            val replacementText: String

            if (dashKeyLine) {
                replacementStart = lineStart
                replacementEnd = lineEnd
                replacementText = "${leadingIndent}- ${key}: "
            } else if (trimmedLine.isEmpty() || !trimmedLine.contains(":")) {
                replacementStart = lineStart
                replacementEnd = lineEnd
                replacementText = "${leadingIndent}${key}: "
            } else {
                replacementStart = insertionContext.startOffset
                replacementEnd = insertionContext.tailOffset
                replacementText = "${key}: "
            }

            document.replaceString(replacementStart, replacementEnd, replacementText)
            insertionContext.editor.caretModel.moveToOffset(replacementStart + replacementText.length)
        }
    }

    fun envInsertHandler(
        template: String,
        variableStartOffset: Int,
        selectionEndOffset: Int = 6,
    ): InsertHandler<LookupElement> {
        return InsertHandler { insertionContext: InsertionContext, _: LookupElement ->
            insertionContext.document.replaceString(
                insertionContext.startOffset,
                insertionContext.tailOffset,
                template,
            )
            val start = insertionContext.startOffset + variableStartOffset
            val end = insertionContext.startOffset + selectionEndOffset
            insertionContext.editor.selectionModel.setSelection(start, end)
            insertionContext.editor.caretModel.moveToOffset(start)
        }
    }

    fun referenceInsertHandler(
        token: WireGoblinReferenceSupport.ReferenceTokenWithOffset,
        suggestion: String,
    ): InsertHandler<LookupElement> {
        return InsertHandler { insertionContext: InsertionContext, _: LookupElement ->
            insertionContext.document.replaceString(token.startOffset, insertionContext.tailOffset, suggestion)
            insertionContext.editor.caretModel.moveToOffset(token.startOffset + suggestion.length)
        }
    }

    fun scalarValueInsertHandler(replacementStart: Int, value: String): InsertHandler<LookupElement> {
        return InsertHandler { insertionContext, _ ->
            insertionContext.document.replaceString(replacementStart, insertionContext.tailOffset, value)
            insertionContext.editor.caretModel.moveToOffset(replacementStart + value.length)
        }
    }

    fun blockTypeInsertHandler(type: String): InsertHandler<LookupElement> {
        return InsertHandler { insertionContext: InsertionContext, _: LookupElement ->
            val document = insertionContext.document
            val elementAtOffset = insertionContext.file.findElementAt(insertionContext.startOffset)
                ?: return@InsertHandler
            val keyValue = PsiTreeUtil.getParentOfType(elementAtOffset, YAMLKeyValue::class.java)
                ?: return@InsertHandler

            val value = keyValue.value
            val valueRange = value?.textRange ?: TextRange(insertionContext.startOffset, insertionContext.tailOffset)
            val mapping = keyValue.parent as? YAMLMapping
            val existingKeys = mapping?.keyValues?.map { it.keyText }?.toSet().orEmpty()
            val keyTextOffset = keyValue.textOffset
            val lineNumber = document.getLineNumber(keyTextOffset)
            val lineStart = document.getLineStartOffset(lineNumber)
            val keyLineText = document.getText(TextRange(lineStart, keyTextOffset))
            val indent = keyLineText.takeWhile { it == ' ' || it == '\t' }
            val templateLines = buildList {
                var includeNestedLines = false
                WireGoblinSchema.blockTypeTemplates[type].orEmpty().forEach { line ->
                    if (line.startsWith(" ") || line.startsWith("\t")) {
                        if (includeNestedLines) {
                            add(line)
                        }
                        return@forEach
                    }

                    val templateKey = line.substringBefore(":").trim()
                    includeNestedLines = templateKey.isNotEmpty() && templateKey !in existingKeys
                    if (includeNestedLines) {
                        add(line)
                    }
                }
            }

            document.replaceString(valueRange.startOffset, valueRange.endOffset, type)

            if (templateLines.isEmpty()) {
                insertionContext.editor.caretModel.moveToOffset(valueRange.startOffset + type.length)
                return@InsertHandler
            }

            val insertedBlock = buildString {
                templateLines.forEach { line ->
                    append('\n')
                    append(indent)
                    append(line)
                }
            }

            val insertOffset = document.getLineEndOffset(lineNumber)
            document.insertString(insertOffset, insertedBlock)

            val firstTemplate = templateLines.first()
            val firstValueOffset = firstTemplate.indexOf(':')
                .takeIf { it >= 0 }
                ?.let { minOf(it + 2, firstTemplate.length) }
                ?: firstTemplate.length
            val caretOffset = insertOffset + 1 + indent.length + firstValueOffset
            insertionContext.editor.caretModel.moveToOffset(caretOffset)
        }
    }
}
