package pl.ejdev.zwoje.components

import com.intellij.icons.AllIcons.Actions.Play_first
import com.intellij.icons.AllIcons.Actions.Play_last
import com.intellij.icons.AllIcons.General.*
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.sun.pdfview.*
import com.sun.pdfview.action.GoToAction
import pl.ejdev.zwoje.components.JPanelKotlin.BorderLayoutKt.*
import pl.ejdev.zwoje.components.SplitPaneOrientation.HORIZONTAL_SPLIT
import pl.ejdev.zwoje.components.actions.*
import pl.ejdev.zwoje.components.threads.PagePreparer
import pl.ejdev.zwoje.components.threads.PrintThread
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.print.Book
import java.awt.print.PageFormat
import java.awt.print.PrinterJob
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel.MapMode.READ_ONLY
import javax.swing.*
import javax.swing.JSplitPane.DIVIDER_LOCATION_PROPERTY
import javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
import javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
import javax.swing.SwingUtilities.invokeAndWait
import javax.swing.SwingUtilities.isEventDispatchThread
import javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import kotlin.concurrent.thread

const val TIMEOUT: Long = 500

private const val READ_MODE = "r"

internal class PDFViewer(
    private val project: Project,
    private val useThumbs: Boolean = true,
) : KeyListener, TreeSelectionListener, PageChangeListener {
    private var documentName: String? = null
    private var split: JSplitPane? = null
    private var thumbScroll: JScrollPane? = null
    private var thumbs: ThumbPanel? = null
    private var fullScreenButton: JToggleButton? = null
    private var pageField: JTextField? = null
    private var outline: OutlineNode? = null
    private var doThumb: Boolean = useThumbs
    private var pagePrep: PagePreparer? = null
    private var dialog: JDialog? = null
    private var pageFormat: PageFormat? = PrinterJob.getPrinterJob().defaultPage()
    private val thumbAction: ThumbAction = ThumbAction { doThumbs(it) }
    var currentSelectedFile: PDFFile? = null
    var currentPage: Int = -1
    var page: PagePanel = PagePanel().also { it.addKeyListener(this) }
    var pagePanel: PagePanel? = null
    var fullScreen: FullScreenWindow? = null

    private var pageBuilder: PageBuilder = PageBuilder(this)
    private var previousDirChoice: File? = null

    val content = panel(BorderLayout()) {
        if (doThumb) {
            thumbs = ThumbPanel(null)
            thumbScroll = scrollPane(thumbs!!, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER) {
            }
            split = splitPane(HORIZONTAL_SPLIT) {
                addPropertyChangeListener(DIVIDER_LOCATION_PROPERTY, thumbAction)
                setOneTouchExpandable(true)
                left(thumbScroll!!)
                right(page)
            }
            +(split!! constraint CENTER)
        } else {
            +(page constraint CENTER)
        }
        +(toolBar {
            +button(icon = Play_first, action = { firstPage() })
            +button(icon = ArrowLeft, action = { previousPage() })
            +textField("-", 3) {
                maximumSize = Dimension(45, 32)
                isEnabled = false
                pageField = this
                addActionListener { pageTyped() }
            }
            +button(icon = ArrowRight, action = { nextPage() })
            +button(icon = Play_last, action = { lastPage() })
            createHorizontalGlue()
            +button(icon = Print, action = { printPage() })
        } constraint NORTH)
        +(menuBar {
            menu("File") {
                action(openAction { doOpen() })
                action(closeAction { doClose() })
                separator()
                action(pageSetupAction { pageSetup() })
                action(printAction { printPage() })
                separator()
            }
        } constraint SOUTH)
        setEnabling()
        setLocation(x = (screenSize.width - width) / 2, y = (screenSize.height - height) / 2)
        if (isEventDispatchThread()) visible()
        else runCatching { invokeAndWait { visible() } }
    }

    fun loadPdfBytes(bytes: ByteArray, name: String = "Preview.pdf") {
        try {
            val buffer = ByteBuffer.wrap(bytes)
            openPDFByteBuffer(buffer, name, name)
        } catch (e: IOException) {
            openError("Failed to open PDF bytes: ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            openError("Unexpected error while opening PDF: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun gotoPage(pagenum: Int) {
        var pageNum = pagenum
        when {
            pageNum < 0 -> pageNum = 0
            pageNum >= currentSelectedFile!!.numPages -> pageNum = currentSelectedFile!!.numPages - 1
        }
        forceGotoPage(pageNum)
    }

    private fun forceGotoPage(pageNumber: Int) {
        var pageNum = pageNumber
        when {
            pageNum <= 0 -> pageNum = 0
            pageNum >= currentSelectedFile!!.numPages -> pageNum = currentSelectedFile!!.numPages - 1
        }
        currentPage = pageNum
        pageField!!.text = (currentPage + 1).toString()

        val page = currentSelectedFile!!.getPage(pageNum + 1)
        when {
            pagePanel != null -> pagePanel?.run {
                showPage(page)
                requestFocus()
            }

            else -> {
                this.page.showPage(page)
                this.page.requestFocus()
            }
        }
        if (doThumb) {
            thumbs!!.pageShown(pageNum)
        }
        if (pagePrep != null) {
            pagePrep!!.quit()
        }
        pagePrep = PagePreparer(this, pageNum)
        pagePrep!!.start()

        setEnabling()
    }

    private fun setEnabling() {
        val fileAvailable = currentSelectedFile != null
        pageField!!.setEnabled(fileAvailable)
    }

    private fun openFile(file: File) {
        val randomAccessFile = RandomAccessFile(file, READ_MODE)
        val channel = randomAccessFile.getChannel()
        val buffer: ByteBuffer = channel.map(READ_ONLY, 0, channel.size())
        openPDFByteBuffer(buffer, file.path, file.getName())
    }

    private fun openPDFByteBuffer(buffer: ByteBuffer, path: String, name: String) {
        val newFile: PDFFile? = try {
            PDFFile(buffer)
        } catch (_: IOException) {
            openError("$path doesn't appear to be a PDF file.")
            null
        }
        doClose()

        this.currentSelectedFile = newFile
        documentName = name
        if (doThumb) {
            thumbs = ThumbPanel(currentSelectedFile)
            thumbs!!.addPageChangeListener(this)
            thumbScroll!!.getViewport().setView(thumbs)
            thumbScroll!!.getViewport().setBackground(JBColor.GRAY)
        }

        setEnabling()
        forceGotoPage(0)
        try {
            outline = currentSelectedFile!!.getOutline()
        } catch (_: IOException) {
        }
        if (outline != null) {
            when {
                outline!!.childCount > 0 -> {
                    dialog!!.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
                    dialog!!.location = this.content.location
                    val tree = JTree(outline)
                    tree.setRootVisible(false)
                    tree.addTreeSelectionListener(this)
                    val jsp = JScrollPane(tree)
                    dialog!!.contentPane.add(jsp)
                    dialog!!.pack()
                    dialog!!.isVisible = true
                }

                else -> if (dialog != null) {
                    dialog!!.isVisible = false
                    dialog = null
                }
            }
        }
    }

    private fun openError(message: String?) = errorOpeningFile(split, message)

    private fun doOpen() = try {
        PdfChooser.choosePdf(project = project) {
            val selectedFile = File(it.path)
            previousDirChoice = selectedFile
            openFile(selectedFile)
        }
    } catch (e: Exception) {
        unavailableFile(split)
        e.printStackTrace()
    }

    private fun pageSetup() {
        pageFormat = PrinterJob.getPrinterJob().pageDialog(pageFormat)
    }

    private fun printPage() {
        val printerJob = PrinterJob.getPrinterJob()
        printerJob.jobName = documentName
        val book = Book()
        val pages = PDFPrintPage(currentSelectedFile)
        book.append(pages, pageFormat, currentSelectedFile!!.numPages)
        printerJob.setPageable(book)
        if (printerJob.printDialog()) {
            PrintThread(this, pages, printerJob).start()
        }
    }

    private fun doClose() {
        if (thumbs != null) {
            thumbs!!.stop()
        }
        if (dialog != null) {
            dialog!!.isVisible = false
            dialog = null
        }
        if (doThumb) {
            thumbs = ThumbPanel(null)
            thumbScroll!!.getViewport().setView(thumbs)
        }

        setFullScreenMode(full = false, force = false)
        page.showPage(null)
        currentSelectedFile = null
        setEnabling()
    }

    private fun doThumbs(show: Boolean) =
        if (show) split!!.setDividerLocation(
            thumbs!!.getPreferredSize().width + thumbScroll!!.getVerticalScrollBar().getWidth() + 4
        )
        else split!!.setDividerLocation(0)

    private fun nextPage() = gotoPage(currentPage + 1)

    private fun previousPage() = gotoPage(currentPage - 1)

    private fun firstPage() = gotoPage(0)

    private fun lastPage() = gotoPage(currentSelectedFile!!.numPages - 1)

    private fun pageTyped() {
        var pageNum = -1
        try {
            pageNum = pageField!!.getText().toInt() - 1
        } catch (_: NumberFormatException) {
        }
        if (pageNum >= currentSelectedFile!!.numPages) {
            pageNum = currentSelectedFile!!.numPages - 1
        }
        if (pageNum >= 0) {
            if (pageNum != currentPage) {
                gotoPage(pageNum)
            }
        } else {
            pageField!!.text = currentPage.toString()
        }
    }

    private fun setFullScreenMode(full: Boolean, force: Boolean) {
        currentPage = -1
        if (full && fullScreen == null) {
            thread(name = "${this::javaClass.name}.setFullScreenMode") {
                PerformFullScreenMode(this, force)
            }.start()
            fullScreenButton!!.setSelected(true)
        } else if (!full && fullScreen != null) {
            fullScreen!!.close()
            pagePanel = null
            fullScreen = null
            gotoPage(currentPage)
            fullScreenButton!!.setSelected(false)
        }
    }

    override fun keyPressed(evt: KeyEvent) {
        val code = evt.getKeyCode()
        when (code) {
            KeyEvent.VK_LEFT -> previousPage()
            KeyEvent.VK_RIGHT -> nextPage()
            KeyEvent.VK_UP -> previousPage()
            KeyEvent.VK_DOWN -> nextPage()
            KeyEvent.VK_HOME -> firstPage()
            KeyEvent.VK_END -> lastPage()
            KeyEvent.VK_PAGE_UP -> previousPage()
            KeyEvent.VK_PAGE_DOWN -> nextPage()
            KeyEvent.VK_SPACE -> nextPage()
            KeyEvent.VK_ESCAPE -> setFullScreenMode(full = false, force = false)
        }
    }

    override fun keyReleased(evt: KeyEvent?) {
    }

    override fun keyTyped(evt: KeyEvent) {
        val key = evt.getKeyChar()
        if (key in '0'..'9') {
            val value = key.code - '0'.code
            pageBuilder.keyTyped(value)
        }
    }

    override fun valueChanged(event: TreeSelectionEvent) {
        if (event.isAddedPath) {
            val node = event.path.lastPathComponent as OutlineNode? ?: return
            try {
                val action = node.action ?: return
                if (action is GoToAction) {
                    val destination = action.destination ?: return
                    val page = destination.page ?: return
                    val pageNum = currentSelectedFile!!.getPageNumber(page)
                    if (pageNum >= 0) {
                        gotoPage(pageNum)
                    }
                }
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        }
    }
}