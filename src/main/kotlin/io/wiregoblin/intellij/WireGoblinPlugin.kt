package io.wiregoblin.intellij

object WireGoblinPlugin {
    const val ID = "io.wiregoblin.intellij"
    const val DISPLAY_NAME = "WireGoblin Config"
}

data class ValueHelperTemplate(
    val template: String,
    val description: String,
    val selectionStartOffset: Int,
    val selectionEndOffset: Int,
)
