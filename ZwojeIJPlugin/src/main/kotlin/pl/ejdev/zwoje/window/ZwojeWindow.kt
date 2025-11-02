package pl.ejdev.zwoje.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import pl.ejdev.zwoje.components.PDFViewer

class ZwojeWindow(
    private val toolWindow: ToolWindow,
    private val project: Project,
) : Window(project) {
    internal val viewer = PDFViewer(project)

    val content: DialogPanel = panel {
        row {
            cell(viewer.content).align(Align.FILL)
        }
    }
}