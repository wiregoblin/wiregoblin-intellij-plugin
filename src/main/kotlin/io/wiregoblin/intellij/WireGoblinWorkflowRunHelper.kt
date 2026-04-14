package io.wiregoblin.intellij

import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

internal object WireGoblinWorkflowRunHelper {
    fun workflowRunTarget(keyValue: YAMLKeyValue): WireGoblinWorkflowRunTarget? {
        if (keyValue.keyText != WireGoblinKeys.ID) {
            return null
        }

        val mapping = keyValue.parent as? YAMLMapping ?: return null
        if (WireGoblinYamlContextLocator.sequenceOwnerKey(mapping) != WireGoblinKeys.WORKFLOWS) {
            return null
        }

        val workflowId = keyValue.valueText
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
        if (workflowId.isBlank()) {
            return null
        }

        val disableRun = mapping.getKeyValueByKey(WireGoblinKeys.DISABLE_RUN)
            ?.valueText
            ?.trim()
            ?.lowercase() == "true"

        return WireGoblinWorkflowRunTarget(
            workflowId = workflowId,
            disableRun = disableRun,
        )
    }
}
