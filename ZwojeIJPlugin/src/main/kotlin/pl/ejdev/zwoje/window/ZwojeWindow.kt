package pl.ejdev.zwoje.window

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.dsl.builder.panel
import pl.ejdev.zwoje.components.pdfViewer
import pl.ejdev.zwoje.service.HtmlEngineSearchService

class ZwojeWindow(
    private val toolWindow: ToolWindow,
    private val project: Project,
) : Window(project) {
    private val htmlEngineSearchService = project.service<HtmlEngineSearchService>()
    init {
        htmlEngineSearchService.templateResolvers()
    }
    val content: DialogPanel = panel {
        row {
            pdfViewer(project)
        }
    }
}