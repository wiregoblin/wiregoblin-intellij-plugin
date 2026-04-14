package io.wiregoblin.intellij

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLSequenceItem

internal object WireGoblinYamlContextLocator {
    private const val maxFallbackLeaves = 100

    private val contextOwnerKeys = WireGoblinKeys.sequenceSections + WireGoblinKeys.mappingSections
    private val sequenceOwnerKeys = WireGoblinKeys.sequenceSections
    private val mappingOwnerKeys = WireGoblinKeys.mappingSections + WireGoblinKeys.sequenceSections

    fun contextKeys(position: PsiElement): List<String> {
        val nearestBlockMapping = nearestEnclosingBlockMapping(position)
        if (nearestBlockMapping != null) {
            return blockKeysFor(nearestBlockMapping)
        }

        previousMeaningfulKey(position)?.let { previousKey ->
            if (previousKey == WireGoblinKeys.BLOCKS || previousKey == WireGoblinKeys.CATCH_ERROR_BLOCKS) {
                return blockKeysFor(null)
            }
        }

        val enclosingKeyValue = PsiTreeUtil.getParentOfType(position, YAMLKeyValue::class.java, false)
        if (enclosingKeyValue?.keyText == WireGoblinKeys.CONDITION) return WireGoblinSchema.conditionKeys
        if (enclosingKeyValue?.keyText == WireGoblinKeys.AI) return WireGoblinSchema.aiKeys
        if (enclosingKeyValue?.keyText == WireGoblinKeys.BLOCKS ||
            enclosingKeyValue?.keyText == WireGoblinKeys.CATCH_ERROR_BLOCKS
        ) {
            return blockKeysFor(null)
        }

        val previousLeaf = PsiTreeUtil.prevVisibleLeaf(position)
        val previousKeyValue = previousLeaf?.let { PsiTreeUtil.getParentOfType(it, YAMLKeyValue::class.java, false) }
        if (previousKeyValue?.keyText == WireGoblinKeys.BLOCKS ||
            previousKeyValue?.keyText == WireGoblinKeys.CATCH_ERROR_BLOCKS
        ) {
            return blockKeysFor(null)
        }

        val mapping = PsiTreeUtil.getParentOfType(position, YAMLMapping::class.java, false)
        val mappingOwner = mapping?.parent as? YAMLKeyValue
        if (mappingOwner?.keyText == WireGoblinKeys.CONDITION) return WireGoblinSchema.conditionKeys
        if (mappingOwner?.keyText == WireGoblinKeys.AI) return WireGoblinSchema.aiKeys

        val sequence = PsiTreeUtil.getParentOfType(position, YAMLSequence::class.java, false)
        val directSequenceOwner = (sequence?.parent as? YAMLKeyValue)?.keyText
        if (directSequenceOwner == WireGoblinKeys.BLOCKS || directSequenceOwner == WireGoblinKeys.CATCH_ERROR_BLOCKS) {
            return blockKeysFor(nearestSequenceItemMapping(position))
        }

        val sequenceOwner = nearestSequenceOwnerKey(position)
        if (sequenceOwner == WireGoblinKeys.BLOCKS || sequenceOwner == WireGoblinKeys.CATCH_ERROR_BLOCKS) {
            return blockKeysFor(nearestSequenceItemMapping(position))
        }
        if (sequenceOwner == WireGoblinKeys.WORKFLOWS) {
            val fallback = fallbackSequenceContext(position)
            if (fallback == WireGoblinKeys.BLOCKS || fallback == WireGoblinKeys.CATCH_ERROR_BLOCKS) {
                return blockKeysFor(nearestSequenceItemMapping(position))
            }
            return WireGoblinSchema.workflowKeys
        }

        return when (val fallback = fallbackSequenceContext(position)) {
            WireGoblinKeys.WORKFLOWS -> WireGoblinSchema.workflowKeys
            WireGoblinKeys.BLOCKS, WireGoblinKeys.CATCH_ERROR_BLOCKS -> blockKeysFor(nearestSequenceItemMapping(position))
            else -> WireGoblinSchema.topLevelKeys
        }
    }

    fun supportsEnvPlaceholder(keyValue: YAMLKeyValue): Boolean {
        if (keyValue.keyText == WireGoblinKeys.TYPE) {
            return false
        }

        val mapping = keyValue.parent as? YAMLMapping ?: return false
        val mappingParent = mapping.parent
        val mappingOwnerKey = (mappingParent as? YAMLKeyValue)?.keyText
        if (mappingOwnerKey == WireGoblinKeys.AI ||
            mappingOwnerKey == WireGoblinKeys.ASSIGN ||
            mappingOwnerKey == WireGoblinKeys.BLOCK ||
            mappingOwnerKey in WireGoblinKeys.envCapableSections
        ) {
            return true
        }

        val sequenceItem = mappingParent as? YAMLSequenceItem
        val sequence = sequenceItem?.parent as? YAMLSequence
        val sequenceOwner = (sequence?.parent as? YAMLKeyValue)?.keyText
        return sequenceOwner == WireGoblinKeys.BLOCKS || sequenceOwner == WireGoblinKeys.CATCH_ERROR_BLOCKS
    }

    fun isMisnamedWorkflowBlockKey(keyValue: YAMLKeyValue): Boolean {
        if (keyValue.keyText != WireGoblinKeys.BLOCK) {
            return false
        }

        val mapping = keyValue.parent as? YAMLMapping ?: return false
        return sequenceOwnerKey(mapping) == WireGoblinKeys.WORKFLOWS
    }

    fun blockKeysFor(mapping: YAMLMapping?): List<String> {
        val specificKeys = currentBlockSpec(mapping)?.fields?.map { it.name }.orEmpty()
        return WireGoblinSchema.blockKeys + specificKeys
    }

    fun currentBlockSpec(mapping: YAMLMapping?): WireGoblinBlockSpec? {
        val type = currentBlockType(mapping) ?: return null
        return WireGoblinSchema.blockSpecsByType[type]
    }

    fun currentBlockType(mapping: YAMLMapping?): String? {
        return mapping?.getKeyValueByKey(WireGoblinKeys.TYPE)?.valueText
            ?.trim()
            ?.removeSurrounding("\"")
            ?.removeSurrounding("'")
    }

    fun sequenceOwnerKey(mapping: YAMLMapping): String? {
        var current: PsiElement? = mapping
        while (current != null) {
            val sequenceItem = current.parent as? YAMLSequenceItem
            if (sequenceItem != null) {
                val sequence = sequenceItem.parent as? YAMLSequence ?: return null
                return (sequence.parent as? YAMLKeyValue)?.keyText
            }
            current = current.parent
        }
        return null
    }

    fun nearestSequenceItemMapping(position: PsiElement): YAMLMapping? {
        val sequenceItem = PsiTreeUtil.getParentOfType(position, YAMLSequenceItem::class.java) ?: return null
        return PsiTreeUtil.findChildOfType(sequenceItem, YAMLMapping::class.java)
    }

    fun nearestEnclosingBlockMapping(position: PsiElement): YAMLMapping? {
        var sequenceItem = PsiTreeUtil.getParentOfType(position, YAMLSequenceItem::class.java)
        while (sequenceItem != null) {
            val sequence = sequenceItem.parent as? YAMLSequence
            val owner = sequence?.parent as? YAMLKeyValue
            if (owner?.keyText == WireGoblinKeys.BLOCKS || owner?.keyText == WireGoblinKeys.CATCH_ERROR_BLOCKS) {
                return PsiTreeUtil.findChildOfType(sequenceItem, YAMLMapping::class.java)
            }
            sequenceItem = PsiTreeUtil.getParentOfType(sequenceItem, YAMLSequenceItem::class.java)
        }
        return null
    }

    fun nearestSequenceOwnerKey(position: PsiElement): String? {
        var sequenceItem = PsiTreeUtil.getParentOfType(position, YAMLSequenceItem::class.java)
        while (sequenceItem != null) {
            val sequence = sequenceItem.parent as? YAMLSequence
            val owner = sequence?.parent as? YAMLKeyValue
            if (owner != null) {
                return owner.keyText
            }
            sequenceItem = PsiTreeUtil.getParentOfType(sequenceItem, YAMLSequenceItem::class.java)
        }
        return null
    }

    fun currentWorkflowMapping(position: PsiElement): YAMLMapping? {
        var sequenceItem = PsiTreeUtil.getParentOfType(position, YAMLSequenceItem::class.java)
        while (sequenceItem != null) {
            val sequence = sequenceItem.parent as? YAMLSequence
            val owner = sequence?.parent as? YAMLKeyValue
            if (owner?.keyText == WireGoblinKeys.WORKFLOWS) {
                return PsiTreeUtil.findChildOfType(sequenceItem, YAMLMapping::class.java)
            }
            sequenceItem = PsiTreeUtil.getParentOfType(sequenceItem, YAMLSequenceItem::class.java)
        }
        return null
    }

    fun enclosingBlockType(position: PsiElement): String? {
        var current: PsiElement? = position
        while (current != null) {
            val mapping = current as? YAMLMapping
            val type = currentBlockType(mapping)
            if (type != null) {
                return type
            }
            current = current.parent
        }
        return null
    }

    fun topLevelMapping(file: YAMLFile?): YAMLMapping? {
        val document = file?.documents?.firstOrNull() ?: return null
        return document.topLevelValue as? YAMLMapping
    }

    private fun fallbackSequenceContext(position: PsiElement): String? {
        contextOwnerKey(position)?.let { return it }

        var leaf = PsiTreeUtil.prevLeaf(position, true)
        var steps = 0
        while (leaf != null && steps < maxFallbackLeaves) {
            contextOwnerKey(leaf)?.let { return it }
            leaf = PsiTreeUtil.prevLeaf(leaf, true)
            steps++
        }
        return null
    }

    private fun contextOwnerKey(element: PsiElement): String? {
        var current: PsiElement? = element
        while (current != null) {
            when (current) {
                is YAMLKeyValue -> if (current.keyText in contextOwnerKeys) {
                    return current.keyText
                }

                is YAMLSequence -> {
                    val ownerKey = (current.parent as? YAMLKeyValue)?.keyText
                    if (ownerKey in sequenceOwnerKeys) return ownerKey
                }

                is YAMLMapping -> {
                    val ownerKey = (current.parent as? YAMLKeyValue)?.keyText
                    if (ownerKey in mappingOwnerKeys) return ownerKey
                }
            }
            current = current.parent
        }
        return null
    }

    private fun previousMeaningfulKey(position: PsiElement): String? {
        val text = position.containingFile?.text ?: return null
        val offset = position.textOffset.coerceIn(0, text.length)
        val prefix = text.substring(0, offset)
        return prefix
            .lineSequence()
            .toList()
            .asReversed()
            .firstNotNullOfOrNull { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty()) {
                    null
                } else {
                    Regex("""^([A-Za-z0-9_]+)\s*:\s*$""").matchEntire(trimmed)?.groupValues?.get(1)
                }
            }
    }
}
