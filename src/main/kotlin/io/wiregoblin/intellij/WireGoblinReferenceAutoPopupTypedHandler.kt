package io.wiregoblin.intellij

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue

class WireGoblinReferenceAutoPopupTypedHandler : TypedHandlerDelegate() {
    private val triggerChars = WireGoblinReferenceKind.prefixes()
    private val keyTriggerChars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '_'
    private val constrainedValueTriggerChars = setOf('"', '\'', '=', '!', '>', '<') + ('a'..'z') + ('A'..'Z')

    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (!WireGoblinFileSupport.isWireGoblinFile(file)) {
            return Result.CONTINUE
        }

        if (charTyped in triggerChars) {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            return Result.STOP
        }

        if (charTyped in keyTriggerChars && shouldTriggerKeyAutoPopup(editor, file)) {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            return Result.STOP
        }

        if (charTyped in constrainedValueTriggerChars && shouldTriggerConstrainedValueAutoPopup(editor, file)) {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
            return Result.STOP
        }

        return Result.CONTINUE
    }

    private fun shouldTriggerKeyAutoPopup(editor: Editor, file: PsiFile): Boolean {
        val offset = editor.caretModel.offset
        val text = file.text
        if (offset <= 0 || offset > text.length) {
            return false
        }
        val lineStart = text.lastIndexOf('\n', (offset - 1).coerceAtLeast(0)).let { it + 1 }
        val linePrefix = text.substring(lineStart, offset)
        if (!Regex("""^\s*(?:-\s*)?[A-Za-z0-9_]+$""").matches(linePrefix.trimEnd())) {
            return false
        }

        val element = file.findElementAt((offset - 1).coerceAtLeast(0)) ?: return false
        return WireGoblinYamlContextLocator.contextKeys(element).isNotEmpty()
    }

    private fun shouldTriggerConstrainedValueAutoPopup(editor: Editor, file: PsiFile): Boolean {
        val offset = editor.caretModel.offset
        val element = file.findElementAt((offset - 1).coerceAtLeast(0))
            ?: file.findElementAt(offset.coerceAtMost(file.textLength - 1))
            ?: return false
        val keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java, false) ?: return false
        if (!WireGoblinCompletionContextHelper.isLikelyValueContextAtOffset(file.text, offset, keyValue)) {
            return false
        }
        return WireGoblinTypeResolver.allowedFieldValues(keyValue) != null || WireGoblinTypeResolver.allowedTypeValues(keyValue) != null
    }
}
