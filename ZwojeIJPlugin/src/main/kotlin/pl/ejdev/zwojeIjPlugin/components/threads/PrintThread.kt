package pl.ejdev.zwojeIjPlugin.components.threads

import com.sun.pdfview.PDFPrintPage
import pl.ejdev.zwojeIjPlugin.components.PDFViewer
import pl.ejdev.zwojeIjPlugin.components.printAborted
import java.awt.print.PrinterException
import java.awt.print.PrinterJob

internal class PrintThread(
    private val pdfViewer: PDFViewer,
    private val printPage: PDFPrintPage,
    private val printerJob: PrinterJob
) : Thread() {
    override fun run() {
        try {
            printPage.show(printerJob)
            printerJob.print()
        } catch (pe: PrinterException) {
            printAborted(pdfViewer, pe)
        }
        printPage.hide()
    }
}