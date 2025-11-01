package pl.ejdev.zwoje.components.actions

import com.intellij.icons.AllIcons
import java.awt.event.ActionEvent
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.AbstractAction
import javax.swing.Action

private const val OPEN = "Open..."
private const val PAGE_SETUP = "Page setup..."
private const val PRINT = "Print..."
private const val CLOSE = "Close"
private const val SHOW_THUMBNAILS = "Show thumbnails"
private const val HIDE_THUMBNAILS = "Hide thumbnails"

/** FILE MENU */
fun openAction(action: () -> Unit): Action = object : AbstractAction(OPEN) {
    override fun isEnabled(): Boolean = true
    override fun actionPerformed(evt: ActionEvent?) = action()
}

fun pageSetupAction(action: () -> Unit): Action = object : AbstractAction(PAGE_SETUP) {
    override fun isEnabled(): Boolean = true
    override fun actionPerformed(evt: ActionEvent?) = action()
}

fun printAction(action: () -> Unit): Action = object : AbstractAction(PRINT, AllIcons.General.Print) {
    override fun isEnabled(): Boolean = true
    override fun actionPerformed(evt: ActionEvent?) = action()
}

fun closeAction(action: () -> Unit): Action = object : AbstractAction(CLOSE) {
    override fun isEnabled(): Boolean = true
    override fun actionPerformed(evt: ActionEvent?) = action()
}


class ThumbAction(
    private val action: (open: Boolean) -> Unit
) : AbstractAction(), PropertyChangeListener {
    var isOpen: Boolean = true

    override fun propertyChange(evt: PropertyChangeEvent) {
        val value = (evt.newValue as Int)
        if (value <= 1) {
            isOpen = false
            putValue(ACTION_COMMAND_KEY, SHOW_THUMBNAILS)
            putValue(NAME, SHOW_THUMBNAILS)
        } else {
            isOpen = true
            putValue(ACTION_COMMAND_KEY, HIDE_THUMBNAILS)
            putValue(NAME, HIDE_THUMBNAILS)
        }
    }

    override fun actionPerformed(evt: ActionEvent?) {
        action(!isOpen)
    }
}
