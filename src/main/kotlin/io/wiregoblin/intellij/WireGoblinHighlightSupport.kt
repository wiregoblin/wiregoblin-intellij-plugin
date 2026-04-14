package io.wiregoblin.intellij

internal object WireGoblinHighlightSupport {
    private val envPlaceholderRegex = Regex("""\$\{[A-Za-z_][A-Za-z0-9_]*(?::=[^}]*)?}""")
    private val sqlPlaceholderRegex = Regex("""(?<![A-Za-z0-9_])\$\d+""")

    fun findEnvironmentPlaceholders(text: String): List<IntRange> {
        return envPlaceholderRegex.findAll(text).map { it.range }.toList()
    }

    fun findSqlPlaceholders(text: String): List<IntRange> {
        return sqlPlaceholderRegex.findAll(text).map { it.range }.toList()
    }
}
