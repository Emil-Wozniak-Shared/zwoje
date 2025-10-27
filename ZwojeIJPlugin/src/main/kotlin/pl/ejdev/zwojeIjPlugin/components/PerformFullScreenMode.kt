package pl.ejdev.zwojeIjPlugin.components

import com.sun.pdfview.FullScreenWindow
import com.sun.pdfview.PagePanel

internal class PerformFullScreenMode(
    private val pdfViewer: PDFViewer,
    private val force: Boolean
) : Runnable {
    override fun run() {
        pdfViewer.pagePanel = PagePanel()
        pdfViewer.pagePanel?.addKeyListener(pdfViewer)
        pdfViewer.page.showPage(null)
        pdfViewer.fullScreen = FullScreenWindow(pdfViewer.pagePanel, force)
        pdfViewer.gotoPage(pdfViewer.currentPage)
    }
}