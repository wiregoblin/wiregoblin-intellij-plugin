package io.wiregoblin.intellij.unit

import io.wiregoblin.intellij.WireGoblinColorSettings
import io.wiregoblin.intellij.WireGoblinHighlightSupport
import io.wiregoblin.intellij.WireGoblinReferenceKind
import io.wiregoblin.intellij.WireGoblinTypeResolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WireGoblinColorSettingsUnitTest {
    @Test
    fun mapsRemoteBlockTypesToRemoteColor() {
        assertEquals(
            WireGoblinColorSettings.remoteCall,
            WireGoblinColorSettings.typeValueAttributes("http", WireGoblinTypeResolver.AllowedTypeKind.BLOCK),
        )
        assertEquals(
            WireGoblinColorSettings.remoteCall,
            WireGoblinColorSettings.typeValueAttributes("openai", WireGoblinTypeResolver.AllowedTypeKind.BLOCK),
        )
    }

    @Test
    fun mapsFlowControlBlockTypesToFlowControlColor() {
        assertEquals(
            WireGoblinColorSettings.flowControl,
            WireGoblinColorSettings.typeValueAttributes("retry", WireGoblinTypeResolver.AllowedTypeKind.BLOCK),
        )
        assertEquals(
            WireGoblinColorSettings.flowControl,
            WireGoblinColorSettings.typeValueAttributes("workflow", WireGoblinTypeResolver.AllowedTypeKind.BLOCK),
        )
    }

    @Test
    fun mapsRetryRuleTypesToRetryRuleColor() {
        assertEquals(
            WireGoblinColorSettings.retryRule,
            WireGoblinColorSettings.typeValueAttributes("status_code", WireGoblinTypeResolver.AllowedTypeKind.RETRY_RULE),
        )
    }

    @Test
    fun mapsReferencesToDistinctColors() {
        assertEquals(
            WireGoblinColorSettings.constantReference,
            WireGoblinColorSettings.referenceAttributes(WireGoblinReferenceKind.CONSTANT_OR_SECRET),
        )
        assertEquals(
            WireGoblinColorSettings.variableReference,
            WireGoblinColorSettings.referenceAttributes(WireGoblinReferenceKind.VARIABLE),
        )
        assertEquals(
            WireGoblinColorSettings.expressionReference,
            WireGoblinColorSettings.referenceAttributes(WireGoblinReferenceKind.EXPRESSION),
        )
    }

    @Test
    fun findsEnvironmentAndSqlPlaceholders() {
        assertTrue(
            WireGoblinHighlightSupport.findEnvironmentPlaceholders("prefix \${HOST:=https://api} suffix").isNotEmpty(),
        )
        assertTrue(
            WireGoblinHighlightSupport.findSqlPlaceholders("where id = \$1 and code = \$2").isNotEmpty(),
        )
    }
}
