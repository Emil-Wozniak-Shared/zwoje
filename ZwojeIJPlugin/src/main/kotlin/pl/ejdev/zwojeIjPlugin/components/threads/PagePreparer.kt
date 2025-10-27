package pl.ejdev.zwojeIjPlugin.components.threads

import pl.ejdev.zwojeIjPlugin.components.PDFViewer
import java.awt.Dimension
import java.awt.geom.Rectangle2D

internal class PagePreparer(
    private val pDFViewer: PDFViewer,
    waitForPage: Int
) : Thread() {
    var waitForPage: Int
    var prepPage: Int
    init {
        setDaemon(true)
        this.waitForPage = waitForPage
        this.prepPage = waitForPage + 1
    }

    fun quit() {
        this@PagePreparer.waitForPage = -1
    }

    override fun run() {
        var size: Dimension? = null
        var clip: Rectangle2D? = null

        if (pDFViewer.pagePanel != null) {
            pDFViewer.pagePanel!!.waitForCurrentPage()
            size = pDFViewer.pagePanel!!.curSize
            clip = pDFViewer.pagePanel!!.curClip
        } else {
            pDFViewer.page.waitForCurrentPage()
            size = pDFViewer.page.curSize
            clip = pDFViewer.page.curClip
        }

        if (this@PagePreparer.waitForPage == pDFViewer.currentPage) {
            val pdfPage = pDFViewer.currentSelectedFile!!.getPage(prepPage + 1, true)
            if (pdfPage != null && this@PagePreparer.waitForPage == pDFViewer.currentPage) {
                pdfPage.getImage(size!!.width, size.height, clip, null, true, true)
            }
        }
    }
}