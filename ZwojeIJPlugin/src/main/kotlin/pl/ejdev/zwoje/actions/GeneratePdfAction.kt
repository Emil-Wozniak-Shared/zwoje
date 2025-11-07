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
import com.intellij.openapi.ui.Messages.showInfoMessage
import com.intellij.openapi.ui.Messages.showWarningDialog
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.io.IOException
import pl.ejdev.zwoje.service.OpenHtmlEngineCompileService
import pl.ejdev.zwoje.service.TemplateResolverService
import pl.ejdev.zwoje.utils.nameWithExtension
import java.io.File
import java.io.FileOutputStream

private const val ZWOJE_CREATE_PDF = "Zwoje Generate PDF"
private const val NO_ACTIVE_PROJECT_FOUND = "No active project found."
private const val PREVIEW_WINDOW_NOT_INITIALIZED = "Preview window not initialized."

private val supportedTypes = listOf("html", "htm")

class GeneratePdfAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.getData(PROJECT) ?: ProjectManager.getInstance().openProjects.firstOrNull()
        if (project == null) {
            showErrorDialog(NO_ACTIVE_PROJECT_FOUND, ZWOJE_CREATE_PDF)
            return
        }
        val editor = event.getData(EDITOR) ?: FileEditorManager.getInstance(project).selectedTextEditor
        val file = virtualFile(event, project)
        if (editor == null || file == null || file.extension !in supportedTypes) {
            return
        }
        FileDocumentManager.getInstance().saveDocument(editor.document)
        createPdf(project, file, editor.document.text)
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

    private fun createPdf(project: Project, file: VirtualFile, content: String) {
        val templateResolverService = project.service<TemplateResolverService>()
        val openHtmlEngineCompileService = project.service<OpenHtmlEngineCompileService>()
        val resolver = templateResolverService.findFor(file) ?: return
        openHtmlEngineCompileService.compile(resolver, file, content)
            .onSuccess {
                System.getenv()
                val fileName = file.nameWithExtension("pdf")
                savePdfToHomeDir(project, fileName, it)

            }
            .onFailure {
                showErrorDialog(project, "${it.message}", ZWOJE_CREATE_PDF)
            }
    }

    private fun savePdfToHomeDir(project: Project, fileName: String, pdfBytes: ByteArray) {
        val homeDir = System.getProperty("user.home")
        val pdfDir = File(homeDir, "Documents/PDFs") // example subfolder

        try {
            // Create directory if it doesn't exist
            if (!pdfDir.exists()) {
                val created = pdfDir.mkdirs()
                if (!created) {
                    showWarningDialog(project, "⚠️ Failed to create directory: ${pdfDir.absolutePath}", ZWOJE_CREATE_PDF)
                    return
                }
            }

            // Create file reference
            val pdfFile = File(pdfDir, fileName)

            // If file doesn’t exist, create it
            if (!pdfFile.exists()) {
                val created = pdfFile.createNewFile()
                if (!created) {
                    showWarningDialog(project, "⚠️ Could not create file: ${pdfFile.absolutePath}", ZWOJE_CREATE_PDF)
                    return
                }
            } else {
                showWarningDialog(project, "ℹ️ File already exists, it will be overwritten.", ZWOJE_CREATE_PDF)
            }

            // Write bytes to file (overwrites existing)
            FileOutputStream(pdfFile).use { it.write(pdfBytes) }

            showInfoMessage(project, "✅ PDF saved at: ${pdfFile.absolutePath}", ZWOJE_CREATE_PDF)

        } catch (e: IOException) {
            showErrorDialog(project, "❌ Error saving PDF: ${e.message}", ZWOJE_CREATE_PDF)
        }
    }

    override fun update(e: AnActionEvent) {
        val file = e.getData(VIRTUAL_FILE)
        val isHtmlFile = file?.extension?.lowercase() in supportedTypes
        e.presentation.isEnabledAndVisible = isHtmlFile
    }
}