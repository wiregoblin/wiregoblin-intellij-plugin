package io.wiregoblin.intellij

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

object WireGoblinFileSupport {
    fun isWireGoblinFile(file: PsiFile): Boolean {
        return matchesWireGoblinName(file.virtualFile) || matchesWireGoblinName(file.name)
    }

    fun matchesWireGoblinName(file: VirtualFile?): Boolean {
        return matchesWireGoblinName(file?.name)
    }

    fun matchesWireGoblinName(fileName: String?): Boolean {
        val name = fileName?.lowercase() ?: return false
        if (!name.endsWith(".yaml") && !name.endsWith(".yml")) {
            return false
        }

        return name == "wiregoblin.yaml" ||
            name == "wiregoblin.yml" ||
            name.endsWith(".wiregoblin.yaml") ||
            name.endsWith(".wiregoblin.yml")
    }
}
