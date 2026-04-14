package io.wiregoblin.intellij.psi

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionContributorEP
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.LineMarkerProviders
import com.intellij.lang.LanguageAnnotators
import com.intellij.lang.LanguageExtensionPoint
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.DefaultPluginDescriptor
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceContributor
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.wiregoblin.intellij.WireGoblinBlockTypeAnnotator
import io.wiregoblin.intellij.WireGoblinCompletionContributor
import io.wiregoblin.intellij.WireGoblinReferenceContributor
import io.wiregoblin.intellij.WireGoblinWorkflowRunLineMarkerProvider
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.YAMLLanguage
import org.jetbrains.yaml.YAMLParserDefinition

abstract class WireGoblinPsiTestCase : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        ensureYamlRegistered()

        val yamlId = YAMLLanguage.INSTANCE.id
        val pluginDescriptor = DefaultPluginDescriptor(
            PluginId.getId("io.wiregoblin.intellij.tests"),
            javaClass.classLoader,
            "WireGoblin tests",
        )
        ExtensionTestUtil.addExtensions(
            CompletionContributor.EP,
            listOf(CompletionContributorEP(yamlId, WireGoblinCompletionContributor::class.java.name, pluginDescriptor)),
            testRootDisposable,
        )
        ExtensionTestUtil.addExtensions(
            LanguageAnnotators.EP_NAME,
            listOf(
                LanguageExtensionPoint<Annotator>(
                    yamlId,
                    WireGoblinBlockTypeAnnotator::class.java.name,
                    pluginDescriptor,
                ),
            ),
            testRootDisposable,
        )
        ExtensionTestUtil.addExtensions(
            PsiReferenceContributor.EP_NAME,
            listOf(
                LanguageExtensionPoint<PsiReferenceContributor>(
                    yamlId,
                    WireGoblinReferenceContributor::class.java.name,
                    pluginDescriptor,
                ),
            ),
            testRootDisposable,
        )
        ExtensionTestUtil.addExtensions(
            LineMarkerProviders.EP_NAME,
            listOf(
                LanguageExtensionPoint<LineMarkerProvider>(
                    yamlId,
                    WireGoblinWorkflowRunLineMarkerProvider::class.java.name,
                    pluginDescriptor,
                ),
            ),
            testRootDisposable,
        )
    }

    private fun ensureYamlRegistered() {
        val ftm = FileTypeManager.getInstance()
        if (ftm.getFileTypeByExtension("yaml") !is YAMLFileType) {
            ApplicationManager.getApplication().runWriteAction {
                ftm.associateExtension(YAMLFileType.YML, "yaml")
            }
        }
        if (ftm.getFileTypeByExtension("yml") !is YAMLFileType) {
            ApplicationManager.getApplication().runWriteAction {
                ftm.associateExtension(YAMLFileType.YML, "yml")
            }
        }
        if (LanguageParserDefinitions.INSTANCE.forLanguage(YAMLLanguage.INSTANCE) == null) {
            LanguageParserDefinitions.INSTANCE.addExplicitExtension(
                YAMLLanguage.INSTANCE,
                YAMLParserDefinition(),
            )
        }
    }

    protected fun configureWireGoblin(text: String): PsiFile {
        return myFixture.configureByText("wiregoblin.yaml", text)
    }

    protected fun configurePlainYaml(text: String): PsiFile {
        return myFixture.configureByText("plain.yaml", text)
    }
}
