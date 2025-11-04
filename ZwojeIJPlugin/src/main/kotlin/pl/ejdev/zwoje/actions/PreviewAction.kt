package pl.ejdev.zwoje.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.*
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages.showErrorDialog
import com.intellij.openapi.ui.Messages.showWarningDialog
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import pl.ejdev.zwoje.service.OpenHtmlEngineCompileService
import pl.ejdev.zwoje.service.TemplateResolverService
import pl.ejdev.zwoje.utils.nameWithExtension
import pl.ejdev.zwoje.window.ZWOJE_WINDOW_KEY

private const val ZWOJE_PREVIEW = "Zwoje Preview"
private const val NO_ACTIVE_PROJECT_FOUND = "No active project found."
private const val PREVIEW_WINDOW_NOT_INITIALIZED = "Preview window not initialized."

private val supportedTypes = listOf("html", "htm")

class PreviewAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PROJECT) ?: ProjectManager.getInstance().openProjects.firstOrNull()
        if (project == null) {
            showErrorDialog(NO_ACTIVE_PROJECT_FOUND, ZWOJE_PREVIEW)
            return
        }
        val editor = event.getData(EDITOR) ?: FileEditorManager.getInstance(project).selectedTextEditor
        val file = virtualFile(event, project)
        if (editor == null || file == null || file.extension !in supportedTypes) {
            return
        }
        FileDocumentManager.getInstance().saveDocument(editor.document)
        showPreview(project, file, editor.document.text)
    }

    private fun virtualFile(event: AnActionEvent, project: Project): VirtualFile? {
        var file = event.getData(VIRTUAL_FILE)
        if (file == null) {
            val selectedEditor = FileEditorManager.getInstance(project).selectedTextEditor
            if (selectedEditor != null) {
                file = FileDocumentManager.getInstance().getFile(selectedEditor.document)
            }
        }
        return file
    }

    private fun showPreview(project: Project, file: VirtualFile, content: String) {
        val templateResolverService = project.service<TemplateResolverService>()
        val openHtmlEngineCompileService = project.service<OpenHtmlEngineCompileService>()
        val zwojeWindow = project.getUserData(ZWOJE_WINDOW_KEY)
        val resolver = templateResolverService.findFor(file) ?: return
        val bytes = openHtmlEngineCompileService.compile(resolver, file, content)
        if (zwojeWindow != null && bytes.isSuccess) {
            zwojeWindow.viewer.loadPdfBytes(bytes.getOrNull()!!, file.nameWithExtension("pdf"))
            ToolWindowManager.getInstance(project).getToolWindow(ZWOJE_PREVIEW)?.show()
        } else {
            showWarningDialog(project, PREVIEW_WINDOW_NOT_INITIALIZED, ZWOJE_PREVIEW)
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(VIRTUAL_FILE)
        val isHtmlFile = file?.extension?.lowercase() in supportedTypes
        e.presentation.isEnabledAndVisible = isHtmlFile
    }
}
