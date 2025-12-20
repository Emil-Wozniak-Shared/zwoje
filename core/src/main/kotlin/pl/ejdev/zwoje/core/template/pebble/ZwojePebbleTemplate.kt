package pl.ejdev.zwoje.core.template.pebble

import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.ClasspathLoader
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.TemplateOutput
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.utils.getMembers
import java.io.StringWriter

abstract class ZwojePebbleTemplate<INPUT : Any>(
    override val templatePath: String
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {

    override fun compile(input: TemplateInputData<INPUT>): TemplateOutput {
        val template = engine.getTemplate(templatePath)
        val writer = StringWriter()
        template.evaluate(writer, input.getMembers().toMap())
        return writer.toString().let { TemplateOutput.Html(it) }
    }

    private companion object {
        private val engine: PebbleEngine by lazy {
            PebbleEngine.Builder()
                .loader(loader())
                .cacheActive(true)
                .strictVariables(false)
                .build()
        }

        private fun loader(): ClasspathLoader = ClasspathLoader().apply {
            prefix = "templates"
            suffix = ".peb"
            charset = "UTF-8"
        }
    }

}
