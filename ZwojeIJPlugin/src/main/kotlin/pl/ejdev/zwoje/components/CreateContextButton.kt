package pl.ejdev.zwoje.components

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages.showErrorDialog
import com.intellij.openapi.ui.Messages.showWarningDialog
import com.intellij.openapi.vfs.VirtualFile
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver
import pl.ejdev.zwoje.service.TemplateResolverService
import pl.ejdev.zwoje.service.ZwojeSampleService
import pl.ejdev.zwoje.utils.isSupported
import javax.swing.JButton
import kotlin.concurrent.thread

private const val TITLE = "Samples Button"
private const val NO_SUITABLE_TEMPLATE_RESOLVER = "No suitable template resolver."
private const val PLEASE_OPEN_TEMPLATE_FILE = "Please open template file."
private const val SELECTED_FILE_IS_NOT_SUPPORTED = "Selected file is not supported."

internal class CreateContextButton(
    private val project: Project
) : JButton("Create context") {
    private val templateResolverService = project.service<TemplateResolverService>()
    private val zwojeSampleService = project.service<ZwojeSampleService>()

    init {
        addActionListener {
            thread {
                findContext()?.let { (root, resolver) ->
                    createZwojeContext(root, resolver)
                }
            }
        }
    }

    private fun findContext(): Pair<VirtualFile, ZwojeTemplateResolver<Any>>? {
        val rootVirtualFile = findRootVirtualFile()
        if (rootVirtualFile == null) {
            showWarningDialog(project, PLEASE_OPEN_TEMPLATE_FILE, TITLE)
            return null
        }
        val resolver = templateResolverService.findFor(rootVirtualFile)
        if (resolver == null) {
            showWarningDialog(project, NO_SUITABLE_TEMPLATE_RESOLVER, TITLE)
            return null
        }
        return rootVirtualFile to resolver
    }

    private fun createZwojeContext(virtualFile: VirtualFile, resolver: ZwojeTemplateResolver<Any>) {
        if (virtualFile.isSupported(resolver)) {
            createFiles(virtualFile, resolver)
        } else {
            showWarningDialog(project, SELECTED_FILE_IS_NOT_SUPPORTED, TITLE)
        }
    }

    private fun createFiles(virtualFile: VirtualFile, resolver: ZwojeTemplateResolver<Any>) =
        WriteCommandAction.runWriteCommandAction(project) {
            when (val result = zwojeSampleService.createSample(virtualFile, resolver)) {
                is ZwojeSampleService.CreateSampleResult.OK -> {
                }

                is ZwojeSampleService.CreateSampleResult.FileExists -> {
                    showWarningDialog(project, "Sample file for ${virtualFile.name} already exists.", TITLE)
                }

                is ZwojeSampleService.CreateSampleResult.Error -> {
                    showErrorDialog(project, "Sample file failed ${result.message}.", TITLE)
                }
            }
        }

    private fun findRootVirtualFile(): VirtualFile? {
        var file: VirtualFile? = null
        val selectedEditor = FileEditorManager.getInstance(project).selectedTextEditor
        if (selectedEditor != null) {
            file = FileDocumentManager.getInstance().getFile(selectedEditor.document)
        }
        return file
    }
}