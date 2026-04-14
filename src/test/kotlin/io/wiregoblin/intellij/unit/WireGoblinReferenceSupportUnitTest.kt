package io.wiregoblin.intellij.unit

import io.wiregoblin.intellij.WireGoblinReferenceKind
import io.wiregoblin.intellij.WireGoblinReferenceSupport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WireGoblinReferenceSupportUnitTest {
    @Test
    fun `parses at reference token`() {
        val token = WireGoblinReferenceSupport.parseReferenceToken("@grpc_h")
        assertNotNull(token)
        assertEquals(WireGoblinReferenceKind.CONSTANT_OR_SECRET, token.kind)
        assertEquals("@grpc_h", token.raw)
    }

    @Test
    fun `parses dollar reference token`() {
        val token = WireGoblinReferenceSupport.parseReferenceToken("\$session.to")
        assertNotNull(token)
        assertEquals(WireGoblinReferenceKind.VARIABLE, token.kind)
        assertEquals("\$session.to", token.raw)
    }

    @Test
    fun `parses bang reference token`() {
        val token = WireGoblinReferenceSupport.parseReferenceToken("!Each.It")
        assertNotNull(token)
        assertEquals(WireGoblinReferenceKind.EXPRESSION, token.kind)
        assertEquals("!Each.It", token.raw)
    }

    @Test
    fun `ignores plain text without reference prefix`() {
        assertNull(WireGoblinReferenceSupport.parseReferenceToken("grpc_host"))
    }

    @Test
    fun `ignores empty token`() {
        assertNull(WireGoblinReferenceSupport.parseReferenceToken(""))
    }

    @Test
    fun `parses single character reference tokens`() {
        assertEquals("@", WireGoblinReferenceSupport.parseReferenceToken("@")?.raw)
        assertEquals("$", WireGoblinReferenceSupport.parseReferenceToken("$")?.raw)
        assertEquals("!", WireGoblinReferenceSupport.parseReferenceToken("!")?.raw)
    }

    @Test
    fun `keeps the last contiguous reference token when text contains multiple markers`() {
        val token = WireGoblinReferenceSupport.parseReferenceToken("prefix @ref@other")
        assertNotNull(token)
        assertEquals(WireGoblinReferenceKind.CONSTANT_OR_SECRET, token.kind)
        assertEquals("@ref@other", token.raw)
    }

    @Test
    fun `supports unicode characters in reference tokens`() {
        val token = WireGoblinReferenceSupport.parseReferenceToken("@ключ.путь")
        assertNotNull(token)
        assertEquals(WireGoblinReferenceKind.CONSTANT_OR_SECRET, token.kind)
        assertTrue(token.raw.contains("ключ"))
    }

    @Test
    fun `supports hyphenated reference tokens`() {
        assertEquals("@my-secret", WireGoblinReferenceSupport.parseReferenceToken("@my-secret")?.raw)
        assertEquals("\$flow-id", WireGoblinReferenceSupport.parseReferenceToken("\$flow-id")?.raw)
    }

    @Test
    fun `ignores numeric dollar placeholders`() {
        assertNull(WireGoblinReferenceSupport.parseReferenceToken("\$1"))
        assertNull(WireGoblinReferenceSupport.parseReferenceToken("values (\$1, \$2, \$3)"))
    }

    @Test
    fun `finds value start after colon and spaces`() {
        assertEquals(5, WireGoblinReferenceSupport.valueStartOffset("url: @host"))
    }

    @Test
    fun `finds value start after colon spaces and quote`() {
        assertEquals(6, WireGoblinReferenceSupport.valueStartOffset("url: \"@host"))
    }

    @Test
    fun `returns line length when colon missing`() {
        assertEquals(4, WireGoblinReferenceSupport.valueStartOffset("name"))
    }

    @Test
    fun `finds reference tokens in scalar text`() {
        val tokens = WireGoblinReferenceSupport.findReferenceTokens("\"prefix @const \$flow.value !Each.Item\"")

        assertEquals(listOf("@const", "\$flow.value", "!Each.Item"), tokens.map { it.raw })
    }

    @Test
    fun `ignores env placeholders and email addresses when finding references`() {
        val tokens = WireGoblinReferenceSupport.findReferenceTokens("\${NAME} user@example.com")

        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `ignores numeric dollar placeholders when finding references`() {
        val tokens = WireGoblinReferenceSupport.findReferenceTokens("values (\$1, \$2, \$3) and \$workflow_id")

        assertEquals(listOf("\$workflow_id"), tokens.map { it.raw })
    }
}
