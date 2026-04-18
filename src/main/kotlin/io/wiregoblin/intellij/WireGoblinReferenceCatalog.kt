package io.wiregoblin.intellij

enum class WireGoblinReferenceKind(
    val prefix: Char,
    val description: String,
) {
    CONSTANT_OR_SECRET('@', "constants and secrets"),
    VARIABLE('$', "variables and secret variables"),
    EXPRESSION('!', "built-in expressions"),
    ;

    companion object {
        fun fromPrefix(prefix: Char): WireGoblinReferenceKind? =
            entries.firstOrNull { it.prefix == prefix }

        fun prefixes(): Set<Char> = entries.mapTo(linkedSetOf()) { it.prefix }
    }
}

internal object WireGoblinReferenceCatalog {
    val kinds = WireGoblinReferenceKind.entries
    private const val EACH_ITEM_FIELD_PREFIX = "!Each.Item."

    val bangReferences = listOf(
        "!RunID",
        "!StartTime",
        "!StartUnix",
        "!StartDate",
        "!ProjectID",
        "!WorkflowID",
        "!WorkflowName",
        "!BlockStartTime",
        "!BlockStartUnix",
        "!ErrorMessage",
        "!ErrorBlockID",
        "!ErrorBlockName",
        "!ErrorBlockType",
        "!ErrorBlockIndex",
        "!Parent.WorkflowID",
        "!Parent.WorkflowName",
        "!Parent.RunID",
        "!Parent.StartTime",
        "!Each.Index",
        "!Each.Count",
        "!Each.First",
        "!Each.Last",
        "!Each.Item",
        "!Each.ItemJSON",
        "!Retry.Attempt",
        "!Retry.MaxAttempts",
    )

    fun isBangReference(raw: String): Boolean {
        return raw in bangReferences || isEachItemFieldReference(raw)
    }

    fun isEachItemFieldReference(raw: String): Boolean {
        return raw.startsWith(EACH_ITEM_FIELD_PREFIX) &&
            raw.removePrefix(EACH_ITEM_FIELD_PREFIX).split('.').all { it.isNotBlank() }
    }

    val helperTemplates = listOf(
        ValueHelperTemplate("\${NAME}", "Environment variable", 2, 6),
        ValueHelperTemplate("\${NAME:=default}", "Environment variable with default", 2, 6),
        ValueHelperTemplate("\$path.to.value", "Variable reference", 1, 14),
        ValueHelperTemplate("@secret_name", "Secret reference", 1, 12),
        ValueHelperTemplate("!expression", "Expression", 1, 11),
    )
}
