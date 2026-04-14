package io.wiregoblin.intellij.psi

import io.wiregoblin.intellij.WireGoblinKeys
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WireGoblinCompletionContributorPsiTest : WireGoblinPsiTestCase() {
    private fun assertContainsAll(actual: Collection<String>, vararg expected: String) {
        assertTrue(
            actual.containsAll(expected.toList()),
            "Expected ${expected.toList()} to be present in $actual",
        )
    }

    fun testSuggestsTopLevelKeysInWireGoblinFile() {
        configureWireGoblin(
            """
                <caret>
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertContainsAll(
            myFixture.lookupElementStrings.orEmpty(),
            WireGoblinKeys.ID,
            WireGoblinKeys.WORKFLOWS,
            WireGoblinKeys.VARIABLES,
        )
    }

    fun testSuggestsBlockTypesForTypeFieldInsideBlocks() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: <caret>
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertContainsAll(myFixture.lookupElementStrings.orEmpty(), "http", "grpc", "retry")
    }

    fun testSuggestsBlockKeysInsideEmptyBlocksSection() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      <caret>
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertContainsAll(
            myFixture.lookupElementStrings.orEmpty(),
            WireGoblinKeys.ID,
            WireGoblinKeys.TYPE,
            WireGoblinKeys.CONDITION,
        )
    }

    fun testSuggestsRetryRuleTypesInsideRetryRules() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "retry-step"
                        type: "retry"
                        retry_on:
                          rules:
                            - type: <caret>
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertContainsAll(myFixture.lookupElementStrings.orEmpty(), "transport_error", "status_code", "path")
    }

    fun testSuggestsWireGoblinReferencesInsideEnvCapableValue() {
        configureWireGoblin(
            """
                constants:
                  grpc_host: "localhost"
                workflows:
                  - id: "wf"
                    constants:
                      local_secret: "token"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: @gr<caret>
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertContainsAll(myFixture.lookupElementStrings.orEmpty(), "@grpc_host")
    }

    fun testSuggestsTopLevelSecretVariablesDollarReference() {
        configureWireGoblin(
            """
                secret_variables:
                  db_pass: ""
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: ${'$'}<caret>
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertContainsAll(myFixture.lookupElementStrings.orEmpty(), "${'$'}db_pass")
    }

    fun testSuggestsReferencesInsideQuotedValueContext() {
        configureWireGoblin(
            """
                constants:
                  grpc_host: "localhost"
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "@gr<caret>"
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertContainsAll(myFixture.lookupElementStrings.orEmpty(), "@grpc_host")
    }

    fun testDoesNotFallBackToKeyCompletionWhenTypingReference() {
        configureWireGoblin(
            """
                constants:
                  users_url: "http://127.0.0.1"
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "@us<caret>"
            """.trimIndent(),
        )

        myFixture.completeBasic()

        val items = myFixture.lookupElementStrings.orEmpty()
        assertTrue("@users_url" in items, "Expected @users_url in $items")
        assertFalse("continue_on_error" in items, "Did not expect block key completion in $items")
    }

    fun testPrefersWorkflowScopedReferenceInCompletion() {
        configureWireGoblin(
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

        myFixture.completeBasic()

        val items = myFixture.lookupElementStrings.orEmpty()
        assertTrue("@users_url" in items, "Expected @users_url in $items")
        assertFalse("timeout_seconds:" in items, "Did not expect key completion in $items")
    }

    fun testSuggestsDollarReferencesInsideQuotedValueContext() {
        configureWireGoblin(
            """
                secret_variables:
                  db_pass: ""
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "postgres://${'$'}db_<caret>pass"
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertContainsAll(myFixture.lookupElementStrings.orEmpty(), "${'$'}db_pass")
    }

    fun testDoesNotOfferKeyCompletionForUnknownReferencePrefix() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: "@zzz<caret>"
            """.trimIndent(),
        )

        myFixture.completeBasic()

        val items = myFixture.lookupElementStrings.orEmpty()
        assertFalse("continue_on_error" in items, "Did not expect block key completion in $items")
        assertFalse("timeout_seconds:" in items, "Did not expect workflow key completion in $items")
    }

    fun testSuggestsEnvHelperTemplatesForEnvCapableValue() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "step"
                        type: "http"
                        url: <caret>
            """.trimIndent(),
        )

        myFixture.completeBasic()

        val items = requireNotNull(myFixture.lookupElementStrings)
        assertContainsAll(items, "\${NAME}", "\$path.to.value", "@secret_name", "!expression")
    }

    fun testSuggestsOperatorKeyInsideAssertBlock() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "assert_status"
                        type: "assert"
                        variable: "${'$'}status"
                        op<caret>
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertTrue(myFixture.file.text.contains("operator: "), myFixture.file.text)
    }

    fun testSuggestsOperatorValuesInsideAssertBlock() {
        configureWireGoblin(
            """
                workflows:
                  - id: "wf"
                    blocks:
                      - id: "assert_status"
                        type: "assert"
                        variable: "${'$'}status"
                        operator: "<caret>"
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertContainsAll(myFixture.lookupElementStrings.orEmpty(), "=", "!=", "contains", "matches")
    }

    fun testDoesNotOfferWireGoblinCompletionForNonWireGoblinFile() {
        configurePlainYaml(
            """
                <caret>
            """.trimIndent(),
        )

        myFixture.completeBasic()

        assertFalse(myFixture.lookupElementStrings.orEmpty().contains(WireGoblinKeys.WORKFLOWS))
    }
}
