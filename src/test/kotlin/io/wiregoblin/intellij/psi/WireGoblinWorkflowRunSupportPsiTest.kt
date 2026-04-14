package io.wiregoblin.intellij.psi

import com.intellij.psi.util.PsiTreeUtil
import io.wiregoblin.intellij.WireGoblinKeys
import io.wiregoblin.intellij.WireGoblinWorkflowRunHelper
import io.wiregoblin.intellij.WireGoblinWorkflowRunLineMarkerProvider
import org.jetbrains.yaml.psi.YAMLKeyValue
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WireGoblinWorkflowRunSupportPsiTest : WireGoblinPsiTestCase() {
    private val lineMarkerProvider = WireGoblinWorkflowRunLineMarkerProvider()

    fun testWorkflowRunTargetResolvesRunnableWorkflowId() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf_main"
                    blocks: []
            """.trimIndent(),
        )

        val idKeyValue = PsiTreeUtil.findChildrenOfType(file, YAMLKeyValue::class.java)
            .firstOrNull { it.keyText == WireGoblinKeys.ID }
        val target = idKeyValue?.let(WireGoblinWorkflowRunHelper::workflowRunTarget)
        assertNotNull(target)

        assertEquals("wf_main", target?.workflowId)
        assertTrue(target?.disableRun == false)
    }

    fun testWorkflowRunTargetSkipsDisabledWorkflow() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf_disabled"
                    disable_run: true
                    blocks: []
            """.trimIndent(),
        )

        val idKeyValue = PsiTreeUtil.findChildrenOfType(file, YAMLKeyValue::class.java)
            .firstOrNull { it.keyText == WireGoblinKeys.ID }
        val target = idKeyValue?.let(WireGoblinWorkflowRunHelper::workflowRunTarget)
        assertNotNull(target)

        assertEquals("wf_disabled", target?.workflowId)
        assertTrue(target?.disableRun == true)
    }

    fun testWorkflowRunTargetIgnoresNonWorkflowIds() {
        val file = configureWireGoblin(
            """
                id: "project_id"
                workflows:
                  - id: "wf_main"
                    blocks:
                      - id: "step_1"
                        type: "log"
                        message: "hello"
            """.trimIndent(),
        )

        val keyValues = PsiTreeUtil.findChildrenOfType(file, YAMLKeyValue::class.java)
        val projectId = keyValues.firstOrNull { it.keyText == WireGoblinKeys.ID && it.valueText == "project_id" }
        val blockId = keyValues.firstOrNull { it.keyText == WireGoblinKeys.ID && it.valueText == "step_1" }

        assertNull(projectId?.let(WireGoblinWorkflowRunHelper::workflowRunTarget))
        assertNull(blockId?.let(WireGoblinWorkflowRunHelper::workflowRunTarget))
    }

    fun testWorkflowRunMarkerIsNotShownOutsideWireGoblinFiles() {
        val file = configurePlainYaml(
            """
                workflows:
                  - id: "wf_main"
                    blocks: []
            """.trimIndent(),
        )

        val idKeyValue = PsiTreeUtil.findChildrenOfType(file, YAMLKeyValue::class.java)
            .firstOrNull { it.keyText == WireGoblinKeys.ID }
        val marker = idKeyValue
            ?.key
            ?.let(lineMarkerProvider::getLineMarkerInfo)

        assertNull(marker)
    }

    fun testWorkflowRunMarkerIsShownInWireGoblinFiles() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf_main"
                    blocks: []
            """.trimIndent(),
        )

        val idKeyValue = PsiTreeUtil.findChildrenOfType(file, YAMLKeyValue::class.java)
            .firstOrNull { it.keyText == WireGoblinKeys.ID }
        val marker = idKeyValue
            ?.key
            ?.let(lineMarkerProvider::getLineMarkerInfo)

        assertNotNull(marker)
        assertEquals("Run WireGoblin workflow 'wf_main'", marker!!.lineMarkerTooltip)
    }
}
