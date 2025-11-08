package pl.ejdev.zwoje.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.*
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.Messages.showErrorDialog
import com.intellij.openapi.vfs.VirtualFile
import pl.ejdev.zwoje.service.ZwojeFileSupportService

private const val NO_ACTIVE_PROJECT_FOUND = "No active project found."

abstract class AnFileAction: AnAction() {
    abstract val actionName: String

    abstract fun onReady(project: Project, file: VirtualFile, editor: Editor)

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PROJECT) ?: ProjectManager.getInstance().openProjects.firstOrNull()
        if (project == null) {
            showErrorDialog(NO_ACTIVE_PROJECT_FOUND, actionName)
            return
        }
        val editor = event.getData(EDITOR) ?: FileEditorManager.getInstance(project).selectedTextEditor
        val file = virtualFile(event, project)
        val supportService = project.service<ZwojeFileSupportService>()
        if (editor == null || file == null || !supportService.isSupported(file.extension ?: "")) {
            return
        }
        onReady(project, file, editor)
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

}