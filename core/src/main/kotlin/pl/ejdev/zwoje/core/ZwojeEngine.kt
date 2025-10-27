package pl.ejdev.zwoje.core

import pl.ejdev.zwoje.core.engine.PdfCompileEngine
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class ZwojeEngine(
    private val compileEngine: PdfCompileEngine,
    private val templateResolver: ZwojeTemplateResolver<*>
) {

    fun <T : Any> compile(template: String, templateData: TemplateInputData<T>): ByteArray {
        val htmlTemplate = templateResolver.get(template)
        val html = htmlTemplate.compile(templateData)
        return compileEngine.compile(html)
    }
}
