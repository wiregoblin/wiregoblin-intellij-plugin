package io.wiregoblin.intellij

internal data class WireGoblinBuiltInReferenceDoc(
    val name: String,
    val exampleValue: String? = null,
    val description: String,
)

internal object WireGoblinBuiltInReferenceDocs {
    private val docs = listOf(
        WireGoblinBuiltInReferenceDoc("!RunID", "\"f47ac10b-58cc-...\"", "Unique UUID generated at the start of each run. Useful as a correlation ID."),
        WireGoblinBuiltInReferenceDoc("!StartTime", "\"2026-04-01T12:00:00Z\"", "Workflow start time in RFC 3339 (UTC)."),
        WireGoblinBuiltInReferenceDoc("!StartUnix", "\"1743508800\"", "Workflow start time as Unix epoch seconds. Useful for TTL and expiry arithmetic."),
        WireGoblinBuiltInReferenceDoc("!StartDate", "\"2026-04-01\"", "Workflow start date in YYYY-MM-DD format (UTC)."),
        WireGoblinBuiltInReferenceDoc("!ProjectID", "\"my-project\"", "ID of the project this workflow belongs to."),
        WireGoblinBuiltInReferenceDoc("!WorkflowID", "\"create_user\"", "ID of the current workflow."),
        WireGoblinBuiltInReferenceDoc("!WorkflowName", "\"Create User\"", "Display name of the current workflow."),
        WireGoblinBuiltInReferenceDoc("!BlockStartTime", "\"2026-04-01T12:00:05Z\"", "Start time of the current step in RFC 3339 (UTC)."),
        WireGoblinBuiltInReferenceDoc("!BlockStartUnix", "\"1743508805\"", "Start time of the current step as Unix epoch seconds."),
        WireGoblinBuiltInReferenceDoc("!ErrorMessage", null, "Error message from the failed step. Available in error-handling flows."),
        WireGoblinBuiltInReferenceDoc("!ErrorBlockID", null, "The id of the failed step. Available in error-handling flows."),
        WireGoblinBuiltInReferenceDoc("!ErrorBlockName", null, "The name of the failed step. Available in error-handling flows."),
        WireGoblinBuiltInReferenceDoc("!ErrorBlockType", null, "The block type of the failed step. Available in error-handling flows."),
        WireGoblinBuiltInReferenceDoc("!ErrorBlockIndex", null, "The 1-based index of the failed step. Available in error-handling flows."),
        WireGoblinBuiltInReferenceDoc("!Parent.WorkflowID", null, "Parent workflow ID. Available inside a child workflow invoked via a workflow block."),
        WireGoblinBuiltInReferenceDoc("!Parent.WorkflowName", null, "Parent workflow name. Available inside a child workflow invoked via a workflow block."),
        WireGoblinBuiltInReferenceDoc("!Parent.RunID", null, "Parent run ID. Available inside a child workflow invoked via a workflow block."),
        WireGoblinBuiltInReferenceDoc("!Parent.StartTime", null, "Parent start time. Available inside a child workflow invoked via a workflow block."),
        WireGoblinBuiltInReferenceDoc("!Each.Index", null, "Current zero-based item index inside a foreach block."),
        WireGoblinBuiltInReferenceDoc("!Each.Count", null, "Total number of items inside a foreach block."),
        WireGoblinBuiltInReferenceDoc("!Each.First", null, "true when the current foreach item is the first item."),
        WireGoblinBuiltInReferenceDoc("!Each.Last", null, "true when the current foreach item is the last item."),
        WireGoblinBuiltInReferenceDoc("!Each.Item", null, "Current foreach item value."),
        WireGoblinBuiltInReferenceDoc("!Each.ItemJSON", null, "Current foreach item serialized as JSON."),
        WireGoblinBuiltInReferenceDoc("!Each.Item.<field>", null, "Field of the current foreach item when iterating over objects."),
        WireGoblinBuiltInReferenceDoc("!Retry.Attempt", null, "Current retry attempt number inside a retry block."),
        WireGoblinBuiltInReferenceDoc("!Retry.MaxAttempts", null, "Maximum configured number of retry attempts inside a retry block."),
    ).associateBy { it.name }

    fun find(name: String): WireGoblinBuiltInReferenceDoc? {
        return docs[name] ?: if (WireGoblinReferenceCatalog.isEachItemFieldReference(name)) {
            docs["!Each.Item.<field>"]
        } else {
            null
        }
    }
}
