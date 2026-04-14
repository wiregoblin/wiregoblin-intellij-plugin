package io.wiregoblin.intellij.psi

import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WireGoblinBlockTypeAnnotatorPsiTest : WireGoblinPsiTestCase() {
    fun testHighlightsUnknownBlockTypeInsideBlocks() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "not_real"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertTrue(descriptions.any { it.contains("Unknown WireGoblin block type 'not_real'") })
    }

    fun testHighlightsUnknownRetryRuleType() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "retry"
                        retry_on:
                          rules:
                            - type: "bad_rule"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertTrue(descriptions.any { it.contains("Unknown WireGoblin retry rule type 'bad_rule'") })
    }

    fun testHighlightsMisnamedWorkflowBlockKey() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    block:
                      - id: "step"
                        type: "http"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertTrue(descriptions.any { it == "Use 'blocks' here, not 'block'." })
    }

    fun testDoesNotHighlightValidBlockType() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it.contains("Unknown WireGoblin") })
    }

    fun testHighlightsUnknownHttpMethod() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        method: "FETCH"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertTrue(descriptions.any { it.contains("Unknown wiregoblin method value 'FETCH'") })
    }

    fun testHighlightsUnknownAtReferenceInsideValue() {
        configureWireGoblin(
            """
                constants:
                  known: "ok"
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "@missing"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertTrue(descriptions.any { it == "Unknown WireGoblin reference '@missing'." })
    }

    fun testHighlightsUnknownBangReferenceInsideValue() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "log"
                        message: "!NotReal"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertTrue(descriptions.any { it == "Unknown WireGoblin reference '!NotReal'." })
    }

    fun testDoesNotHighlightAssignKeysAsUnknownReferences() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        method: "GET"
                        url: "https://example.com"
                        assign:
                          ${'$'}user_id: "body.id"
                          ${'$'}user_name: "body.name"
                          ${'$'}http_status: "outputs.statusCode"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it.contains("Unknown WireGoblin reference") })
    }

    fun testDoesNotHighlightAssignVariableUsedInLaterBlock() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step1"
                        type: "http"
                        method: "GET"
                        url: "https://example.com"
                        assign:
                          ${'$'}user_id: "body.id"
                      - id: "step2"
                        type: "log"
                        message: "${'$'}user_id"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it.contains("Unknown WireGoblin reference") })
    }

    fun testDoesNotHighlightAssignVariableUsedInAssertBlockVariableField() {
        configureWireGoblin(
            """
                workflows:
                  - id: "http_example"
                    name: "HTTP Example"
                    blocks:
                      - id: "request1"
                        name: "Get User"
                        type: "http"
                        method: "GET"
                        url: "@users_url"
                        assign:
                          ${'$'}user_id: "body.id"
                          ${'$'}user_name: "body.name"
                          ${'$'}user_role: "body.role"
                          ${'$'}http_status: "outputs.statusCode"
                          ${'$'}http_response_time_ms: "outputs.responseTimeMs"
                      - id: "assert_http_status"
                        name: "Assert HTTP Status"
                        type: "assert"
                        variable: "${'$'}http_status"
                        operator: "="
                        expected: "200"
                        error_message: "HTTP example did not return 200"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it == "Unknown WireGoblin reference '${'$'}http_status'." })
    }

    fun testDoesNotHighlightAssignVariablesFromNestedRetryBlockInLaterBlocks() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "retry_fetch_user"
                        type: "retry"
                        max_attempts: 5
                        delay_ms: 100
                        retry_on:
                          match: "any"
                          rules:
                            - type: "transport_error"
                            - type: "status_code"
                              in: [429, 500, 502, 503]
                        block:
                          type: "http"
                          method: "GET"
                          url: "@users_url"
                          headers:
                            X-Retry-Attempt: "!Retry.Attempt"
                          assign:
                            ${'$'}retry_user_id: "body.id"
                            ${'$'}retry_http_status: "outputs.statusCode"
                      - id: "assert_retry_status"
                        type: "assert"
                        variable: "${'$'}retry_http_status"
                        operator: "="
                        expected: "200"
                      - id: "log_retry_result"
                        type: "log"
                        level: "info"
                        message: "Retry example fetched user ${'$'}retry_user_id"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it == "Unknown WireGoblin reference '${'$'}retry_http_status'." })
        assertFalse(descriptions.any { it == "Unknown WireGoblin reference '${'$'}retry_user_id'." })
    }

    fun testDoesNotHighlightEnvPlaceholdersOrKnownReferences() {
        configureWireGoblin(
            """
                constants:
                  known: "ok"
                workflows:
                  - id: "wf"
                    variables:
                      token: "abc"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "${'$'}{HOST}/@known/${'$'}token/!Each.Item"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it.contains("Unknown WireGoblin reference") })
    }

    fun testDoesNotHighlightNumericDollarPlaceholdersInsidePostgresQuery() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "seed_postgres"
                        type: "postgres"
                        dsn: "@postgres_dsn"
                        query: >
                          insert into workflow_runs (id, user_name, status)
                          values ($1, $2, $3)
                          on conflict (id) do update
                          set user_name = excluded.user_name, status = excluded.status
                        params:
                          - "@local_run_id"
                          - "Alice Goblin"
                          - "cached"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it == "Unknown WireGoblin reference '\$1'." })
        assertFalse(descriptions.any { it == "Unknown WireGoblin reference '\$2'." })
        assertFalse(descriptions.any { it == "Unknown WireGoblin reference '\$3'." })
    }

    fun testDoesNotHighlightCollectVariableUsedInLaterAssertBlock() {
        configureWireGoblin(
            """
                workflows:
                  - id: "foreach_range_example"
                    name: "Foreach Range Example"
                    blocks:
                      - id: "generate_range"
                        name: "Generate Range"
                        type: "foreach"
                        items:
                          from: 1
                          to: 5
                          step: 2
                        block:
                          type: "log"
                          level: "info"
                          message: "Range item !Each.Item at index !Each.Index"
                        collect:
                          ${'$'}range_values: "item"
                      - id: "assert_range_values"
                        name: "Assert Range Values"
                        type: "assert"
                        variable: "${'$'}range_values"
                        operator: "="
                        expected: "[1,3,5]"
                        error_message: "Foreach range example did not collect the expected values"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it == "Unknown WireGoblin reference '${'$'}range_values'." })
    }

    fun testDoesNotHighlightAssignVariableFromPostgresTransactionUsedInLaterAssertBlock() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "run_postgres_transaction"
                        type: "postgres"
                        dsn: "@postgres_dsn"
                        transaction:
                          - query: >
                              insert into workflow_tx_runs (id, status)
                              values ($1, $2)
                              on conflict (id) do update
                              set status = excluded.status
                            params:
                              - "@local_run_id"
                              - "prepared"
                          - query: >
                              select count(*) as total
                              from workflow_tx_runs
                              where id = $1
                            params:
                              - "@local_run_id"
                            assign:
                              ${'$'}tx_row_count: "body.rows.0.total"
                          - query: >
                              update workflow_tx_runs
                              set status = $1
                              where id = $2
                            params:
                              - "counted_${'$'}tx_row_count"
                              - "@local_run_id"
                      - id: "assert_postgres_transaction_row_count"
                        type: "assert"
                        variable: "${'$'}tx_row_count"
                        operator: "="
                        expected: "1"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it == "Unknown WireGoblin reference '${'$'}tx_row_count'." })
    }

    fun testDoesNotHighlightShellEnvVariableInsideContainerCommand() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "run_container"
                        type: "container"
                        image: "@container_image"
                        command: printf '%s' "${'$'}MESSAGE"
                        env:
                          MESSAGE: "${'$'}cached_user_name"
                        assign:
                          ${'$'}container_stdout: "body.stdout"
                          ${'$'}container_exit_code: "outputs.exitCode"
            """.trimIndent(),
        )

        val descriptions = myFixture.doHighlighting().mapNotNull { it.description }
        assertFalse(descriptions.any { it == "Unknown WireGoblin reference '\$MESSAGE'." })
    }
}
