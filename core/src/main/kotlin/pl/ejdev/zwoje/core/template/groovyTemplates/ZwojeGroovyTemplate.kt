package pl.ejdev.zwoje.core.template.groovyTemplates

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.utils.getMembers
import java.io.StringWriter

abstract class ZwojeGroovyMarkupTemplate<INPUT : Any>(
    override val templatePath: String
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {

    companion object {
        private val engine: MarkupTemplateEngine by lazy {
            MarkupTemplateEngine(
                Thread.currentThread().contextClassLoader,
                TemplateConfiguration()
            )
        }
    }

    override fun compile(input: TemplateInputData<INPUT>): String {
        val template = engine.createTemplateByPath(templatePath)
            ?: throw IllegalArgumentException("Template not found at path: $templatePath")

        val writer = StringWriter()
        template
            .make(input.getMembers().toMap())
            .writeTo(writer)
        return writer.toString()
    }
}

