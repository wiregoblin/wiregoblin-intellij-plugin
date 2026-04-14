package io.wiregoblin.intellij.contract

import com.fasterxml.jackson.databind.ObjectMapper
import io.wiregoblin.intellij.WireGoblinSchemaGenerator
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertTrue

class WireGoblinSchemaGeneratorContractTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `generated schema matches embedded schema file`() {
        val expected = WireGoblinSchemaGenerator.generateSchemaJson()
        val actual = Path.of("src/main/resources/schema/wiregoblin.schema.json").readText()

        kotlin.test.assertEquals(objectMapper.readTree(expected), objectMapper.readTree(actual))
    }

    @Test
    fun `generated schema marks file as generated`() {
        val schema = WireGoblinSchemaGenerator.generateSchemaJson()

        assertTrue(schema.contains("GENERATED FILE. DO NOT EDIT MANUALLY"))
        assertTrue(schema.contains("\"oneOf\""))
        assertTrue(schema.contains("\"block_http\""))
    }
}
