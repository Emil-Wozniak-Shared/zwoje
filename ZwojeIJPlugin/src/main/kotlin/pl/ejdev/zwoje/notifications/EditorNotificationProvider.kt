package pl.ejdev.zwoje.notifications

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUiKind.Companion.POPUP
import com.intellij.openapi.actionSystem.AnActionEvent.createEvent
import com.intellij.openapi.actionSystem.DataContext.EMPTY_CONTEXT
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import pl.ejdev.zwoje.ZwojeIcon
import pl.ejdev.zwoje.actions.CreateTemplateContextAction
import pl.ejdev.zwoje.actions.GeneratePdfAction
import pl.ejdev.zwoje.actions.PreviewAction
import pl.ejdev.zwoje.service.TemplateResolverService
import pl.ejdev.zwoje.service.ZwojeFileSupportService
import pl.ejdev.zwoje.service.ZwojeSampleService
import java.util.function.Function
import javax.swing.JComponent

class ZwojeEditorNotificationProvider : EditorNotificationProvider {

    override fun collectNotificationData(
        project: Project,
        file: VirtualFile
    ): Function<in FileEditor, out JComponent?>? {
        val zwojeSampleService = project.service<ZwojeSampleService>()
        val templateResolverService = project.service<TemplateResolverService>()
        val zwojeFileSupportService = project.service<ZwojeFileSupportService>()

        return if (!zwojeFileSupportService.isSupported(file.extension ?: "")) null
        else Function { _: FileEditor ->
            val panel = EditorNotificationPanel()
            panel.text = ""
            panel.icon(ZwojeIcon.ico)
            panel.createActionLabel("Create context") {
                val action = CreateTemplateContextAction(project, zwojeSampleService, templateResolverService)
                val event = createEvent(action, EMPTY_CONTEXT, null, "", POPUP, null)
                action.actionPerformed(event)
            }
            panel.createActionLabel("Preview") {
                val action = PreviewAction()
                val event = createEvent(action, EMPTY_CONTEXT, null, "", POPUP, null)
                action.actionPerformed(event)
            }
            panel.createActionLabel("Generate PDF") {
                val action = GeneratePdfAction()
                val event = createEvent(action, EMPTY_CONTEXT, null, "", POPUP, null)
                action.actionPerformed(event)
            }
            panel
        }
    }
}
