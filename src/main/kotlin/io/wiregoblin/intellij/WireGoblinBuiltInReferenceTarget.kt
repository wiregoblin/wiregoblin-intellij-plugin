package io.wiregoblin.intellij

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement

internal class WireGoblinBuiltInReferenceTarget(
    private val myProject: Project,
    private val sourceElement: PsiElement,
    val referenceName: String,
) : FakePsiElement() {
    override fun getProject(): Project = myProject

    override fun getParent(): PsiElement = sourceElement

    override fun getNavigationElement(): PsiElement = sourceElement

    override fun getName(): String = referenceName

    override fun canNavigate(): Boolean = false

    override fun canNavigateToSource(): Boolean = false
}
