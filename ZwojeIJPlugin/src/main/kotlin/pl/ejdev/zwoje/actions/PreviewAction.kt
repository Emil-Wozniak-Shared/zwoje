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
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.service.HtmlEngineSearchService
import pl.ejdev.zwoje.service.OpenHtmlEngineCompileService
import pl.ejdev.zwoje.service.OpenHtmlEngineCompileService.IJTemplateInputData
import pl.ejdev.zwoje.service.TemplateService
import pl.ejdev.zwoje.window.ZwojeWindowFactory.Companion.ZWOJE_WINDOW_KEY

class PreviewAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PROJECT) ?: ProjectManager.getInstance().openProjects.firstOrNull()
        if (project == null) {
            Messages.showErrorDialog("No active project found.", "Zwoje Preview")
            return
        }
        val editor = event.getData(EDITOR) ?: FileEditorManager.getInstance(project).selectedTextEditor
        val file = virtualFile(event, project)

        if (editor == null || file == null || file.extension !in supportedTypes) {
            return
        }

        FileDocumentManager.getInstance().saveDocument(editor.document)
        showPreview(project, file)
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

    private fun showPreview(project: Project, file: VirtualFile) {
        val htmlEngineSearchService = project.service<HtmlEngineSearchService>()
        val templateService = project.service<TemplateService>()
        val openHtmlEngineCompileService = project.service<OpenHtmlEngineCompileService>()
        val moduleTemplates = htmlEngineSearchService.getModuleTemplates()
        val templateResolvers = templateService.templateResolvers(moduleTemplates)
        val resolver = templateResolvers
            .asSequence()
            .filter { if (it is TemplateProvider) it.extension == file.extension else true }
            .distinct()
            .firstOrNull()
            ?: return

        val input = IJTemplateInputData(listOf<Any>())
        val nameWithoutExtension = file.nameWithoutExtension
        val bytes = openHtmlEngineCompileService.compile(resolver, file.name, input, file.path)

        val zwojeWindow = project.getUserData(ZWOJE_WINDOW_KEY)
        if (zwojeWindow != null) {
            zwojeWindow.viewer.loadPdfBytes(bytes, "$nameWithoutExtension.pdf")
            ToolWindowManager.getInstance(project).getToolWindow("Zwoje Preview")?.show()
        } else {
            Messages.showWarningDialog(project, "Preview window not initialized.", "Zwoje Preview")
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(VIRTUAL_FILE)
        val isHtmlFile = file?.extension?.lowercase() in supportedTypes
        e.presentation.isEnabledAndVisible = isHtmlFile
    }
}

private val supportedTypes = listOf("html", "htm")