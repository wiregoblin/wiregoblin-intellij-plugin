package io.wiregoblin.intellij

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile

class WireGoblinFileRunActionGroup : ActionGroup("WireGoblin", true), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val file = event.wireGoblinFile()
        event.presentation.isEnabledAndVisible = file != null
    }

    override fun getChildren(event: AnActionEvent?): Array<AnAction> {
        val currentEvent = event ?: return emptyArray()
        val project = currentEvent.project ?: return emptyArray()
        val file = currentEvent.wireGoblinFile() ?: return emptyArray()

        if (WireGoblinWorkflowRunner.isFileRunning(project, file)) {
            return arrayOf(
                object : DumbAwareAction("Stop", null, WireGoblinWorkflowRunIcons.stop) {
                    override fun actionPerformed(event: AnActionEvent) {
                        WireGoblinWorkflowRunner.runOrStopFile(project, file)
                    }
                },
            )
        }

        return WorkflowRunVerbosity.entries
            .map { verbosity ->
                object : DumbAwareAction(verbosity.menuText, null, WireGoblinWorkflowRunIcons.run) {
                    override fun actionPerformed(event: AnActionEvent) {
                        WireGoblinWorkflowRunner.runOrStopFile(project, file, verbosity)
                    }
                }
            }
            .toTypedArray()
    }

    private fun AnActionEvent.wireGoblinFile(): VirtualFile? {
        val files = getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        val file = when {
            files != null && files.size == 1 -> files.single()
            files == null -> getData(CommonDataKeys.VIRTUAL_FILE)
            else -> null
        } ?: getData(CommonDataKeys.PSI_FILE)?.virtualFile ?: return null

        return file.takeUnless { it.isDirectory }
            ?.takeIf(WireGoblinFileSupport::matchesWireGoblinName)
    }
}
