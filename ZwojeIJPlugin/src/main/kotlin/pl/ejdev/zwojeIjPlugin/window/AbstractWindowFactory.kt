package pl.ejdev.zwojeIjPlugin.window

import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

abstract class AbstractWindowFactory : ToolWindowFactory {
    protected val contentFactory: ContentFactory = ContentFactory.getInstance()

    protected companion object {
        internal const val LOCKED = false
    }
}