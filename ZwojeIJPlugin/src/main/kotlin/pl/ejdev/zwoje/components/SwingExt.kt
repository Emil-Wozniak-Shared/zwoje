package pl.ejdev.zwoje.components

import java.awt.Component
import java.awt.LayoutManager
import java.awt.Point
import java.awt.event.ActionListener
import javax.swing.*

@DslMarker
annotation class SwingDsl

@SwingDsl
fun toolBar(
    floatable: Boolean = false,
    ctx: JToolbarSwingKotlin.() -> Unit
) = JToolbarSwingKotlin().apply { ctx(this) }.build().apply {
    isFloatable = floatable
}

@SwingDsl
fun button(
    icon: Icon, action: ActionListener, ctx: JButton.() -> Unit = {}
): JButton =
    JButton(null, icon).apply(ctx).apply {
        addActionListener(action)
    }

enum class SplitPaneOrientation(val value: Int) {
    VERTICAL_SPLIT(0),
    HORIZONTAL_SPLIT(1)
}

@SwingDsl
fun splitPane(
    orientation: SplitPaneOrientation = SplitPaneOrientation.HORIZONTAL_SPLIT,
    ctx: JSplitPaneKotlin.() -> Unit
) = JSplitPaneKotlin(orientation).apply(ctx)

@SwingDsl
fun scrollPane(
    view: Component,
    vsbPolicy: Int,
    hsbPolicy: Int,
    ctx: JScrollPane.() -> Unit
) = JScrollPane(view, vsbPolicy, hsbPolicy).apply(ctx)

@SwingDsl
fun menuBar(ctx: JMenuBarKotlin.() -> Unit = {}): JMenuBar = JMenuBarKotlin().apply(ctx).build()

@SwingDsl
fun textField(text: String, columns: Int = 0, ctx: JTextField.() -> Unit = {}): JTextField =
    JTextField(text, columns).apply {
        ctx()
    }

class JSplitPaneKotlin(
    orientation: SplitPaneOrientation
) : JSplitPane(orientation.value) {
    @SwingDsl
    fun left(component: Component) = this.setLeftComponent(component)

    @SwingDsl
    fun right(component: Component) = this.setRightComponent(component)
}


class JMenuKotlin(text: String) {
    private val menu = JMenu(text)

    @SwingDsl
    fun action(action: Action): JMenuItem = this.menu.add(action)

    @SwingDsl
    fun separator() = this.menu.addSeparator()

    @SwingDsl
    fun menu(text: String, ctx: JMenuKotlin.() -> Unit): JMenu = JMenuKotlin(text)
        .apply(ctx)
        .build()
        .apply { menu.add(this) }

    fun build() = menu
}

class JMenuBarKotlin {
    private val menuBar = JMenuBar()

    @SwingDsl
    fun menu(text: String, ctx: JMenuKotlin.() -> Unit): JMenu = JMenuKotlin(text)
        .apply(ctx)
        .build()
        .also(menuBar::add)

    fun build() = menuBar
}

@SwingDsl
fun panel(
    layout: LayoutManager,
    ctx: JPanelKotlin.() -> Unit
) = JPanelKotlin(layout).apply(ctx).build()

class JPanelKotlin(
    private val layout: LayoutManager
) {
    enum class BorderLayoutKt(val value: String) {
        NORTH("North"),
        SOUTH("South"),
        EAST("East"),
        WEST("West"),
        CENTER("Center")
    }

    private val panel = JPanel(layout)

    val width = panel.getWidth()
    val height = panel.getHeight()
    val location: Point = panel.location

    infix fun JComponent.constraint(layout: BorderLayoutKt) = this to layout

    fun visible(value: Boolean = true) {
        panel.isVisible = value
    }

    fun setLocation(x: Int, y: Int) = panel.setLocation(x, y)

    fun build() = panel

    operator fun JToolBar.unaryPlus() {
        panel.add(this)
    }

    operator fun Pair<JComponent, BorderLayoutKt>.unaryPlus() {
        panel.add(this.first, this.second.value)
    }
}

class JToolbarSwingKotlin : JToolBar() {
    val toolbar = JToolBar()

    operator fun Action.unaryPlus() {
        toolbar.add(this)
    }

    operator fun JButton.unaryPlus() {
        toolbar.add(this)
    }

    operator fun JComponent.unaryPlus() {
        toolbar.add(this)
    }

    operator fun Component.unaryPlus() {
        toolbar.add(this)
    }

    @SwingDsl
    fun createHorizontalGlue(): Component = this.add(Box.createHorizontalGlue())

    fun build() = toolbar
}

