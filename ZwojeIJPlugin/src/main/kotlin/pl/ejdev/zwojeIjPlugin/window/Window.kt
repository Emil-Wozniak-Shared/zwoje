package pl.ejdev.zwojeIjPlugin.window

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbUtil
import com.intellij.openapi.project.Project
import com.intellij.ui.components.Label
import com.intellij.ui.components.dialog
import javax.swing.JLabel

abstract class Window(
    private val project: Project,
) : DumbUtil, DumbAware {

    @Suppress("UnstableApiUsage")
    override fun mayUseIndices(): Boolean = false


    protected var engineStatusLabel: JLabel = JLabel("")

    private companion object {
        protected fun logAndDisplay(throwable: Throwable) {
            logger.error(throwable)
            println(throwable)
            dialog(
                title = "Error",
                Label("Error: ${throwable.cause}")
            )
        }

        private val logger = Logger.getInstance(Window::class.java)
    }
}