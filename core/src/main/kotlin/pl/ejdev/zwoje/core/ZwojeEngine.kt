package pl.ejdev.zwoje.core

import pl.ejdev.zwoje.core.engine.CompileData
import pl.ejdev.zwoje.core.engine.PdfCompileEngine
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class ZwojeEngine(
    private val compileEngine: PdfCompileEngine,
    private val templateResolver: ZwojeTemplateResolver<*>
) {

    fun <T : Any> compile(template: String, templateData: TemplateInputData<T>): ByteArray {
        val htmlTemplate = templateResolver[template]
        val html = htmlTemplate.compile(templateData)
        val templatePath = htmlTemplate.templatePath?.let { getTemplatePath(it) }
        val compileData = CompileData(html, templatePath)
        return compileEngine.compile(compileData)
    }

    private fun getTemplatePath(templatePath: String): String? = when (templateResolver) {
        is TemplateProvider -> "${templatePath.substringBefore(templateResolver.templatesDir)}${templateResolver.templatesDir}/"
        else -> null
    }
}
