package io.wiregoblin.intellij

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement

class WireGoblinDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val target = element as? WireGoblinBuiltInReferenceTarget ?: return null
        val doc = WireGoblinBuiltInReferenceDocs.find(target.referenceName) ?: return null
        val example = doc.exampleValue?.let { "<p><b>Example value:</b> <code>$it</code></p>" }.orEmpty()
        return """
            <h2><code>${doc.name}</code></h2>
            <p>${doc.description}</p>
            $example
        """.trimIndent()
    }

    override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
        val target = element as? WireGoblinBuiltInReferenceTarget ?: return null
        val doc = WireGoblinBuiltInReferenceDocs.find(target.referenceName) ?: return null
        return "${doc.name} - ${doc.description}"
    }
}
