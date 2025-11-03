package pl.ejdev.zwoje.components

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.VariableType
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import pl.ejdev.zwoje.service.HtmlEngineSearchService
import pl.ejdev.zwoje.service.TemplateParserService
import pl.ejdev.zwoje.service.TemplateService
import pl.ejdev.zwoje.service.TemplateTypeService
import pl.ejdev.zwoje.utils.isSupported
import javax.swing.JButton
import kotlin.concurrent.thread

private const val TITLE = "Create Samples Button"
private const val CANNOT_FIND_ROOT_OF_THE_PROJECT = "Cannot find root of the project."
private const val DATA_DIRECTORY_NAME = "data"

internal class CreateContextButton(
    private val project: Project
) : JButton("Create context") {
    private val htmlEngineSearchService = this.project.service<HtmlEngineSearchService>()
    private val templateService = project.service<TemplateService>()
    private val templateParserService = project.service<TemplateParserService>()
    private val templateTypeService = project.service<TemplateTypeService>()

    init {
        addActionListener {
            thread {
                context()
                    ?.let { (root, resolver) ->
                        createSamples(root, resolver)
                    }
                    ?: Messages.showWarningDialog(project, CANNOT_FIND_ROOT_OF_THE_PROJECT, TITLE)
            }
        }
    }

    private fun context(): Pair<VirtualFile, ZwojeTemplateResolver<Any>>? {
        val rootVirtualFile = rootVirtualFile()
        val moduleTemplates = htmlEngineSearchService.getModuleTemplates()
        val templateResolvers = templateService.templateResolvers(moduleTemplates)
        val resolver = templateResolvers
            .asSequence()
            .filter { if (it is TemplateProvider) it.extension == rootVirtualFile?.extension else true }
            .distinct()
            .firstOrNull()
        return if (rootVirtualFile == null || resolver == null) null
        else rootVirtualFile to resolver
    }

    private fun createSamples(virtualFile: VirtualFile, resolver: ZwojeTemplateResolver<Any>) {
        if (virtualFile.isSupported(resolver)) {
            WriteCommandAction.runWriteCommandAction(project) {
                try {
                    val dir = VfsUtil.createDirectoryIfMissing(virtualFile.parent, DATA_DIRECTORY_NAME)
                    val fileName = "${virtualFile.nameWithoutExtension}.json"
                    var file: VirtualFile? = dir.findChild(fileName)
                    if (file == null) {
                        file = dir.createChildData(this, fileName)
                    }
                    val id = virtualFile.name
                    registerTemplateIfNecessary(resolver, id, virtualFile)
                    val content = createContent(id, resolver)
                    VfsUtil.saveText(file, content)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
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

    private fun registerTemplateIfNecessary(
        resolver: ZwojeTemplateResolver<Any>,
        id: String,
        virtualFile: VirtualFile
    ) {
        val template = templateTypeService.getTemplate(resolver.type, id, virtualFile.path)
        if (!resolver.exists(id)) {
            resolver.register(id, template)
        }
    }

    private fun rootVirtualFile(): VirtualFile? {
        var file: VirtualFile? = null
        val selectedEditor = FileEditorManager.getInstance(project).selectedTextEditor
        if (selectedEditor != null) {
            file = FileDocumentManager.getInstance().getFile(selectedEditor.document)
        }
        return file
    }
}