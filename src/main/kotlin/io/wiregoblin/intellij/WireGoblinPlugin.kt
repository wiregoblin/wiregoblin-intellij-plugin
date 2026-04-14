package io.wiregoblin.intellij

object WireGoblinPlugin {
    const val ID = "io.wiregoblin.plugin"
    const val DISPLAY_NAME = "WireGoblin Config"
}

data class ValueHelperTemplate(
    val template: String,
    val description: String,
    val selectionStartOffset: Int,
    val selectionEndOffset: Int,
)
