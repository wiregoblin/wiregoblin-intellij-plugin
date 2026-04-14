package io.wiregoblin.intellij.smoke

import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.inputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

class WireGoblinPluginRegistrationSmokeTest {
    private val pluginXmlPath = Path.of("src/main/resources/META-INF/plugin.xml")

    @Test
    fun `plugin descriptor exposes expected id and required dependencies`() {
        val document = parsePluginXml()

        assertEquals("io.wiregoblin.intellij", document.documentElement.getElementsByTagName("id").item(0).textContent.trim())

        val dependencies = document.documentElement
            .getElementsByTagName("depends")
            .toList()
            .map { it.textContent.trim() }

        assertTrue("com.intellij.modules.platform" in dependencies)
        assertTrue("org.jetbrains.plugins.yaml" in dependencies)
    }

    @Test
    fun `plugin descriptor registrations point to loadable implementation classes`() {
        val implementationClasses = parsePluginXml()
            .documentElement
            .getElementsByTagName("*")
            .toList()
            .filterIsInstance<Element>()
            .flatMap { element ->
                listOf("implementationClass", "implementation", "className")
                    .map(element::getAttribute)
                    .filter { it.isNotBlank() }
            }

        assertTrue(implementationClasses.isNotEmpty())
        implementationClasses.forEach { className ->
            Class.forName(className)
        }
    }

    private fun parsePluginXml() = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(pluginXmlPath.inputStream())

    private fun NodeList.toList(): List<Node> =
        (0 until length).map(::item)
}
