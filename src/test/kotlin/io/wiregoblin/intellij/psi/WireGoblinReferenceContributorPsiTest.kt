package io.wiregoblin.intellij.psi

import io.wiregoblin.intellij.WireGoblinBuiltInReferenceTarget
import org.jetbrains.yaml.psi.YAMLKeyValue
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WireGoblinReferenceContributorPsiTest : WireGoblinPsiTestCase() {
    fun testResolvesAtReferenceToTopLevelConstant() {
        val file = configureWireGoblin(
            """
                constants:
                  users_url: "http://127.0.0.1"
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "@users_<caret>url"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as YAMLKeyValue

        assertEquals("users_url", target.keyText)
        assertEquals("http://127.0.0.1", target.valueText)
    }

    fun testPrefersWorkflowScopedAtReferenceOverTopLevelConstant() {
        val file = configureWireGoblin(
            """
                constants:
                  users_url: "http://top-level"
                workflows:
                  - id: "wf"
                    constants:
                      users_url: "http://workflow-local"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "@users_<caret>url"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as YAMLKeyValue

        assertEquals("users_url", target.keyText)
        assertEquals("http://workflow-local", target.valueText)
    }

    fun testResolvesDollarReferenceToWorkflowVariable() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    variables:
                      user_id: "42"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "https://example.com/${'$'}user<caret>_id"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as YAMLKeyValue

        assertEquals("user_id", target.keyText)
        assertEquals("42", target.valueText)
    }

    fun testResolvesDollarReferenceToTopLevelSecretVariableInPlainValue() {
        val file = configureWireGoblin(
            """
                secret_variables:
                  api_token: "demo"
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "log"
                        message: ${'$'}api_<caret>token
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as YAMLKeyValue

        assertEquals("api_token", target.keyText)
        assertEquals("demo", target.valueText)
    }

    fun testResolvesBangExpressionToBuiltInTarget() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "log"
                        message: "!Each.<caret>Item"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as WireGoblinBuiltInReferenceTarget
        assertEquals("!Each.Item", target.referenceName)
    }

    fun testDoesNotResolveUnknownAtReference() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "@miss<caret>ing"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        requireNotNull(reference)
        assertNull(reference?.resolve())
    }

    fun testResolvesAssignVariableInAssertBlockVariableField() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "http_example"
                    blocks:
                      - id: "request1"
                        type: "http"
                        method: "GET"
                        url: "https://example.com"
                        assign:
                          ${'$'}http_status: "outputs.statusCode"
                      - id: "assert_http_status"
                        type: "assert"
                        variable: "${'$'}http_<caret>status"
                        operator: "="
                        expected: "200"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as YAMLKeyValue

        assertEquals("${'$'}http_status", target.keyText)
        assertEquals("outputs.statusCode", target.valueText)
    }

    fun testResolvesAssignVariablesFromNestedRetryBlockInLaterBlocks() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "retry_fetch_user"
                        type: "retry"
                        max_attempts: 5
                        delay_ms: 100
                        block:
                          type: "http"
                          method: "GET"
                          url: "https://example.com"
                          assign:
                            ${'$'}retry_user_id: "body.id"
                            ${'$'}retry_http_status: "outputs.statusCode"
                      - id: "assert_retry_status"
                        type: "assert"
                        variable: "${'$'}retry_http_<caret>status"
                        operator: "="
                        expected: "200"
                      - id: "log_retry_result"
                        type: "log"
                        message: "Retry example fetched user ${'$'}retry_user_id"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as YAMLKeyValue

        assertEquals("${'$'}retry_http_status", target.keyText)
        assertEquals("outputs.statusCode", target.valueText)
    }

    fun testResolvesAtReferenceInsideNestedRetryBlock() {
        val file = configureWireGoblin(
            """
                constants:
                  users_url: "http://127.0.0.1"
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "retry_fetch_user"
                        type: "retry"
                        max_attempts: 5
                        delay_ms: 100
                        block:
                          type: "http"
                          method: "GET"
                          url: "@users_<caret>url"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as YAMLKeyValue

        assertEquals("users_url", target.keyText)
        assertEquals("http://127.0.0.1", target.valueText)
    }

    fun testResolvesCollectVariableInLaterAssertBlock() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "foreach_range_example"
                    blocks:
                      - id: "generate_range"
                        type: "foreach"
                        items:
                          from: 1
                          to: 5
                          step: 2
                        block:
                          type: "log"
                          message: "Range item !Each.Item at index !Each.Index"
                        collect:
                          ${'$'}range_values: "item"
                      - id: "assert_range_values"
                        type: "assert"
                        variable: "${'$'}range_<caret>values"
                        operator: "="
                        expected: "[1,3,5]"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as YAMLKeyValue

        assertEquals("${'$'}range_values", target.keyText)
        assertEquals("item", target.valueText)
    }

    fun testResolvesAssignVariableFromPostgresTransactionInLaterAssertBlock() {
        val file = configureWireGoblin(
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
                      - id: "assert_postgres_transaction_row_count"
                        type: "assert"
                        variable: "${'$'}tx_row_<caret>count"
                        operator: "="
                        expected: "1"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        val target = requireNotNull(reference?.resolve()) as YAMLKeyValue

        assertEquals("${'$'}tx_row_count", target.keyText)
        assertEquals("body.rows.0.total", target.valueText)
    }

    fun testDoesNotCreateReferenceForShellEnvVariableInsideContainerCommand() {
        val file = configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "run_container"
                        type: "container"
                        image: "@container_image"
                        command: printf '%s' "${'$'}MES<caret>SAGE"
                        env:
                          MESSAGE: "${'$'}cached_user_name"
            """.trimIndent(),
        )

        val reference = file.findReferenceAt(myFixture.caretOffset)
        assertNull(reference)
    }
}
