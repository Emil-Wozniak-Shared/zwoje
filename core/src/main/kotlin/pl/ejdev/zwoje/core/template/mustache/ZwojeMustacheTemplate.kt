package pl.ejdev.zwoje.core.template.mustache

import com.github.mustachejava.DefaultMustacheFactory
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import java.io.File
import java.io.StringWriter

abstract class ZwojeMustacheTemplate<INPUT : Any>(
    override val templatePath: String? = null
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {

    override fun compile(input: TemplateInputData<INPUT>): String {
        val templateFile = File(templatePath!!)
        val mustacheFactory = DefaultMustacheFactory(templateFile.getParentFile())
        val mustache = mustacheFactory.compile(templateFile.getName())
        val writer = StringWriter()
        mustache.execute(writer, input).flush()
        return writer.toString()
    }
}
