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
            val parts = variable.name.split(".")
            var current: MutableMap<String, Any> = sample

            parts.forEachIndexed { index, part ->
                if (index == parts.lastIndex) {
                    // Assign value depending on variable type
                    when (variable.type) {
                        VariableType.COLLECTION -> current[part] = emptyList<Any>()
                        VariableType.SINGLE, VariableType.OBJECT -> current[part] = ""
                    }
                } else {
                    // Walk or create nested map
                    val next = current.getOrPut(part) { mutableMapOf<String, Any>() }
                    current = next as MutableMap<String, Any>
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