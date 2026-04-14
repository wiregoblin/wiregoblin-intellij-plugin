package io.wiregoblin.intellij

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType

class WireGoblinJsonSchemaProviderFactory : JsonSchemaProviderFactory {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
        return listOf(WireGoblinJsonSchemaFileProvider())
    }
}

private class WireGoblinJsonSchemaFileProvider : JsonSchemaFileProvider {
    companion object {
        private val LOG = Logger.getInstance(WireGoblinJsonSchemaFileProvider::class.java)
    }

    override fun isAvailable(file: VirtualFile): Boolean {
        return WireGoblinFileSupport.matchesWireGoblinName(file)
    }

    override fun getName(): String = "WireGoblin Config Schema"

    override fun getSchemaFile(): VirtualFile? {
        val url = javaClass.classLoader.getResource("schema/wiregoblin.schema.json")
        if (url == null) {
            LOG.warn("Missing embedded schema resource: schema/wiregoblin.schema.json")
            return null
        }
        return VfsUtil.findFileByURL(url)
    }

    override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema
}
