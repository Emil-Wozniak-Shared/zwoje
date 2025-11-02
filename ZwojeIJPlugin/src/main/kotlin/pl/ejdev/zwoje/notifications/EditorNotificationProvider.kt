package pl.ejdev.zwoje.notifications

import com.intellij.openapi.actionSystem.ActionUiKind.Companion.POPUP
import com.intellij.openapi.actionSystem.AnActionEvent.createEvent
import com.intellij.openapi.actionSystem.DataContext.EMPTY_CONTEXT
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import pl.ejdev.zwoje.actions.GeneratePdfAction
import pl.ejdev.zwoje.actions.PreviewAction
import java.util.function.Function
import javax.swing.JComponent

class ZwojeEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? =
        if (file.fileType.name != "HTML") null
        else Function { _: FileEditor ->
            val panel = EditorNotificationPanel()
            panel.text = "Preview or generate PDF"
            panel.createActionLabel("Preview in Zwoje") {
                val event = createEvent(PreviewAction(), EMPTY_CONTEXT, null, "", POPUP, null)
                PreviewAction().actionPerformed(event)
            }
            panel.createActionLabel("Generate PDF") {
                val event = createEvent(GeneratePdfAction(), EMPTY_CONTEXT, null, "", POPUP, null)
                GeneratePdfAction().actionPerformed(event)
            }
            panel
        }
}
