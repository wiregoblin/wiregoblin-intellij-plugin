package io.wiregoblin.intellij

internal object WireGoblinValueHelp {
    private val blockTypeDescriptions = mapOf(
        "http" to "send an HTTP request",
        "grpc" to "call a gRPC method",
        "postgres" to "run a SQL query or transaction",
        "redis" to "execute a Redis command",
        "openai" to "call an OpenAI-compatible chat completion API",
        "smtp" to "send an email via SMTP",
        "imap" to "wait for or read email via IMAP",
        "slack" to "send a Slack incoming webhook message",
        "telegram" to "send a Telegram message",
        "container" to "run a command in a Docker container",
        "delay" to "pause execution for a duration",
        "log" to "write a message to workflow output",
        "setvars" to "assign runtime variables",
        "assert" to "fail when a condition is not met",
        "goto" to "conditionally jump to another step",
        "transform" to "build structured data and cast values",
        "retry" to "retry one nested block with backoff",
        "foreach" to "run one nested block for each item",
        "parallel" to "run blocks concurrently",
        "workflow" to "invoke another workflow",
    )

    private val retryRuleTypeDescriptions = mapOf(
        "transport_error" to "retry on connection or transport failure",
        "status_code" to "retry when response status code matches",
        "path" to "retry when a response path matches a condition",
    )

    private val operatorDescriptions = mapOf(
        "=" to "equal to",
        "!=" to "not equal to",
        ">" to "greater than",
        ">=" to "greater than or equal to",
        "<" to "less than",
        "<=" to "less than or equal to",
        "contains" to "contains substring or item",
        "not_contains" to "does not contain substring or item",
        "matches" to "matches regular expression",
        "not_matches" to "does not match regular expression",
    )

    private val providerDescriptions = mapOf(
        "ollama" to "local Ollama-compatible AI runtime",
        "openai_compatible" to "OpenAI-style chat completions endpoint",
    )

    private val logLevelDescriptions = mapOf(
        "debug" to "verbose diagnostic message",
        "info" to "normal informational message",
        "warn" to "warning message",
        "error" to "error message",
    )

    private val parseModeDescriptions = mapOf(
        "HTML" to "Telegram HTML formatting mode",
        "Markdown" to "legacy Telegram Markdown mode",
        "MarkdownV2" to "Telegram MarkdownV2 formatting mode",
    )

    private val selectModeDescriptions = mapOf(
        "first" to "return the first matching email",
        "all" to "return all matching emails",
        "count" to "return only the number of matches",
    )

    fun descriptionFor(key: String, value: String): String? {
        return when (key) {
            WireGoblinKeys.TYPE -> blockTypeDescriptions[value] ?: retryRuleTypeDescriptions[value]
            WireGoblinKeys.OPERATOR -> operatorDescriptions[value]
            WireGoblinKeys.PROVIDER -> providerDescriptions[value]
            "level" -> logLevelDescriptions[value]
            "parse_mode" -> parseModeDescriptions[value]
            "select_mode" -> selectModeDescriptions[value]
            else -> null
        }
    }
}
