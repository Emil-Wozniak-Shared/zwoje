package pl.ejdev.zwoje.actions

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages.*
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.io.IOException
import pl.ejdev.zwoje.service.OpenHtmlEngineCompileService
import pl.ejdev.zwoje.service.TemplateResolverService
import pl.ejdev.zwoje.utils.nameWithExtension
import java.io.File
import java.io.FileOutputStream

private const val ZWOJE_CREATE_PDF = "Zwoje Generate PDF"

class GeneratePdfAction() : AnFileAction() {
    override val actionName: String = ZWOJE_CREATE_PDF

    override fun onReady(project: Project, file: VirtualFile, editor: Editor) {
        FileDocumentManager.getInstance().saveDocument(editor.document)
        createPdf(project, file, editor.document.text)
    }

    private fun createPdf(project: Project, file: VirtualFile, content: String) {
        val templateResolverService = project.service<TemplateResolverService>()
        val openHtmlEngineCompileService = project.service<OpenHtmlEngineCompileService>()
        val resolver = templateResolverService.findFor(file) ?: return
        openHtmlEngineCompileService.compile(resolver, file, content)
            .onSuccess {
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
                    showWarningDialog(
                        project,
                        "⚠️ Failed to create directory: ${pdfDir.absolutePath}",
                        ZWOJE_CREATE_PDF
                    )
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
}