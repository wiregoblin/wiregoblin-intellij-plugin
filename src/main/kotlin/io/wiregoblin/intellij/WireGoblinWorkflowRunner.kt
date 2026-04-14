package io.wiregoblin.intellij

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.util.Disposer
import java.nio.charset.StandardCharsets

internal object WireGoblinWorkflowRunner {
    private val candidateCommands = listOf("wiregoblin-cli", "wiregoblin")

    fun run(project: Project, file: VirtualFile, workflowId: String) {
        FileDocumentManager.getInstance().saveAllDocuments()

        val executable = candidateCommands
            .firstNotNullOfOrNull { PathEnvironmentVariableUtil.findInPath(it)?.path }

        if (executable == null) {
            notify(
                project = project,
                content = "WireGoblin CLI was not found in PATH. Install `wiregoblin-cli` or `wiregoblin` to run workflows from the gutter.",
                type = NotificationType.ERROR,
            )
            return
        }

        val commandLine = GeneralCommandLine(
            executable,
            "run",
            "-p",
            file.path,
            workflowId,
        ).withCharset(StandardCharsets.UTF_8)

        file.parent?.let { commandLine.withWorkDirectory(it.path) }

        try {
            val processHandler = KillableColoredProcessHandler(commandLine)
            val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
            console.attachToProcess(processHandler)

            val descriptor = RunContentDescriptor(
                console,
                processHandler,
                console.component,
                "WireGoblin: $workflowId",
            )
            descriptor.setActivateToolWindowWhenAdded(true)
            descriptor.isAutoFocusContent = true
            Disposer.register(descriptor, console)

            RunContentManager.getInstance(project)
                .showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor)

            processHandler.startNotify()
        } catch (error: ExecutionException) {
            notify(
                project = project,
                content = "Failed to start WireGoblin workflow '$workflowId': ${error.message ?: "unknown error"}",
                type = NotificationType.ERROR,
            )
        }
    }

    private fun notify(project: Project, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("WireGoblin Notifications")
            .createNotification(content, type)
            .notify(project)
    }
}
