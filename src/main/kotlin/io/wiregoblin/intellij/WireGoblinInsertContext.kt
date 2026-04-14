package io.wiregoblin.intellij

data class WireGoblinInsertContext(
    val caretOffset: Int,
    val lineStartOffset: Int,
    val indent: String,
)

