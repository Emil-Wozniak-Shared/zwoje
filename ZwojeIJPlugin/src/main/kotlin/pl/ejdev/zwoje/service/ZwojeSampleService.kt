package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import pl.ejdev.zwoje.utils.nameWithExtension

private const val DATA_DIRECTORY_NAME = "data"
private const val CONFIG_FILENAME = "zwoje.json"

@Service(Service.Level.PROJECT)
class ZwojeSampleService(
    private val project: Project
) {
    private val templateParserService = project.service<TemplateParserService>()
    private val templateResolverService = project.service<TemplateResolverService>()
    private val jsonParseService = project.service<JsonParseService>()

    fun getSamples(virtualFile: VirtualFile): GetSampleResult {
        return try {
            val dir = VfsUtil.createDirectoryIfMissing(virtualFile.parent, DATA_DIRECTORY_NAME)
                ?: return GetSampleResult.Error("Unable to create or access data directory")

            val fileName = "${virtualFile.nameWithoutExtension}.json"
            val file = dir.findChild(fileName) ?: return GetSampleResult.FileNotExists

            val content = VfsUtil.loadText(file)
            GetSampleResult.OK(content)
        } catch (e: Exception) {
            GetSampleResult.Error(e.message)
        }
    }

    fun createSample(virtualFile: VirtualFile, resolver: ZwojeTemplateResolver<Any>): CreateSampleResult {
        try {
            val parent = createConfigFileOrAddFile(resolver, virtualFile)

            val id = virtualFile.name
            val filePath = virtualFile.path
            templateResolverService.register(resolver, id, filePath)

            val dir = VfsUtil.createDirectoryIfMissing(parent, DATA_DIRECTORY_NAME)
            val fileName = virtualFile.nameWithExtension("json")
            var file: VirtualFile? = dir.findChild(fileName)
            if (file != null) {
                return CreateSampleResult.FileExists
            }
            file = dir.createChildData(this, fileName)
            VfsUtil.saveText(file, createContent(resolver))
            return CreateSampleResult.OK
        } catch (e: Exception) {
            e.printStackTrace()
            return CreateSampleResult.Error(e.message)
        }
    }

    private fun createConfigFileOrAddFile(resolver: ZwojeTemplateResolver<Any>, virtualFile: VirtualFile): VirtualFile {
        val specification = templateResolverService.toSpecification(resolver)
        var relativeFilePath = ""
        var resource: VirtualFile = virtualFile
        var parent: VirtualFile = virtualFile
        while (resource.name != "resources") {
            if (parent.name != specification.templatesDir) {
                parent = parent.parent
            }
            relativeFilePath = "/${resource.name}$relativeFilePath"
            resource = resource.parent
        }
        createConfigFile(resource, parent, virtualFile.name, "/resources$relativeFilePath")
        return parent
    }


    private fun createConfigFile(dir: VirtualFile, parent: VirtualFile, id: String, filePath: String) {
        val zwojeFile = dir.findChild(CONFIG_FILENAME)
        val (file, text) =
            if (zwojeFile == null) createNewZwojeFile(dir, parent, id, filePath)
            else addTemplateToZwojeFile(zwojeFile, id, filePath)

        VfsUtil.saveText(file, text)
    }

    private fun addTemplateToZwojeFile(
        zwojeFile: VirtualFile,
        id: String,
        filePath: String
    ): Pair<VirtualFile, String> {
        val data = zwojeFile.readText()
        val zwojeConfigFile = jsonParseService.parse<ZwojeConfigFile>(data)
        if (!zwojeConfigFile.config.templates.contains(id)) {
            zwojeConfigFile.config.templates[id] = filePath
        }
        val text = jsonParseService.toJson(zwojeConfigFile)
        return zwojeFile to text
    }

    private fun createNewZwojeFile(
        dir: VirtualFile,
        parent: VirtualFile,
        id: String,
        filePath: String
    ): Pair<VirtualFile, String> {
        val zwojeFile = dir.createChildData(this, CONFIG_FILENAME)
        val root = mapOf(
            "config" to mapOf(
                "root" to parent.name,
                "templates" to mapOf(
                    id to filePath
                )
            )
        )
        val json = jsonParseService.toJson(root)

        VfsUtil.saveText(zwojeFile, json)
        return zwojeFile to json
    }

    private fun createContent(resolver: ZwojeTemplateResolver<Any>): String {
        val parser = templateParserService.getParser(resolver.type)
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        FileDocumentManager.getInstance().saveDocument(editor.document)
        val templateVariables = parser.parse(editor.document.text)
        val body = createJsonFromVariables(templateVariables)
        return jsonParseService.toJson(body)
    }

    private fun createJsonFromVariables(templateVariables: Set<TemplateVariable>): Map<String, List<MutableMap<String, Any>>> {
        val sample = mutableMapOf<String, Any>()

        templateVariables.forEach { variable ->
            when {
                variable.type == VariableType.COLLECTION && variable.children.isNotEmpty() -> {
                    // Handle collection with known item properties
                    val parts = variable.name.split(".")
                    var current: MutableMap<String, Any> = sample

                    // Navigate to the collection's parent
                    parts.dropLast(1).forEach { part ->
                        val next = current.getOrPut(part) { mutableMapOf<String, Any>() }
                        current = next as MutableMap<String, Any>
                    }

                    val collectionKey = parts.last()
                    val itemSample = mutableMapOf<String, Any>()
                    variable.children.forEach { child ->
                        val childParts = child.name.split(".")
                        var itemCurrent: MutableMap<String, Any> = itemSample

                        childParts.forEachIndexed { index, part ->
                            if (index == childParts.lastIndex) {
                                itemCurrent[part] = ""
                            } else {
                                val next = itemCurrent.getOrPut(part) { mutableMapOf<String, Any>() }
                                itemCurrent = next as MutableMap<String, Any>
                            }
                        }
                    }

                    current[collectionKey] = listOf(itemSample)
                }

                variable.type != VariableType.COLLECTION -> {
                    val parts = variable.name.split(".")
                    var current: MutableMap<String, Any> = sample

                    parts.forEachIndexed { index, part ->
                        if (index == parts.lastIndex) {
                            current[part] = ""
                        } else {
                            val next = current.getOrPut(part) { mutableMapOf<String, Any>() }
                            current = next as MutableMap<String, Any>
                        }
                    }
                }
            }
        }
        return mapOf("samples" to listOf(sample))
    }

    data class ZwojeConfigFile(
        val config: ZwojeConfig
    )

    data class ZwojeConfig(
        val root: String,
        val templates: MutableMap<String, String>
    )

    sealed interface GetSampleResult {
        class OK(val content: String) : GetSampleResult
        object FileNotExists : GetSampleResult
        class Error(val message: String?) : GetSampleResult
    }

    sealed interface CreateSampleResult {
        data object OK : CreateSampleResult
        data object FileExists : CreateSampleResult
        data class Error(val message: String?) : CreateSampleResult
    }

}