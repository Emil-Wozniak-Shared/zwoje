package pl.ejdev.zwoje.core.engine

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import java.io.ByteArrayOutputStream
import java.io.File

class OpenHtmlToPdfCompileEngine : PdfCompileEngine() {
    override fun compile(compileData: CompileData): ByteArray = ByteArrayOutputStream().use { stream ->
        val baseDir = compileData.templatePath?.let {  File(it).toURI().toString() }
        PdfRendererBuilder().run {
            useFastMode()
            withHtmlContent(compileData.html, baseDir)
            toStream(stream)
            run()
        }
        stream
    }.toByteArray()
}