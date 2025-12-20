package pl.ejdev.zwoje.actions

import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages.showErrorDialog
import com.intellij.openapi.ui.Messages.showWarningDialog
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import pl.ejdev.zwoje.service.PdfEngineCompileService
import pl.ejdev.zwoje.service.TemplateResolverService
import pl.ejdev.zwoje.utils.nameWithExtension
import pl.ejdev.zwoje.window.ZWOJE_WINDOW_KEY

private const val ZWOJE_PREVIEW = "Zwoje Preview"
private const val PREVIEW_WINDOW_NOT_INITIALIZED = "Preview window not initialized."

class PreviewAction() : AnFileAction() {
    override val actionName: String = ZWOJE_PREVIEW

    override fun onReady(project: Project, file: VirtualFile, editor: Editor) {
        FileDocumentManager.getInstance().saveDocument(editor.document)
        showPreview(project, file, editor.document.text)
    }

    private fun showPreview(project: Project, file: VirtualFile, content: String) {
        val templateResolverService = project.service<TemplateResolverService>()
        val pdfEngineCompileService = project.service<PdfEngineCompileService>()
        val zwojeWindow = project.getUserData(ZWOJE_WINDOW_KEY)
        val resolver = templateResolverService.findFor(file) ?: return
        pdfEngineCompileService.compile(resolver, file, content)
            .onSuccess {
                if (zwojeWindow != null) {
                    zwojeWindow.viewer.loadPdfBytes(it, file.nameWithExtension("pdf"))
                    ToolWindowManager.getInstance(project).getToolWindow(ZWOJE_PREVIEW)?.show()
                } else {
                    showWarningDialog(project, PREVIEW_WINDOW_NOT_INITIALIZED, ZWOJE_PREVIEW)
                }
            }
            .onFailure {
                showErrorDialog(project, "${it.message}", ZWOJE_PREVIEW)
            }
    }
}
