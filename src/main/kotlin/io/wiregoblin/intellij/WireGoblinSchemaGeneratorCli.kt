package io.wiregoblin.intellij

import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val objectMapper = ObjectMapper()

object WireGoblinSchemaGenerator {
    private val schemaPath: Path = Path.of("src/main/resources/schema/wiregoblin.schema.json")

    fun generateSchemaJson(): String {
        val root = linkedMapOf<String, Any>(
            "\$schema" to "https://json-schema.org/draft/2020-12/schema",
            "\$id" to "https://wiregoblin.dev/schemas/wiregoblin.schema.json",
            "\$comment" to "GENERATED FILE. DO NOT EDIT MANUALLY. Run generateWireGoblinSchema.",
            "title" to "WireGoblin Project",
            "type" to "object",
            "additionalProperties" to false,
            "required" to listOf(WireGoblinKeys.ID, WireGoblinKeys.WORKFLOWS),
            "properties" to topLevelProperties(),
            "\$defs" to defs(),
        )

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root) + "\n"
    }

    fun writeSchemaFile() {
        Files.createDirectories(schemaPath.parent)
        schemaPath.writeText(generateSchemaJson())
    }

    fun checkSchemaFile() {
        val expected = generateSchemaJson()
        val actual = if (Files.exists(schemaPath)) schemaPath.readText() else ""
        check(actual.isNotBlank()) {
            "Embedded schema is missing. Run generateWireGoblinSchema."
        }
        check(objectMapper.readTree(actual) == objectMapper.readTree(expected)) {
            "Embedded schema is out of date. Run generateWireGoblinSchema."
        }
    }

    private fun topLevelProperties(): Map<String, Any> {
        return linkedMapOf(
            WireGoblinKeys.ID to stringSchema(),
            WireGoblinKeys.NAME to stringSchema(),
            WireGoblinKeys.VERSION to schemaForType(WireGoblinSchemaType.STRING_OR_INTEGER),
            WireGoblinKeys.AI to mapOf("\$ref" to "#/\$defs/ai"),
            WireGoblinKeys.CONSTANTS to stringMapSchema(),
            WireGoblinKeys.SECRETS to stringMapSchema(),
            WireGoblinKeys.VARIABLES to stringMapSchema(),
            WireGoblinKeys.SECRET_VARIABLES to stringMapSchema(),
            WireGoblinKeys.WORKFLOWS to mapOf(
                "type" to "array",
                "items" to mapOf("\$ref" to "#/\$defs/workflow"),
            ),
        )
    }

    private fun defs(): Map<String, Any> {
        val defs = linkedMapOf<String, Any>()
        defs[WireGoblinKeys.AI] = objectSchema(
            properties = WireGoblinSchema.aiFields.associate { it.name to schemaForField(it) },
            required = emptyList(),
            additionalProperties = false,
        )
        defs[WireGoblinKeys.CONDITION] = objectSchema(
            properties = WireGoblinSchema.conditionFields.associate { it.name to schemaForField(it) },
            required = WireGoblinSchema.conditionFields.filter { it.required }.map { it.name },
            additionalProperties = false,
        )
        defs["block_common"] = objectSchema(
            properties = WireGoblinSchema.commonBlockFields.associate { field ->
                field.name to if (field.name == WireGoblinKeys.CONDITION) {
                    mapOf("\$ref" to "#/\$defs/${WireGoblinKeys.CONDITION}")
                } else {
                    schemaForField(field)
                }
            },
            required = WireGoblinSchema.commonBlockFields.filter { it.required }.map { it.name },
            additionalProperties = true,
        )
        defs["workflow"] = objectSchema(
            properties = workflowProperties(),
            required = listOf(WireGoblinKeys.ID, WireGoblinKeys.BLOCKS),
            additionalProperties = false,
        )

        WireGoblinSchema.blockSpecs.forEach { spec ->
            defs["block_${spec.type}"] = mapOf(
                "allOf" to listOf(
                    mapOf("\$ref" to "#/\$defs/block_common"),
                    objectSchema(
                        properties = linkedMapOf<String, Any>().apply {
                            put(WireGoblinKeys.TYPE, mapOf("type" to "string", "const" to spec.type))
                            spec.fields.forEach { put(it.name, schemaForField(it)) }
                        },
                        required = listOf(WireGoblinKeys.TYPE) + spec.requiredFields.map { it.name },
                        additionalProperties = true,
                    ),
                ),
            )
        }

        defs["block"] = mapOf(
            "oneOf" to WireGoblinSchema.blockSpecs.map { spec ->
                mapOf("\$ref" to "#/\$defs/block_${spec.type}")
            },
        )
        return defs
    }

    private fun workflowProperties(): Map<String, Any> {
        return linkedMapOf(
            WireGoblinKeys.ID to stringSchema(),
            WireGoblinKeys.NAME to stringSchema(),
            WireGoblinKeys.DISABLE_RUN to schemaForType(WireGoblinSchemaType.BOOLEAN),
            WireGoblinKeys.TIMEOUT_SECONDS to schemaForType(WireGoblinSchemaType.INTEGER),
            WireGoblinKeys.CONSTANTS to stringMapSchema(),
            WireGoblinKeys.SECRETS to stringMapSchema(),
            WireGoblinKeys.VARIABLES to stringMapSchema(),
            WireGoblinKeys.SECRET_VARIABLES to stringMapSchema(),
            WireGoblinKeys.OUTPUTS to stringMapSchema(),
            WireGoblinKeys.BLOCKS to mapOf(
                "type" to "array",
                "items" to mapOf("\$ref" to "#/\$defs/block"),
            ),
            WireGoblinKeys.CATCH_ERROR_BLOCKS to mapOf(
                "type" to "array",
                "items" to mapOf("\$ref" to "#/\$defs/block"),
            ),
        )
    }

    private fun schemaForField(field: WireGoblinFieldSpec): Map<String, Any> {
        if (field.name == WireGoblinKeys.CONDITION) {
            return mapOf("\$ref" to "#/\$defs/${WireGoblinKeys.CONDITION}")
        }

        return linkedMapOf<String, Any>().apply {
            putAll(schemaForType(field.schemaType))
            if (field.allowedValues.isNotEmpty()) {
                put("enum", field.allowedValues)
            }
        }
    }

    private fun schemaForType(type: WireGoblinSchemaType): Map<String, Any> {
        return when (type) {
            WireGoblinSchemaType.STRING -> stringSchema()
            WireGoblinSchemaType.INTEGER -> mapOf("type" to "integer")
            WireGoblinSchemaType.BOOLEAN -> mapOf("type" to "boolean")
            WireGoblinSchemaType.STRING_OR_INTEGER -> mapOf("type" to listOf("integer", "string"))
            WireGoblinSchemaType.STRING_MAP -> stringMapSchema()
            WireGoblinSchemaType.STRING_LIST -> mapOf("type" to "array", "items" to stringSchema())
            WireGoblinSchemaType.BLOCK -> mapOf("\$ref" to "#/\$defs/block")
            WireGoblinSchemaType.BLOCK_LIST -> mapOf("type" to "array", "items" to mapOf("\$ref" to "#/\$defs/block"))
            WireGoblinSchemaType.OBJECT -> mapOf("type" to "object", "additionalProperties" to true)
        }
    }

    private fun stringSchema(): Map<String, Any> = mapOf("type" to "string")

    private fun stringMapSchema(): Map<String, Any> = mapOf(
        "type" to "object",
        "additionalProperties" to stringSchema(),
    )

    private fun objectSchema(
        properties: Map<String, Any>,
        required: List<String>,
        additionalProperties: Boolean,
    ): Map<String, Any> {
        return linkedMapOf(
            "type" to "object",
            "additionalProperties" to additionalProperties,
            "required" to required,
            "properties" to properties,
        )
    }
}

fun main(args: Array<String>) {
    if (args.contains("--check")) {
        WireGoblinSchemaGenerator.checkSchemaFile()
    } else {
        WireGoblinSchemaGenerator.writeSchemaFile()
    }
}
