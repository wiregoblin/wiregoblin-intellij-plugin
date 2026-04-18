package io.wiregoblin.intellij

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon

internal object WireGoblinWorkflowRunner {
    private val candidateCommands = listOf("wiregoblin-cli", "wiregoblin")
    private val runningWorkflows = ConcurrentHashMap<WorkflowRunKey, RunningWorkflow>()

    fun runOrStop(project: Project, file: VirtualFile, workflowId: String, verbosity: WorkflowRunVerbosity = WorkflowRunVerbosity.DEFAULT) {
        runOrStop(project, file, workflowId, workflowId, verbosity)
    }

    fun runOrStopFile(project: Project, file: VirtualFile, verbosity: WorkflowRunVerbosity = WorkflowRunVerbosity.DEFAULT) {
        runOrStop(project, file, null, file.name, verbosity)
    }

    fun isFileRunning(project: Project, file: VirtualFile): Boolean {
        return isRunningKey(project, file, null)
    }

    private fun runOrStop(
        project: Project,
        file: VirtualFile,
        workflowId: String?,
        runName: String,
        verbosity: WorkflowRunVerbosity = WorkflowRunVerbosity.DEFAULT,
    ) {
        val key = WorkflowRunKey(project.locationHash, file.path, workflowId)
        if (stop(key)) {
            restartLineMarkers(project, file)
            return
        }

        FileDocumentManager.getInstance().saveAllDocuments()

        val executable = candidateCommands
            .firstNotNullOfOrNull { PathEnvironmentVariableUtil.findInPath(it)?.path }

        if (executable == null) {
            notify(
                project = project,
                content = "WireGoblin CLI was not found in PATH. Install `wiregoblin-cli` or `wiregoblin` to run workflows.",
                type = NotificationType.ERROR,
            )
            return
        }

        val runningWorkflow = RunningWorkflow()
        if (runningWorkflows.putIfAbsent(key, runningWorkflow) != null) {
            stop(key)
            restartLineMarkers(project, file)
            return
        }
        restartLineMarkers(project, file)

        val commandLineArguments = buildList {
            add("run")
            addAll(verbosity.arguments)
            add("-p")
            add(file.path)
            workflowId?.let(::add)
        }
        val commandLine = GeneralCommandLine(executable, *commandLineArguments.toTypedArray())
            .withCharset(StandardCharsets.UTF_8)

        file.parent?.let { commandLine.withWorkDirectory(it.path) }

        try {
            val profile = WireGoblinRunProfile(project, runName, file, commandLine, key, runningWorkflow)
            ExecutionEnvironmentBuilder
                .create(project, DefaultRunExecutor.getRunExecutorInstance(), profile)
                .buildAndExecute()
        } catch (error: ExecutionException) {
            runningWorkflows.remove(key, runningWorkflow)
            restartLineMarkers(project, file)
            notify(
                project = project,
                content = "Failed to start WireGoblin run '$runName': ${error.message ?: "unknown error"}",
                type = NotificationType.ERROR,
            )
        }
    }

    fun isRunning(project: Project, file: VirtualFile, workflowId: String): Boolean {
        return isRunningKey(project, file, workflowId)
    }

    private fun isRunningKey(project: Project, file: VirtualFile, workflowId: String?): Boolean {
        val key = WorkflowRunKey(project.locationHash, file.path, workflowId)
        val runningWorkflow = runningWorkflows[key] ?: return false
        if (!runningWorkflow.isActive()) {
            runningWorkflows.remove(key, runningWorkflow)
            return false
        }
        return true
    }

    private fun stop(key: WorkflowRunKey): Boolean {
        val runningWorkflow = runningWorkflows[key] ?: return false
        runningWorkflow.stopRequested = true
        runningWorkflow.processHandler?.let { processHandler ->
            if (!processHandler.isProcessTerminated && !processHandler.isProcessTerminating) {
                processHandler.destroyProcess()
            }
        }
        return true
    }

    private fun restartLineMarkers(project: Project, file: VirtualFile) {
        ApplicationManager.getApplication().invokeLater {
            if (!project.isDisposed && file.isValid) {
                PsiManager.getInstance(project).findFile(file)?.let { psiFile ->
                    DaemonCodeAnalyzer.getInstance(project).restart(psiFile, "WireGoblin workflow run state changed")
                }
            }
        }
    }

    private fun notify(project: Project, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("WireGoblin Notifications")
            .createNotification(content, type)
            .notify(project)
    }

    private class WireGoblinRunProfile(
        private val project: Project,
        private val runName: String,
        private val file: VirtualFile,
        private val commandLine: GeneralCommandLine,
        private val key: WorkflowRunKey,
        private val runningWorkflow: RunningWorkflow,
    ) : RunProfile {
        override fun getName(): String = "WireGoblin: $runName"

        override fun getIcon(): Icon = WireGoblinWorkflowRunIcons.run

        override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
            return object : CommandLineState(environment) {
                init {
                    consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project)
                }

                override fun startProcess(): ProcessHandler {
                    val processHandler = try {
                        KillableColoredProcessHandler(commandLine)
                    } catch (error: ExecutionException) {
                        runningWorkflows.remove(key, runningWorkflow)
                        restartLineMarkers(project, file)
                        throw error
                    }
                    runningWorkflow.processHandler = processHandler
                    processHandler.addProcessListener(object : ProcessListener {
                        override fun processTerminated(event: ProcessEvent) {
                            runningWorkflows.remove(key, runningWorkflow)
                            restartLineMarkers(project, file)
                        }
                    })
                    if (runningWorkflow.stopRequested) {
                        processHandler.destroyProcess()
                    }
                    return processHandler
                }
            }
        }
    }

    private data class WorkflowRunKey(
        val projectLocationHash: String,
        val filePath: String,
        val workflowId: String?,
    )

    private class RunningWorkflow {
        @Volatile
        var processHandler: ProcessHandler? = null

        @Volatile
        var stopRequested: Boolean = false

        fun isActive(): Boolean {
            if (stopRequested) {
                return true
            }
            val handler = processHandler
            return handler == null || !handler.isProcessTerminated
        }
    }
}

internal enum class WorkflowRunVerbosity(
    val menuText: String,
    val arguments: List<String>,
) {
    DEFAULT("Run", emptyList()),
    VERBOSE("Run -v", listOf("-v")),
    VERY_VERBOSE("Run -vv", listOf("-vv")),
    TRACE("Run -vvv", listOf("-vvv")),
}
