package pl.ejdev.zwojeIjPlugin.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.dsl.builder.panel
import pl.ejdev.zwojeIjPlugin.components.pdfViewer

class ZwojeWindow(
    private val toolWindow: ToolWindow,
    private val project: Project,
) : Window(project) {
    val content: DialogPanel = panel {
        row {
            pdfViewer(project)
        }
    }
}