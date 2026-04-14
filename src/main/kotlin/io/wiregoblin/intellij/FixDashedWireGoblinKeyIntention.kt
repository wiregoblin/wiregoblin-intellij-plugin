package io.wiregoblin.intellij

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLSequenceItem

class FixDashedWireGoblinKeyIntention : PsiElementBaseIntentionAction() {
    companion object {
        private val LOG = Logger.getInstance(FixDashedWireGoblinKeyIntention::class.java)
    }

    override fun getFamilyName(): String = "WireGoblin"

    override fun getText(): String = "Convert '- key:' to WireGoblin mapping key"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (editor == null) return false
        if (!WireGoblinFileSupport.isWireGoblinFile(element.containingFile)) return false

        val keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java) ?: return false
        val sequenceItem = PsiTreeUtil.getParentOfType(keyValue, YAMLSequenceItem::class.java) ?: return false
        val sequence = sequenceItem.parent as? YAMLSequence ?: return false
        val sequenceOwner = sequence.parent as? YAMLKeyValue ?: return false

        val validKeys = when (sequenceOwner.keyText) {
            WireGoblinKeys.WORKFLOWS -> WireGoblinSchema.workflowKeys
            WireGoblinKeys.BLOCKS, WireGoblinKeys.CATCH_ERROR_BLOCKS -> WireGoblinSchema.blockKeys
            else -> emptyList()
        }

        val document = editor.document
        val lineNumber = document.getLineNumber(keyValue.textOffset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineText = document.getText(TextRange(lineStart, keyValue.textOffset))
        return keyValue.keyText in validKeys && lineText.contains("- ")
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return

        val keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java) ?: return
        val offset = keyValue.textOffset
        val document = editor.document
        val lineNumber = document.getLineNumber(offset)
        val lineStart = document.getLineStartOffset(lineNumber)
        val lineEnd = document.getLineEndOffset(lineNumber)
        val lineText = document.getText(TextRange(lineStart, lineEnd))
        val dashIndex = lineText.indexOf("- ")
        if (dashIndex < 0) {
            LOG.warn("FixDashedWireGoblinKeyIntention was available but no dashed key prefix was found at offset $offset")
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            document.replaceString(lineStart + dashIndex, lineStart + dashIndex + 2, "")
        }
    }
}
