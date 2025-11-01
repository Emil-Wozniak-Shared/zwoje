package pl.ejdev.zwoje.components

import java.awt.print.PrinterException
import javax.swing.JOptionPane.ERROR_MESSAGE
import javax.swing.JOptionPane.showMessageDialog
import javax.swing.JSplitPane

private const val ERROR_OPENING_FILE = "Error opening file"
private const val ERROR_OPENING_DIRECTORY = "Error opening directory"
private const val PRINT_ABORTED = "Print Aborted"

internal fun unavailableFile(split: JSplitPane?) = showMessageDialog(
    split,
    "Opening files from your local " +
            "disk is not available\nfrom the " +
            "Java Web Start version of this " +
            "program.\n",
    ERROR_OPENING_DIRECTORY,
    ERROR_MESSAGE
)

internal fun printAborted(pdfViewer: PDFViewer, pe: PrinterException) = showMessageDialog(
    pdfViewer.content,
    "Printing Error: ${pe.message}",
    PRINT_ABORTED,
    ERROR_MESSAGE
)

internal fun errorOpeningFile(split: JSplitPane?, message: String?) =
    showMessageDialog(split, message, ERROR_OPENING_FILE, ERROR_MESSAGE)