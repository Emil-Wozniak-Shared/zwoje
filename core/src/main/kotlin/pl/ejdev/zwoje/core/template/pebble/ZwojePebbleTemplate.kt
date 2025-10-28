package pl.ejdev.zwoje.core.template.pebble

import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.ClasspathLoader
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.utils.dataClassMembersFilter
import pl.ejdev.zwoje.core.utils.getMembers
import java.io.StringWriter

abstract class ZwojePebbleTemplate<INPUT : Any>(
    private val templatePath: String
) : ZwojeTemplate<TemplateInputData<INPUT>, INPUT> {

    companion object {
        private val engine: PebbleEngine by lazy {
            PebbleEngine.Builder()
                .loader(ClasspathLoader().apply {
                    prefix = "templates"
                    suffix = ".peb"
                    charset = "UTF-8"
                })
                .cacheActive(true)
                .strictVariables(false)
                .build()
        }
    }

    override fun compile(input: TemplateInputData<INPUT>): String {
        val template = engine.getTemplate(templatePath)
        val writer = StringWriter()
        input.getMembers<INPUT>().toMap()
        template.evaluate(writer, input.getMembers<INPUT>().toMap())
        return writer.toString()
    }
}
