package pl.ejdev.zwoje.core.template.pebble

import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.ClasspathLoader
import pl.ejdev.zwoje.core.template.TemplateInputData
import pl.ejdev.zwoje.core.template.ZwojeTemplate
import pl.ejdev.zwoje.core.utils.dataClassMembersFilter
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
        template.evaluate(writer, toBinding(input.data))
        return writer.toString()
    }

    private fun toBinding(data: Any): MutableMap<String, Any?> {
        return data::class.members
            .filter(dataClassMembersFilter)
            .associateTo(mutableMapOf()) { it.name to runCatching { it.call(data) }.getOrNull() }
    }
}
