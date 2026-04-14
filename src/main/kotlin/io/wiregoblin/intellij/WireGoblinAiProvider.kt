package io.wiregoblin.intellij

enum class WireGoblinAiProvider(val value: String) {
    OLLAMA("ollama"),
    OPENAI_COMPATIBLE("openai_compatible"),
    ;

    companion object {
        fun fromValue(value: String): WireGoblinAiProvider =
            entries.firstOrNull { it.value == value } ?: OLLAMA

        fun valuesList(): List<String> = entries.map { it.value }
    }

    override fun toString(): String = value
}
