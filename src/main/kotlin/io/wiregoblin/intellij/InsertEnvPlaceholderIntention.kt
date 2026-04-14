package io.wiregoblin.intellij

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue

class InsertEnvPlaceholderIntention : PsiElementBaseIntentionAction() {
    override fun getFamilyName(): String = "WireGoblin"

    override fun getText(): String = "Insert \${NAME:=default} placeholder"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (editor == null) return false
        if (!WireGoblinFileSupport.isWireGoblinFile(element.containingFile)) return false
        val keyValue = PsiTreeUtil.getParentOfType(element, YAMLKeyValue::class.java) ?: return false
        val value = keyValue.value ?: return false
        return PsiTreeUtil.isAncestor(value, element, false)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null) return
        val template = "\${NAME:=default}"
        val offset = editor.caretModel.offset
        WriteCommandAction.runWriteCommandAction(project) {
            editor.document.insertString(offset, template)
            editor.selectionModel.setSelection(offset + 2, offset + 6)
            editor.caretModel.moveToOffset(offset + 2)
        }
    }
}
