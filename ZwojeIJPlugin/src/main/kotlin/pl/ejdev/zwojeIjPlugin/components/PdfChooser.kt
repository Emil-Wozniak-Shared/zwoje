package pl.ejdev.zwojeIjPlugin.components

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile


private const val PDF = "pdf"

object PdfChooser {
    fun choosePdf(project: Project, consume: (VirtualFile) -> Unit) {
        val descriptor: FileChooserDescriptor = object : FileChooserDescriptor(
            true,  // chooseFiles
            false,  // chooseFolders
            false,  // chooseJars
            false,  // chooseJarsAsFiles
            false,  // chooseJarContents
            false // chooseMultiple
        ) {
            @Deprecated("Deprecated in Java")
            override fun isFileVisible(file: VirtualFile, showHiddenFiles: Boolean): Boolean =
                file.isDirectory || PDF.equals(file.extension, ignoreCase = true)

            override fun isFileSelectable(file: VirtualFile?): Boolean =
                PDF.equals(file?.extension, ignoreCase = true)
        }

        descriptor.title = "Select a PDF File"
        descriptor.description = "Choose a PDF file to open"

        FileChooser.chooseFile(descriptor, project, null) { file ->
            println("Selected PDF: " + file!!.path)
            consume(file)
        }
    }
}
