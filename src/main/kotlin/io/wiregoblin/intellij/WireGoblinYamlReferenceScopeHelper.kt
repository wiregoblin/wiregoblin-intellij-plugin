package io.wiregoblin.intellij

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLSequenceItem

internal object WireGoblinYamlReferenceScopeHelper {
    private val shellEnvRegex = Regex("""^\$[A-Z_][A-Z0-9_]*$""")

    fun shouldIgnoreReferenceToken(
        keyValue: YAMLKeyValue,
        token: WireGoblinReferenceSupport.ReferenceOccurrence,
    ): Boolean {
        if (token.kind != WireGoblinReferenceKind.VARIABLE) {
            return false
        }

        val mapping = keyValue.parent as? YAMLMapping ?: return false
        return WireGoblinYamlContextLocator.currentBlockType(mapping) == "container" &&
            keyValue.keyText == "command" &&
            shellEnvRegex.matches(token.raw)
    }

    fun sectionKeys(mapping: YAMLMapping?, sectionName: String): List<String> {
        return sectionEntries(mapping, sectionName).map { it.keyText }
    }

    fun sectionEntries(mapping: YAMLMapping?, sectionName: String): List<YAMLKeyValue> {
        val section = mapping?.getKeyValueByKey(sectionName)?.value as? YAMLMapping ?: return emptyList()
        return section.keyValues.toList()
    }

    fun assignVariables(workflowMapping: YAMLMapping?, position: PsiElement? = null): List<String> {
        return assignVariableEntries(workflowMapping, position).map { it.keyText }
    }

    fun assignVariableEntries(workflowMapping: YAMLMapping?, position: PsiElement? = null): List<YAMLKeyValue> {
        val blocks = workflowMapping?.getKeyValueByKey(WireGoblinKeys.BLOCKS)?.value as? YAMLSequence ?: return emptyList()
        val currentBlockItem = position?.let(::currentBlockSequenceItem)

        return blocks.items.takeWhile { item ->
            currentBlockItem == null || item != currentBlockItem
        }.flatMap { item ->
            val blockMapping = PsiTreeUtil.findChildOfType(item, YAMLMapping::class.java)
            blockMapping?.outputEntriesRecursively().orEmpty()
        } + buildList {
            if (currentBlockItem != null && sequenceOwnerKey(currentBlockItem) == WireGoblinKeys.BLOCKS) {
                val currentBlockMapping = PsiTreeUtil.findChildOfType(currentBlockItem, YAMLMapping::class.java)
                addAll(currentBlockMapping?.outputEntriesRecursively().orEmpty())
            }
        }
    }

    private fun currentBlockSequenceItem(position: PsiElement): YAMLSequenceItem? {
        var sequenceItem = PsiTreeUtil.getParentOfType(position, YAMLSequenceItem::class.java)
        while (sequenceItem != null) {
            if (sequenceOwnerKey(sequenceItem) == WireGoblinKeys.BLOCKS) {
                return sequenceItem
            }
            sequenceItem = PsiTreeUtil.getParentOfType(sequenceItem, YAMLSequenceItem::class.java)
        }
        return null
    }

    private fun sequenceOwnerKey(sequenceItem: YAMLSequenceItem): String? {
        val sequence = sequenceItem.parent as? YAMLSequence ?: return null
        return (sequence.parent as? YAMLKeyValue)?.keyText
    }

    private fun YAMLMapping.outputEntriesRecursively(): List<YAMLKeyValue> {
        val directAssignEntries = (getKeyValueByKey(WireGoblinKeys.ASSIGN)?.value as? YAMLMapping)
            ?.keyValues
            ?.toList()
            .orEmpty()
        val directCollectEntries = (getKeyValueByKey(WireGoblinKeys.COLLECT)?.value as? YAMLMapping)
            ?.keyValues
            ?.toList()
            .orEmpty()

        val nestedBlockEntries = listOfNotNull(
            getKeyValueByKey(WireGoblinKeys.BLOCK)?.value as? YAMLMapping,
        ).flatMap { it.outputEntriesRecursively() }

        val nestedBlocksEntries = ((getKeyValueByKey(WireGoblinKeys.BLOCKS)?.value as? YAMLSequence)
            ?.items
            ?.mapNotNull { PsiTreeUtil.findChildOfType(it, YAMLMapping::class.java) }
            .orEmpty())
            .flatMap { it.outputEntriesRecursively() }

        val transactionEntries = ((getKeyValueByKey("transaction")?.value as? YAMLSequence)
            ?.items
            ?.mapNotNull { PsiTreeUtil.findChildOfType(it, YAMLMapping::class.java) }
            .orEmpty())
            .flatMap { it.outputEntriesRecursively() }

        return directAssignEntries + directCollectEntries + nestedBlockEntries + nestedBlocksEntries + transactionEntries
    }
}
