package pl.ejdev.zwoje.core

import pl.ejdev.zwoje.core.engine.CompileData
import pl.ejdev.zwoje.core.engine.HtmlOutput
import pl.ejdev.zwoje.core.engine.JasperFill
import pl.ejdev.zwoje.core.engine.PdfCompileEngine
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateOutput
import pl.ejdev.zwoje.core.template.TemplateProvider
import pl.ejdev.zwoje.core.template.ZwojeTemplateResolver

class ZwojeEngine(
    private val compileEngine: PdfCompileEngine<CompileData>,
    private val templateResolver: ZwojeTemplateResolver<*>
) {

    fun <T : Any> compile(template: String, templateData: TemplateInputData<T>): ByteArray {
        val htmlTemplate = templateResolver[template]
        val output = htmlTemplate.compile(templateData)
        val templatePath = htmlTemplate.templatePath?.let { getTemplatePath(it) }
        val compileData = when (output) {
            is TemplateOutput.JasperFill -> JasperFill(output.print, templatePath)
            is TemplateOutput.Html -> HtmlOutput(output.html, templatePath)
        }
        return compileEngine.compile(compileData)
    }

    private fun getTemplatePath(templatePath: String): String? = when (templateResolver) {
        is TemplateProvider -> "${templatePath.substringBefore(templateResolver.templatesDir)}${templateResolver.templatesDir}/"
        else -> null
    }
}
