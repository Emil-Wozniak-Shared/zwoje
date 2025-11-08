package pl.ejdev.zwoje.service

import com.google.gson.GsonBuilder
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import pl.ejdev.zwoje.core.template.TemplateVariable
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import pl.ejdev.zwoje.utils.nameWithExtension

private const val DATA_DIRECTORY_NAME = "data"

@Service(Service.Level.PROJECT)
class ZwojeSampleService(
    private val project: Project
) {
    private val templateParserService = project.service<TemplateParserService>()
    private val templateResolverService = project.service<TemplateResolverService>()

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
            val dir = VfsUtil.createDirectoryIfMissing(virtualFile.parent, DATA_DIRECTORY_NAME)
            val fileName = virtualFile.nameWithExtension("json")
            var file: VirtualFile? = dir.findChild(fileName)
            if (file != null) {
                return CreateSampleResult.FileExists
            }
            file = dir.createChildData(this, fileName)
            val id = virtualFile.name
            templateResolverService.register(resolver, id, virtualFile.path)
            val content = createContent(resolver)
            VfsUtil.saveText(file, content)
            return CreateSampleResult.OK
        } catch (e: Exception) {
            e.printStackTrace()
            return CreateSampleResult.Error(e.message)
        }
    }

    private fun createContent(resolver: ZwojeTemplateResolver<Any>): String {
        val parser = templateParserService.getParser(resolver.type)
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        FileDocumentManager.getInstance().saveDocument(editor.document)
        val templateVariables = parser.parse(editor.document.text)
        return nestedJsonWithGson(templateVariables)
    }


    private fun nestedJsonWithGson(templateVariables: Set<TemplateVariable>): String {
        val sample = mutableMapOf<String, Any>()

        for (variable in templateVariables) {
            if (variable.type == VariableType.COLLECTION && variable.children.isNotEmpty()) {
                // Handle collection with known item properties
                val parts = variable.name.split(".")
                var current: MutableMap<String, Any> = sample

                // Navigate to the collection's parent
                parts.dropLast(1).forEach { part ->
                    val next = current.getOrPut(part) { mutableMapOf<String, Any>() }
                    current = next as MutableMap<String, Any>
                }

                // Create the collection with sample item
                val collectionKey = parts.last()
                val itemSample = mutableMapOf<String, Any>()

                // Add all item properties
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
            } else if (variable.type != VariableType.COLLECTION) {
                // Handle regular variables
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

        val root = mapOf("samples" to listOf(sample))
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(root)
    }

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