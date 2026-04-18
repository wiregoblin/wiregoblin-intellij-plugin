package io.wiregoblin.intellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue
import javax.swing.Icon

internal object WireGoblinWorkflowRunIcons {
    val run: Icon = AllIcons.Actions.Execute
    val stop: Icon = AllIcons.Actions.Suspend
}

class WireGoblinWorkflowRunLineMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName(): String = "WireGoblin workflow run marker"

    override fun getIcon() = WireGoblinWorkflowRunIcons.run

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
        val running = WireGoblinWorkflowRunner.isRunning(project, virtualFile, target.workflowId)
        val icon = if (running) WireGoblinWorkflowRunIcons.stop else WireGoblinWorkflowRunIcons.run
        val tooltip = if (running) {
            "Stop WireGoblin workflow '${target.workflowId}'"
        } else {
            "Run WireGoblin workflow '${target.workflowId}'"
        }

        val popupActions = if (running) {
            DefaultActionGroup(
                object : DumbAwareAction("Stop", null, WireGoblinWorkflowRunIcons.stop) {
                    override fun actionPerformed(event: AnActionEvent) {
                        WireGoblinWorkflowRunner.runOrStop(project, virtualFile, target.workflowId)
                    }
                },
            )
        } else {
            DefaultActionGroup(
                WorkflowRunVerbosity.entries.map { verbosity ->
                    object : DumbAwareAction(verbosity.menuText, null, WireGoblinWorkflowRunIcons.run) {
                        override fun actionPerformed(event: AnActionEvent) {
                            WireGoblinWorkflowRunner.runOrStop(project, virtualFile, target.workflowId, verbosity)
                        }
                    }
                },
            )
        }

        return WireGoblinWorkflowLineMarkerInfo(
            element,
            icon,
            tooltip,
            { _, _ -> WireGoblinWorkflowRunner.runOrStop(project, virtualFile, target.workflowId) },
            popupActions,
        )
    }
}

private class WireGoblinWorkflowLineMarkerInfo(
    element: PsiElement,
    icon: Icon,
    private val tooltip: String,
    navigationHandler: GutterIconNavigationHandler<PsiElement>,
    private val popupActions: ActionGroup,
) : LineMarkerInfo<PsiElement>(
    element,
    element.textRange,
    icon,
    { tooltip },
    navigationHandler,
    GutterIconRenderer.Alignment.LEFT,
    { tooltip },
) {
    override fun createGutterRenderer(): GutterIconRenderer {
        return object : LineMarkerGutterIconRenderer<PsiElement>(this) {
            override fun getPopupMenuActions(): ActionGroup = popupActions
        }
    }
}
