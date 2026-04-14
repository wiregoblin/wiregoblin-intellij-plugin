package io.wiregoblin.intellij

internal object WireGoblinKeys {
    const val ID = "id"
    const val NAME = "name"
    const val VERSION = "version"
    const val TYPE = "type"
    const val DISABLE_RUN = "disable_run"
    const val CONTINUE_ON_ERROR = "continue_on_error"
    const val TIMEOUT_SECONDS = "timeout_seconds"
    const val PROVIDER = "provider"
    const val BASE_URL = "base_url"
    const val MODEL = "model"
    const val COLLECT = "collect"

    const val WORKFLOWS = "workflows"
    const val BLOCKS = "blocks"
    const val CATCH_ERROR_BLOCKS = "catch_error_blocks"
    const val ASSIGN = "assign"
    const val AI = "ai"
    const val CONDITION = "condition"
    const val BLOCK = "block"
    const val RULES = "rules"
    const val RETRY_ON = "retry_on"
    const val VARIABLE = "variable"
    const val OPERATOR = "operator"
    const val EXPECTED = "expected"

    const val CONSTANTS = "constants"
    const val SECRETS = "secrets"
    const val VARIABLES = "variables"
    const val SECRET_VARIABLES = "secret_variables"
    const val OUTPUTS = "outputs"

    val envCapableSections = setOf(
        CONSTANTS,
        SECRETS,
        VARIABLES,
        SECRET_VARIABLES,
    )

    val sequenceSections = setOf(
        WORKFLOWS,
        BLOCKS,
        CATCH_ERROR_BLOCKS,
    )

    val mappingSections = setOf(
        CONDITION,
        AI,
    )
}
