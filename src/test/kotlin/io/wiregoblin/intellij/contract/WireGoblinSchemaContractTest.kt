package io.wiregoblin.intellij.contract

import io.wiregoblin.intellij.WireGoblinKeys
import io.wiregoblin.intellij.WireGoblinSchema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WireGoblinSchemaContractTest {
    @Test
    fun `workflow keys include current fields and exclude legacy on_error`() {
        assertTrue(WireGoblinKeys.DISABLE_RUN in WireGoblinSchema.workflowKeys)
        assertTrue(WireGoblinKeys.TIMEOUT_SECONDS in WireGoblinSchema.workflowKeys)
        assertTrue(WireGoblinKeys.SECRETS in WireGoblinSchema.workflowKeys)
        assertTrue(WireGoblinKeys.OUTPUTS in WireGoblinSchema.workflowKeys)
        assertTrue(WireGoblinKeys.CATCH_ERROR_BLOCKS in WireGoblinSchema.workflowKeys)
        assertFalse("on_error" in WireGoblinSchema.workflowKeys)
    }

    @Test
    fun `retry rule types match current docs`() {
        assertEquals(listOf("transport_error", "status_code", "path"), WireGoblinSchema.retryRuleTypes)
    }

    @Test
    fun `http block spec exposes required fields and method values`() {
        val http = requireNotNull(WireGoblinSchema.blockSpecsByType["http"])

        assertEquals(listOf("method", "url"), http.requiredFields.map { it.name })
        assertEquals(listOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"), http.field("method")?.allowedValues)
    }
}
