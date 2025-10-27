package pl.ejdev.zwoje.core.template.groovyTemplates

import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import java.io.StringWriter

abstract class ZwojeGroovyMarkupTemplate<INPUT : Any>(
    private val templatePath: String
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {

    companion object {
        private val engine: MarkupTemplateEngine by lazy {
            val config = TemplateConfiguration().apply {
//                autoNewLine = true
//                autoIndent = true
//                declarationEncoding = "UTF-8"
//                useDoubleQuotes = true
            }
            MarkupTemplateEngine(
                Thread.currentThread().contextClassLoader,
                config
            )
        }
    }

    override fun compile(input: TemplateInputData<INPUT>): String {
        val template = engine.createTemplateByPath(templatePath)
            ?: throw IllegalArgumentException("Template not found at path: $templatePath")

        val writer = StringWriter()
        template.make(toBinding(input.data)).writeTo(writer)
        return writer.toString()
    }

    private fun toBinding(data: Any): Map<String, Any?> {
        return data::class.members
            .filter {
                !it.name.startsWith("component") &&
                        !it.name.startsWith("hashCode") &&
                        !it.name.startsWith("toString")
            }
            .associate { it.name to runCatching { it.call(data) }.getOrNull() }
    }
}

