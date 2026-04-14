package io.wiregoblin.intellij

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

class WireGoblinSqlLanguageInjector : MultiHostInjector {
    override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(YAMLScalar::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (!WireGoblinFileSupport.isWireGoblinFile(context.containingFile)) {
            return
        }

        val host = context as? PsiLanguageInjectionHost ?: return
        val keyValue = context.parent as? YAMLKeyValue ?: return
        if (keyValue.keyText != "query" || WireGoblinYamlContextLocator.enclosingBlockType(keyValue) != "postgres") {
            return
        }

        val sqlLanguage = Language.findLanguageByID("SQL") ?: return
        registrar.startInjecting(sqlLanguage)
        registrar.addPlace(null, null, host, ElementManipulators.getValueTextRange(host))
        registrar.doneInjecting()
    }
}
