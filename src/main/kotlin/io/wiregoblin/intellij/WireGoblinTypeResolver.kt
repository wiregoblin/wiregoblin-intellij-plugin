package io.wiregoblin.intellij

import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLSequenceItem

internal object WireGoblinTypeResolver {
    enum class AllowedTypeKind {
        BLOCK,
        RETRY_RULE,
    }

    data class TypeConstraint(
        val kind: AllowedTypeKind,
        val values: List<String>,
    ) {
        val description: String = when (kind) {
            AllowedTypeKind.BLOCK -> "WireGoblin block type"
            AllowedTypeKind.RETRY_RULE -> "WireGoblin retry rule type"
        }
    }

    data class ValueConstraint(
        val description: String,
        val values: List<String>,
    )

    fun allowedTypeValues(keyValue: YAMLKeyValue): TypeConstraint? {
        val mapping = keyValue.parent as? YAMLMapping ?: return null
        val sequenceOwner = WireGoblinYamlContextLocator.sequenceOwnerKey(mapping)
        if (sequenceOwner == WireGoblinKeys.BLOCKS || sequenceOwner == WireGoblinKeys.CATCH_ERROR_BLOCKS) {
            return TypeConstraint(AllowedTypeKind.BLOCK, WireGoblinSchema.blockTypes)
        }

        val sequenceItem = mapping.parent as? YAMLSequenceItem
        val sequence = sequenceItem?.parent as? YAMLSequence
        val mappingOwner = sequence?.parent as? YAMLKeyValue
        if (mappingOwner?.keyText == WireGoblinKeys.BLOCK) {
            return TypeConstraint(AllowedTypeKind.BLOCK, WireGoblinSchema.blockTypes)
        }
        if (mappingOwner?.keyText == WireGoblinKeys.RULES) {
            val rulesOwnerMapping = mappingOwner.parent as? YAMLMapping ?: return null
            val retryOnKeyValue = rulesOwnerMapping.parent as? YAMLKeyValue
            if (retryOnKeyValue?.keyText == WireGoblinKeys.RETRY_ON) {
                return TypeConstraint(AllowedTypeKind.RETRY_RULE, WireGoblinSchema.retryRuleTypes)
            }
        }

        return null
    }

    fun allowedFieldValues(keyValue: YAMLKeyValue): ValueConstraint? {
        if (keyValue.keyText == WireGoblinKeys.TYPE) {
            val typeConstraint = allowedTypeValues(keyValue) ?: return null
            return ValueConstraint(typeConstraint.description, typeConstraint.values)
        }

        val mapping = keyValue.parent as? YAMLMapping ?: return null
        val mappingOwner = mapping.parent as? YAMLKeyValue
        if (mappingOwner?.keyText == WireGoblinKeys.CONDITION) {
            val field = WireGoblinSchema.conditionFieldsByName[keyValue.keyText] ?: return null
            return field.toValueConstraint("WireGoblin condition value")
        }
        if (mappingOwner?.keyText == WireGoblinKeys.AI) {
            val field = WireGoblinSchema.aiFieldsByName[keyValue.keyText] ?: return null
            return field.toValueConstraint("WireGoblin AI setting")
        }

        val blockSpec = WireGoblinYamlContextLocator.currentBlockSpec(mapping)
        val field = blockSpec?.field(keyValue.keyText)
        return field?.toValueConstraint("WireGoblin ${field.name} value")
            ?: commonValueConstraint(keyValue.keyText, mapping)
    }

    private fun WireGoblinFieldSpec.toValueConstraint(description: String): ValueConstraint? {
        if (allowedValues.isEmpty()) {
            return null
        }
        return ValueConstraint(description, allowedValues)
    }

    private fun commonValueConstraint(key: String, mapping: YAMLMapping): ValueConstraint? {
        val sequenceOwner = WireGoblinYamlContextLocator.sequenceOwnerKey(mapping)
        return when {
            key == WireGoblinKeys.OPERATOR &&
                (sequenceOwner == WireGoblinKeys.BLOCKS || sequenceOwner == WireGoblinKeys.CATCH_ERROR_BLOCKS) ->
                WireGoblinSchema.conditionFieldsByName[key]?.toValueConstraint("WireGoblin operator value")

            else -> null
        }
    }
}
