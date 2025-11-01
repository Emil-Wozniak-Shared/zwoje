package pl.ejdev.zwoje.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow

class ZwojeWindowFactory  : AbstractWindowFactory() {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val pluginWindow = window(toolWindow, project)
        val content = contentFactory.createContent(pluginWindow.content, null, LOCKED)
        toolWindow.contentManager.apply {
            addContent(content)
            setSelectedContent(content)
        }
    }

    private fun window(toolWindow: ToolWindow, project: Project) =
        ZwojeWindow(
            toolWindow = toolWindow,
            project = project
        )
}