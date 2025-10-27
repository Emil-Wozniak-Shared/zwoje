package pl.ejdev.zwoje.core.engine

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import pl.ejdev.zwoje.core.engine.PdfCompileEngine
import java.io.ByteArrayOutputStream

class OpenHtmlToPdfCompileEngine : PdfCompileEngine() {

    override fun compile(template: String): ByteArray = ByteArrayOutputStream().use { stream ->
        PdfRendererBuilder().run {
            useFastMode()
            withHtmlContent(template, null)
            toStream(stream)
            run()
        }
        stream
    }.toByteArray()
}