package io.wiregoblin.intellij.psi

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import io.wiregoblin.intellij.WireGoblinReferenceAutoPopupTypedHandler
import kotlin.test.assertEquals

class WireGoblinReferenceAutoPopupTypedHandlerPsiTest : WireGoblinPsiTestCase() {
    private val handler = WireGoblinReferenceAutoPopupTypedHandler()

    fun testTriggersAutoPopupForAtReferenceInWireGoblinFile() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: @
            """.trimIndent(),
        )

        val result = handler.checkAutoPopup('@', project, myFixture.editor, file)

        assertEquals(TypedHandlerDelegate.Result.STOP, result)
    }

    fun testTriggersAutoPopupForDollarReferenceInWireGoblinFile() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: $
            """.trimIndent(),
        )

        val result = handler.checkAutoPopup('$', project, myFixture.editor, file)

        assertEquals(TypedHandlerDelegate.Result.STOP, result)
    }

    fun testDoesNotTriggerAutoPopupForOtherCharacters() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf"
            """.trimIndent(),
        )

        val result = handler.checkAutoPopup('a', project, myFixture.editor, file)

        assertEquals(TypedHandlerDelegate.Result.CONTINUE, result)
    }

    fun testDoesNotTriggerAutoPopupOutsideWireGoblinFile() {
        val file = configurePlainYaml(
            """
                value: @
            """.trimIndent(),
        )

        val result = handler.checkAutoPopup('@', project, myFixture.editor, file)

        assertEquals(TypedHandlerDelegate.Result.CONTINUE, result)
    }
}
