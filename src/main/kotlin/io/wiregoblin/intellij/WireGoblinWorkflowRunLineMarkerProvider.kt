package io.wiregoblin.intellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue

class WireGoblinWorkflowRunLineMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName(): String = "WireGoblin workflow run marker"

    override fun getIcon() = AllIcons.Actions.Execute

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val keyValue = element.parent as? YAMLKeyValue ?: return null
        if (!WireGoblinFileSupport.isWireGoblinFile(keyValue.containingFile)) {
            return null
        }
        if (element.text != keyValue.keyText || element.textOffset != keyValue.textOffset) {
            return null
        }

        val target = WireGoblinWorkflowRunHelper.workflowRunTarget(keyValue)
            ?.takeUnless { it.disableRun }
            ?: return null

        val virtualFile = keyValue.containingFile.virtualFile ?: return null
        val project = keyValue.project
        val tooltip = "Run WireGoblin workflow '${target.workflowId}'"

        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Actions.Execute,
            { tooltip },
            { _, _ -> WireGoblinWorkflowRunner.run(project, virtualFile, target.workflowId) },
            GutterIconRenderer.Alignment.LEFT,
            { tooltip },
        )
    }
}
