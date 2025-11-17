package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import pl.ejdev.zwoje.CONFIG_FILENAME

@Service(Service.Level.PROJECT)
class ZwojeConfigFileService(
    private val project: Project
) {
    private val jsonParseService = project.service<JsonParseService>()

    fun getConfigs(): List<RootConfig> = findConfigs(project)
        .asSequence()
        .map { it.readText() }
        .filter { it.isNotEmpty() }
        .map { jsonParseService.parse<RootConfig>(it) }
        .toList()

    fun createConfigFile(dir: VirtualFile, parent: VirtualFile, id: String, filePath: String) {
        val zwojeFile = dir.findChild(CONFIG_FILENAME)
        val (file, text) =
            if (zwojeFile == null) {
                createNew(dir, parent, id, filePath)
            } else {
                addTemplate(zwojeFile, id, filePath)
            }

        VfsUtil.saveText(file, text)
    }

    private fun createNew(
        dir: VirtualFile, parent: VirtualFile, id: String, filePath: String,
    ): Pair<VirtualFile, String> {
        val dataPath = getDataPath(filePath, id)
        val configFile = RootConfig.createNew(parent.name, id, filePath, dataPath)
        val zwojeFile = dir.createChildData(this, CONFIG_FILENAME)
        val json = jsonParseService.toJson(configFile)

        VfsUtil.saveText(zwojeFile, json)
        return zwojeFile to json
    }

    private fun addTemplate(zwojeFile: VirtualFile, id: String, filePath: String): Pair<VirtualFile, String> {
        val data = zwojeFile.readText()
        val configFile = jsonParseService.parse<RootConfig>(data)
        configFile.addTemplate(id, filePath, getDataPath(filePath, id))
        val text = jsonParseService.toJson(configFile)
        return zwojeFile to text
    }

    private fun getDataPath(filePath: String, id: String): String =
        filePath.replace(id, "data/" + id.replaceAfter(".", "json"))

    private fun findConfigs(project: Project): List<VirtualFile> {
        val result = mutableListOf<VirtualFile>()

        ModuleManager.getInstance(project).modules.forEach { module ->
            val roots = ModuleRootManager.getInstance(module).contentRoots
            for (root in roots) {
                root.findFileByRelativePath("src/main/resources")?.let {
                    VfsUtilCore.iterateChildrenRecursively(it, { true }) { file ->
                        if (!file.isDirectory && file.name == CONFIG_FILENAME) {
                            result.add(file)
                        }
                        true
                    }
                }
            }
        }
        return result
    }

    data class RootConfig(
        val config: Config
    ) {

        fun addTemplate(id: String, filePath: String, dataPath: String) {
            if (!config.templates.contains(id)) {
                val templateData = TemplateData(
                    filePath, dataPath,
                )
                config.templates[id] = templateData
            }
        }

        data class Config(
            val root: String,
            val templates: MutableMap<String, TemplateData>
        )

        data class TemplateData(
            val path: String,
            val data: String
        )

        companion object {
            fun createNew(name: String, id: String, filePath: String, dataPath: String) = RootConfig(
                config = RootConfig.Config(
                    root = name,
                    templates = mutableMapOf(id to RootConfig.TemplateData(filePath, dataPath))
                )
            )
        }
    }

}