package io.wiregoblin.intellij

internal object WireGoblinReferenceSupport {
    private val referenceKinds = WireGoblinReferenceKind.prefixes()
    private val referenceTokenChars = setOf('_', '.', '-')

    fun parseReferenceToken(prefixText: String): ReferenceToken? {
        val token = prefixText.takeLastWhile { it.isReferenceTokenChar() || it in referenceKinds }
        if (token.isEmpty()) return null
        val kind = WireGoblinReferenceKind.fromPrefix(token.first()) ?: return null
        if (token.isNumericDollarPlaceholder()) return null
        return ReferenceToken(kind, token)
    }

    fun findReferenceTokens(text: String): List<ReferenceOccurrence> {
        if (text.isEmpty()) {
            return emptyList()
        }

        val matches = mutableListOf<ReferenceOccurrence>()
        var index = 0
        while (index < text.length) {
            val ch = text[index]
            if (ch !in referenceKinds) {
                index++
                continue
            }

            if (ch == WireGoblinReferenceKind.VARIABLE.prefix && index + 1 < text.length && text[index + 1] == '{') {
                index++
                continue
            }

            if (index > 0 && text[index - 1].isReferenceTokenChar()) {
                index++
                continue
            }

            var end = index + 1
            while (end < text.length && text[end].isReferenceTokenChar()) {
                end++
            }

            if (end - index > 1) {
                val raw = text.substring(index, end)
                if (!raw.isNumericDollarPlaceholder()) {
                    val kind = WireGoblinReferenceKind.fromPrefix(ch) ?: continue
                    matches += ReferenceOccurrence(kind, raw, index, end)
                }
            }
            index = end
        }

        return matches
    }

    fun valueStartOffset(linePrefix: String): Int {
        val colonIndex = linePrefix.indexOf(':')
        if (colonIndex < 0) {
            return linePrefix.length
        }

        var offset = colonIndex + 1
        while (offset < linePrefix.length) {
            val ch = linePrefix[offset]
            if (ch != ' ' && ch != '\t' && ch != '"' && ch != '\'') {
                break
            }
            offset++
        }
        return offset
    }

    data class ReferenceToken(
        val kind: WireGoblinReferenceKind,
        val raw: String,
    )

    data class ReferenceTokenWithOffset(
        val kind: WireGoblinReferenceKind,
        val raw: String,
        val startOffset: Int,
    )

    data class ReferenceOccurrence(
        val kind: WireGoblinReferenceKind,
        val raw: String,
        val startOffset: Int,
        val endOffset: Int,
    )

    private fun Char.isReferenceTokenChar(): Boolean {
        return isLetterOrDigit() || this in referenceTokenChars
    }

    private fun String.isNumericDollarPlaceholder(): Boolean {
        return firstOrNull() == WireGoblinReferenceKind.VARIABLE.prefix && drop(1).all { it.isDigit() } && length > 1
    }
}
