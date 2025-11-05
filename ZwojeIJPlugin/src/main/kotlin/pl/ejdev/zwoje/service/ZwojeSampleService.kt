package pl.ejdev.zwoje.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
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
            val content = createContent(id, resolver)
            VfsUtil.saveText(file, content)
            return CreateSampleResult.OK
        } catch (e: Exception) {
            e.printStackTrace()
            return CreateSampleResult.Error(e.message)
        }
    }

    private fun createContent(id: String, resolver: ZwojeTemplateResolver<Any>): String {
        val parser = templateParserService.getParser(id, resolver)
        val editor = FileEditorManager.getInstance(project).selectedTextEditor!!
        FileDocumentManager.getInstance().saveDocument(editor.document)
        val templateVariables = parser.parse(editor.document.text)
        val sample = templateVariables.joinToString(",\n\t\t\t", "{\n\t\t\t", "\n\t\t}") {
            when (it.type) {
                VariableType.SINGLE -> "\"${it.name}\": \"\""
                VariableType.COLLECTION -> "\"${it.name}\": []"
            }
        }
        return """{
                   |"samples": [
                   |        $sample
                   |   ]
                   |}""".trimMargin()
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