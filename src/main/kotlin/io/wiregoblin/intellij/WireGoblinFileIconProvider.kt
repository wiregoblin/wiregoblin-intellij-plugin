package io.wiregoblin.intellij

import com.intellij.ide.FileIconProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class WireGoblinFileIconProvider : FileIconProvider {
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon? {
        return if (WireGoblinFileSupport.matchesWireGoblinName(file)) WireGoblinIcons.File else null
    }
}
