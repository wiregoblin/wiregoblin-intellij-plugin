package io.wiregoblin.intellij.unit

import com.intellij.testFramework.LightVirtualFile
import io.wiregoblin.intellij.WireGoblinFileSupport
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WireGoblinFileSupportUnitTest {
    @Test
    fun `matches canonical wiregoblin file names only`() {
        assertTrue(WireGoblinFileSupport.matchesWireGoblinName(LightVirtualFile("wiregoblin.yaml", "")))
        assertTrue(WireGoblinFileSupport.matchesWireGoblinName(LightVirtualFile("wiregoblin.yml", "")))
        assertTrue(WireGoblinFileSupport.matchesWireGoblinName(LightVirtualFile("example.wiregoblin.yaml", "")))
        assertTrue(WireGoblinFileSupport.matchesWireGoblinName(LightVirtualFile("example.wiregoblin.yml", "")))

        assertFalse(WireGoblinFileSupport.matchesWireGoblinName(LightVirtualFile("example.yaml", "")))
        assertFalse(WireGoblinFileSupport.matchesWireGoblinName(LightVirtualFile("example.yml", "")))
    }
}
